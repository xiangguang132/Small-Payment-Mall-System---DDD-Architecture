package cn.bugstack.domain.auth.service;

public interface ILoginService {

    String createQrCodeTicket();

    String checkLogin(String ticket);

    void saveLoginState(String ticket, String openid) throws Exception;

}
