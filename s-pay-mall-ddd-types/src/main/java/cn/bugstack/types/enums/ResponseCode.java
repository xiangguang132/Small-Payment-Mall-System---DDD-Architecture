package cn.bugstack.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS(200, "成功"),
    CREATED(201, "创建成功"),
    NO_CONTENT(204, "无内容"),
    MOVED_PERMANENTLY(301, "永久重定向"),
    FOUND(302, "临时重定向"),
    ILLEGAL_PARAMETER(400, "非法参数"),
    NO_LOGIN(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    CONFLICT(409, "资源冲突"),
    UNPROCESSABLE_ENTITY(422, "参数校验失败"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    UN_ERROR(500, "未知失败"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),

    // 拼团业务码，10000+ 避免与 HTTP 状态码冲突
    E0001(10001, "不存在对应的折扣计算服务"),
    E0002(10002, "无拼团营销配置"),
    E0003(10003, "拼团活动降级拦截"),
    E0004(10004, "拼团活动切量拦截"),
    E0005(10005, "拼团组队失败，记录更新为0"),
    E0006(10006, "拼团组队完结，锁单量已达成"),
    E0007(10007, "拼团人群限定，不可参与"),
    E0008(10008, "拼团组队失败，缓存库存不足"),

    E0101(10101, "拼团活动未生效"),
    E0102(10102, "不在拼团活动有效时间内"),
    E0103(10103, "当前用户参与此拼团次数已达上限"),
    E0104(10104, "不存在的外部交易单号或用户已退单"),
    E0105(10105, "SC渠道黑名单拦截"),
    E0106(10106, "订单交易时间不在拼团有效时间范围内"),

    // 用户/认证业务码
    E0201(10201, "请先完善账号信息"),
    ;

    private Integer code;
    private String info;

}
