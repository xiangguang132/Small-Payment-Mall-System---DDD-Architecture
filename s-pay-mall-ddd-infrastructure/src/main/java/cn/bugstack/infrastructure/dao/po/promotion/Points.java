package cn.bugstack.infrastructure.dao.po.promotion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Points {

    private Long id;
    private String userId;
    private Integer availablePoints;
    private Integer frozenPoints;
    private LocalDateTime updateTime;

}
