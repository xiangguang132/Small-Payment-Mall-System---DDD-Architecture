package cn.bugstack.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdTagsDetail {

    private Long id;

    private String tagId;

    private String userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
