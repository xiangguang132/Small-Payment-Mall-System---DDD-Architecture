package cn.bugstack.domain.groupbuy.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsEntity {

    private Long id;

    private String userId;

    private Integer availablePoints;

    private Integer frozenPoints;

    private LocalDateTime updateTime;

}
