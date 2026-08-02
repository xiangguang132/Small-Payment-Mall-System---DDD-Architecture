alter table material_stock_allocation
    add column retry_count int not null default 0 comment '自动处理失败重试次数' after status,
    add column fail_reason varchar(255) default null comment '自动处理失败原因' after retry_count;

alter table material_stock_allocation
    modify column status tinyint not null default 0 comment '状态 0待锁库 1已锁库 2已出库 3已释放 4已取消 5自动处理失败';
