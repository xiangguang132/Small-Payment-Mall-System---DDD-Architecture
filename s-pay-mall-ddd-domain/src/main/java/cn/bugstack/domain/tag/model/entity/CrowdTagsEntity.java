package cn.bugstack.domain.tag.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdTagsEntity {

    private Integer id;

    private String tagId;

    private String tagName;

    private String tagDesc;

    private Integer statistics;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
