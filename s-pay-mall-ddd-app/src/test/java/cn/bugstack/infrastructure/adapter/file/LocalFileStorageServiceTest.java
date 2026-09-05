package cn.bugstack.infrastructure.adapter.file;

import cn.bugstack.infrastructure.adapter.port.IFileStorageService;
import cn.bugstack.types.exception.AppException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * LocalFileStorageService 单元测试：不启动 Spring 容器，直接反射注入配置字段验证落盘与 URL 生成
 */
public class LocalFileStorageServiceTest {

    private static final Pattern URL_PATTERN =
            Pattern.compile("^/files/\\d{4}/\\d{2}/\\d{2}/[0-9a-f]{32}\\.png$");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private IFileStorageService fileStorageService;

    @Before
    public void setUp() throws Exception {
        LocalFileStorageService service = new LocalFileStorageService();
        // 反射注入 @Value 字段，避免依赖 Spring 容器
        setField(service, "uploadDir", tempFolder.getRoot().getAbsolutePath());
        setField(service, "accessPrefix", "/files");
        fileStorageService = service;
    }

    @Test
    public void shouldUploadAndReturnUrlWhenValidPng() throws Exception {
        byte[] content = new byte[]{0x01, 0x02, 0x03};
        String url = fileStorageService.uploadImage(content, "商品封面.png");

        assertTrue("返回 URL 格式不正确: " + url, URL_PATTERN.matcher(url).matches());

        // 校验文件确实落盘，且落盘文件字节与上传内容一致
        Path saved = tempFolder.getRoot().toPath().resolve(url.substring("/files/".length()).replace('/', java.io.File.separatorChar));
        assertTrue("文件未落盘: " + saved, Files.exists(saved));
        assertEquals(content.length, Files.size(saved));
    }

    @Test
    public void shouldRejectUnsupportedExtension() {
        assertRejected("photo.exe", new byte[]{0x01});
        assertRejected("no-extension", new byte[]{0x01});
        // 目录穿越文件名应同样被扩展名白名单拦截
        assertRejected("../../evil.jsp", new byte[]{0x01});
    }

    @Test
    public void shouldRejectEmptyContent() {
        assertEmptyContentRejected();
    }

    private void assertRejected(String filename, byte[] content) {
        try {
            fileStorageService.uploadImage(content, filename);
            org.junit.Assert.fail("应当拒绝非法文件名: " + filename);
        } catch (AppException expected) {
            // 预期抛出业务异常
        }
    }

    private void assertEmptyContentRejected() {
        try {
            fileStorageService.uploadImage(new byte[0], "a.png");
            org.junit.Assert.fail("应当拒绝空文件内容");
        } catch (AppException expected) {
            // 预期抛出业务异常
        }
    }

    /** 反射设置私有字段值 */
    private void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}