package cn.bugstack.domain.groupbuy.service.user;

public interface IUserNotifyTaskService {

    /**
     * 处理成团成功消息，为团内用户写站内信
     * @param message MQ消息体 parameterJson: {"teamId":"xxx","outTradeNoList":[".."]}
     */
    void writeTeamSuccessNotify(String message);

}
