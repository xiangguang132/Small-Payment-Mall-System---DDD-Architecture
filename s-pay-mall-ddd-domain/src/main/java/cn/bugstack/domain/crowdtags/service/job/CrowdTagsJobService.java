package cn.bugstack.domain.crowdtags.service.job;

import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;
import cn.bugstack.domain.crowdtags.repository.job.ICrowdTagsJobRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CrowdTagsJobService implements ICrowdTagsJobService {

    @Resource
    private ICrowdTagsJobRepository crowdTagsJobRepository;

    @Override
    public Long addCrowdTagJob(CrowdTagsJobEntity crowdTagJob) {
        validate(crowdTagJob);
        LocalDateTime now = LocalDateTime.now();
        crowdTagJob.setBatchId(buildBatchId());
        crowdTagJob.setStatus(0);
        crowdTagJob.setIsDel(0);
        crowdTagJob.setCreateTime(now);
        crowdTagJob.setUpdateTime(now);
        return crowdTagsJobRepository.save(crowdTagJob);
    }

    @Override
    public CrowdTagsJobEntity queryCrowdTagJobById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签任务id不能为空");
        }
        CrowdTagsJobEntity crowdTagJob = crowdTagsJobRepository.queryById(id);
        if (crowdTagJob == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "人群标签任务不存在");
        }
        return crowdTagJob;
    }

    private void validate(CrowdTagsJobEntity crowdTagJob) {
        if (crowdTagJob == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签任务信息不能为空");
        }
        if (crowdTagJob.getTagId() == null || crowdTagJob.getTagId().trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签ID不能为空");
        }
        if (crowdTagJob.getTagType() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "标签类型不能为空");
        }
        if (crowdTagJob.getTagRule() == null || crowdTagJob.getTagRule().trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "打标规则不能为空");
        }
    }

    private String buildBatchId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }
}
