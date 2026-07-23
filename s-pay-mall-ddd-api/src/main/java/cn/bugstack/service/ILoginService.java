package cn.bugstack.service;

public interface ILoginService {

    void saveLoginState(String ticket, String openid);
}
