package cn.bugstack.types.sdk.weixin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 验证http是否真的来自微信
 */
public class SignatureUtil {

    /**
     * 验证
     * 使用 token signature timestamp nonce
     * 逻辑梳理
     * 1. 排序字符数组
     * 2. 转变成一个字符串
     * 3. 字符串加密
     * 4. 加密字符串与 signature 对比
     */
    // todo check
    public static boolean check(String token, String signature, String timestamp, String nonce) {
        String[] arr = new String[]{token, timestamp, nonce};
        Arrays.sort(arr);
        StringBuilder content = new StringBuilder();
        for  (String str : arr) {
            content.append(str);
        }
        MessageDigest md;
        String str = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(content.toString().getBytes());
            str = byteToStr(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return str!= null && str.equals(signature.toUpperCase());
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private static String byteToStr(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(byteToHexStr(b));
        }
        return sb.toString();
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
}
