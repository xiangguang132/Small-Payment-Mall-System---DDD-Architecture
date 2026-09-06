package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.product.model.entity.ProductCarouselEntity;
import cn.bugstack.domain.product.repository.IProductCarouselRepository;
import cn.bugstack.infrastructure.adapter.repository.AbstractRepository;
import cn.bugstack.infrastructure.dao.IProductCarouselDao;
import cn.bugstack.infrastructure.dao.po.product.ProductCarousel;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductCarouselRepository extends AbstractRepository implements IProductCarouselRepository {

    private static final String CACHE_KEY_ACTIVE_LIST = "s-pay-mall:carousel:active";

    @Resource
    private IProductCarouselDao productCarouselDao;

    @Override
    public Long save(ProductCarouselEntity entity) {
        if (entity == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "轮播图信息不能为空");
        }
        ProductCarousel po = ProductCarousel.builder()
                .title(entity.getTitle())
                .coverImg(entity.getCoverImg())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .linkUrl(entity.getLinkUrl())
                .status(entity.getStatus() == null ? 1 : entity.getStatus())
                .isDel(0)
                .build();
        productCarouselDao.insert(po);
        redisService.remove(CACHE_KEY_ACTIVE_LIST);
        return po.getId();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "轮播图id不能为空");
        }
        productCarouselDao.deleteById(id);
        redisService.remove(CACHE_KEY_ACTIVE_LIST);
    }

    @Override
    public ProductCarouselEntity queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "轮播图id不能为空");
        }
        return getFromCacheOrDb(
                "s-pay-mall:carousel:id:" + id,
                () -> toEntity(productCarouselDao.queryById(id))
        );
    }

    @Override
    public void updateById(ProductCarouselEntity entity) {
        if (entity == null || entity.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "轮播图信息不能为空");
        }
        ProductCarousel po = ProductCarousel.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .coverImg(entity.getCoverImg())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .linkUrl(entity.getLinkUrl())
                .status(entity.getStatus())
                .build();
        productCarouselDao.update(po);
        redisService.remove(CACHE_KEY_ACTIVE_LIST);
        redisService.remove("s-pay-mall:carousel:id:" + entity.getId());
    }

    @Override
    public List<ProductCarouselEntity> queryActiveList() {
        return getFromCacheOrDb(
                CACHE_KEY_ACTIVE_LIST,
                () -> {
                    List<ProductCarousel> list = productCarouselDao.queryActiveList();
                    if (list == null || list.isEmpty()) {
                        return Collections.emptyList();
                    }
                    return list.stream().map(this::toEntity).collect(Collectors.toList());
                },
                30 * 60 * 1000L
        );
    }

    @Override
    public List<ProductCarouselEntity> queryPage(Integer pageNo, Integer pageSize) {
        int safePageNo = (pageNo == null || pageNo <= 0) ? 1 : pageNo;
        int safePageSize = (pageSize == null || pageSize <= 0) ? 10 : Math.min(pageSize, 100);
        int offset = (safePageNo - 1) * safePageSize;
        List<ProductCarousel> list = productCarouselDao.queryPage(offset, safePageSize);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public long countPage() {
        return productCarouselDao.countPage();
    }

    private ProductCarouselEntity toEntity(ProductCarousel po) {
        if (po == null) {
            return null;
        }
        return ProductCarouselEntity.builder()
                .id(po.getId())
                .title(po.getTitle())
                .coverImg(po.getCoverImg())
                .targetType(po.getTargetType())
                .targetId(po.getTargetId())
                .linkUrl(po.getLinkUrl())
                .status(po.getStatus())
                .isDel(po.getIsDel())
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
}
