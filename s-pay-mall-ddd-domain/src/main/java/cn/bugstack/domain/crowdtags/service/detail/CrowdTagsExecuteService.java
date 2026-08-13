package cn.bugstack.domain.crowdtags.service.detail;

import cn.bugstack.domain.crowdtags.repository.detail.ICrowdTagsDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 该类是系统自动执行的类
 */
@Slf4j
@Service
public class CrowdTagsExecuteService implements ICrowdTagsExecuteService {

    @Resource
    private ICrowdTagsDetailRepository tagRepository;

    @Override
    public void execTagBatchJob(String tagId, String batchId) {
        log.info("人群标签批次任务 标签是 tagId:{}， 批次是 batchId:{}", tagId, batchId);

        // todo 采集用户信息

        // 数据写入记录
        // 根据规则生成应该命中的人群 userId 列表
        List<String> userIdList = new ArrayList<String>(){{
            add("xiaofuge");
            add("liergou");
        }};

        // 把用户写入 crowd_tags_detail，并同步写入 Redis RBitSet[tagId]
        for (String userId : userIdList) {
            tagRepository.addCrowdTagsByUserId(tagId, userId);
        }
    }
}
