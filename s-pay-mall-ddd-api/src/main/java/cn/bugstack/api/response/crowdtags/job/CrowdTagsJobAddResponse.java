package cn.bugstack.api.response.crowdtags.job;

import lombok.Data;

@Data
public class CrowdTagsJobAddResponse {

    private String tagId;

    private Integer tagType;

    private String tagRule;
}
