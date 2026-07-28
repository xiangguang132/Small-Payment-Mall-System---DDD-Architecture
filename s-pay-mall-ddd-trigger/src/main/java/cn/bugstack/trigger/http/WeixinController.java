package cn.bugstack.trigger.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信服务对接，对接地址：<a href="http://xfg-studio.natapp1.cc/api/v1/weixin/portal/receive">/api/v1/weixin/portal/receive</a>
 * <p>
 * http://xfg-studio.natapp1.cc/api/v1/weixin/portal/receive/
 */

@Slf4j
@RequestMapping("/api/v1/weixin.portal/")
@RestController()
@CrossOrigin("*")
public class WeixinController {

    /**
     * 验签接口
     * validate(signature, timestamp, nonce, echostr)
     * 返回 String
     */
    // todo validate

    /**
     * 请求微信公众号接口
     * post(requestBody, signature, timestamp, nonce, openid, encType, msgSignature)
     * 返回 String
     */
    // todo post

    /**
     * 构建返回给微信的请求体
     * buildTextEntity(openid, content)
     * 返回 String
     */
    // todo buildTextEntity


}
