CREATE TABLE production_order_material (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    production_order_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL COMMENT '原料ID',
    material_quantity INT NOT NULL COMMENT '需要数量',
    allocation_no VARCHAR(64) DEFAULT NULL COMMENT '备料单号',
    status VARCHAR(32) NOT NULL COMMENT '状态',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_del TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
);