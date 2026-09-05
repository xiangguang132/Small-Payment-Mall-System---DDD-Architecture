package cn.bugstack.types.common;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 图片 URL 拼接工具类
 * <p>
 * 用于将相对路径（如 /files/2026/09/05/uuid.png）与域名前缀（如 http://localhost:8080）拼接为完整可访问 URL。
 * </p>
 */
public class ImageUrlUtils {

    private ImageUrlUtils() {
    }

    /**
     * 拼接完整图片 URL
     * <p>
     * 支持单个路径和 JSON 数组字符串（如 ["a.png","b.png"]），
     * 数组中的每个元素会分别拼接后重新组合为 JSON 数组字符串返回。
     * </p>
     *
     * @param baseUrl     域名前缀，如 http://localhost:8080、https://cdn.example.com
     * @param relativeUrl 相对路径，如 /files/2026/09/05/uuid.png 或 ["\/files\/a.png"]
     * @return 完整 URL
     */
    public static String buildFullUrl(String baseUrl, String relativeUrl) {
        if (StringUtils.isBlank(relativeUrl)) {
            return relativeUrl;
        }
        if (StringUtils.isBlank(baseUrl)) {
            return relativeUrl;
        }
        // 如果已经是完整 URL，直接返回
        if (relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://")) {
            return relativeUrl;
        }
        // JSON 数组字符串：["/files/a.png", "/files/b.png"]
        if (relativeUrl.startsWith("[")) {
            return buildFullUrlForJsonArray(baseUrl, relativeUrl);
        }
        return stripTrailingSlash(baseUrl) + relativeUrl;
    }

    /**
     * 对 JSON 数组字符串中的每个路径分别拼接域名前缀
     */
    private static String buildFullUrlForJsonArray(String baseUrl, String jsonArray) {
        String trimmed = jsonArray.substring(1, jsonArray.length() - 1).trim();
        if (trimmed.isEmpty()) {
            return "[]";
        }
        String[] parts = trimmed.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        StringBuilder sb = new StringBuilder("[");
        String prefix = stripTrailingSlash(baseUrl);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            // 去掉引号
            if (part.startsWith("\"") && part.endsWith("\"")) {
                part = part.substring(1, part.length() - 1);
            }
            // 已是完整 URL 则不拼
            if (part.startsWith("http://") || part.startsWith("https://")) {
                sb.append("\"").append(part).append("\"");
            } else {
                sb.append("\"").append(prefix).append(part).append("\"");
            }
            if (i < parts.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 批量拼接完整图片 URL
     *
     * @param baseUrl     域名前缀
     * @param relativeUrls 相对路径列表
     * @return 完整 URL 列表
     */
    public static List<String> buildFullUrls(String baseUrl, List<String> relativeUrls) {
        if (relativeUrls == null || relativeUrls.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(relativeUrls.size());
        for (String url : relativeUrls) {
            result.add(buildFullUrl(baseUrl, url));
        }
        return result;
    }

    /**
     * 从完整 URL 中提取相对路径部分
     *
     * @param fullUrl 完整 URL，如 http://localhost:8080/files/2026/09/05/uuid.png
     * @return 相对路径，如 /files/2026/09/05/uuid.png
     */
    public static String extractRelativeUrl(String fullUrl) {
        if (StringUtils.isBlank(fullUrl)) {
            return fullUrl;
        }
        // 如果不是完整 URL，原样返回
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            return fullUrl;
        }
        // 找到第一个 / 后面的部分（跳过 http:// 或 https://）
        int schemeEnd = fullUrl.indexOf("://");
        if (schemeEnd < 0) {
            return fullUrl;
        }
        int pathStart = fullUrl.indexOf('/', schemeEnd + 3);
        if (pathStart < 0) {
            return "/";
        }
        return fullUrl.substring(pathStart);
    }

    /**
     * 从完整 URL 中提取域名前缀
     *
     * @param fullUrl 完整 URL，如 http://localhost:8080/files/2026/09/05/uuid.png
     * @return 域名前缀，如 http://localhost:8080
     */
    public static String extractBaseUrl(String fullUrl) {
        if (StringUtils.isBlank(fullUrl)) {
            return fullUrl;
        }
        if (!fullUrl.startsWith("http://") && !fullUrl.startsWith("https://")) {
            return "";
        }
        int schemeEnd = fullUrl.indexOf("://");
        if (schemeEnd < 0) {
            return "";
        }
        int pathStart = fullUrl.indexOf('/', schemeEnd + 3);
        if (pathStart < 0) {
            return fullUrl;
        }
        return fullUrl.substring(0, pathStart);
    }

    private static String stripTrailingSlash(String url) {
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
