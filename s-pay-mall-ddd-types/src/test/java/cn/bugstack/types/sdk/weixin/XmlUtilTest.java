package cn.bugstack.types.sdk.weixin;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletInputStream;
import javax.servlet.ReadListener;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class XmlUtilTest {

    @Test
    public void shouldConvertBeanToXmlAndBack() {
        MessageTextEntity entity = new MessageTextEntity();
        entity.setToUserName("gh_123");
        entity.setFromUserName("openid-1");
        entity.setMsgType("text");
        entity.setContent("hello");

        String xml = XmlUtil.beanToXml(entity);

        assertNotNull(xml);
        assertTrue(xml.contains("<ToUserName>"));
        assertTrue(xml.contains("hello"));

        MessageTextEntity parsed = XmlUtil.xmlToBean(xml, MessageTextEntity.class);
        assertEquals("gh_123", parsed.getToUserName());
        assertEquals("openid-1", parsed.getFromUserName());
        assertEquals("hello", parsed.getContent());
    }

    @Test
    public void shouldParseRequestXmlToMap() throws Exception {
        String xml = "<xml><ToUserName>gh_123</ToUserName><Content>hello</Content></xml>";
        HttpServletRequest request = mock(HttpServletRequest.class);
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        when(request.getInputStream()).thenReturn(new ServletInputStream() {
            private final ByteArrayInputStream delegate = new ByteArrayInputStream(bytes);

            @Override
            public boolean isFinished() {
                return delegate.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public int read() {
                return delegate.read();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        });

        Map<String, String> map = XmlUtil.xmlToMap(request);

        assertEquals("gh_123", map.get("ToUserName"));
        assertEquals("hello", map.get("Content"));
    }

    @Test
    public void shouldConvertMapToXml() {
        Map<String, Object> map = new HashMap<>();
        map.put("ToUserName", "gh_123");
        map.put("Content", "hello");

        String xml = XmlUtil.mapToXML(map);

        assertTrue(xml.startsWith("<xml>"));
        assertTrue(xml.contains("<ToUserName><![CDATA[gh_123]]></ToUserName>"));
        assertTrue(xml.contains("<Content><![CDATA[hello]]></Content>"));
    }
}
