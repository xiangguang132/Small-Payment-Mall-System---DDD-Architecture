package cn.bugstack.types.sdk.weixin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * {签名验证}
 * 该类是验证HTTP是否来源于微信服务器
 * 按照微信文档步骤如下：
 * 1. 打包关键字段并排序 token timestamp nonce ； 打包（构建一个String字符串拼接器，将关键字段追加进去）
 *  1.1 如 ["mytoken", "1625000000", "abc123"] 追加后的结果是 "mytoken1625000000abc123"
 * 2. 定义一个 SHA-1 的加密器，然后进行加密，将加密后的字节数组转换为大写十六进制字符串
 * 3. 然后对比签名是否一致
 */

public class SignatureUtil {
    /**
     * 验证签名
     * 验证 HTTP 请求是否真的来源于微信服务器
     */
    public static boolean check(String token, String signature, String timestamp, String nonce) {
        String[] arr = new String[]{token, timestamp, nonce};
        // 将token、timestamp、nonce三个参数进行字典序排序
        sort(arr);
        StringBuilder content = new StringBuilder();
        for (String s : arr) {
            content.append(s);
        }
        MessageDigest md;
        String tmpStr = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            // 将三个参数字符串拼接成一个字符串进行sha1加密
            byte[] digest = md.digest(content.toString().getBytes());
            tmpStr = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // 将sha1加密后的字符串可与signature对比，标识该请求来源于微信
        return tmpStr != null && tmpStr.equals(signature.toUpperCase());
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String byteToStr(byte[] byteArray) {
        StringBuilder strDigest = new StringBuilder();
        for (byte b : byteArray) {
            strDigest.append(byteToHexStr(b));
        }
        return strDigest.toString();
    }

    /**
     * 将字节转换为十六进制字符串
     */
    private static String byteToHexStr(byte mByte) {
        char[] Digit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] tempArr = new char[2];
        tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
        tempArr[1] = Digit[mByte & 0X0F];
        return new String(tempArr);
    }

    /**
     * 进行字典排序-冒泡排序
     */
    private static void sort(String[] str) {
        for (int i = 0; i < str.length - 1; i++) {
            for (int j = i + 1; j < str.length; j++) {
                if (str[j].compareTo(str[i]) < 0) {
                    String temp = str[i];
                    str[i] = str[j];
                    str[j] = temp;
                }
            }
        }
    }
}
