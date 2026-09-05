package cn.bugstack.infrastructure.adapter.file;

import cn.bugstack.infrastructure.adapter.port.IFileStorageService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 本地磁盘文件存储实现：图片写入上传目录，通过 /files/** 静态资源映射对外提供访问
 */
@Slf4j
@Service
public class LocalFileStorageService implements IFileStorageService {

    /** 允许的图片扩展名 */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp");
    /** 日期分目录 yyyy/MM/dd，避免单目录文件过多 */
    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /** 上传根目录，默认相对工程运行目录的 ./upload */
    @Value("${file.upload-dir:./upload}")
    private String uploadDir;

    /** 静态资源访问前缀，需与 WebMvcConfig 中 /files/** 资源映射一致 */
    @Value("${file.access-prefix:/files}")
    private String accessPrefix;

    @Override
    public String uploadImage(byte[] content, String originalFilename) {
        if (content == null || content.length == 0) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "上传文件不能为空");
        }
        // 扩展名白名单校验（同时防路径穿透：文件名一律由 UUID 生成，不采用用户原始名）
        String ext = resolveExtension(originalFilename);

        String relativeDir = LocalDate.now().format(DATE_DIR);
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path targetDir = Paths.get(uploadDir, relativeDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(targetDir);
            Files.write(targetDir.resolve(filename), content);
        } catch (IOException e) {
            log.error("本地图片保存失败 filename:{}", filename, e);
            throw new AppException(ResponseCode.UN_ERROR, "图片保存失败");
        }

        String url = accessPrefix + "/" + relativeDir.replace('\\', '/') + "/" + filename;
        log.info("本地图片上传成功 url:{} size:{}", url, content.length);
        return url;
    }

    /** 从原始文件名推导扩展名并做白名单校验 */
    private String resolveExtension(String originalFilename) {
        if (StringUtils.isBlank(originalFilename) || !originalFilename.contains(".")) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "无法识别图片类型");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "不支持的图片格式: " + ext);
        }
        return "." + ext;
    }
}