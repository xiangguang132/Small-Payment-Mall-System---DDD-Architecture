package cn.bugstack.trigger.interceptor;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthInterceptorTest {

    @Mock
    private IJwtPort jwtPort;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter writer;

    @InjectMocks
    private AuthInterceptor interceptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldAllowLoginAndWeixinPortalPaths() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/weixin/portal");

        assertTrue(interceptor.preHandle(request, response, null));
        verify(jwtPort, never()).verifyToken(anyString());
    }

    @Test
    public void shouldRejectWhenTokenMissing() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getCookies()).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        assertFalse(interceptor.preHandle(request, response, null));
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(writer).write(anyString());
    }

    @Test
    public void shouldAcceptValidBearerToken() throws Exception {
        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer token-1");
        when(jwtPort.verifyToken("token-1")).thenReturn(true);
        when(jwtPort.parseOpenid("token-1")).thenReturn("openid-1");

        assertTrue(interceptor.preHandle(request, response, null));
        verify(request).setAttribute("openid", "openid-1");
    }
}
