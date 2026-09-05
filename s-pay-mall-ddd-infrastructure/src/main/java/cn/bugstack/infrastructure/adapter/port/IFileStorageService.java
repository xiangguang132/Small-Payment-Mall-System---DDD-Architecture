package cn.bugstack.infrastructure.adapter.port;

/**
 * 文件存储端口：上传文件并返回可访问的相对 URL。
 * 实现可替换为本地磁盘 / 云存储（OSS/COS/MinIO）等。
 */
public interface IFileStorageService {

    /**
     * 上传图片内容并返回可访问的相对 URL（如 /files/2026/09/05/uuid.png）
     * @param content           图片二进制内容
     * @param originalFilename  原始文件名（用于推导扩展名）
     * @return 可访问的相对 URL
     */
    String uploadImage(byte[] content, String originalFilename);
}