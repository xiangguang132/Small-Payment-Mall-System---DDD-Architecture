package cn.bugstack.domain.auth.service;

import cn.bugstack.domain.auth.adapter.port.IJwtPort;
import cn.bugstack.domain.auth.adapter.port.ILoginPort;
import cn.bugstack.domain.auth.adapter.repository.IUserRepository;
import cn.bugstack.domain.auth.model.entity.UserEntity;
import com.google.common.cache.Cache;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WeixinLoginServiceTest {

    @Mock
    private ILoginPort loginPort;

    @Mock
    private IJwtPort jwtPort;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private Cache<String, String> openidToken;

    @InjectMocks
    private WeixinLoginService loginService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateQrCodeTicket() throws Exception {
        when(loginPort.createQrCodeTicket()).thenReturn("ticket-1");

        assertEquals("ticket-1", loginService.createQrCodeTicket());
    }

    @Test
    public void shouldReturnNullWhenTicketHasNoLoginState() {
        when(openidToken.getIfPresent("ticket-1")).thenReturn(null);

        assertNull(loginService.checkLogin("ticket-1"));
    }

    @Test
    public void shouldCreateTokenAndInvalidateTicketAfterLogin() {
        when(openidToken.getIfPresent("ticket-1")).thenReturn("openid-1");
        when(jwtPort.createToken("openid-1")).thenReturn("jwt-token");

        assertEquals("jwt-token", loginService.checkLogin("ticket-1"));
        verify(openidToken).invalidate("ticket-1");
    }

    @Test
    public void shouldSaveLoginStateAndCreateProfileForNewUser() throws Exception {
        when(userRepository.queryByUserId("openid-1")).thenReturn(null);

        loginService.saveLoginState("ticket-1", "openid-1");

        verify(userRepository).save(any(UserEntity.class));
        verify(openidToken).put("ticket-1", "openid-1");
        verify(loginPort).sendLoginTempleteMessage("openid-1");
    }

    @Test
    public void shouldSkipProfileCreationForExistingUser() throws Exception {
        when(userRepository.queryByUserId("openid-1")).thenReturn(UserEntity.builder().userId("openid-1").build());

        loginService.saveLoginState("ticket-1", "openid-1");

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(openidToken).put("ticket-1", "openid-1");
        verify(loginPort).sendLoginTempleteMessage("openid-1");
    }
}
