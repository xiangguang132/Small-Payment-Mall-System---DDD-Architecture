# RabbitMQ 支付宝异步回调参考实现

> 这是“MQ 消费最小闭环”的参考代码，不是最终直接提交版本。落地前先确认 `orderType` 来源、重试上限和分布式锁的归属。

## 1. 消息约定

MQ 消息只发送 `outTradeNo`，支付宝原始参数保存在 `alipay_notify_task.parameter_json`。

```text
AliPayController.payNotify
  -> saveNotifyTask(params)
  -> EventPublisher.publish(routingKey, outTradeNo)
  -> AlipayNotifyListener.listener(outTradeNo)
  -> AlipayNotifyTaskService.processTask(outTradeNo)
```

## 2. 模块依赖

`trigger` 要使用 infrastructure 的 `EventPublisher`，需要补依赖：

```xml
<dependency>
    <groupId>cn.bugstack</groupId>
    <artifactId>s-pay-mall-ddd-infrastructure</artifactId>
</dependency>
```

## 3. Domain：接口

```java
package cn.bugstack.domain.payment.service;

import java.util.Map;

public interface IAlipayNotifyTaskService {

    void saveNotifyTask(Map<String, String> params);

    void processTask(String outTradeNo);
}
```

## 4. Domain：AlipayNotifyTaskService

```java
package cn.bugstack.domain.payment.service;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuySettlementCommandEntity;
import cn.bugstack.domain.groupbuy.service.settlement.IGroupBuySettlementService;
import cn.bugstack.domain.order.adapter.repository.IOrderRepository;
import cn.bugstack.domain.order.model.entity.PayOrderEntity;
import cn.bugstack.domain.order.service.IOrderService;
import cn.bugstack.domain.payment.adapter.port.IAlipayNotifyPort;
import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import cn.bugstack.domain.payment.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.types.enums.OrderTypeEnum;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Slf4j
@Service
public class AlipayNotifyTaskService implements IAlipayNotifyTaskService {

    private static final int MAX_RETRY_COUNT = 5;

    @Resource
    private IAlipayNotifyTaskRepository repository;
    @Resource
    private IOrderRepository orderRepository;
    @Resource
    private IOrderService orderService;
    @Resource
    private IGroupBuySettlementService groupBuySettlementService;
    @Resource
    private IAlipayNotifyPort alipayNotifyPort;

    @Override
    public void saveNotifyTask(Map<String, String> params) {
        String outTradeNo = params.get("out_trade_no");
        String tradeNo = params.get("trade_no");

        AlipayNotifyTaskEntity isExisting = repository.queryByOutTradeNoAndTradeNo(outTradeNo, tradeNo);
        if (isExisting != null) {
            log.info("支付通知任务已存在 outTradeNo:{} tradeNo:{}", outTradeNo, tradeNo);
            return;
        }

        AlipayNotifyTaskEntity entity = AlipayNotifyTaskEntity.builder()
                .outTradeNo(outTradeNo)
                .tradeNo(tradeNo)
                .taskStatus(0)
                .retryCount(0)
                .parameterJson(JSON.toJSONString(params))
                .build();
        repository.save(entity);
    }

    @Override
    public void processTask(String outTradeNo) {
        if (StringUtils.isBlank(outTradeNo)) {
            throw new IllegalArgumentException("outTradeNo不能为空");
        }

        if (!alipayNotifyPort.tryLock(outTradeNo)) {
            log.warn("获取支付宝通知处理锁失败 outTradeNo:{}", outTradeNo);
            return;
        }
        try {
            AlipayNotifyTaskEntity task = repository.queryByOutTradeNo(outTradeNo);
            if (task == null) {
                log.warn("支付宝通知任务不存在 outTradeNo:{}", outTradeNo);
                return;
            }
            if (Integer.valueOf(1).equals(task.getTaskStatus())) {
                log.info("支付宝通知任务已处理完成 outTradeNo:{}", outTradeNo);
                return;
            }
            if (task.getRetryCount() != null && task.getRetryCount() >= MAX_RETRY_COUNT) {
                repository.updateTaskDead(outTradeNo);
                return;
            }

            PayOrderEntity payOrder = orderRepository.queryPayOrderByOutTradeNo(outTradeNo);
            if (payOrder == null) {
                throw new AppException(ResponseCode.UN_ERROR, "支付单不存在 outTradeNo:" + outTradeNo);
            }

            JSONObject params = JSON.parseObject(task.getParameterJson());
            LocalDateTime payTime = parseAlipayTime(params == null ? null : params.getString("gmt_payment"));

            if (OrderTypeEnum.GROUP_BUY.equals(payOrder.getOrderType())) {
                GroupBuySettlementCommandEntity command = GroupBuySettlementCommandEntity.builder()
                        .userId(payOrder.getUserId())
                        .outTradeNo(outTradeNo)
                        .payTime(payTime)
                        .source("ALIPAY")
                        .channel("ALIPAY")
                        .build();
                groupBuySettlementService.settlementGroupBuyOrder(command);
            } else {
                Date outTradeTime = payTime == null
                        ? null
                        : Date.from(payTime.atZone(ZoneId.systemDefault()).toInstant());
                orderService.changeOrderPaySuccess(outTradeNo, outTradeTime);
            }

            repository.updateTaskSuccess(outTradeNo);
            log.info("支付宝通知任务处理完成 outTradeNo:{}", outTradeNo);
        } catch (Exception e) {
            log.error("支付宝通知任务处理失败 outTradeNo:{}", outTradeNo, e);
            repository.updateTaskRetry(outTradeNo);
            throw new RuntimeException(e);
        } finally {
            alipayNotifyPort.unlock(outTradeNo);
        }
    }

    private LocalDateTime parseAlipayTime(String alipayTime) {
        if (StringUtils.isBlank(alipayTime)) {
            return null;
        }
        try {
            return LocalDateTime.parse(alipayTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("支付宝支付时间解析失败 alipayTime:{}", alipayTime, e);
            return null;
        }
    }
}
```

## 4.1 Domain：锁 Port

```java
package cn.bugstack.domain.payment.adapter.port;

public interface IAlipayNotifyPort {

    boolean tryLock(String outTradeNo);

    void unlock(String outTradeNo);
}
```

## 4.2 Infrastructure：AlipayNotifyPort

```java
package cn.bugstack.infrastructure.adapter.port;

import cn.bugstack.domain.payment.adapter.port.IAlipayNotifyPort;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Component
public class AlipayNotifyPort implements IAlipayNotifyPort {

    private static final String LOCK_KEY_PREFIX = "alipay_notify_task_lock:";

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean tryLock(String outTradeNo) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + outTradeNo);
        try {
            return lock.tryLock(3, 30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void unlock(String outTradeNo) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + outTradeNo);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
```

## 5. Domain：Repository 接口

```java
package cn.bugstack.domain.payment.repository;

import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;

import java.util.List;

public interface IAlipayNotifyTaskRepository {

    AlipayNotifyTaskEntity queryByOutTradeNoAndTradeNo(String outTradeNo, String tradeNo);

    void save(AlipayNotifyTaskEntity alipayNotifyTaskEntity);

    AlipayNotifyTaskEntity queryByOutTradeNo(String outTradeNo);

    List<String> queryRetryOutTradeNoList();

    void updateTaskSuccess(String outTradeNo);

    void updateTaskRetry(String outTradeNo);

    void updateTaskDead(String outTradeNo);
}
```

## 6. Infrastructure：Repository 实现

```java
package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.payment.model.entity.AlipayNotifyTaskEntity;
import cn.bugstack.domain.payment.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.infrastructure.dao.IAlipayNotifyTaskDao;
import cn.bugstack.infrastructure.dao.po.payment.AlipayNotifyTask;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class AlipayNotifyTaskRepository implements IAlipayNotifyTaskRepository {

    @Resource
    private IAlipayNotifyTaskDao alipayNotifyTaskDao;

    @Override
    public AlipayNotifyTaskEntity queryByOutTradeNoAndTradeNo(String outTradeNo, String tradeNo) {
        if (outTradeNo == null || tradeNo == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "outTradeNo和tradeNo不能为空");
        }
        return toEntity(alipayNotifyTaskDao.queryByOutTradeNoAndTradeNo(outTradeNo, tradeNo));
    }

    @Override
    public void save(AlipayNotifyTaskEntity entity) {
        if (entity == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "支付通知任务不能为空");
        }
        try {
            alipayNotifyTaskDao.insert(toPo(entity));
        } catch (DuplicateKeyException ignore) {
            // 并发重复回调由唯一键拦截
        }
    }

    @Override
    public AlipayNotifyTaskEntity queryByOutTradeNo(String outTradeNo) {
        return toEntity(alipayNotifyTaskDao.queryByOutTradeNo(outTradeNo));
    }

    @Override
    public List<String> queryRetryOutTradeNoList() {
        return alipayNotifyTaskDao.queryRetryOutTradeNoList();
    }

    @Override
    public void updateTaskSuccess(String outTradeNo) {
        alipayNotifyTaskDao.updateTaskSuccess(outTradeNo);
    }

    @Override
    public void updateTaskRetry(String outTradeNo) {
        alipayNotifyTaskDao.updateTaskRetry(outTradeNo);
    }

    @Override
    public void updateTaskDead(String outTradeNo) {
        alipayNotifyTaskDao.updateTaskDead(outTradeNo);
    }

    private AlipayNotifyTask toPo(AlipayNotifyTaskEntity entity) {
        return AlipayNotifyTask.builder()
                .id(entity.getId())
                .outTradeNo(entity.getOutTradeNo())
                .tradeNo(entity.getTradeNo())
                .orderType(entity.getOrderType())
                .taskStatus(entity.getTaskStatus())
                .retryCount(entity.getRetryCount())
                .parameterJson(entity.getParameterJson())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private AlipayNotifyTaskEntity toEntity(AlipayNotifyTask po) {
        if (po == null) {
            return null;
        }
        return AlipayNotifyTaskEntity.builder()
                .id(po.getId())
                .outTradeNo(po.getOutTradeNo())
                .tradeNo(po.getTradeNo())
                .orderType(po.getOrderType())
                .taskStatus(po.getTaskStatus())
                .retryCount(po.getRetryCount())
                .parameterJson(po.getParameterJson())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}
```

## 7. Infrastructure：DAO

```java
package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.payment.AlipayNotifyTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IAlipayNotifyTaskDao {

    AlipayNotifyTask queryByOutTradeNoAndTradeNo(@Param("outTradeNo") String outTradeNo, @Param("tradeNo") String tradeNo);

    void insert(AlipayNotifyTask alipayNotifyTask);

    AlipayNotifyTask queryByOutTradeNo(@Param("outTradeNo") String outTradeNo);

    List<String> queryRetryOutTradeNoList();

    void updateTaskSuccess(@Param("outTradeNo") String outTradeNo);

    void updateTaskRetry(@Param("outTradeNo") String outTradeNo);

    void updateTaskDead(@Param("outTradeNo") String outTradeNo);
}
```

## 8. App：Mapper XML 追加

```xml
<select id="queryByOutTradeNo" resultMap="dataMap">
    select id, out_trade_no, trade_no, order_type, task_status, retry_count, parameter_json, create_time, update_time
    from alipay_notify_task
    where out_trade_no = #{outTradeNo}
    order by id desc
    limit 1
</select>

<select id="queryRetryOutTradeNoList" resultType="java.lang.String">
    select out_trade_no
    from alipay_notify_task
    where task_status in (0, 2)
      and retry_count &lt; 5
    order by update_time asc
</select>

<update id="updateTaskSuccess">
    update alipay_notify_task
    set task_status = 1, update_time = now()
    where out_trade_no = #{outTradeNo}
      and task_status != 1
</update>

<update id="updateTaskRetry">
    update alipay_notify_task
    set task_status = 2, retry_count = retry_count + 1, update_time = now()
    where out_trade_no = #{outTradeNo}
      and task_status != 1
</update>

<update id="updateTaskDead">
    update alipay_notify_task
    set task_status = 3, update_time = now()
    where out_trade_no = #{outTradeNo}
      and task_status != 1
</update>
```

## 9. Trigger：Controller 发布消息

```java
@Value("${spring.rabbitmq.config.producer.topic_alipay_notify.routing_key}")
private String alipayNotifyRoutingKey;

@Resource
private EventPublisher eventPublisher;

// payNotify 中 saveNotifyTask 之后：
alipayNotifyTaskService.saveNotifyTask(params);
eventPublisher.publish(alipayNotifyRoutingKey, params.get("out_trade_no"));
```

## 10. Trigger：AlipayNotifyListener

```java
package cn.bugstack.trigger.listener;

import cn.bugstack.domain.payment.service.IAlipayNotifyTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class AlipayNotifyListener {

    @Resource
    private IAlipayNotifyTaskService alipayNotifyTaskService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "${spring.rabbitmq.config.producer.topic_alipay_notify.queue}"),
                    exchange = @Exchange(value = "${spring.rabbitmq.config.producer.exchange}", type = ExchangeTypes.TOPIC),
                    key = "${spring.rabbitmq.config.producer.topic_alipay_notify.routing_key}"
            )
    )
    public void listener(String message) {
        log.info("接收支付宝异步通知 message:{}", message);
        try {
            alipayNotifyTaskService.processTask(message);
        } catch (Exception e) {
            log.error("处理支付宝异步通知失败 message:{}", message, e);
            throw e;
        }
    }
}
```

## 11. Trigger：AlipayNotifyJob 定时补偿

```java
package cn.bugstack.trigger.job;

import cn.bugstack.domain.payment.repository.IAlipayNotifyTaskRepository;
import cn.bugstack.infrastructure.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class AlipayNotifyJob {

    @Resource
    private IAlipayNotifyTaskRepository repository;
    @Resource
    private EventPublisher eventPublisher;
    @Value("${spring.rabbitmq.config.producer.topic_alipay_notify.routing_key}")
    private String routingKey;

    @Scheduled(cron = "0 0/1 * * * ?")
    public void exec() {
        try {
            List<String> outTradeNos = repository.queryRetryOutTradeNoList();
            if (outTradeNos == null || outTradeNos.isEmpty()) {
                return;
            }
            for (String outTradeNo : outTradeNos) {
                eventPublisher.publish(routingKey, outTradeNo);
                log.info("重新投递支付宝通知任务 outTradeNo:{}", outTradeNo);
            }
        } catch (Exception e) {
            log.error("重新投递支付宝通知任务失败", e);
        }
    }
}
```

## 12. 落地顺序

1. trigger pom 补 infrastructure 依赖。
2. 新增 `IAlipayNotifyPort`，infrastructure 实现 `AlipayNotifyPort`。
3. 补 DAO/Mapper 的状态查询和更新 SQL。
4. 补 Domain Service 的 `processTask` 和 Repository 方法。
5. Controller 落库后 publish。
6. Listener 调用 `processTask`。
7. Job 定时扫描 `task_status in (0, 2)` 重新投递。
8. `mvn -DskipTests compile` 编译验证。
