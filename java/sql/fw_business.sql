-- ----------------------------
-- 命运转盘 Fate Wheel 业务表
-- 配套若依 RuoYi-Vue 3.9.2 后端
-- 在导入 ry_20260417.sql 之后再导入本脚本
-- ----------------------------

-- 1、学校表
drop table if exists fw_school;
create table fw_school (
  school_id      bigint(20)   not null auto_increment    comment '学校id',
  school_name    varchar(60)  default ''                 comment '学校名称',
  school_code    varchar(30)  default ''                 comment '学校编码',
  city           varchar(30)  default ''                 comment '所在城市',
  sort           int(4)       default 0                  comment '显示顺序',
  status         char(1)      default '0'                comment '状态（0正常 1停用）',
  create_time    datetime                                comment '创建时间',
  update_time    datetime                                comment '更新时间',
  primary key (school_id)
) engine=innodb auto_increment=100 comment = '学校表';

insert into fw_school values(1,  '苏州大学',     'school_suda',   '苏州', 1, '0', sysdate(), null);
insert into fw_school values(2,  '南京大学',     'school_nju',    '南京', 2, '0', sysdate(), null);
insert into fw_school values(3,  '东南大学',     'school_seu',    '南京', 3, '0', sysdate(), null);
insert into fw_school values(4,  '西交利物浦大学','school_xjtlu',  '苏州', 4, '0', sysdate(), null);
insert into fw_school values(5,  '中国人民大学(苏州)','school_ruc_sz','苏州', 5, '0', sysdate(), null);

-- 2、用户扩展表（关联 sys_user）
drop table if exists fw_user_extend;
create table fw_user_extend (
  extend_id       bigint(20)   not null auto_increment    comment '扩展id',
  user_id         bigint(20)   default null               comment '关联sys_user.user_id',
  openid          varchar(64)  default ''                 comment '微信openid',
  school_id       bigint(20)   default null               comment '学校id',
  streak          int(8)       default 0                  comment '连续决策天数',
  is_pro          char(1)      default '0'                comment '是否Pro会员（0否 1是）',
  pro_expire_time datetime                               comment 'Pro到期时间',
  shared_count    int(8)       default 0                  comment '共享地点数',
  total_favorites int(8)       default 0                  comment '被收藏总数',
  invite_code     varchar(16)  default ''                 comment '小组邀请码',
  create_time     datetime                               comment '创建时间',
  update_time     datetime                               comment '更新时间',
  primary key (extend_id),
  key idx_user_id (user_id),
  key idx_openid (openid)
) engine=innodb auto_increment=100 comment = '用户扩展表';

-- 3、地点标记表
drop table if exists fw_mark;
create table fw_mark (
  mark_id        bigint(20)   not null auto_increment    comment '标记id',
  user_id        bigint(20)   default null               comment '创建者sys_user.user_id',
  name           varchar(60)  default ''                 comment '地点名称',
  category       varchar(10)  default '美食'             comment '分类（美食/玩乐/购物/自习/其他）',
  lat            decimal(10,6) default null              comment '纬度',
  lng            decimal(10,6) default null              comment '经度',
  address        varchar(200) default ''                 comment '地址',
  remark         varchar(500) default ''                 comment '备注小贴士',
  rating         int(2)       default 0                  comment '评分1-5',
  price          decimal(10,2) default 0                 comment '人均/价格',
  tags           varchar(200) default ''                 comment '标签（逗号分隔）',
  images         varchar(1000) default ''                comment '图片url（逗号分隔）',
  share_scope    varchar(10)  default 'private'          comment '共享范围（private仅自己/group小组/school同校）',
  group_ids      varchar(200) default ''                 comment '共享的小组id（逗号分隔）',
  school_id      bigint(20)   default null               comment '所属学校id（同校共享时）',
  status         varchar(10)  default 'normal'           comment '状态（normal未去/visited已去/off下架）',
  del_flag       char(1)      default '0'                comment '删除标志（0存在 2删除）',
  favorite_count int(8)       default 0                  comment '被收藏次数',
  create_time    datetime                               comment '创建时间',
  update_time    datetime                               comment '更新时间',
  primary key (mark_id),
  key idx_user_id (user_id),
  key idx_school (school_id),
  key idx_scope (share_scope)
) engine=innodb auto_increment=100 comment = '地点标记表';

-- 4、小组表
drop table if exists fw_group;
create table fw_group (
  group_id      bigint(20)   not null auto_increment    comment '小组id',
  group_name    varchar(60)  default ''                 comment '小组名称',
  owner_id      bigint(20)   default null               comment '组长user_id',
  invite_code   varchar(16)  default ''                 comment '邀请码',
  member_count  int(4)       default 0                  comment '成员数',
  status        char(1)      default '0'                comment '状态（0正常 1解散）',
  create_time   datetime                               comment '创建时间',
  update_time   datetime                               comment '更新时间',
  primary key (group_id),
  key idx_invite (invite_code)
) engine=innodb auto_increment=100 comment = '小组表';

-- 5、小组成员表
drop table if exists fw_group_member;
create table fw_group_member (
  id           bigint(20)   not null auto_increment    comment '主键',
  group_id     bigint(20)   default null               comment '小组id',
  user_id      bigint(20)   default null               comment '用户id',
  role         char(1)      default '1'                comment '角色（0组长 1成员）',
  join_time    datetime                               comment '加入时间',
  primary key (id),
  unique key uk_group_user (group_id, user_id)
) engine=innodb auto_increment=100 comment = '小组成员表';

-- 6、决策记录表
drop table if exists fw_decision;
create table fw_decision (
  decision_id   bigint(20)   not null auto_increment    comment '决策id',
  user_id       bigint(20)   default null               comment '用户id',
  decision_type varchar(10)      default '0'                comment '类型（0选什么 1去不去 2买不买）',
  title         varchar(200) default ''                 comment '决策主题',
  result        varchar(200) default ''                 comment '决策结果',
  detail        varchar(500) default ''                 comment '决策详情/备注',
  executed      char(1)      default '0'                comment '是否已执行（0否 1是）',
  create_time   datetime                               comment '创建时间',
  primary key (decision_id),
  key idx_user (user_id)
) engine=innodb auto_increment=100 comment = '决策记录表';

-- 7、收藏表
drop table if exists fw_favorite;
create table fw_favorite (
  id           bigint(20)   not null auto_increment    comment '主键',
  user_id      bigint(20)   default null               comment '用户id',
  mark_id      bigint(20)   default null               comment '标记id',
  create_time  datetime                               comment '收藏时间',
  primary key (id),
  unique key uk_user_mark (user_id, mark_id)
) engine=innodb auto_increment=100 comment = '收藏表';

-- 8、举报表
drop table if exists fw_report;
create table fw_report (
  report_id    bigint(20)   not null auto_increment    comment '举报id',
  user_id      bigint(20)   default null               comment '举报人user_id',
  mark_id      bigint(20)   default null               comment '被举报标记id',
  reason       varchar(200) default ''                 comment '举报原因',
  status       char(1)      default '0'                comment '状态（0待处理 1已处理 2驳回）',
  handle_by    bigint(20)   default null               comment '处理人',
  handle_time  datetime                               comment '处理时间',
  handle_remark varchar(200) default ''                comment '处理备注',
  create_time  datetime                               comment '举报时间',
  primary key (report_id),
  key idx_mark (mark_id)
) engine=innodb auto_increment=100 comment = '举报表';

-- 9、权益订单表（App 端个人虚拟支付对接）
drop table if exists fw_order;
create table fw_order (
  order_id     bigint(20)   not null auto_increment    comment '订单id',
  order_no     varchar(32)  default ''                 comment '订单号',
  user_id      bigint(20)   default null               comment '用户id',
  item_type    char(1)      default '0'                comment '商品类型（0Pro会员 1无限标记 2转盘皮肤 3去广告）',
  amount       decimal(10,2) default 0                 comment '支付金额',
  status       char(1)      default '0'                comment '状态（0待支付 1已支付 2已关闭）',
  pay_time     datetime                               comment '支付时间',
  create_time  datetime                               comment '创建时间',
  primary key (order_id),
  key idx_user (user_id)
) engine=innodb auto_increment=100 comment = '权益订单表';

-- ----------------------------
-- 菜单与权限（命运转盘管理）
-- 菜单id 从 2000 开始，避免与若依默认菜单冲突
-- ----------------------------
insert into sys_menu values('2000', '命运转盘管理', '0', '10', 'fatewheel', null, '', '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '', null, '命运转盘管理目录');

-- 标记管理
insert into sys_menu values('2001', '标记管理', '2000', '1', 'mark', 'fatewheel/mark/index', '', '', 1, 0, 'C', '0', '0', 'fatewheel:mark:list', 'location', 'admin', sysdate(), '', null, '标记管理菜单');
insert into sys_menu values('2002', '标记查询', '2001', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:mark:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2003', '标记新增', '2001', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:mark:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2004', '标记修改', '2001', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:mark:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2005', '标记删除', '2001', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:mark:remove', '#', 'admin', sysdate(), '', null, '');

-- 举报处理
insert into sys_menu values('2010', '举报处理', '2000', '2', 'report', 'fatewheel/report/index', '', '', 1, 0, 'C', '0', '0', 'fatewheel:report:list', 'warning', 'admin', sysdate(), '', null, '举报处理菜单');
insert into sys_menu values('2011', '举报查询', '2010', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:report:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2012', '举报处理', '2010', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:report:edit', '#', 'admin', sysdate(), '', null, '');

-- 学校管理
insert into sys_menu values('2020', '学校管理', '2000', '3', 'school', 'fatewheel/school/index', '', '', 1, 0, 'C', '0', '0', 'fatewheel:school:list', 'education', 'admin', sysdate(), '', null, '学校管理菜单');
insert into sys_menu values('2021', '学校查询', '2020', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:school:query', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2022', '学校新增', '2020', '2', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:school:add', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2023', '学校修改', '2020', '3', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:school:edit', '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('2024', '学校删除', '2020', '4', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:school:remove', '#', 'admin', sysdate(), '', null, '');

-- 用户扩展管理
insert into sys_menu values('2030', '用户管理', '2000', '4', 'userext', 'fatewheel/userext/index', '', '', 1, 0, 'C', '0', '0', 'fatewheel:userext:list', 'peoples', 'admin', sysdate(), '', null, '用户扩展管理菜单');
insert into sys_menu values('2031', '用户查询', '2030', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:userext:query', '#', 'admin', sysdate(), '', null, '');

-- 订单管理
insert into sys_menu values('2040', '订单管理', '2000', '5', 'order', 'fatewheel/order/index', '', '', 1, 0, 'C', '0', '0', 'fatewheel:order:list', 'money', 'admin', sysdate(), '', null, '订单管理菜单');
insert into sys_menu values('2041', '订单查询', '2040', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:order:query', '#', 'admin', sysdate(), '', null, '');

-- 数据统计
insert into sys_menu values('2050', '数据统计', '2000', '6', 'stat', 'fatewheel/stat/index', '', '', 1, 0, 'C', '0', '0', 'fatewheel:stat:list', 'chart', 'admin', sysdate(), '', null, '数据统计菜单');
insert into sys_menu values('2051', '统计查看', '2050', '1', '#', '', '', '', 1, 0, 'F', '0', '0', 'fatewheel:stat:query', '#', 'admin', sysdate(), '', null, '');

-- 给管理员角色(1)分配命运转盘管理菜单权限
insert into sys_role_menu values('1', '2000');
insert into sys_role_menu values('1', '2001');
insert into sys_role_menu values('1', '2002');
insert into sys_role_menu values('1', '2003');
insert into sys_role_menu values('1', '2004');
insert into sys_role_menu values('1', '2005');
insert into sys_role_menu values('1', '2010');
insert into sys_role_menu values('1', '2011');
insert into sys_role_menu values('1', '2012');
insert into sys_role_menu values('1', '2020');
insert into sys_role_menu values('1', '2021');
insert into sys_role_menu values('1', '2022');
insert into sys_role_menu values('1', '2023');
insert into sys_role_menu values('1', '2024');
insert into sys_role_menu values('1', '2030');
insert into sys_role_menu values('1', '2031');
insert into sys_role_menu values('1', '2040');
insert into sys_role_menu values('1', '2041');
insert into sys_role_menu values('1', '2050');
insert into sys_role_menu values('1', '2051');
