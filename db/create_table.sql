create database if not exists `xiaorui_app_creator` default character set utf8mb4 collate utf8mb4_general_ci;

use xiaorui_app_creator;

-- 由于项目确实不好支持批量测试数据导入，所以这里不创建测试数据

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
create table `xr_app`
(
    `app_id`          varchar(36)   not null default ''               comment '应用id',
    `app_name`        varchar(256)  not null default ''               comment '应用名称',
    `app_cover`       varchar(1000)          default null             comment '应用封面',
    `app_init_prompt` text                   default null             comment '应用初始化的 prompt',
    `app_description` varchar(500)           default null             comment '应用描述',
    `code_gen_type`   varchar(64)            default null             comment '代码生成类型（枚举）',
    `deploy_key`      varchar(64)            default null             comment '部署唯一标识',
    `deploy_url`      varchar(255)           default null             comment '部署访问地址',
    `deployed_time`   datetime               default null             comment '部署时间',
    `app_priority`    int                    default 0                comment '应用排序优先级',
    `app_category`    varchar(64)            default 'general'        comment '应用分类',
    `recommend_score` decimal(10,2)          default 0.00             comment '推荐分',
    `user_id`         varchar(36)  not null                          comment '创建用户id',
    `comment_count`   bigint       default 0                         comment '评论数',
    `like_count`      bigint       default 0                         comment '点赞数',
    `share_count`     bigint       default 0                         comment '分享数',
    `view_count`      bigint       default 0                         comment '浏览量',
    `create_time`     datetime     default CURRENT_TIMESTAMP         comment '创建时间',
    `update_time`     datetime     default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted`      tinyint(4)   default '0'                       comment '是否删除 0-未删除 1-已删除',
    primary key (`app_id`),
    unique key `uk_deploy_key` (`deploy_key`),
    index `idx_app_name` (`app_name`),
    index `idx_user_id` (`user_id`),
    index `idx_create_time` (`create_time`),
    index `idx_like_count` (`like_count`),
    index `idx_view_count` (`view_count`)
) engine = InnoDB default charset = utf8mb4 comment '应用表';

-- 对话历史表
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

-- 代码优化结果表
drop table if exists `xr_code_optimize_result`;
CREATE TABLE `xr_code_optimize_result` (
   `code_optimize_history_id`   varchar(36)  not null default ''    comment '代码优化历史id',
   `app_id`                     varchar(36)                not null comment '应用id',
   `code_optimize_summary`      text                       not null comment '代码优化总结',
   `code_optimize_issues`       text                       not null comment '代码优化问题',
   `code_optimize_suggestions`  text                       not null comment '代码优化建议',
   `platform_experience`        text                       not null comment '平台经验',
   `agent_confidence`           double                     not null comment 'Agent 置信度',
   `create_time`                datetime default CURRENT_TIMESTAMP  comment '创建时间',
   `is_deleted`                 tinyint(4)  default '0'             comment '是否删除 0-未删除 1-已删除',
   PRIMARY KEY (code_optimize_history_id),
   INDEX idx_app_id (app_id),
   INDEX idx_create_time (create_time),
   INDEX idx_app_id_create_time (app_id, create_time)
) COMMENT='代码优化结果表';


-- agent执行任务表
drop table if exists `xr_agent_task`;
CREATE TABLE `xr_agent_task` (
   `agent_task_id`   varchar(36)  not null default ''    comment 'agent执行任务id',
   `task_id`         varchar(36)  not null               comment '任务id',
   `thread_id`       varchar(64)  not null               comment '对话线程id',
   `app_id`          varchar(36)  not null               comment '应用id',
   `task_status`     varchar(32)  not null               comment '任务状态',
   `task_result`     text                  default null  comment '任务结果',
   `task_error`      text                  default null  comment '任务错误信息',
   `retry_count`     int(11)      not null default 0     comment '重试次数',
   `fail_type`       varchar(32)  not null default ''    comment '失败类型',
   `next_retry_time` datetime              default null  comment '下次重试时间',
   `create_time`     datetime default CURRENT_TIMESTAMP  comment '创建时间',
   `update_time`     datetime default CURRENT_TIMESTAMP  on update CURRENT_TIMESTAMP comment '更新时间',
   `is_deleted`      tinyint(4)  default '0'             comment '是否删除 0-未删除 1-已删除',
   PRIMARY KEY (agent_task_id),
   UNIQUE KEY uk_task_id (task_id),
   INDEX idx_task_id (task_id),
   INDEX idx_create_time (create_time),
   INDEX idx_task_id_create_time (task_id, create_time)
) COMMENT='agent执行任务表';

-- 应用评论表
drop table if exists `xr_app_comment`;
create table `xr_app_comment`
(
    `comment_id`      varchar(36)  not null default ''               comment '评论id',
    `user_id`         varchar(36)  not null                          comment '评论用户id',
    `app_id`          varchar(36)  not null                          comment '被评论应用id',
    `app_user_id`     varchar(36)  not null                          comment '被评论应用所属用户id',
    `comment_content` text         not null                          comment '评论内容',
    `parent_id`       varchar(36)           default null             comment '父评论id，null表示顶级评论',
    `like_count`      bigint       default 0                         comment '点赞数',
    `dislike_count`   bigint       default 0                         comment '点踩数',
    `is_deleted`      tinyint(4)   default '0'                       comment '是否删除 0-未删除 1-已删除',
    `is_read`         tinyint(4)   default '0'                       comment '是否已读 0-未读 1-已读',
    `create_time`     datetime     default CURRENT_TIMESTAMP         comment '创建时间',
    `update_time`     datetime     default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    primary key (`comment_id`),
    index `idx_user_id` (`user_id`),
    index `idx_app_id` (`app_id`),
    index `idx_create_time` (`create_time`),
    index `idx_like_count` (`like_count`)
) engine = InnoDB default charset = utf8mb4 comment '应用评论表';

-- 点赞记录表
drop table if exists `xr_like_record`;
create table `xr_like_record`
(
    `like_id`       varchar(36) not null default ''               comment '点赞记录id',
    `user_id`       varchar(36) not null                          comment '用户id',
    `target_id`     varchar(36) not null                          comment '被点赞内容id',
    `target_user_id` varchar(36) not null                         comment '被点赞内容所属用户id',
    `is_liked`      tinyint(4)  not null default '1'              comment '是否点赞 0-取消 1-点赞',
    `first_like_time` datetime  default CURRENT_TIMESTAMP         comment '第一次点赞时间',
    `last_like_time` datetime   default CURRENT_TIMESTAMP         comment '最近一次点赞时间',
    `is_read`       tinyint(4)  default '0'                       comment '是否已读 0-未读 1-已读',
    `create_time`   datetime    default CURRENT_TIMESTAMP         comment '创建时间',
    `update_time`   datetime    default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted`    tinyint(4)  default '0'                       comment '是否删除 0-未删除 1-已删除',
    primary key (`like_id`),
    unique key `uk_user_target` (`user_id`, `target_id`),
    index `idx_target_id` (`target_id`),
    index `idx_target_user_id` (`target_user_id`),
    index `idx_user_id` (`user_id`)
) engine = InnoDB default charset = utf8mb4 comment '点赞记录表';

-- 分享记录表
drop table if exists `xr_share_record`;
create table `xr_share_record`
(
    `share_id`       varchar(36) not null default ''               comment '分享记录id',
    `user_id`        varchar(36) not null                          comment '用户id',
    `target_id`      varchar(36) not null                          comment '被分享内容id',
    `target_user_id` varchar(36) not null                          comment '被分享内容所属用户id',
    `is_shared`      tinyint(4)  not null default '1'              comment '是否分享 0-取消 1-分享',
    `share_time`     datetime    default CURRENT_TIMESTAMP         comment '分享时间',
    `is_read`        tinyint(4)  default '0'                       comment '是否已读 0-未读 1-已读',
    `create_time`    datetime    default CURRENT_TIMESTAMP         comment '创建时间',
    `update_time`    datetime    default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted`     tinyint(4)  default '0'                       comment '是否删除 0-未删除 1-已删除',
    primary key (`share_id`),
    unique key `uk_user_target` (`user_id`, `target_id`),
    index `idx_target_id` (`target_id`),
    index `idx_target_user_id` (`target_user_id`),
    index `idx_user_id` (`user_id`)
) engine = InnoDB default charset = utf8mb4 comment '分享记录表';

-- 应用版本表
drop table if exists `xr_app_version`;
create table `xr_app_version`
(
    `app_version_id` varchar(36)  not null default ''               comment '应用版本id',
    `app_id`         varchar(36)  not null                          comment '应用id',
    `version_number` int          not null                          comment '版本号',
    `version_source` varchar(32)  not null                          comment '版本来源 GENERATED/DEPLOYED/RESTORED',
    `version_note`   varchar(255)          default null             comment '版本备注',
    `snapshot_path`  varchar(255) not null                          comment '快照相对路径',
    `entry_file`     varchar(255)          default null             comment '入口文件',
    `deploy_url`     varchar(255)          default null             comment '部署地址',
    `created_by`     varchar(36)           default null             comment '创建人',
    `create_time`    datetime     default CURRENT_TIMESTAMP         comment '创建时间',
    `update_time`    datetime     default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted`     tinyint(4)   default '0'                       comment '是否删除 0-未删除 1-已删除',
    primary key (`app_version_id`),
    unique key `uk_app_version_number` (`app_id`, `version_number`),
    index `idx_app_id` (`app_id`),
    index `idx_version_source` (`version_source`),
    index `idx_create_time` (`create_time`)
) engine = InnoDB default charset = utf8mb4 comment '应用版本表';

-- 应用模板表
drop table if exists `xr_app_template`;
create table `xr_app_template`
(
    `template_id`          varchar(36)  not null default ''               comment '应用模板id',
    `template_name`        varchar(128) not null                          comment '模板名称',
    `template_description` varchar(500)          default null             comment '模板描述',
    `code_gen_type`        varchar(64)           default null             comment '代码生成类型',
    `entry_file`           varchar(255)          default null             comment '入口文件',
    `source_app_id`        varchar(36)           default null             comment '来源应用id',
    `storage_path`         varchar(255)          default null             comment '模板文件相对路径',
    `created_by`           varchar(36)           default null             comment '创建人',
    `create_time`          datetime     default CURRENT_TIMESTAMP         comment '创建时间',
    `update_time`          datetime     default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted`           tinyint(4)   default '0'                       comment '是否删除 0-未删除 1-已删除',
    primary key (`template_id`),
    index `idx_template_name` (`template_name`),
    index `idx_source_app_id` (`source_app_id`),
    index `idx_created_by` (`created_by`),
    index `idx_create_time` (`create_time`)
) engine = InnoDB default charset = utf8mb4 comment '应用模板表';

-- 模型调用记录表
drop table if exists `xr_model_call_log`;
create table `xr_model_call_log`
(
    `model_call_log_id` varchar(36) not null default '' comment '模型调用记录 id',
    `user_id` varchar(36) default null comment '用户 id',
    `app_id` varchar(36) default null comment '应用 id',
    `thread_id` varchar(64) default null comment '线程 id',
    `agent_name` varchar(64) default null comment 'Agent 名称',
    `provider` varchar(32) default null comment '模型提供方',
    `model_name` varchar(128) default null comment '模型名称',
    `call_type` varchar(16) default null comment '调用类型 SYNC/STREAM',
    `call_status` varchar(16) default null comment '调用状态 SUCCESS/FAILED',
    `prompt_tokens` int default '0' comment '输入 token 数',
    `completion_tokens` int default '0' comment '输出 token 数',
    `total_tokens` int default '0' comment '总 token 数',
    `latency_ms` bigint default '0' comment '调用耗时 ms',
    `error_message` varchar(1000) default null comment '错误信息',
    `create_time` datetime default CURRENT_TIMESTAMP comment '创建时间',
    `update_time` datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP comment '更新时间',
    `is_deleted` tinyint(4) default '0' comment '是否删除 0-未删除 1-已删除',
    primary key (`model_call_log_id`),
    index `idx_user_id` (`user_id`),
    index `idx_app_id` (`app_id`),
    index `idx_thread_id` (`thread_id`),
    index `idx_agent_name` (`agent_name`),
    index `idx_model_name` (`model_name`),
    index `idx_call_status` (`call_status`),
    index `idx_create_time` (`create_time`)
) engine = InnoDB default charset = utf8mb4 comment '模型调用记录表';
