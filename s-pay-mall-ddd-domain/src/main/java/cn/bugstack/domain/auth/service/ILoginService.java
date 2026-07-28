package cn.bugstack.domain.auth.service;

/**
 * 登录业务本身需要的动作
 */
public interface ILoginService {

    String createQrCodeTicket();

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openid) throws Exception;

}
