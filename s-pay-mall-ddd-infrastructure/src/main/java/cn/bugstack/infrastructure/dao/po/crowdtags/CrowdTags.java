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
public class CrowdTags {

    private Long id;

    private String tagId;

    private String tagName;

    private String tagDesc;

    private Integer statistics;

    private Integer isDel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
