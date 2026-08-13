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
public class CrowdTagsDetailEntity {

    private Integer id;

    private String tagId;

    private String userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
