package cn.bugstack.api.response.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /** 可访问的相对 URL（如 /files/2026/09/05/uuid.png），前端可拼域名后用于商品封面/图片集字段 */
    private String url;

}