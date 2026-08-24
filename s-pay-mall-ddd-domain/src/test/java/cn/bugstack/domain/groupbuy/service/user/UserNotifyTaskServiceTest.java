package cn.bugstack.domain.groupbuy.service.user;

import cn.bugstack.domain.groupbuy.model.entity.UserNotifyEntity;
import cn.bugstack.domain.groupbuy.repository.IGroupBuyOrderRepository;
import cn.bugstack.domain.groupbuy.repository.IUserNotifyRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

public class UserNotifyTaskServiceTest {

    @Mock
    private IGroupBuyOrderRepository groupBuyOrderRepository;

    @Mock
    private IUserNotifyRepository userNotifyRepository;

    @InjectMocks
    private UserNotifyTaskService userNotifyTaskService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * 正常场景：解析 teamId → 查到 3 个成员 → 批量写入 3 条站内信
     */
    @Test
    public void shouldInsertNotifyListWhenTeamHasMembers() {
        String message = "{\"teamId\":\"T100\",\"outTradeNoList\":[\"A1\",\"A2\"]}";
        when(groupBuyOrderRepository.queryUserIdListByTeamId("T100"))
                .thenReturn(Arrays.asList("U1", "U2", "U3"));

        userNotifyTaskService.writeTeamSuccessNotify(message);

        // 捕获 insertList 参数，验证写入数量和内容
        ArgumentCaptor<List<UserNotifyEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(userNotifyRepository).insertList(captor.capture());

        List<UserNotifyEntity> written = captor.getValue();
        assertEquals(3, written.size());

        // 校验每条站内信字段
        for (UserNotifyEntity entity : written) {
            assertEquals("T100", entity.getTeamId());
            assertEquals("TEAM_SUCCESS", entity.getNotifyType());
            assertEquals("拼团成功提醒", entity.getTitle());
            assertEquals("您参与的拼团已成团", entity.getContent());
            assertEquals(Integer.valueOf(0), entity.getIsRead());
        }
        assertEquals("U1", written.get(0).getUserId());
        assertEquals("U2", written.get(1).getUserId());
        assertEquals("U3", written.get(2).getUserId());

        verify(groupBuyOrderRepository).queryUserIdListByTeamId("T100");
        verifyNoMoreInteractions(groupBuyOrderRepository);
    }

    /**
     * 边界场景：团内无成员 → 不调用 insertList
     */
    @Test
    public void shouldSkipInsertWhenNoMembers() {
        String message = "{\"teamId\":\"T200\",\"outTradeNoList\":[]}";
        when(groupBuyOrderRepository.queryUserIdListByTeamId("T200"))
                .thenReturn(Collections.emptyList());

        userNotifyTaskService.writeTeamSuccessNotify(message);

        verify(userNotifyRepository, never()).insertList(anyList());
    }

    /**
     * 边界场景：queryUserIdListByTeamId 返回 null → 不调用 insertList
     */
    @Test
    public void shouldSkipInsertWhenQueryReturnsNull() {
        String message = "{\"teamId\":\"T300\"}";
        when(groupBuyOrderRepository.queryUserIdListByTeamId("T300"))
                .thenReturn(null);

        userNotifyTaskService.writeTeamSuccessNotify(message);

        verify(userNotifyRepository, never()).insertList(anyList());
    }
}
