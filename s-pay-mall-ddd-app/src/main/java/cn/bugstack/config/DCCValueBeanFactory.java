package cn.bugstack.config;

import cn.bugstack.types.annotations.DCCValue;
import cn.bugstack.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class DCCValueBeanFactory implements BeanPostProcessor {

    private static final String BASE_CONFIG_PATH = "group_buy_market_dcc_";

    private final RedissonClient redissonClient;

    private final Map<String, Object> dccObjGroup = new HashMap<>();

    // 构造方法
    public DCCValueBeanFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
    }

    /**
     * 启动初始化阶段
     * 定义：这个方法属于 BeanPostProcessor，在 Spring 容器创建完每一个 Bean 之后执行
     * 作用：把配置中心（Redis）的值，注入到带有 @DCCValue 注解的字段中
     * @param bean the new bean instance
     * @param beanName the name of the bean
     * @return
     * @throws BeansException
     */
    // 系统启动时把配置值塞进对象里
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        /**
         * 确保无论当前 Bean 是一个“普通对象”，还是一个被 Spring 包装过的“代理对象”，
         * 我们都能准确地拿到它“最原始的真实对象（Target）”以及“真实的类结构”，
         * 以便后续能正确地使用反射 ----》 剥花生
         */
        // 假设当前的 bean 是一个普通对象，直接获取它的类和对象本身。
        Class<?> targetBeanClass = bean.getClass();
        Object targetBeanObject = bean;
        //判断它是不是一个被 Spring AOP 代理过的对象
        if (AopUtils.isAopProxy(bean)) {
            // 如果是代理对象，我们需要“剥开外壳”，拿到里面真实对象的类
            targetBeanClass = AopUtils.getTargetClass(bean);
            targetBeanObject = AopProxyUtils.getSingletonTarget(bean);
        }
        // 获取类的所有字段
        // getFiles只能拿到所有的public字段，需要使用Declare
        Field[] fileds = targetBeanClass.getDeclaredFields();
        // 遍历所有的字段，检查每一个字段是否贴上了@DCCValue标签
        for (Field field : fileds) {
            if (!field.isAnnotationPresent(DCCValue.class)) {
                continue;
            }

            // 如果有该注解，取出注解对象，并读取里面配置的字符串
            // 比如 @DCCValue("downgradeSwitch:0") 拿到的就是 "downgradeSwitch:0"
            DCCValue dccValue = field.getAnnotation(DCCValue.class);

            // 检查从注解里拿到的 value 是不是空的
            String value = dccValue.value();
            if (StringUtils.isBlank(value)) {
                throw new RuntimeException(field.getName() + " @DCCValue is not config value config case 「isSwitch/isSwitch:1」");
            }

            // 解析注解里的字符串，分割成 key:value
            String[] splits = value.split(":");
            String key = BASE_CONFIG_PATH.concat(splits[0]);
            String defaultValue = splits.length == 2 ? splits[1]:null;
            // 设置值
            String setValue = defaultValue;

            try {
                // 如果value值为空则抛出异常
                // 这里的值就是 downgradeSwitch:0 后面的这个数字
                if (StringUtils.isBlank(setValue)) {
                    throw new RuntimeException("dcc config error " + key + " is not null - 请配置默认值！");
                }

                // Redis 操作，判断配置Key是否存在，不存在则创建，存在则获取最新值
                // 获取 Redis 中的 Key 对象
                RBucket<String> bucket = redissonClient.getBucket(key);
                // 判断是否存在
                boolean exists = bucket.isExists();
                if (!exists) {
                    // 不存在则设置值
                    bucket.set(defaultValue);
                } else {
                    // 存在则从 Redis 中拉取最新的值，并赋给 setValue 变量
                    setValue = bucket.get();
                }

                // 使用反射，强行从redis里拿到最新配置值，塞入springboot的私有字段里
                // 关闭 Java 的访问权限检查
                field.setAccessible(true);
                // 赋值操作
                // 将 setValue（从 Redis 中获取的最新配置值）赋值给 targetBeanObject（真实的 Spring Bean 对象）中的当前字段
                field.set(targetBeanObject, setValue);
                // 开启 Java 的访问权限检查
                field.setAccessible(false);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            // 把“Redis 的 Key”和“Spring Bean 对象”的对应关系，存放到一个内存 Map 中
            dccObjGroup.put(key, targetBeanObject);
        }

        return bean;
    }

    /**
     * 实现热更新引擎
     * AI解释：
     * 我派了一个哨兵（RTopic）一直盯着 Redis 的某个频道。只要有人在后台改了配置，
     * 哨兵就会立刻收到通知。然后它拿着小本本（dccObjGroup）找到对应的 Spring 对象，
     * 用反射强行把新值塞进去，让系统立刻生效，连服务器都不用重启
     */
    @Bean("dccTopic")
    public RTopic testRedisTopicListener(RedissonClient redissonClient) {
        // 1. 订阅 Redis 频道（建立监听）
        RTopic topic = redissonClient.getTopic("group_buy_market_dcc");
        topic.addListener(String.class, (charSequence, s) -> {
            // 2. 解析变更消息
            // 含义：当收到消息 s 时，将其按分隔符劈开。
            // 作用：提取出被修改的字段名（attribute）和新值（value），并拼接出对应的 Redis Key。比如消息是 downgradeSwitch:1，提取后就知道要把 downgradeSwitch 改成 1。
            String[] split = s.split(Constants.SPLIT);
            String attribute = split[0];
            String key = BASE_CONFIG_PATH + attribute;
            String value = split[1];

            // 3. 更新 Redis 并找到目标对象
            // 先把新值同步更新到 Redis 中，保证数据一致性
            RBucket<String> bucket = redissonClient.getBucket(key);
            boolean exists = bucket.isExists();
            if (!exists) return ;
            bucket.set(value);

            // 4. 反射注入新值（热更新生效）
            /**
             * 过程是：
             * 4.1 先判断拿到的对象是不是 AOP 代理对象，如果是，就“脱掉马甲”拿到真实的类结构（防止找不到字段）。
             * 4.2 通过反射获取对应的字段（getDeclaredField(attribute)）。
             * 4.3 打开私有属性大门（setAccessible(true)），把新值塞进去（field.set(objBean, value)），然后重新锁上大门。
             */
            Object objBean = dccObjGroup.get(key);
            if (null == objBean) return;
            Class<?> objBeanClass = objBean.getClass();
            if (AopUtils.isAopProxy(objBean)) {
                objBeanClass = AopUtils.getTargetClass(objBean);
            }

            try {
                // 1. getDeclaredField 方法用于获取指定类中声明的所有字段，包括私有字段、受保护字段和公共字段。
                // 2. getField 方法用于获取指定类中的公共字段，即只能获取到公共访问修饰符（public）的字段。
                Field field = objBeanClass.getDeclaredField(attribute);
                field.setAccessible(true);
                field.set(objBean, value);
                field.setAccessible(false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return topic;
    }


}




