package cn.bugstack.trigger.http;

import cn.bugstack.api.request.user.CompleteProfileRequest;
import cn.bugstack.api.request.user.UpdateProfileRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.file.FileUploadResponse;
import cn.bugstack.api.response.user.UserInfoResponse;
import cn.bugstack.domain.auth.model.entity.UserEntity;
import cn.bugstack.domain.auth.service.IUserProfileService;
import cn.bugstack.infrastructure.adapter.port.IFileStorageService;
import cn.bugstack.types.common.Constants;
import cn.bugstack.types.enums.RoleEnum;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @Resource
    private IFileStorageService fileStorageService;

    /** 单文件大小上限（字节，5MB），与 UploadController 保持一致 */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    /**
     * 当前登录用户信息
     * <a href="http://localhost:8080/api/v1/user/me">/api/v1/user/me</a>
     */
    @RequestMapping(value = "me", method = RequestMethod.GET)
    public Response<UserInfoResponse> me(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            UserEntity userEntity = userProfileService.queryMe(userId);
            boolean completed = StringUtils.isNotBlank(userEntity.getPhone());
            return Response.<UserInfoResponse>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(UserInfoResponse.builder()
                            .userId(userEntity.getUserId())
                            .nickname(userEntity.getNickname())
                            .phoneMasked(maskPhone(userEntity.getPhone()))
                            .role(userEntity.getRole())
                            .roleName(RoleEnum.of(userEntity.getRole()).getInfo())
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
            String userId = (String) httpRequest.getAttribute("userId");
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

    /**
     * 获取用户基本信息（昵称、头像、脱敏手机号等）
     * <a href="http://localhost:8080/api/v1/user/info">/api/v1/user/info</a>
     */
    @RequestMapping(value = "info", method = RequestMethod.GET)
    public Response<UserInfoResponse> info(HttpServletRequest request) {
        try {
            String userId = (String) request.getAttribute("userId");
            UserEntity userEntity = userProfileService.queryMe(userId);
            boolean completed = StringUtils.isNotBlank(userEntity.getPhone())
                    && StringUtils.isNotBlank(userEntity.getPassword());
            return Response.<UserInfoResponse>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(UserInfoResponse.builder()
                            .userId(userEntity.getUserId())
                            .nickname(userEntity.getNickname())
                            .avatar(userEntity.getAvatar())
                            .phoneMasked(maskPhone(userEntity.getPhone()))
                            .role(userEntity.getRole())
                            .roleName(RoleEnum.of(userEntity.getRole()).getInfo())
                            .profileCompleted(completed)
                            .build())
                    .build();
        } catch (AppException e) {
            return Response.<UserInfoResponse>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("获取用户基本信息失败", e);
            return Response.<UserInfoResponse>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 修改用户信息：仅更新传入的字段（如本次只改 nickname 或只改 password），手机号不可修改
     * <a href="http://localhost:8080/api/v1/user/profile/update">/api/v1/user/profile/update</a>
     */
    @RequestMapping(value = "profile/update", method = RequestMethod.POST)
    public Response<Boolean> updateProfile(@RequestBody UpdateProfileRequest request, HttpServletRequest httpRequest) {
        try {
            // 修改密码时校验两次输入一致性（前端已校验，此处兜底）
            if (StringUtils.isNotBlank(request.getPassword())
                    && !StringUtils.equals(request.getPassword(), request.getConfirmPassword())) {
                throw new AppException(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(), "两次输入的密码不一致");
            }
            String userId = (String) httpRequest.getAttribute("userId");
            log.info("修改用户信息开始 userId:{} nickname:{} avatar:{} 修改密码:{}",
                    userId, request.getNickname(), request.getAvatar(), StringUtils.isNotBlank(request.getPassword()));
            userProfileService.updateInfo(userId, request.getNickname(), request.getAvatar(), request.getPassword());
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(true)
                    .build();
        } catch (AppException e) {
            log.warn("修改用户信息业务失败 info:{}", e.getInfo());
            return Response.<Boolean>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("修改用户信息失败", e);
            return Response.<Boolean>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info(Constants.ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    /**
     * 用户上传头像（multipart/form-data，参数名 file），返回可访问的相对 URL
     * <a href="http://localhost:8080/api/v1/user/avatar/upload">/api/v1/user/avatar/upload</a>
     */
    @RequestMapping(value = "avatar/upload", method = RequestMethod.POST)
    public Response<FileUploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        try {
            if (file == null || file.isEmpty()) {
                throw new AppException(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(), "请选择要上传的图片");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new AppException(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(), "图片大小不能超过 5MB");
            }
            if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
                throw new AppException(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(), "只能上传图片文件");
            }
            String userId = (String) httpRequest.getAttribute("userId");
            String url = fileStorageService.uploadImage(file.getBytes(), file.getOriginalFilename());
            log.info("用户头像上传成功 userId:{} url:{} size:{}", userId, url, file.getSize());
            return Response.<FileUploadResponse>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(FileUploadResponse.builder().url(url).build())
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户头像上传失败", e);
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "图片上传失败");
        }
    }

    private String maskPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        return phone.length() == 11 ? phone.substring(0, 3) + "****" + phone.substring(7) : phone;
    }

}
