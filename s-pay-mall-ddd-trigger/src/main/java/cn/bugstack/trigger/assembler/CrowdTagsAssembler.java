package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.crowdtags.CrowdTagsAddRequest;
import cn.bugstack.api.request.crowdtags.CrowdTagsUpdateRequest;
import cn.bugstack.api.response.crowdtags.CrowdTagsDetailResponse;
import cn.bugstack.domain.tag.model.aggregate.CrowdTagsAggregate;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;

public class CrowdTagsAssembler {

    private CrowdTagsAssembler() {
    }

    public static CrowdTagsAggregate toAggregate(CrowdTagsAddRequest request) {
        if (request == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签信息不能为空");
        }
        return CrowdTagsAggregate.create(
                trim(request.getTagId()),
                trim(request.getTagName()),
                trim(request.getTagDesc())
        );
    }

    public static CrowdTagsAggregate toUpdatedAggregate(CrowdTagsAggregate current,
                                                        CrowdTagsUpdateRequest request) {
        if (current == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "人群标签不存在");
        }
        if (request == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "人群标签信息不能为空");
        }
        return CrowdTagsAggregate.builder()
                .id(current.getId())
                .tagId(current.getTagId())
                .tagName(request.getTagName() != null ? trim(request.getTagName()) : current.getTagName())
                .tagDesc(request.getTagDesc() != null ? trim(request.getTagDesc()) : current.getTagDesc())
                .statistics(current.getStatistics())
                .createTime(current.getCreateTime())
                .updateTime(current.getUpdateTime())
                .build();
    }

    public static CrowdTagsDetailResponse toDetailResponse(CrowdTagsAggregate crowdTag) {
        if (crowdTag == null) {
            throw new AppException(ResponseCode.NOT_FOUND, "人群标签不存在");
        }
        CrowdTagsDetailResponse response = new CrowdTagsDetailResponse();
        response.setTagId(crowdTag.getTagId());
        response.setTagName(crowdTag.getTagName());
        response.setTagDesc(crowdTag.getTagDesc());
        return response;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
