package cn.bugstack.trigger.http;

import cn.bugstack.api.request.user.CompleteProfileRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.user.UserInfoResponse;
import cn.bugstack.domain.auth.model.entity.UserEntity;
import cn.bugstack.domain.auth.service.IUserProfileService;
import cn.bugstack.types.common.Constants;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户中心
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/user")
public class UserController {

    @Resource
    private IUserProfileService userProfileService;

    /**
     * 当前登录用户信息
     * <a href="http://localhost:8080/api/v1/user/me">/api/v1/user/me</a>
     */
    @RequestMapping(value = "me", method = RequestMethod.GET)
    public Response<UserInfoResponse> me(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("openid");
            UserEntity userEntity = userProfileService.queryMe(userId);
            boolean completed = StringUtils.isNotBlank(userEntity.getPhone());
            return Response.<UserInfoResponse>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(UserInfoResponse.builder()
                            .userId(userEntity.getUserId())
                            .nickname(userEntity.getNickname())
                            .phoneMasked(maskPhone(userEntity.getPhone()))
                            .profileCompleted(completed)
                            .build())
                    .build();
        } catch (AppException e) {
            return Response.<UserInfoResponse>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("查询当前用户信息失败", e);
            return Response.<UserInfoResponse>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 首次扫码注册后完善资料：绑定手机号、设置密码、更新昵称
     * <a href="http://localhost:8080/api/v1/user/profile/complete">/api/v1/user/profile/complete</a>
     */
    @RequestMapping(value = "profile/complete", method = RequestMethod.POST)
    public Response<Boolean> completeProfile(@RequestBody CompleteProfileRequest request, HttpServletRequest httpRequest) {
        try {
            String userId = (String) httpRequest.getAttribute("openid");
            log.info("完善账号资料开始 userId:{} phone:{}", userId, request.getPhone());
            userProfileService.completeProfile(userId, request.getPhone(), request.getPassword(), request.getNickname());
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (AppException e) {
            log.warn("完善账号资料业务失败 info:{}", e.getInfo());
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("完善账号资料失败", e);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        return phone.length() == 11 ? phone.substring(0, 3) + "****" + phone.substring(7) : phone;
    }

}
