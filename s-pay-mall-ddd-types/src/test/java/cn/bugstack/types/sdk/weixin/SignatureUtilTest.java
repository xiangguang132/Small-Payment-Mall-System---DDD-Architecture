package cn.bugstack.types.sdk.weixin;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignatureUtilTest {

    @Test
    public void shouldVerifyWeixinSignature() throws Exception {
        String expected = sha1("noncetimestamptoken");

        assertTrue(SignatureUtil.check("token", expected, "timestamp", "nonce"));
        assertFalse(SignatureUtil.check("token", "WRONG", "timestamp", "nonce"));
    }

    private String sha1(String value) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}
