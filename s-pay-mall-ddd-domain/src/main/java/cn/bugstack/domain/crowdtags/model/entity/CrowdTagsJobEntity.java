package cn.bugstack.domain.crowdtags.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdTagsJobEntity {

    private Integer id;

    private String tagId;

    private String batchId;

    private Integer tagType;

    private String tagRule;

    private LocalDateTime statStartTime;

    private LocalDateTime statEndTime;

    private LocalDateTime lastExecuteTime;

    private Integer status;

    private Integer isDel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
