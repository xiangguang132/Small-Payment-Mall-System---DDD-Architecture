CREATE TABLE production_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    product_id BIGINT NOT NULL COMMENT '生产成品ID',
    product_quantity INT NOT NULL COMMENT '生产数量',
    warehouse_id BIGINT NOT NULL COMMENT '成品入库仓库ID',
    status TINYINT NOT NULL COMMENT '状态',
    retry_count int not null default 0 comment '重试次数',
    fail_reason varchar(512) default null comment '失败原因',
    next_retry_time datetime default null comment '下次重试时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_del TINYINT(1) DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
);

ALTER TABLE production_order
    MODIFY COLUMN status TINYINT NOT NULL COMMENT '0:待处理, 1:处理中, 2:已完成, 3:失败可重试, 4:失败终态, 5:已取消';
