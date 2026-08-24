package cn.bugstack.infrastructure.dcc;

import cn.bugstack.types.annotations.DCCValue;
import org.springframework.stereotype.Service;

/**
 * 动态配置中心（DCC, Dynamic Configuration Center）客户端服务
 * 作用是让你的程序可以在不重启、不重新发布的情况下，通过修改配置来动态调整业务逻辑。
 * 这在生产环境中非常关键，常用于灰度发布和降级开关。
 * 动态配置服务
 */
@Service
public class DCCService {

    /**
     * 自定义注解-降级开关 0关闭、1开启
     * 作用：：从远程配置中心（如 Nacos, Apollo, Zookeeper）获取 key 对应的值，并注入到这个字段中
     */
    // 尝试获取 downgradeSwitch 开关的值，如果配置中心没有，默认为0
    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    // 尝试获取 cutRange 的值，默认是 "100"（代表 100%）
    /**
     * cutRange 在这里指的是“切量范围”或“灰度比例”
     * 在 DCCService 中，cutRange 的值通常是一个 0 到 100 之间的整数，代表流量的百分比。
     * 如果 cutRange = 10：代表只切出 10% 的流量或用户来体验新功能。
     * 如果 cutRange = 100：代表 100% 全量放开，所有用户都走新逻辑。
     * 代码中通过 userId.hashCode() % 100 计算出用户的“桶号”，然后与 cutRange 比较，以此来精准控制哪些用户属于这 10% 或 100%
     */
    @DCCValue("cutRange:100")
    private String cutRange;

    /**
     * 缓存开关
     */
    @DCCValue("cacheSwitch:0")
    private String cacheOpenSwitch;

    /**
     * 降级开关逻辑
     * 作用：用于故障应急
     * 逻辑：判断 配置项downgradeSwitch =？ 1
     * 场景：如果某一个功能挂了，可以修改配置中心的 downgradeSwitch = 1，提供给代码检测，然后跳出其余的逻辑，返回默认值或者友好提示
     */
    public boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }

    /**
     * 灰度/切量逻辑
     * 用途：用于灰度发布或A/B 测试
     * 逻辑：获取userID, 计算它的哈希值，并对 100 取模。这意味着无论用户 ID 是什么，结果都会落在 0 到 99 之间,与配置项 cutRange 比较
     * 场景：假设你上线了一个新功能，不敢直接给所有人用。你在配置中心把 cutRange 设置为 "10"。那么，只有哈希值尾数在 0-10 之间的用户（约 10% 的用户）会返回 true，看到新功能。其他 90% 的用户返回 false，维持旧逻辑。如果你想全量发布，只需要把 cutRange 改为 "100"，所有用户都会命中
     */
    public boolean isCutRange(String userId) {
        if (userId == null) {
            return true;
        }
        // 计算哈希码的绝对值
        int hashCode = Math.abs(userId.hashCode());

        // 获取最后两位（0-99之间的数字）
        int lastTwoDigits = hashCode % 100;

        // 判断是否在切量范围内
        if (lastTwoDigits <= Integer.parseInt(cutRange)) {
            return true;
        }
        return false;
    }

    /**
     * 缓存开启开关，默认 开=0 关=1
     * @return
     */
    public boolean isCacheOpenSwitch() {
        return "0".equals(cacheOpenSwitch);
    }
}
