package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import cn.bugstack.domain.supplier.repository.ISupplierRepository;
import cn.bugstack.infrastructure.dao.ISupplierDao;
import cn.bugstack.infrastructure.dao.po.Supplier;
import cn.bugstack.infrastructure.redis.IRedisService;
import cn.bugstack.types.enums.ResponseCode;
import cn.bugstack.types.exception.AppException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class SupplierRepository implements ISupplierRepository {

    @Resource
    private ISupplierDao supplierDao;

    @Resource
    private IRedisService redisService;

    @Override
    public Long save(SupplierAggregate supplierAggregate) {
        if (supplierAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "供应商信息不能为空");
        }
        Supplier supplier = Supplier.builder()
                .supplierCode(supplierAggregate.getSupplierCode())
                .name(supplierAggregate.getName())
                .contactName(supplierAggregate.getContactName())
                .contactPhone(supplierAggregate.getContactPhone())
                .address(supplierAggregate.getAddress())
                .status(supplierAggregate.getStatus())
                .isDel(supplierAggregate.getIsDel())
                .createTime(supplierAggregate.getCreateTime())
                .updateTime(supplierAggregate.getUpdateTime())
                .build();
        supplierDao.insert(supplier);
        return supplier.getId();
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "供应商id不能为空");
        }
        supplierDao.deleteById(id);
        redisService.delete(cacheKeyById(id));
    }

    @Override
    public SupplierAggregate queryById(Long id) {
        if (id == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "供应商id不能为空");
        }
        String cacheKey = cacheKeyById(id);
        SupplierAggregate cached = redisService.get(cacheKey, SupplierAggregate.class);
        if (cached != null) {
            return cached;
        }
        Supplier supplier = supplierDao.queryById(id);
        if (supplier == null) {
            return null;
        }
        SupplierAggregate aggregate = SupplierAggregate.builder()
                .id(supplier.getId())
                .supplierCode(supplier.getSupplierCode())
                .name(supplier.getName())
                .contactName(supplier.getContactName())
                .contactPhone(supplier.getContactPhone())
                .address(supplier.getAddress())
                .status(supplier.getStatus())
                .isDel(supplier.getIsDel())
                .createTime(supplier.getCreateTime())
                .updateTime(supplier.getUpdateTime())
                .build();
        redisService.set(cacheKey, aggregate);
        return aggregate;
    }

    @Override
    public void updateById(SupplierAggregate supplierAggregate) {
        if (supplierAggregate == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "供应商信息不能为空");
        }
        if (supplierAggregate.getId() == null) {
            throw new AppException(ResponseCode.UNPROCESSABLE_ENTITY, "供应商id不能为空");
        }
        Supplier supplier = Supplier.builder()
                .id(supplierAggregate.getId())
                .supplierCode(supplierAggregate.getSupplierCode())
                .name(supplierAggregate.getName())
                .contactName(supplierAggregate.getContactName())
                .contactPhone(supplierAggregate.getContactPhone())
                .address(supplierAggregate.getAddress())
                .status(supplierAggregate.getStatus())
                .isDel(supplierAggregate.getIsDel())
                .createTime(supplierAggregate.getCreateTime())
                .updateTime(supplierAggregate.getUpdateTime())
                .build();
        supplierDao.update(supplier);
        redisService.delete(cacheKeyById(supplierAggregate.getId()));
    }

    private String cacheKeyById(Long id) {
        return "s-pay-mall:supplier:id:" + id;
    }
}
