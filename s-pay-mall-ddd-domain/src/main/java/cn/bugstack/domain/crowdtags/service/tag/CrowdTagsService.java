package cn.bugstack.domain.crowdtags.service.tag;

import cn.bugstack.domain.crowdtags.model.aggregate.CrowdTagsAggregate;
import cn.bugstack.domain.crowdtags.repository.tag.ICrowdTagsRepository;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CrowdTagsService implements ICrowdTagsService {

    @Resource
    private ICrowdTagsRepository crowdTagsRepository;

    @Override
    public Long addCrowdTag(CrowdTagsAggregate crowdTag) {
        validateCreate(crowdTag);
        CrowdTagsAggregate exist = crowdTagsRepository.queryByTagId(crowdTag.getTagId());
        if (exist != null) {
            throw new AppException(ResponseCode.CONFLICT, "人群标签ID已存在");
        }
        return crowdTagsRepository.save(crowdTag);
    }

    @Override
    public void deleteCrowdTagById(Long id) {
        CrowdTagsAggregate current = queryCrowdTagById(id);
        long activityCount = crowdTagsRepository.countActivityByTagId(current.getTagId());
        if (activityCount > 0) {
            throw new AppException(ResponseCode.CONFLICT, "人群标签已被拼团活动引用，不能删除");
        }
        long discountCount = crowdTagsRepository.countDiscountByTagId(current.getTagId());
        if (discountCount > 0) {
            throw new AppException(ResponseCode.CONFLICT, "人群标签已被拼团优惠引用，不能删除");
        }
        crowdTagsRepository.deleteById(id);
    }

    @Override
    public CrowdTagsAggregate queryCrowdTagById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签id不能为空");
        }
        CrowdTagsAggregate crowdTag = crowdTagsRepository.queryById(id);
        if (crowdTag == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "人群标签不存在");
        }
        return crowdTag;
    }

    @Override
    public CrowdTagsAggregate updateCrowdTag(Long id, CrowdTagsAggregate updated) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签id不能为空");
        }
        if (updated == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签信息不能为空");
        }
        CrowdTagsAggregate current = queryCrowdTagById(id);
        if (updated.getTagName() != null && updated.getTagName().trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签名称不能为空");
        }

        CrowdTagsAggregate result = CrowdTagsAggregate.builder()
                .id(current.getId())
                .tagId(current.getTagId())
                .tagName(updated.getTagName() != null ? updated.getTagName().trim() : current.getTagName())
                .tagDesc(updated.getTagDesc() != null ? updated.getTagDesc().trim() : current.getTagDesc())
                .statistics(current.getStatistics())
                .createTime(current.getCreateTime())
                .updateTime(LocalDateTime.now())
                .build();
        crowdTagsRepository.update(result);
        return result;
    }

    @Override
    public List<CrowdTagsAggregate> queryCrowdTags(String tagId, String tagName, int pageNo, int pageSize) {
        return crowdTagsRepository.queryPage(tagId, tagName, (pageNo - 1) * pageSize, pageSize);
    }

    @Override
    public long countCrowdTags(String tagId, String tagName) {
        return crowdTagsRepository.count(tagId, tagName);
    }

    private void validateCreate(CrowdTagsAggregate crowdTag) {
        if (crowdTag == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签信息不能为空");
        }
        if (crowdTag.getTagId() == null || crowdTag.getTagId().trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签ID不能为空");
        }
        if (crowdTag.getTagName() == null || crowdTag.getTagName().trim().isEmpty()) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签名称不能为空");
        }
    }
}
