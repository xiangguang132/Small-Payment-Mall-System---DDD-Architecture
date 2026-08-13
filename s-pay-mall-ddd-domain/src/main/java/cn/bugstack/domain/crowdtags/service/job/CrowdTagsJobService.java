package cn.bugstack.domain.crowdtags.service.job;

import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;
import cn.bugstack.domain.crowdtags.repository.job.ICrowdTagsJobRepository;
import cn.bugstack.domain.crowdtags.service.detail.ICrowdTagsExecuteService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CrowdTagsJobService implements ICrowdTagsJobService {

    private static final Integer STATUS_OPEN = 1;
    private static final Integer STATUS_CLOSE = 2;

    @Resource
    private ICrowdTagsExecuteService crowdTagsExecuteService;
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
    public void operateSwitch(Long id, boolean open) {
        if (id == null) {
            throw new AppException(ResponseCode.NOT_FOUND.getCode(), ResponseCode.NOT_FOUND.getInfo());
        }
        crowdTagsJobRepository.updateStatus(id, open ? STATUS_OPEN : STATUS_CLOSE);
    }

    @Override
    public void manualExecute(Long id) {
        CrowdTagsJobEntity job = crowdTagsJobRepository.queryById(id);
        if (job == null
                || job.getBatchId() == null
                || job.getId() == null
                || Integer.valueOf(1).equals(job.getIsDel())) {
            throw new AppException(ResponseCode.NOT_FOUND, "人群标签任务不存在或已删除");
        }

        if (!Integer.valueOf(1).equals(job.getStatus())) {
            throw new AppException(ResponseCode.CONFLICT, "人群标签任务未开启");
        }

        crowdTagsExecuteService.execTagBatchJob(job.getTagId(), job.getBatchId());
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

    @Override
    public List<CrowdTagsJobEntity> queryCrowdTagJob(String tagId,
                                                     String batchId,
                                                     Integer tagType,
                                                     String tagRule,
                                                     int pageNo,
                                                     int pageSize) {
        return crowdTagsJobRepository.queryPage(
                tagId,
                batchId,
                tagType,
                tagRule,
                (pageNo - 1) * pageSize,
                pageSize
        );
    }

    @Override
    public long countCrowdTagJob(String tagId,
                                 String batchId,
                                 Integer tagType,
                                 String tagRule) {
        return crowdTagsJobRepository.count(tagId, batchId, tagType, tagRule);
    }

    @Override
    public void deleteCrowdTagsJobById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER, "删除人群标签任务请求id不能为空");
        }
        CrowdTagsJobEntity crowdTagsJobEntity = crowdTagsJobRepository.queryById(id);
        if (Integer.valueOf(1).equals(crowdTagsJobEntity.getIsDel())) {
            throw new AppException(ResponseCode.NOT_FOUND, "人群标签任务资源已被删除");
        }
        crowdTagsJobRepository.deleteById(id);
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
