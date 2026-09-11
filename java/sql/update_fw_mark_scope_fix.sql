-- ==========================================================
-- 功能模块名称：摇去哪 - 地点标记权限与私密可见性修复脚本
-- 变更说明：
--   1. 修复历史地点标记中创建者 user_id 为 NULL 的脏数据，防止全表逃逸泄露
--   2. 将无归属的私密标记安全绑定至系统默认管理员（user_id = 1），或安全下架
--   3. 优化 fw_mark 表复合索引 (share_scope, user_id, del_flag)，提升可见性过滤性能
-- 作者：摇去哪开发组
-- 修改日期：2026-09-09
-- ==========================================================

-- 1. 修复 user_id 为 NULL 的历史测试数据，统一归属到管理员账户（ID=1），避免私密标记在无归属状态下被越权穿透
UPDATE fw_mark 
SET user_id = 1, update_time = SYSDATE() 
WHERE user_id IS NULL AND del_flag = '0';

-- 2. 确保 share_scope 字段合法性与默认值约束（private / group / school）
UPDATE fw_mark 
SET share_scope = 'private' 
WHERE (share_scope IS NULL OR share_scope = '') AND del_flag = '0';

-- 3. 补充复合索引以大幅提升 App 端 selectVisibleMarks 权限过滤与管理端分页检索性能
ALTER TABLE fw_mark ADD INDEX idx_scope_user (share_scope, user_id, del_flag);
