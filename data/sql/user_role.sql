-- ============================================================
-- 用户表 — 新增 role 角色字段（存量迁移脚本）
-- 执行：mysql -uroot -p s-pay-mall < user_role.sql
-- ============================================================

ALTER TABLE `user`
    ADD COLUMN `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色；0顾客、1管理员、2原料库存管理人员' AFTER `status`;
