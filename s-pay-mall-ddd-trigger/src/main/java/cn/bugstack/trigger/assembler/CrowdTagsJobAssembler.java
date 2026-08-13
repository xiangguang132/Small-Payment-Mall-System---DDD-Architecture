package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.crowdtags.CrowdTagsJobAddRequest;
import cn.bugstack.api.response.crowdtags.job.CrowdTagsJobAddResponse;
import cn.bugstack.domain.crowdtags.model.entity.CrowdTagsJobEntity;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;

public class CrowdTagsJobAssembler {

    private CrowdTagsJobAssembler() {
    }

    public static CrowdTagsJobEntity toEntity(CrowdTagsJobAddRequest request) {
        if (request == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签任务信息不能为空");
        }
        return CrowdTagsJobEntity.builder()
                .tagId(trim(request.getTagId()))
                .tagType(request.getTagType())
                .tagRule(trim(request.getTagRule()))
                .build();
    }

    public static CrowdTagsJobAddResponse toAddResponse(CrowdTagsJobEntity crowdTagJob) {
        if (crowdTagJob == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "人群标签任务不存在");
        }
        CrowdTagsJobAddResponse response = new CrowdTagsJobAddResponse();
        response.setTagId(crowdTagJob.getTagId());
        response.setTagType(crowdTagJob.getTagType());
        response.setTagRule(crowdTagJob.getTagRule());
        return response;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

}
