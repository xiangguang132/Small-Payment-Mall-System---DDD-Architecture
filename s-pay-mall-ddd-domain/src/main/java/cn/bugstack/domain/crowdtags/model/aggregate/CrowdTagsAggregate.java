package cn.bugstack.domain.crowdtags.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrowdTagsAggregate {

    private Long id;

    private String tagId;

    private String tagName;

    private String tagDesc;

    @Builder.Default
    private Integer statistics = 0;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static CrowdTagsAggregate create(String tagId, String tagName, String tagDesc) {
        LocalDateTime now = LocalDateTime.now();
        return CrowdTagsAggregate.builder()
                .tagId(tagId)
                .tagName(tagName)
                .tagDesc(tagDesc == null ? "" : tagDesc)
                .statistics(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }
}
