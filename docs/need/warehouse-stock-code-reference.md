# `warehouse_stock` 参考代码

这份文档只演示一个最小可用版本，目标是把下面 5 个能力串起来：

- 库存详情查询
- 仓库商品库存查询
- 库存调整
- 入库加库存
- 出库减库存

它不是完整 ERP，只是适合个人练习项目的最小闭环骨架。

## 1. 推荐目录

按你当前项目的分层，建议新增这些文件：

```text
s-pay-mall-ddd-api
├─ src/main/java/cn/bugstack/api/request/stock/StockAdjustRequest.java
└─ src/main/java/cn/bugstack/api/response/stock/StockDetailResponse.java

s-pay-mall-ddd-domain
├─ src/main/java/cn/bugstack/domain/stock/model/aggregate/StockAggregate.java
├─ src/main/java/cn/bugstack/domain/stock/repository/IStockRepository.java
├─ src/main/java/cn/bugstack/domain/stock/service/IStockService.java
└─ src/main/java/cn/bugstack/domain/stock/service/StockService.java

s-pay-mall-ddd-infrastructure
├─ src/main/java/cn/bugstack/infrastructure/dao/po/WarehouseStock.java
├─ src/main/java/cn/bugstack/infrastructure/dao/IWarehouseStockDao.java
└─ src/main/java/cn/bugstack/infrastructure/repository/WarehouseStockRepository.java

s-pay-mall-ddd-trigger
├─ src/main/java/cn/bugstack/trigger/assembler/StockAssembler.java
└─ src/main/java/cn/bugstack/trigger/http/StockController.java

s-pay-mall-ddd-app
└─ src/main/resources/mybatis/mapper/warehouse_stock_mapper.xml
```

## 2. API 层

### 2.1 `StockAdjustRequest`

```java
package cn.bugstack.api.request.stock;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustRequest {

    @NotNull(message = "仓库ID不能为空")
    private Long warehouseId;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "调整数量不能为空")
    private Integer quantity;

    private String reason;
}
```

说明：

- `quantity > 0` 表示加库存
- `quantity < 0` 表示减库存
- 你也可以在服务层拆成两个方法，避免前端传负数

### 2.2 `StockDetailResponse`

```java
package cn.bugstack.api.response.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StockDetailResponse {

    private Long id;
    private Long warehouseId;
    private Long productId;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

## 3. Domain 层

### 3.1 `StockAggregate`

```java
package cn.bugstack.domain.stock.model.aggregate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockAggregate {

    private Long id;
    private Long warehouseId;
    private Long productId;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static StockAggregate create(Long warehouseId, Long productId) {
        return StockAggregate.builder()
                .warehouseId(warehouseId)
                .productId(productId)
                .availableQty(BigDecimal.ZERO)
                .lockedQty(BigDecimal.ZERO)
                .totalQty(BigDecimal.ZERO)
                .isDel(0)
                .build();
    }
}
```

### 3.2 `IStockRepository`

```java
package cn.bugstack.domain.stock.repository;

import cn.bugstack.domain.stock.model.aggregate.StockAggregate;

public interface IStockRepository {

    StockAggregate queryById(Long id);

    StockAggregate queryByWarehouseIdAndProductId(Long warehouseId, Long productId);

    Long save(StockAggregate stock);

    void updateById(StockAggregate stock);
}
```

### 3.3 `IStockService`

```java
package cn.bugstack.domain.stock.service;

import cn.bugstack.domain.stock.model.aggregate.StockAggregate;

public interface IStockService {

    StockAggregate queryStockById(Long id);

    StockAggregate queryStockByWarehouseIdAndProductId(Long warehouseId, Long productId);

    Long addStock(Long warehouseId, Long productId);

    void adjustStock(Long warehouseId, Long productId, Integer quantity, String reason);

    void inbound(Long warehouseId, Long productId, Integer quantity);

    void outbound(Long warehouseId, Long productId, Integer quantity);
}
```

### 3.4 `StockService`

```java
package cn.bugstack.domain.stock.service;

import cn.bugstack.domain.stock.model.aggregate.StockAggregate;
import cn.bugstack.domain.stock.repository.IStockRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class StockService implements IStockService {

    @Resource
    private IStockRepository stockRepository;

    @Override
    public StockAggregate queryStockById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("库存ID不能为空");
        }
        return stockRepository.queryById(id);
    }

    @Override
    public StockAggregate queryStockByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        if (warehouseId == null) {
            throw new IllegalArgumentException("仓库ID不能为空");
        }
        if (productId == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }
        return stockRepository.queryByWarehouseIdAndProductId(warehouseId, productId);
    }

    @Override
    public Long addStock(Long warehouseId, Long productId) {
        if (warehouseId == null || productId == null) {
            throw new IllegalArgumentException("仓库ID和商品ID不能为空");
        }

        StockAggregate current = stockRepository.queryByWarehouseIdAndProductId(warehouseId, productId);
        if (current != null) {
            throw new IllegalArgumentException("库存记录已存在");
        }

        StockAggregate stock = StockAggregate.create(warehouseId, productId);
        stock.setCreateTime(LocalDateTime.now());
        stock.setUpdateTime(LocalDateTime.now());
        return stockRepository.save(stock);
    }

    @Override
    public void adjustStock(Long warehouseId, Long productId, Integer quantity, String reason) {
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("调整数量不能为空且不能为0");
        }
        applyDelta(warehouseId, productId, new BigDecimal(quantity), reason);
    }

    @Override
    public void inbound(Long warehouseId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("入库数量必须大于0");
        }
        applyDelta(warehouseId, productId, new BigDecimal(quantity), "入库");
    }

    @Override
    public void outbound(Long warehouseId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("出库数量必须大于0");
        }
        applyDelta(warehouseId, productId, new BigDecimal(quantity).negate(), "出库");
    }

    private void applyDelta(Long warehouseId, Long productId, BigDecimal delta, String reason) {
        if (warehouseId == null || productId == null) {
            throw new IllegalArgumentException("仓库ID和商品ID不能为空");
        }

        StockAggregate current = stockRepository.queryByWarehouseIdAndProductId(warehouseId, productId);
        if (current == null) {
            current = StockAggregate.create(warehouseId, productId);
        }

        BigDecimal totalQty = current.getTotalQty() == null ? BigDecimal.ZERO : current.getTotalQty();
        BigDecimal nextQty = totalQty.add(delta);
        if (nextQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("库存不能小于0");
        }

        current.setTotalQty(nextQty);
        current.setAvailableQty(nextQty);
        current.setUpdateTime(LocalDateTime.now());

        if (current.getId() == null) {
            current.setCreateTime(LocalDateTime.now());
            stockRepository.save(current);
        } else {
            stockRepository.updateById(current);
        }
    }
}
```

说明：

- 这里为了最小闭环，把 `availableQty = totalQty` 简化处理了
- 真正的锁库/解锁库存，后面再引入 `lockedQty`
- `reason` 暂时保留，后面可以接库存流水

## 4. Infrastructure 层

### 4.1 `WarehouseStock`

```java
package cn.bugstack.infrastructure.dao.po;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class WarehouseStock {

    private Long id;
    private Long warehouseId;
    private Long productId;
    private BigDecimal availableQty;
    private BigDecimal lockedQty;
    private BigDecimal totalQty;
    private Integer isDel;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

### 4.2 `IWarehouseStockDao`

```java
package cn.bugstack.infrastructure.dao;

import cn.bugstack.infrastructure.dao.po.WarehouseStock;
import org.apache.ibatis.annotations.Param;

public interface IWarehouseStockDao {

    void insert(WarehouseStock stock);

    WarehouseStock queryById(@Param("id") Long id);

    WarehouseStock queryByWarehouseIdAndProductId(@Param("warehouseId") Long warehouseId,
                                                  @Param("productId") Long productId);

    void update(WarehouseStock stock);
}
```

### 4.3 `WarehouseStockRepository`

```java
package cn.bugstack.infrastructure.repository;

import cn.bugstack.domain.stock.model.aggregate.StockAggregate;
import cn.bugstack.domain.stock.repository.IStockRepository;
import cn.bugstack.infrastructure.dao.IWarehouseStockDao;
import cn.bugstack.infrastructure.dao.po.WarehouseStock;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

@Repository
public class WarehouseStockRepository implements IStockRepository {

    @Resource
    private IWarehouseStockDao warehouseStockDao;

    @Override
    public StockAggregate queryById(Long id) {
        WarehouseStock stock = warehouseStockDao.queryById(id);
        return toAggregate(stock);
    }

    @Override
    public StockAggregate queryByWarehouseIdAndProductId(Long warehouseId, Long productId) {
        WarehouseStock stock = warehouseStockDao.queryByWarehouseIdAndProductId(warehouseId, productId);
        return toAggregate(stock);
    }

    @Override
    public Long save(StockAggregate stock) {
        WarehouseStock po = toPo(stock);
        warehouseStockDao.insert(po);
        return po.getId();
    }

    @Override
    public void updateById(StockAggregate stock) {
        WarehouseStock po = toPo(stock);
        warehouseStockDao.update(po);
    }

    private StockAggregate toAggregate(WarehouseStock stock) {
        if (stock == null) {
            return null;
        }
        return StockAggregate.builder()
                .id(stock.getId())
                .warehouseId(stock.getWarehouseId())
                .productId(stock.getProductId())
                .availableQty(stock.getAvailableQty())
                .lockedQty(stock.getLockedQty())
                .totalQty(stock.getTotalQty())
                .isDel(stock.getIsDel())
                .createTime(stock.getCreateTime())
                .updateTime(stock.getUpdateTime())
                .build();
    }

    private WarehouseStock toPo(StockAggregate stock) {
        WarehouseStock po = new WarehouseStock();
        po.setId(stock.getId());
        po.setWarehouseId(stock.getWarehouseId());
        po.setProductId(stock.getProductId());
        po.setAvailableQty(stock.getAvailableQty());
        po.setLockedQty(stock.getLockedQty());
        po.setTotalQty(stock.getTotalQty());
        po.setIsDel(stock.getIsDel());
        po.setCreateTime(stock.getCreateTime());
        po.setUpdateTime(stock.getUpdateTime());
        return po;
    }
}
```

## 5. Trigger 层

### 5.1 `StockAssembler`

```java
package cn.bugstack.trigger.assembler;

import cn.bugstack.api.request.stock.StockAdjustRequest;
import cn.bugstack.api.response.stock.StockDetailResponse;
import cn.bugstack.domain.stock.model.aggregate.StockAggregate;

public class StockAssembler {

    private StockAssembler() {
    }

    public static StockDetailResponse toDetailResponse(StockAggregate stock) {
        if (stock == null) {
            throw new IllegalArgumentException("库存不存在");
        }
        StockDetailResponse response = new StockDetailResponse();
        response.setId(stock.getId());
        response.setWarehouseId(stock.getWarehouseId());
        response.setProductId(stock.getProductId());
        response.setAvailableQty(stock.getAvailableQty());
        response.setLockedQty(stock.getLockedQty());
        response.setTotalQty(stock.getTotalQty());
        response.setIsDel(stock.getIsDel());
        response.setCreateTime(stock.getCreateTime());
        response.setUpdateTime(stock.getUpdateTime());
        return response;
    }
}
```

### 5.2 `StockController`

```java
package cn.bugstack.trigger.http;

import cn.bugstack.api.request.stock.StockAdjustRequest;
import cn.bugstack.api.response.Response;
import cn.bugstack.api.response.stock.StockDetailResponse;
import cn.bugstack.domain.stock.model.aggregate.StockAggregate;
import cn.bugstack.domain.stock.service.IStockService;
import cn.bugstack.trigger.assembler.StockAssembler;
import cn.bugstack.types.enums.ResponseCode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@Validated
@RequestMapping("/api/v1/stock")
public class StockController {

    @Resource
    private IStockService stockService;

    @GetMapping("{id}")
    public Response<StockDetailResponse> detail(@PathVariable Long id) {
        StockAggregate stock = stockService.queryStockById(id);
        StockDetailResponse response = StockAssembler.toDetailResponse(stock);
        return Response.<StockDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @GetMapping("query")
    public Response<StockDetailResponse> queryByWarehouseAndProduct(@RequestParam Long warehouseId,
                                                                    @RequestParam Long productId) {
        StockAggregate stock = stockService.queryStockByWarehouseIdAndProductId(warehouseId, productId);
        StockDetailResponse response = StockAssembler.toDetailResponse(stock);
        return Response.<StockDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(response)
                .build();
    }

    @PostMapping("add")
    public Response<Long> add(@Valid @RequestBody StockAdjustRequest request) {
        Long id = stockService.addStock(request.getWarehouseId(), request.getProductId());
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(id)
                .build();
    }

    @PostMapping("adjust")
    public Response<Boolean> adjust(@Valid @RequestBody StockAdjustRequest request) {
        stockService.adjustStock(request.getWarehouseId(), request.getProductId(),
                request.getQuantity(), request.getReason());
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    @PostMapping("inbound")
    public Response<Boolean> inbound(@Valid @RequestBody StockAdjustRequest request) {
        stockService.inbound(request.getWarehouseId(), request.getProductId(), request.getQuantity());
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }

    @PostMapping("outbound")
    public Response<Boolean> outbound(@Valid @RequestBody StockAdjustRequest request) {
        stockService.outbound(request.getWarehouseId(), request.getProductId(), request.getQuantity());
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(true)
                .build();
    }
}
```

## 6. MyBatis Mapper

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.bugstack.infrastructure.dao.IWarehouseStockDao">

    <resultMap id="stockMap" type="cn.bugstack.infrastructure.dao.po.WarehouseStock">
        <id column="id" property="id"/>
        <result column="warehouse_id" property="warehouseId"/>
        <result column="product_id" property="productId"/>
        <result column="available_qty" property="availableQty"/>
        <result column="locked_qty" property="lockedQty"/>
        <result column="total_qty" property="totalQty"/>
        <result column="is_del" property="isDel"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
    </resultMap>

    <insert id="insert" parameterType="cn.bugstack.infrastructure.dao.po.WarehouseStock"
            useGeneratedKeys="true" keyProperty="id">
        insert into warehouse_stock
        (warehouse_id, product_id, available_qty, locked_qty, total_qty, is_del, create_time, update_time)
        values
        (#{warehouseId}, #{productId}, #{availableQty}, #{lockedQty}, #{totalQty},
         #{isDel}, #{createTime}, #{updateTime})
    </insert>

    <select id="queryById" resultMap="stockMap">
        select id, warehouse_id, product_id, available_qty, locked_qty, total_qty, is_del, create_time, update_time
        from warehouse_stock
        where id = #{id} and is_del = 0
    </select>

    <select id="queryByWarehouseIdAndProductId" resultMap="stockMap">
        select id, warehouse_id, product_id, available_qty, locked_qty, total_qty, is_del, create_time, update_time
        from warehouse_stock
        where warehouse_id = #{warehouseId}
          and product_id = #{productId}
          and is_del = 0
    </select>

    <update id="update" parameterType="cn.bugstack.infrastructure.dao.po.WarehouseStock">
        update warehouse_stock
        set available_qty = #{availableQty},
            locked_qty = #{lockedQty},
            total_qty = #{totalQty},
            update_time = #{updateTime}
        where id = #{id} and is_del = 0
    </update>

</mapper>
```

## 7. 这 5 个动作怎么理解

### 7.1 库存详情查询

用途：

- 根据 `id` 查一条库存记录

接口：

- `GET /api/v1/stock/{id}`

### 7.2 仓库商品库存查询

用途：

- 根据 `warehouseId + productId` 查库存

接口：

- `GET /api/v1/stock/query?warehouseId=1&productId=2`

### 7.3 库存调整

用途：

- 手工修正库存
- 允许正数和负数

接口：

- `POST /api/v1/stock/adjust`

### 7.4 入库加库存

用途：

- 采购入库后库存增加

接口：

- `POST /api/v1/stock/inbound`

### 7.5 出库减库存

用途：

- 销售出库后库存减少

接口：

- `POST /api/v1/stock/outbound`

## 8. 你写的时候要注意的点

- 先把最小闭环跑通，不要一开始就加批次、库位、锁库
- `available_qty` 和 `total_qty` 的关系要先统一
- 出库前必须校验库存不能为负
- 同一仓库同一商品必须唯一
- 库存调整和入库/出库，最后最好都沉淀到库存流水表

## 9. 建议你下一步继续补的东西

等这套代码跑通后，再补：

1. `inventory_log`
2. `purchase_order`
3. `purchase_order_item`

这样你的供应链练习项目就开始完整了。

