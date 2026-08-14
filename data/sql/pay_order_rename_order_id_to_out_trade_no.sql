alter table pay_order
    change column order_id out_trade_no varchar(64) not null comment '商户订单号/支付宝out_trade_no';

-- 如果 pay_order 上原本有 uk_order_id 之类的唯一索引，请按实际索引名同步改名：
-- alter table pay_order
--     rename index uk_order_id to uk_out_trade_no;
