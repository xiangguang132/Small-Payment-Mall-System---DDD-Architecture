package cn.bugstack.api.request.groupbuy;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupBuyOrderPageRequest extends PageRequest {

    /** 拼团订单展示态；10待付款、20拼团中、30已成团、40已退款（null=全部） */
    private Integer status;

    /** 用户ID（可选，不过滤则查询所有用户） */
    private String userId;

}
