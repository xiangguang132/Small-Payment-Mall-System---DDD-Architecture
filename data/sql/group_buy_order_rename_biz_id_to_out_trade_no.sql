alter table group_buy_order
    change column biz_id out_trade_no varchar(64) default null comment '商户订单号';

alter table group_buy_order
    rename index uk_biz_id to uk_out_trade_no;
