package cn.bugstack.api.request.groupbuy;

import cn.bugstack.api.request.page.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class GroupBuyOrderPageRequest extends PageRequest {

    /** 拼团订单状态；0已锁定、1已支付、2已完成、3已取消/退单（默认0） */
    private Integer status = 0;

    /** 用户ID（可选，不过滤则查询所有用户） */
    private String userId;

}
