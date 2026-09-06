package cn.bugstack.trigger.http;

import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.user.UserInfoResponse;
import cn.bugstack.domain.auth.model.entity.UserEntity;
import cn.bugstack.domain.auth.service.IUserProfileService;
import cn.bugstack.infrastructure.adapter.port.IFileStorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = UserControllerTest.UserControllerTestConfig.class)
@TestPropertySource(properties = "file.base-url=http://localhost:8080")
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @Autowired
    private IUserProfileService userProfileService;

    @Test
    public void infoShouldReturnAvatarWithConfiguredBaseUrl() {
        String userId = "oxf0n3aZpX_OA8c_g8Wag2qsyf1U";
        String avatarPath = "/files/2026/09/06/29ac43f825754ac690e96a50242101b9.png";
        UserEntity user = UserEntity.builder()
                .userId(userId)
                .nickname("test1")
                .avatar(avatarPath)
                .phone("13800000000")
                .role(1)
                .build();
        when(userProfileService.queryMe(userId)).thenReturn(user);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute("userId")).thenReturn(userId);

        Response<UserInfoResponse> response = userController.info(request);

        assertEquals("http://localhost:8080" + avatarPath, response.getData().getAvatar());
    }

    @Configuration
    @Import(UserController.class)
    public static class UserControllerTestConfig {

        @Bean
        public IUserProfileService userProfileService() {
            return mock(IUserProfileService.class);
        }

        @Bean
        public IFileStorageService fileStorageService() {
            return mock(IFileStorageService.class);
        }
    }
}
