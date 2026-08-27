package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.groupbuy.model.entity.PointsEntity;
import cn.bugstack.domain.groupbuy.repository.IPointsRepository;
import cn.bugstack.infrastructure.dao.IPointsDao;
import cn.bugstack.infrastructure.dao.po.promotion.Points;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class PointsRepository implements IPointsRepository {

    @Resource
    private IPointsDao pointsDao;

    public PointsEntity queryPointsByUserId(String userId) {
        Points points = pointsDao.queryPointsByUserId(userId);
        if (points == null) {
            return null;
        }
        return PointsEntity.builder()
                .id(points.getId())
                .userId(points.getUserId())
                .availablePoints(points.getAvailablePoints())
                .frozenPoints(points.getFrozenPoints())
                .updateTime(points.getUpdateTime())
                .build();
    }
}
