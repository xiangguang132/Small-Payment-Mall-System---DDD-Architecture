# product_type 表 CRUD 实现说明

这份文档给你一个能直接落库的 `product_type` 建表 SQL，以及一套按当前项目风格写的 CRUD 参考代码。

你的场景是“生产 - 供货 - 上架 - 销售 - 快递 - 买家”，所以分类表建议一开始就支持树形结构，后面给 `product` 绑定分类时不会返工。

## 1. 建表 SQL

推荐字段：

- `id`
- `parent_id`
- `name`
- `description`
- `type_code`
- `sort`
- `status`
- `is_del`
- `create_time`
- `update_time`

对应 SQL 已放到：

- [product_type.sql](../data/sql/product_type.sql)

SQL 内容如下：

```sql
CREATE TABLE `product_type` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示根分类',
  `name` VARCHAR(128) NOT NULL COMMENT '分类名称',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '分类描述',
  `type_code` VARCHAR(64) DEFAULT NULL COMMENT '分类编码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序值，越小越靠前',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  `is_del` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_type_code` (`type_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status_is_del` (`status`, `is_del`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
```

## 2. 设计说明

### 2.1 为什么要有 `parent_id`

商品分类后面大概率会做树：

- 服装
  - 男装
  - 女装
- 食品
  - 零食
  - 生鲜

`parent_id` 可以支持这类层级。

### 2.2 为什么要有 `sort`

后台分类列表一般都要可排序，`sort` 越小越靠前。

### 2.3 为什么要有 `status`

分类也会有启用和禁用的状态，避免前台还能选到废弃分类。

### 2.4 为什么要有 `is_del`

和当前项目的 `product` 一样，建议软删除，不做物理删除。

### 2.5 `type_code` 要不要保留

如果你后面会接第三方系统、导入导出、或做稳定编码，`type_code` 很有用。

如果你只想做最小版本，也可以去掉它。

## 3. CRUD 参考代码

下面这套代码按当前仓库的写法来，分成：

- `domain`
- `infrastructure`
- `mapper`

如果你先只想落库，直接用 SQL 就够了。

### 3.1 Infrastructure PO

文件建议：

- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/po/ProductType.java`

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
public class ProductType {

    private Long id;

    private Long parentId;

    private String name;

    private String description;

    private String typeCode;

    private Integer sort;

    private Integer status;

    private Integer isDel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
```

### 3.2 DAO

文件建议：

- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/dao/IProductTypeDao.java`

```java
package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.ProductType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IProductTypeDao {

    void insert(ProductType productType);

    void deleteById(@Param("id") Long id);

    ProductType queryById(@Param("id") Long id);

    void update(ProductType productType);

    List<ProductType> queryList(@Param("parentId") Long parentId,
                                @Param("name") String name,
                                @Param("status") Integer status);
}
```

### 3.3 MyBatis Mapper

文件建议：

- `s-pay-mall-ddd-app/src/main/resources/mybatis/mapper/product_type_mapper.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="cn.bugstack.infrastructure.dao.IProductTypeDao">

    <insert id="insert" parameterType="cn.bugstack.infrastructure.dao.po.ProductType"
            useGeneratedKeys="true" keyProperty="id">
        insert into product_type
        (parent_id, name, description, type_code, sort, status, is_del, create_time, update_time)
        values
        (#{parentId}, #{name}, #{description}, #{typeCode}, #{sort}, #{status}, #{isDel}, now(), now())
    </insert>

    <update id="deleteById" parameterType="java.lang.Long">
        update product_type
        set is_del = 1,
            update_time = now()
        where id = #{id}
          and is_del = 0
    </update>

    <select id="queryById" parameterType="java.lang.Long" resultType="cn.bugstack.infrastructure.dao.po.ProductType">
        select id, parent_id, name, description, type_code, sort, status, is_del, create_time, update_time
        from product_type
        where id = #{id}
          and is_del = 0
    </select>

    <update id="update" parameterType="cn.bugstack.infrastructure.dao.po.ProductType">
        update product_type
        set parent_id = #{parentId},
            name = #{name},
            description = #{description},
            type_code = #{typeCode},
            sort = #{sort},
            status = #{status},
            update_time = now()
        where id = #{id}
          and is_del = 0
    </update>

    <select id="queryList" resultType="cn.bugstack.infrastructure.dao.po.ProductType">
        select id, parent_id, name, description, type_code, sort, status, is_del, create_time, update_time
        from product_type
        where is_del = 0
        <if test="parentId != null">
            and parent_id = #{parentId}
        </if>
        <if test="name != null and name != ''">
            and name like concat('%', #{name}, '%')
        </if>
        <if test="status != null">
            and status = #{status}
        </if>
        order by sort asc, id desc
    </select>

</mapper>
```

### 3.4 Repository

文件建议：

- `s-pay-mall-ddd-infrastructure/src/main/java/cn/bugstack/infrastructure/repository/ProductTypeRepository.java`

```java
package cn.bugstack.infrastructure.repository;

import cn.bugstack.infrastructure.dao.IProductTypeDao;
import cn.bugstack.infrastructure.dao.po.ProductType;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Repository
public class ProductTypeRepository {

    @Resource
    private IProductTypeDao productTypeDao;

    public Long save(ProductType productType) {
        if (productType == null) {
            throw new IllegalArgumentException("商品分类信息不能为空");
        }
        productTypeDao.insert(productType);
        return productType.getId();
    }

    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("分类id不能为空");
        }
        productTypeDao.deleteById(id);
    }

    public ProductType queryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("分类id不能为空");
        }
        return productTypeDao.queryById(id);
    }

    public void update(ProductType productType) {
        if (productType == null || productType.getId() == null) {
            throw new IllegalArgumentException("分类信息不能为空");
        }
        productTypeDao.update(productType);
    }

    public List<ProductType> queryList(Long parentId, String name, Integer status) {
        return productTypeDao.queryList(parentId, name, status);
    }
}
```

### 3.5 Domain 参考

如果你想按当前项目的分层继续往上接，可以再加一层领域对象和服务。

文件建议：

- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/producttype/model/aggregate/ProductTypeAggregate.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/producttype/repository/IProductTypeRepository.java`
- `s-pay-mall-ddd-domain/src/main/java/cn/bugstack/domain/producttype/service/IProductTypeService.java`

#### `ProductTypeAggregate`

```java
package cn.bugstack.domain.producttype.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductTypeAggregate {

    private Long id;
    private Long parentId;
    private String name;
    private String description;
    private String typeCode;
    private Integer sort;
    private Integer status;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ProductTypeAggregate create(Long parentId, String name, String description, String typeCode, Integer sort, Integer status) {
        LocalDateTime now = LocalDateTime.now();
        return ProductTypeAggregate.builder()
                .parentId(parentId == null ? 0L : parentId)
                .name(name)
                .description(description)
                .typeCode(typeCode)
                .sort(sort == null ? 0 : sort)
                .status(status == null ? 1 : status)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    public void markDeleted() {
        this.isDel = 1;
    }
}
```

#### `IProductTypeRepository`

```java
package cn.bugstack.domain.producttype.repository;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;

import java.util.List;

public interface IProductTypeRepository {

    Long save(ProductTypeAggregate aggregate);

    void deleteById(Long id);

    ProductTypeAggregate queryById(Long id);

    void update(ProductTypeAggregate aggregate);

    List<ProductTypeAggregate> queryList(Long parentId, String name, Integer status);
}
```

#### `IProductTypeService`

```java
package cn.bugstack.domain.producttype.service;

import cn.bugstack.domain.producttype.model.aggregate.ProductTypeAggregate;

import java.util.List;

public interface IProductTypeService {

    Long add(ProductTypeAggregate aggregate);

    void update(ProductTypeAggregate aggregate);

    void delete(Long id);

    ProductTypeAggregate queryById(Long id);

    List<ProductTypeAggregate> queryList(Long parentId, String name, Integer status);
}
```

## 4. 和现有 `product` 表的关系

你当前项目里的 `product` 已经有 `categoryId` 这个字段，后续建议把它指向 `product_type.id`。

这样链路就会变成：

- `product_type` 负责分类
- `product` 负责具体商品
- 订单下单时仍然先查 `product`

也就是说，`product_type` 是给商品体系补的基础设施，不会破坏你现在的支付链路。

## 5. 你现在最适合的落地顺序

1. 先执行 `product_type` 建表 SQL
2. 再补 `product` 表的 `category_id` 约束逻辑
3. 然后把分类 CRUD 接到后台管理
4. 最后再往生产、供货、上架、销售、快递、买家这条链路扩展
