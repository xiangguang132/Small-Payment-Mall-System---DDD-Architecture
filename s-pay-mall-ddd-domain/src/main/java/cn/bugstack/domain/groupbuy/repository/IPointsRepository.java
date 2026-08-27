package cn.bugstack.domain.groupbuy.repository;

import cn.bugstack.domain.groupbuy.model.entity.PointsEntity;

public interface IPointsRepository {

    PointsEntity queryPointsByUserId(String userId);

}