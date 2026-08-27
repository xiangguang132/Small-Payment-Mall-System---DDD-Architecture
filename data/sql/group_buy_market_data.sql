-- ============================================================
-- 拼团模块种子数据
-- 库：s-pay-mall-test
-- 说明：拼团试算走数据库 product 表，group_buy_activity/discount 必须有数据，
--       否则 MarketNode 抛 NOT_FOUND。本脚本只插数据，不改表结构。
-- ============================================================

-- 1. 商品分类（product.category_id 必填）
INSERT INTO product_type (id, parent_id, name, description, type_code, sort, status, is_del)
SELECT 1, 0, '默认分类', '拼团测试根分类', 'DEFAULT', 0, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM product_type WHERE id = 1);

-- 2. 商品（拼团试算 MarketNode 按 product_id 查价）
INSERT INTO product (id, name, description, sku, category_id, status, price, is_del)
SELECT 1, '拼团测试商品', '拼团模块种子商品', 'SKU_GB_001', 1, 1, 100.00, 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE id = 1);

-- 3. 拼团折扣（ZK 9折：payPrice = 100 * 0.9 = 90）
INSERT INTO group_buy_discount (discount_id, discount_name, discount_desc, discount_type, market_plan, market_expr, tag_id)
SELECT 'GB_DISCOUNT_ZK', '拼团9折', '拼团默认9折优惠', 0, 'ZK', '0.9', NULL
WHERE NOT EXISTS (SELECT 1 FROM group_buy_discount WHERE discount_id = 'GB_DISCOUNT_ZK');

-- 4. 拼团活动（关联商品1 + 折扣GB_DISCOUNT_ZK，达成目标拼团，目标3人，时长15分钟）
INSERT INTO group_buy_activity
(activity_id, activity_name, product_id, discount_id, group_type, take_limit_count,
 target_count, valid_time, status, start_time, end_time, tag_id, tag_scope)
SELECT 10001, '拼团测试活动', 1, 'GB_DISCOUNT_ZK', 1, 10,
       3, 15, 1, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, NULL
WHERE NOT EXISTS (SELECT 1 FROM group_buy_activity WHERE activity_id = 10001);