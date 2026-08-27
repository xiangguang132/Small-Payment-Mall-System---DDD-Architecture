-- ============================================================
-- 用户表手机号唯一索引（方案一：首次扫码注册后完善资料，手机号即账号）
-- 库：s-pay-mall / s-pay-mall-test
-- 用法：mysql -uroot -p --default-character-set=utf8mb4 <库名> < user_phone_uk.sql
-- ============================================================

ALTER TABLE `user` ADD UNIQUE KEY `uk_phone` (`phone`);
