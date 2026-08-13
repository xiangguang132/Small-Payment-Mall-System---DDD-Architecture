package cn.bugstack.infrastructure.dao.po.crowdtags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdTagsJob {

    private Long id;

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
