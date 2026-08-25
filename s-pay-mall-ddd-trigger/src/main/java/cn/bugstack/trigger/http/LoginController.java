package cn.bugstack.trigger.http;

import cn.bugstack.api.request.auth.AccountLoginRequest;
import cn.bugstack.api.request.auth.AccountRegisterRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.domain.auth.service.IAccountLoginService;
import cn.bugstack.domain.auth.service.ILoginService;
import cn.bugstack.types.common.Constants;
import cn.bugstack.types.exception.AppException;
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
    @Resource
    private IAccountLoginService accountLoginService;

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

    /**
     * 账号密码注册
     * <a href="http://localhost:8080/api/v1/login/register">/api/v1/login/register</a>
     */
    @RequestMapping(value = "register", method = RequestMethod.POST)
    public Response<Boolean> register(@RequestBody AccountRegisterRequest request) {
        try {
            log.info("账号密码注册开始 userId:{}", request.getUserId());
            accountLoginService.register(request.getUserId(), request.getPassword());
            log.info("账号密码注册完成 userId:{}", request.getUserId());
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (AppException e) {
            log.warn("账号密码注册业务失败 userId:{} info:{}", request.getUserId(), e.getInfo());
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("账号密码注册失败 userId:{}", request.getUserId(), e);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 账号密码登录，返回 JWT token
     * <a href="http://localhost:8080/api/v1/login/account">/api/v1/login/account</a>
     */
    @RequestMapping(value = "account", method = RequestMethod.POST)
    public Response<String> accountLogin(@RequestBody AccountLoginRequest request) {
        try {
            log.info("账号密码登录开始 userId:{}", request.getUserId());
            String token = accountLoginService.login(request.getUserId(), request.getPassword());
            log.info("账号密码登录完成 userId:{}", request.getUserId());
            return Response.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(token)
                    .build();
        } catch (AppException e) {
            log.warn("账号密码登录业务失败 userId:{} info:{}", request.getUserId(), e.getInfo());
            return Response.<String>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("账号密码登录失败 userId:{}", request.getUserId(), e);
            return Response.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
