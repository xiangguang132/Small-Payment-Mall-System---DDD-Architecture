package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.domain.auth.service.ILoginService;
import cn.bugstack.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/login")
public class LoginController {

    @Resource
    private ILoginService loginService;

    /**
     * 获取微信 ticket 凭证
     * <a href="http://xfg-studio.natapp1.cc/api/v1/login/weixin_qrcode_ticket">/api/v1/login/weixin_qrcode_ticket</a>
     */
    @RequestMapping(value = "weixin_qrcode_ticket", method = RequestMethod.GET)
    public Response<String> weixinQrCodeTicket() {
        try {
            log.info("生成微信扫码登录 ticket 开始");
            String qrCodeTicket = loginService.createQrCodeTicket();
            log.info("生成微信扫码登录 ticket 完成 ticket:{}", qrCodeTicket);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(qrCodeTicket)
                    .build();

        } catch (Exception e) {
            log.error("生成微信扫码登录 ticket 失败", e);
                return Response.<String>builder()
                        .code(Constants.ResponseCode.UN_ERROR.getCode())
                        .info(Constants.ResponseCode.UN_ERROR.getInfo())
                        .build();
        }
    }

    /**
     * 轮训登录
     * <a href="http://xfg-studio.natapp1.cc/api/v1/login/check_login">/api/v1/login/check_login</a>
     */
    @RequestMapping(value = "check_login", method = RequestMethod.GET)
    public Response<String> checkLogin(@RequestParam String ticket) {
        try {
            log.info("扫描检测登录结果开始 ticket:{}", ticket);
            String token = loginService.checkLogin(ticket);
            if (StringUtils.isNotBlank(token)) {
                log.info("扫描检测登录结果完成 ticket:{} token:{}", ticket, token);
                return Response.<String>builder()
                        .code(Constants.ResponseCode.SUCCESS.getCode())
                        .info(Constants.ResponseCode.SUCCESS.getInfo())
                        .data(token)
                        .build();
            } else {
                log.info("扫描检测登录结果未登录 ticket:{}", ticket);
                return Response.<String>builder()
                        .code(Constants.ResponseCode.NO_LOGIN.getCode())
                        .info(Constants.ResponseCode.NO_LOGIN.getInfo())
                        .build();
            }
        } catch (Exception e) {
            log.error("扫描检测登录结果失败 ticket:{}", ticket, e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
