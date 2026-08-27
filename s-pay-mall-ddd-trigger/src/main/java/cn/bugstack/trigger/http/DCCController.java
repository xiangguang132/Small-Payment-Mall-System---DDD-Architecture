package cn.bugstack.trigger.http;

import cn.bugstack.api.IDCCService;
import cn.bugstack.api.response.Response;
import cn.bugstack.trigger.interceptor.RequireRole;
import cn.bugstack.types.common.Constants;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.enums.RoleEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/dcc")
@RequireRole({RoleEnum.ADMIN})
public class DCCController implements IDCCService {

    @Resource
    private RTopic dccTopic;

    @PostMapping("update_config")
    @Override
    public Response<Boolean> updateConfig(@RequestParam String key, @RequestParam String value) {
        try {
            log.info("DCC 动态配置值变更 key:{} value:{}", key, value);
            dccTopic.publish(key + Constants.SPLIT + value);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(buildConfigInfo(key, value))
                    .build();
        } catch (Exception e) {
            log.error("DCC 动态配置值变更失败 key:{} value:{}", key, value, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }

    private String buildConfigInfo(String key, String value) {
        if ("downgradeSwitch".equals(key)) {
            if ("1".equals(value)) {
                return "开启降级，拼团试算将被拦截";
            }
            if ("0".equals(value)) {
                return "关闭降级，拼团试算正常放行";
            }
            return "downgradeSwitch=" + value + " 不是“降级 100%”，而是无效值；未开启降级，拼团试算正常放行";
        }
        if ("cutRange".equals(key)) {
            return "已修改切量范围为" + value;
        }
        return "DCC 配置更新成功";
    }
}
