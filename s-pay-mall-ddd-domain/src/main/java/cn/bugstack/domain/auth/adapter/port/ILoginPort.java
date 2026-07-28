package cn.bugstack.domain.auth.adapter.port;

import java.io.IOException;

/**
 * 调用微信接口/缓存/第三方 SDK 的动作
 */
public interface ILoginPort {

    String createQrCodeTicket() throws IOException;

    void sendLoginTempleteMessage(String openid) throws IOException;

}