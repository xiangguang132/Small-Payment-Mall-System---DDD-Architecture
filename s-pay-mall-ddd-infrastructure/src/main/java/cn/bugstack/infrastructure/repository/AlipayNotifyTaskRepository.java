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

@Repository
public class AlipayNotifyTaskRepository implements IAlipayNotifyTaskRepository {

    @Resource
    private IAlipayNotifyTaskDao alipayNotifyTaskDao;

    @Override
    public AlipayNotifyTaskEntity queryByOutTradeNoAndTradeNo(String outTradeNo, String tradeNo) {
        if (outTradeNo == null || tradeNo == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "请求的内部订单号tradeNo和外部订单号outTradeNo不能为空");
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
            // 并发重复回调由唯一键拦截，按已接收处理
        }
    }

    private AlipayNotifyTask toPo(AlipayNotifyTaskEntity entity) {
        AlipayNotifyTask po = new AlipayNotifyTask();
        po.setId(entity.getId());
        po.setOutTradeNo(entity.getOutTradeNo());
        po.setTradeNo(entity.getTradeNo());
        po.setOrderType(entity.getOrderType());
        po.setTaskStatus(entity.getTaskStatus());
        po.setRetryCount(entity.getRetryCount());
        po.setParameterJson(entity.getParameterJson());
        po.setCreateTime(entity.getCreateTime());
        po.setUpdateTime(entity.getUpdateTime());
        return po;
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
