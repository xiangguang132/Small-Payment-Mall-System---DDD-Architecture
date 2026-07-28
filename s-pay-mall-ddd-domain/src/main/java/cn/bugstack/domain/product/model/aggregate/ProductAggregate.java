package cn.bugstack.domain.product.model.aggregate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAggregate {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private Long categoryId;
    private Integer status;
    private BigDecimal price;
    @Builder.Default
    private Integer isDel = 0;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static ProductAggregate create(String name, String description, String sku,
                                          Long categoryId, Integer status, BigDecimal price) {
        LocalDateTime now = LocalDateTime.now();
        return ProductAggregate.builder()
                .name(name)
                .description(description)
                .sku(sku)
                .categoryId(categoryId)
                .status(status)
                .price(price)
                .isDel(0)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    public void changeStatus(Integer status) {
        this.status = status;
    }

    public void markDeleted() {
        this.isDel = 1;
    }

    public void changePrice(BigDecimal price) {
        this.price = price;
    }
}
