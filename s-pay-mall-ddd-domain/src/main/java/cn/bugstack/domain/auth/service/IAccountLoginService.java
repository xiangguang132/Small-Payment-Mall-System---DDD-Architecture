package cn.bugstack.domain.auth.service;

public interface IAccountLoginService {

    void register(String userId, String password);

    String login(String userId, String password);

}
