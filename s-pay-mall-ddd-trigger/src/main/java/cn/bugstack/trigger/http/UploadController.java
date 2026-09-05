package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.file.FileUploadResponse;
import cn.bugstack.infrastructure.adapter.port.IFileStorageService;
import cn.bugstack.trigger.interceptor.RequireRole;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 通用图片上传：一次上传得到 URL，商品封面(covering_img)/图片集(imgs) 等 URL 字段共用
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/file")
public class UploadController {

    /** 单文件大小上限（字节，5MB）；spring.servlet.multipart.max-file-size 需 >= 该值 */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;

    @Resource
    private IFileStorageService fileStorageService;

    /** 上传图片（multipart/form-data，参数名 file），返回可访问的相对 URL */
    @RequireRole({RoleEnum.ADMIN})
    @PostMapping("upload")
    public Response<FileUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "图片大小不能超过 5MB");
        }
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "只能上传图片文件");
        }
        try {
            String url = fileStorageService.uploadImage(file.getBytes(), file.getOriginalFilename());
            log.info("图片上传成功 url:{} size:{}", url, file.getSize());
            return Response.<FileUploadResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(FileUploadResponse.builder().url(url).build())
                    .build();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("图片上传失败 fileName:{} size:{}", file.getOriginalFilename(), file.getSize(), e);
            throw new AppException(ResponseCode.UN_ERROR, "图片上传失败");
        }
    }

}