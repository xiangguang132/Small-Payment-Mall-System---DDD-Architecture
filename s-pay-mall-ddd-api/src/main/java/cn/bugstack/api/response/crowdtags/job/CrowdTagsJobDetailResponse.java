package cn.bugstack.api.response.crowdtags.job;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CrowdTagsJobDetailResponse {

    private Long id;

    private String tagId;

    private String batchId;

    private Integer tagType;

    private String tagRule;

    private LocalDateTime statStartTime;

    private LocalDateTime statEndTime;

    private LocalDateTime lastExecuteTime;

    private Integer status;

}
