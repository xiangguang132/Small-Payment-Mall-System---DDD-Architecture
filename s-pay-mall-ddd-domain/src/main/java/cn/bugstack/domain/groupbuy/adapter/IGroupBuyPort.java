package cn.bugstack.domain.groupbuy.adapter;

import cn.bugstack.domain.groupbuy.model.entity.GroupBuyNotifyTaskEntity;

public interface IGroupBuyPort {

    /** 成团回调：返回 "success" / "error" */
    String groupBuyNotify(GroupBuyNotifyTaskEntity notifyTask) throws Exception;

}
