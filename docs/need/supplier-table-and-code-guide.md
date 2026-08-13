# supplier 表与最小实现参考

这份文档只做一件事：在现有 `product / product_type / warehouse` 基础上，补一张最适合做供应链起点的表 `supplier`，并给出按当前项目分层写代码时的文件位置和参考骨架。

## 1. 先说明目标

你当前项目已经有：

- 商品 `product`
- 商品分类 `product_type`
- 仓库 `warehouse`

这三张表属于主数据。  
下一步最适合补的是 `supplier`，因为它是后续采购单、入库单、库存流水的起点。

这张表做完后，后续可以自然接：

- `purchase_order`
- `purchase_order_item`
- `inventory_flow`
- `sales_order`

但第一步只做 `supplier` 就够了。

## 2. `supplier` 表建表语句

建议放在：

- `data/sql/supplier.sql`

建表示例：

```sql
CREATE TABLE `supplier` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `supplier_code` VARCHAR(64) NOT NULL COMMENT '供应商编码',
  `name` VARCHAR(128) NOT NULL COMMENT '供应商名称',
  `contact_name` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  KEY `idx_status_is_del` (`status`, `is_del`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商表';
```

### 字段说明

- `supplier_code`：供应商编码，业务侧生成，必须唯一
- `name`：供应商名称
- `contact_name`：联系人
- `contact_phone`：联系电话
- `address`：地址
- `status`：状态，0 禁用，1 启用
- `is_del`：软删除标记
- `create_time` / `update_time`：审计字段

## 3. 推荐的文件位置

下面按你当前工程的分层给出文件路径。

### 3.1 API 层

放请求和响应对象。

- `s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/request/supplier/SupplierAddRequest.java`
- `s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/response/supplier/SupplierDetailResponse.java`

### 3.2 Trigger 层

放 Controller 和组装器。

- `s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/http/SupplierController.java`
- `s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/assembler/SupplierAssembler.java`

### 3.3 Domain 层

放领域对象、领域规则、仓储接口、领域服务。

- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/model/aggregate/SupplierAggregate.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/model/vo/SupplierStatusVO.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/repository/ISupplierRepository.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/service/ISupplierService.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/service/SupplierService.java`

### 3.4 Infrastructure 层

放数据库 PO、DAO、Repository 实现。

- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/po/Supplier.java`
- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/ISupplierDao.java`
- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/SupplierRepository.java`

### 3.5 MyBatis

- `s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/supplier_mapper.xml`

## 4. 文件职责说明

这一部分是按你当前项目的写法来对齐的。

### 4.1 `SupplierAddRequest`

职责：

- 接收新增请求
- 做字段级校验

建议字段：

- `supplierCode`
- `name`
- `contactName`
- `contactPhone`
- `address`
- `status`

### 4.2 `SupplierDetailResponse`

职责：

- 返回详情页数据
- 给前端展示使用

建议字段：

- `id`
- `supplierCode`
- `name`
- `contactName`
- `contactPhone`
- `address`
- `status`
- `isDel`
- `createTime`
- `updateTime`

### 4.3 `SupplierAssembler`

职责：

- `SupplierAddRequest -> SupplierAggregate`
- `SupplierAggregate -> SupplierDetailResponse`

这层只做对象转换，不写业务规则。

### 4.4 `SupplierController`

职责：

- 接收 HTTP 请求
- 调用领域服务
- 返回统一 `Response`

建议先做 4 个接口：

- 新增
- 详情
- 修改
- 删除

### 4.5 `SupplierAggregate`

职责：

- 供应商领域对象
- 持有状态、编码、名称、联系人等字段
- 提供 `create()` 工厂方法

### 4.6 `SupplierStatusVO`

职责：

- 定义状态值
- 提供合法性判断

### 4.7 `ISupplierRepository`

职责：

- 定义领域层对外的仓储接口

### 4.8 `SupplierRepository`

职责：

- 负责领域对象和 PO 的转换
- 调用 DAO 落库和查询

### 4.9 `ISupplierDao`

职责：

- 只放 MyBatis 接口方法

### 4.10 `supplier_mapper.xml`

职责：

- 完成 insert / update / delete / queryById

## 5. 参考代码骨架

下面是按当前仓库风格整理的参考骨架。  
这不是完整实现，但你可以直接照着建文件。

### 5.1 `SupplierAddRequest.java`

路径：

- `s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/request/supplier/SupplierAddRequest.java`

参考结构：

```java
package cn.bugstack.api.request.supplier;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class SupplierAddRequest {

    @NotBlank(message = "供应商编码不能为空")
    private String supplierCode;

    @NotBlank(message = "供应商名称不能为空")
    private String name;

    private String contactName;

    private String contactPhone;

    private String address;

    @NotNull(message = "供应商状态不能为空")
    @Min(value = 0, message = "供应商状态值非法")
    @Max(value = 1, message = "供应商状态值非法")
    private Integer status;
}
```

### 5.2 `SupplierDetailResponse.java`

路径：

- `s-pay-mall-ddd-api/src/main/java/cn/bugstack/api/response/supplier/SupplierDetailResponse.java`

参考结构：

```java
package cn.bugstack.api.response.supplier;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierDetailResponse {

    private Long id;
    private String supplierCode;
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### 5.3 `SupplierStatusVO.java`

路径：

- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/model/vo/SupplierStatusVO.java`

参考结构：

```java
package cn.bugstack.domain.supplier.model.vo;

public class SupplierStatusVO {

    public static final int DISABLED = 0;
    public static final int ENABLED = 1;

    private SupplierStatusVO() {
    }

    public static boolean isValid(Integer status) {
        return status != null && (status == DISABLED || status == ENABLED);
    }
}
```

### 5.4 `SupplierAggregate.java`

路径：

- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/model/aggregate/SupplierAggregate.java`

参考结构：

```java
package cn.bugstack.domain.supplier.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierAggregate {

    private Long id;
    private String supplierCode;
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private Integer status;
    @Builder.Default
    private Integer isDel = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static SupplierAggregate create(String supplierCode, String name,
                                           String contactName, String contactPhone,
                                           String address, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return SupplierAggregate.builder()
                .supplierCode(supplierCode)
                .name(name)
                .contactName(contactName)
                .contactPhone(contactPhone)
                .address(address)
                .status(status)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }
}
```

### 5.5 `ISupplierRepository.java`

路径：

- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/repository/ISupplierRepository.java`

参考结构：

```java
package cn.bugstack.domain.supplier.repository;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;

public interface ISupplierRepository {

    Long save(SupplierAggregate supplierAggregate);

    void deleteById(Long id);

    SupplierAggregate queryById(Long id);

    void updateById(SupplierAggregate supplierAggregate);
}
```

### 5.6 `ISupplierService.java`

路径：

- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/service/ISupplierService.java`

参考结构：

```java
package cn.bugstack.domain.supplier.service;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;

public interface ISupplierService {

    Long addSupplier(SupplierAggregate supplier);

    void deleteSupplierById(Long id);

    SupplierAggregate querySupplierById(Long id);

    void updateSupplierById(SupplierAggregate supplier);
}
```

### 5.7 `SupplierService.java`

路径：

- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/supplier/service/SupplierService.java`

参考结构：

```java
package cn.bugstack.domain.supplier.service;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import cn.bugstack.domain.supplier.model.vo.SupplierStatusVO;
import cn.bugstack.domain.supplier.repository.ISupplierRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SupplierService implements ISupplierService {

    @Resource
    private ISupplierRepository supplierRepository;

    @Override
    public Long addSupplier(SupplierAggregate supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        validate(supplier);
        return supplierRepository.save(supplier);
    }

    @Override
    public void deleteSupplierById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        SupplierAggregate current = supplierRepository.queryById(id);
        if (current == null) {
            throw new IllegalArgumentException("供应商不存在");
        }
        supplierRepository.deleteById(id);
    }

    @Override
    public SupplierAggregate querySupplierById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        return supplierRepository.queryById(id);
    }

    @Override
    public void updateSupplierById(SupplierAggregate supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        if (supplier.getId() == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }
        validate(supplier);

        SupplierAggregate current = supplierRepository.queryById(supplier.getId());
        if (current == null) {
            throw new IllegalArgumentException("供应商不存在");
        }

        supplier.setCreateTime(current.getCreateTime());
        supplier.setIsDel(current.getIsDel());
        supplierRepository.updateById(supplier);
    }

    private void validate(SupplierAggregate supplier) {
        if (!SupplierStatusVO.isValid(supplier.getStatus())) {
            throw new IllegalArgumentException("供应商状态值非法");
        }
        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().trim().isEmpty()) {
            throw new IllegalArgumentException("供应商编码不能为空");
        }
        if (supplier.getName() == null || supplier.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("供应商名称不能为空");
        }
    }
}
```

### 5.8 `SupplierAssembler.java`

路径：

- `s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/assembler/SupplierAssembler.java`

参考结构：

```java
package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.supplier.SupplierAddRequest;
import cn.bugstack.api.response.supplier.SupplierDetailResponse;
import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;

public class SupplierAssembler {

    private SupplierAssembler() {
    }

    public static SupplierAggregate toAggregate(SupplierAddRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        return SupplierAggregate.create(
                trim(request.getSupplierCode()),
                trim(request.getName()),
                trim(request.getContactName()),
                trim(request.getContactPhone()),
                trim(request.getAddress()),
                request.getStatus()
        );
    }

    public static SupplierDetailResponse toDetailResponse(SupplierAggregate supplier) {
        if (supplier == null) {
            throw new IllegalArgumentException("暂无相关供应商详情");
        }
        SupplierDetailResponse response = new SupplierDetailResponse();
        response.setId(supplier.getId());
        response.setSupplierCode(supplier.getSupplierCode());
        response.setName(supplier.getName());
        response.setContactName(supplier.getContactName());
        response.setContactPhone(supplier.getContactPhone());
        response.setAddress(supplier.getAddress());
        response.setStatus(supplier.getStatus());
        response.setIsDel(supplier.getIsDel());
        response.setCreateTime(supplier.getCreateTime());
        response.setUpdateTime(supplier.getUpdateTime());
        return response;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }
}
```

### 5.9 `SupplierController.java`

路径：

- `s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/http/SupplierController.java`

参考结构：

```java
package cn.bugstack.trigger.http;

import cn.bugstack.api.request.supplier.SupplierAddRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.supplier.SupplierDetailResponse;
import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import cn.bugstack.domain.supplier.service.ISupplierService;
import cn.bugstack.trigger.assembler.SupplierAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/supplier")
public class SupplierController {

    @Resource
    private ISupplierService supplierService;

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody SupplierAddRequest request) {
        SupplierAggregate supplier = SupplierAssembler.toAggregate(request);
        Long id = supplierService.addSupplier(supplier);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    @GetMapping("{id}")
    public Response<SupplierDetailResponse> detail(@PathVariable Long id) {
        SupplierAggregate supplier = supplierService.querySupplierById(id);
        SupplierDetailResponse response = SupplierAssembler.toDetailResponse(supplier);
        return Response.<SupplierDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PutMapping("{id}")
    public Response<SupplierDetailResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody SupplierAddRequest request) {
        SupplierAggregate current = supplierService.querySupplierById(id);
        if (current == null) {
            throw new IllegalArgumentException("供应商不存在");
        }

        SupplierAggregate updated = SupplierAggregate.builder()
                .id(current.getId())
                .supplierCode(request.getSupplierCode() != null ? request.getSupplierCode().trim() : current.getSupplierCode())
                .name(request.getName() != null ? request.getName().trim() : current.getName())
                .contactName(request.getContactName() != null ? request.getContactName().trim() : current.getContactName())
                .contactPhone(request.getContactPhone() != null ? request.getContactPhone().trim() : current.getContactPhone())
                .address(request.getAddress() != null ? request.getAddress().trim() : current.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : current.getStatus())
                .isDel(current.getIsDel())
                .createTime(current.getCreateTime())
                .build();
        supplierService.updateSupplierById(updated);

        SupplierAggregate refreshed = supplierService.querySupplierById(id);
        SupplierDetailResponse response = SupplierAssembler.toDetailResponse(refreshed);
        return Response.<SupplierDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @DeleteMapping("{id}")
    public Response<Boolean> delete(@PathVariable Long id) {
        supplierService.deleteSupplierById(id);
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }
}
```

### 5.10 `Supplier.java`

路径：

- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/po/Supplier.java`

参考结构：

```java
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
public class Supplier {

    private Long id;
    private String supplierCode;
    private String name;
    private String contactName;
    private String contactPhone;
    private String address;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### 5.11 `ISupplierDao.java`

路径：

- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/ISupplierDao.java`

参考结构：

```java
package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.supplier.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ISupplierDao {

    void insert(Supplier supplier);

    void deleteById(@Param("id") Long id);

    Supplier queryById(@Param("id") Long id);

    void update(Supplier supplier);
}
```

### 5.12 `SupplierRepository.java`

路径：

- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/SupplierRepository.java`

参考结构：

```java
package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.supplier.model.aggregate.SupplierAggregate;
import cn.bugstack.domain.supplier.repository.ISupplierRepository;
import cn.bugstack.infrastructure.dao.ISupplierDao;
import cn.bugstack.infrastructure.dao.po.supplier.Supplier;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class SupplierRepository implements ISupplierRepository {

    @Resource
    private ISupplierDao supplierDao;

    @Override
    public Long save(SupplierAggregate supplierAggregate) {
        if (supplierAggregate == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
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
            throw new IllegalArgumentException("供应商id不能为空");
        }
        supplierDao.deleteById(id);
    }

    @Override
    public SupplierAggregate queryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("供应商id不能为空");
        }

        Supplier supplier = supplierDao.queryById(id);
        if (supplier == null) {
            return null;
        }

        return SupplierAggregate.builder()
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
    }

    @Override
    public void updateById(SupplierAggregate supplierAggregate) {
        if (supplierAggregate == null) {
            throw new IllegalArgumentException("供应商信息不能为空");
        }
        if (supplierAggregate.getId() == null) {
            throw new IllegalArgumentException("供应商id不能为空");
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
    }
}
```

### 5.13 `supplier_mapper.xml`

路径：

- `s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/supplier_mapper.xml`

参考结构：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.bugstack.infrastructure.dao.ISupplierDao">

    <insert id="insert" parameterType="cn.bugstack.infrastructure.dao.po.supplier.Supplier"
            useGeneratedKeys="true" keyProperty="id">
        insert into supplier
        (supplier_code, name, contact_name, contact_phone, address, status, is_del, create_time, update_time)
        values
        (#{supplierCode}, #{name}, #{contactName}, #{contactPhone}, #{address}, #{status}, #{isDel}, #{createTime},
        #{updateTime})
    </insert>

    <update id="deleteById" parameterType="java.lang.Long">
        update supplier
        set is_del = 1,
        update_time = now()
        where id = #{id}
        and is_del = 0
    </update>

    <select id="queryById" parameterType="java.lang.Long"
            resultType="cn.bugstack.infrastructure.dao.po.supplier.Supplier">
        select
        id,
        supplier_code,
        name,
        contact_name,
        contact_phone,
        address,
        status,
        is_del,
        create_time,
        update_time
        from supplier
        where id = #{id}
        and is_del = 0
    </select>

    <update id="update" parameterType="cn.bugstack.infrastructure.dao.po.supplier.Supplier">
        update supplier
        set supplier_code = #{supplierCode},
        name = #{name},
        contact_name = #{contactName},
        contact_phone = #{contactPhone},
        address = #{address},
        status = #{status},
        update_time = now()
        where id = #{id}
        and is_del = 0
    </update>

</mapper>
```

## 6. 推荐实现顺序

如果你现在要开始写，建议严格按这个顺序：

1. 新建 `data/sql/supplier.sql`
2. 新建 `Supplier` PO 和 `ISupplierDao`
3. 新建 `supplier_mapper.xml`
4. 新建 `SupplierAggregate`
5. 新建 `ISupplierRepository` 和 `SupplierRepository`
6. 新建 `SupplierStatusVO`
7. 新建 `ISupplierService` 和 `SupplierService`
8. 新建 `SupplierAddRequest` 和 `SupplierDetailResponse`
9. 新建 `SupplierAssembler`
10. 新建 `SupplierController`

这样写的好处是：

- 分层清楚
- 先数据库后业务
- 先单体表，再扩展采购链路

## 7. 可以直接参考的现有文件

这些文件就是你当前项目里现成的模板，推荐先看它们再照着写 `supplier`：

- `data/sql/warehouse.sql`
- `s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/http/WarehouseController.java`
- `s-pay-mall-ddd-trigger/src/main/java/cn.bugstack.trigger/assembler/WarehouseAssembler.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/warehouse/model/aggregate/WarehouseAggregate.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/warehouse/service/WarehouseService.java`
- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/WarehouseRepository.java`
- `s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/warehouse_mapper.xml`

## 8. 下一步建议

如果这张表做完，下一张最建议补的是：

1. `purchase_order`
2. `purchase_order_item`

因为这样就能把 `supplier -> 采购 -> 入库` 这条供应链最小闭环接起来。

