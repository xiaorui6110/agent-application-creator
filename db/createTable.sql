create database if not exists `xiaorui_app_creator` default character set utf8mb4 collate utf8mb4_general_ci;

use xiaorui_app_creator;

-- 用户表
drop table if exists `xr_user`;
create table `xr_user`
(
    `user_id`        varchar(36) not null default ''     comment '用户id',
    `nick_name`      varchar(50)          default null   comment '用户昵称',
    `user_email`     varchar(100)         default null   comment '用户邮箱',
    `login_password` varchar(255)         default null   comment '登录密码',
    `user_phone`     varchar(50)          default null   comment '用户手机号',
    `user_avatar`    varchar(255)         default null   comment '用户头像',
    `user_sex`       char(1)              default null   comment '用户性别 m-男 f-女',
    `user_birthday`  char(10)             default null   comment '用户生日 yyyy-mm-dd',
    `user_profile`   varchar(500)         default null   comment '用户简介',
    `user_role`      varchar(50)          default 'user' comment '用户角色 user-普通用户 admin-管理员',
    `user_status`    tinyint(4)           default '1'    comment '用户状态 1-正常 2-禁用',
    `user_regip`     varchar(50)          default null   comment '注册ip',
    `user_lastip`    varchar(50)          default null   comment '最后登录ip',
    `user_lasttime`  datetime             default null   comment '最后登录时间',
    `user_score`     int(11)              default null   comment '用户积分',
    `create_time`    datetime             default CURRENT_TIMESTAMP   comment '创建时间',
    `update_time`    datetime             default CURRENT_TIMESTAMP  on update CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted`     tinyint(4)           default '0'    comment '是否删除 0-未删除 1-已删除',
    primary key (`user_id`),
    unique key `ud_user_email` (`user_email`),
    unique key `ud_unique_user_phone` (`user_phone`)
) engine = InnoDB  default charset = utf8mb4 comment '用户表';

create index `idx_user_id` on `xr_user` (`nick_name`);
create index `idx_user_phone` on `xr_user` (`user_phone`);
create index `idx_user_email` on `xr_user` (`user_email`);

-- 用户-智能体会话绑定表
drop table if exists `xr_user_thread_bind`;
CREATE TABLE `xr_user_thread_bind` (
   `bind_id`        VARCHAR(36) NOT NULL    DEFAULT ''      COMMENT '绑定ID',
   `user_id`        VARCHAR(64) NOT NULL                    COMMENT '用户ID',
   `thread_id`      VARCHAR(64) NOT NULL                    COMMENT 'Agent 对话线程ID',
   `agent_name`     VARCHAR(64) DEFAULT 'app_creator_agent' COMMENT 'Agent 名称',
   `bind_status`    TINYINT(4)  DEFAULT '0'                 COMMENT '绑定状态 0-未绑定 1-已绑定',
   `create_time`    DATETIME    DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
   `update_time`    DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `is_deleted`     TINYINT(4)  DEFAULT '0'                 COMMENT '是否删除 0-未删除 1-已删除',
   PRIMARY KEY (bind_id),
   UNIQUE KEY uk_thread (thread_id),
   KEY idx_user (user_id)
) COMMENT='用户-智能体会话绑定表';

-- 应用表
drop table if exists `xr_app`;
CREATE TABLE `xr_app` (
   `app_id`          varchar(36)  not null default ''    comment '应用id',
   `app_name`        varchar(256) not null default ''    comment '应用名称',
   `app_cover`       varchar(255)          default null  comment '应用封面',
   `app_init_prompt` text                  default null  comment '应用初始化的 prompt',
   `app_description` varchar(500)          default null  comment '应用描述',
   `code_gen_type`   varchar(64)           default null  comment '代码生成类型（枚举）',
   `deploy_key`      varchar(64)           default null  comment '部署唯一标识',
   `deploy_url`      varchar(255)          default null  comment '部署访问地址',
   `deployed_time`   datetime              default null  comment '部署时间',
   `app_priority`    int                   default 0     comment '应用排序优先级',
   `user_id`         varchar(36)               not null  comment '创建用户id',
   `create_time`     datetime default CURRENT_TIMESTAMP  comment '创建时间',
   `update_time`     datetime default CURRENT_TIMESTAMP  on update CURRENT_TIMESTAMP comment '更新时间',
   `is_deleted`      tinyint(4)  default '0'             comment '是否删除 0-未删除 1-已删除',
   PRIMARY KEY (app_id),
   UNIQUE KEY uk_deploy_key (deploy_key),
   INDEX idx_app_name (app_name),
   INDEX idx_user_id (user_id),
   INDEX idx_create_time (create_time)
) COMMENT='应用表';

-- 对话历史表（基本上同 AgentChatMessage，mongodb 实体类，只是新增了数据库的实现）
drop table if exists `xr_chat_history`;
CREATE TABLE `xr_chat_history` (
    `chat_history_id`   varchar(36)  not null default ''    comment '对话历史id',
    `chat_message`      text                      not null  comment '对话消息',
    `chat_message_type` varchar(32)               not null  comment '消息类型：user/ai',
    `app_id`            varchar(36)               not null  comment '应用id',
    `user_id`           varchar(36)               not null  comment '创建用户id',
    `parent_id`         varchar(36)                   null  comment '父消息id（用于上下文关联）',
    `create_time`       datetime default CURRENT_TIMESTAMP  comment '创建时间',
    `update_time`       datetime default CURRENT_TIMESTAMP  on update CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted`        tinyint(4)  default '0'             comment '是否删除 0-未删除 1-已删除',
    PRIMARY KEY (chat_history_id),
    INDEX idx_app_id (app_id),
    INDEX idx_create_time (create_time),
    INDEX idx_app_id_create_time (app_id, create_time)
) COMMENT='对话历史表';

-- 副 Agent: Project 审计层 + Platform 经验层（共 4 个表）
-- 代码优化审计主表
drop table if exists `xr_code_optimization_run`;
CREATE TABLE `xr_code_optimization_run` (
   `run_id`             varchar(36)  not null default ''    comment '运行id',
   `app_id`             varchar(36)               not null  comment '应用id',
   `app_goal`           varchar(36)                   null  comment '应用目标',
   `tech_stack`         varchar(36)                   null  comment '技术栈',
   `summary`            text                          null  comment '代码优化总结',
   `create_time`        datetime default CURRENT_TIMESTAMP  comment '创建时间',
   `is_deleted`         tinyint(4)  default '0'             comment '是否删除 0-未删除 1-已删除',
   PRIMARY KEY (run_id),
   INDEX idx_app_id (app_id),
   INDEX idx_create_time (create_time),
   INDEX idx_app_id_create_time (app_id, create_time)
) COMMENT='代码优化审计主表';

-- 代码优化问题清单表
drop table if exists `xr_code_optimization_issue`;
CREATE TABLE `xr_code_optimization_issue` (
  `issue_id`         varchar(36)  not null default ''    comment '问题id',
  `app_id`           varchar(36)               not null  comment '应用id',
  `level`            varchar(36)                   null  comment '问题级别，INFO / WARN / ERROR',
  `type`             varchar(36)                   null  comment '问题类型，ARCHITECTURE / STYLE / BUG / SMELL',
  `path`             varchar(255)                  null  comment '问题路径',
  `message`          text                          null  comment '问题消息',
  `create_time`      datetime default CURRENT_TIMESTAMP  comment '创建时间',
  `is_deleted`       tinyint(4)  default '0'             comment '是否删除 0-未删除 1-已删除',
  PRIMARY KEY (issue_id),
  INDEX idx_app_id (app_id),
  INDEX idx_create_time (create_time),
  INDEX idx_app_id_create_time (app_id, create_time)
) COMMENT='代码优化问题清单表';

-- 代码优化修改建议表
drop table if exists `xr_code_optimization_patch`;
CREATE TABLE `xr_code_optimization_patch` (
     `patch_id`         varchar(36)  not null default ''    comment '修改建议id',
     `app_id`           varchar(36)               not null  comment '应用id',
     `path`             varchar(255)                  null  comment '文件路径',
     `action`           varchar(36)                   null  comment '操作类型，add / modify / delete',
     `content`          varchar(36)                   null  comment '文件内容',
     `create_time`      datetime default CURRENT_TIMESTAMP  comment '创建时间',
     `is_deleted`       tinyint(4)  default '0'             comment '是否删除 0-未删除 1-已删除',
     PRIMARY KEY (patch_id),
     INDEX idx_app_id (app_id),
     INDEX idx_create_time (create_time),
     INDEX idx_app_id_create_time (app_id, create_time)
) COMMENT='代码优化修改建议表';

-- 代码优化平台级经验表
drop table if exists `xr_platform_pattern`;
CREATE TABLE `xr_platform_pattern` (
   `pattern_id`       varchar(36)  not null default ''    comment '经验id',
   `pattern_text`     varchar(512)                  null  comment '经验文本',
   `hit_count`        int(11)             default 1 null  comment '命中次数',
   `last_seen_at`     datetime default CURRENT_TIMESTAMP  comment '创建时间',
   `is_deleted`       tinyint(4)  default '0'             comment '是否删除 0-未删除 1-已删除',
   PRIMARY KEY (pattern_id),
   UNIQUE KEY uk_pattern_text (pattern_text)
) COMMENT='代码优化平台级经验表';










