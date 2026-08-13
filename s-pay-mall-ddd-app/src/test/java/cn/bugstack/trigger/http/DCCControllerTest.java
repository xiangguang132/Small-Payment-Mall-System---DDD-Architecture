package cn.bugstack.trigger.http;

import org.junit.Before;
import org.junit.Test;
import org.redisson.api.RTopic;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DCCControllerTest {

    private RTopic topic;

    @Before
    public void setUp() {
        topic = mock(RTopic.class);
    }

    @Test
    public void shouldPublishDowngradeSwitchAndReturnCustomInfo() throws Exception {
        MockMvc mockMvc = dccMockMvc();

        mockMvc.perform(post("/api/v1/gbm/dcc/update_config")
                        .param("key", "downgradeSwitch")
                        .param("value", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info").value("开启降级，拼团试算将被拦截"));

        verify(topic).publish("downgradeSwitch,1");
    }

    @Test
    public void shouldReturnCustomInfoWhenDowngradeSwitchUsesInvalidValue() throws Exception {
        MockMvc mockMvc = dccMockMvc();

        mockMvc.perform(post("/api/v1/gbm/dcc/update_config")
                        .param("key", "downgradeSwitch")
                        .param("value", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info").value(
                        "downgradeSwitch=100 不是“降级 100%”，而是无效值；未开启降级，拼团试算正常放行"
                ));

        verify(topic).publish("downgradeSwitch,100");
    }

    @Test
    public void shouldReturnCustomInfoWhenCutRangeUpdated() throws Exception {
        MockMvc mockMvc = dccMockMvc();

        mockMvc.perform(post("/api/v1/gbm/dcc/update_config")
                        .param("key", "cutRange")
                        .param("value", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info").value("已修改切量范围为50"));

        verify(topic).publish("cutRange,50");
    }

    private MockMvc dccMockMvc() {
        DCCController controller = new DCCController();
        ReflectionTestUtils.setField(controller, "dccTopic", topic);
        return MockMvcBuilders.standaloneSetup(controller).build();
    }
}
