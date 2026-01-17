-- auto-generated definition
create schema xiaorui_app_creator collate utf8mb4_general_ci;

create table xr_user
(
    user_id        varchar(36) default ''                not null comment '用户id'
        primary key,
    nick_name      varchar(50)                           null comment '用户昵称',
    user_email     varchar(100)                          null comment '用户邮箱',
    login_password varchar(255)                          null comment '登录密码',
    user_phone     varchar(50)                           null comment '用户手机号',
    user_avatar    varchar(255)                          null comment '用户头像',
    user_sex       char                                  null comment '用户性别 m-男 f-女',
    user_birthday  char(10)                              null comment '用户生日 yyyy-mm-dd',
    user_profile   varchar(500)                          null comment '用户简介',
    user_role      varchar(50) default 'user'            null comment '用户角色 user-普通用户 admin-管理员',
    user_status    tinyint     default 1                 null comment '用户状态 1-正常 2-禁用',
    user_regip     varchar(50)                           null comment '注册ip',
    user_lastip    varchar(50)                           null comment '最后登录ip',
    user_lasttime  datetime                              null comment '最后登录时间',
    user_score     int                                   null comment '用户积分',
    create_time    datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted     tinyint     default 0                 null comment '是否删除 0-未删除 1-已删除',
    constraint ud_unique_user_phone
        unique (user_phone),
    constraint ud_user_email
        unique (user_email)
)
    comment '用户表' charset = utf8mb4;

create index idx_user_email
    on xr_user (user_email);

create index idx_user_id
    on xr_user (nick_name);

create index idx_user_phone
    on xr_user (user_phone);

create table xr_app
(
    app_id          varchar(36)  default ''                not null comment '应用id'
        primary key,
    app_name        varchar(256) default ''                not null comment '应用名称',
    app_cover       varchar(255)                           null comment '应用封面',
    app_init_prompt text                                   null comment '应用初始化的 prompt',
    code_gen_type   varchar(64)                            null comment '代码生成类型（枚举）',
    deploy_key      varchar(64)                            null comment '部署唯一标识',
    deploy_url      varchar(255)                           null comment '部署访问地址',
    deployed_time   datetime                               null comment '部署时间',
    app_priority    int          default 0                 null comment '应用排序优先级',
    user_id         varchar(36)                            not null comment '创建用户id',
    create_time     datetime     default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime     default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted      tinyint      default 0                 null comment '是否删除 0-未删除 1-已删除',
    app_description varchar(500)                           null comment '应用描述',
    constraint uk_deploy_key
        unique (deploy_key)
)
    comment '应用表';

create index idx_app_name
    on xr_app (app_name);

create index idx_create_time
    on xr_app (create_time);

create index idx_user_id
    on xr_app (user_id);

create table xr_chat_history
(
    chat_history_id   varchar(36) default ''                not null comment '对话历史id'
        primary key,
    chat_message      text                                  not null comment '对话消息',
    chat_message_type varchar(32)                           not null comment '消息类型：user/ai',
    app_id            varchar(36)                           not null comment '应用id',
    user_id           varchar(36)                           not null comment '创建用户id',
    parent_id         varchar(36)                           null comment '父消息id（用于上下文关联）',
    create_time       datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time       datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted        tinyint     default 0                 null comment '是否删除 0-未删除 1-已删除'
)
    comment '对话历史表';

create index idx_app_id
    on xr_chat_history (app_id);

create index idx_app_id_create_time
    on xr_chat_history (app_id, create_time);

create index idx_create_time
    on xr_chat_history (create_time);

create table xr_user_thread_bind
(
    bind_id     varchar(36) default ''                  not null comment '绑定ID'
        primary key,
    user_id     varchar(64)                             not null comment '用户ID',
    thread_id   varchar(64)                             not null comment 'Agent 对话线程ID',
    agent_name  varchar(64) default 'app_creator_agent' null comment 'Agent 名称',
    create_time datetime    default CURRENT_TIMESTAMP   null comment '创建时间',
    update_time datetime    default CURRENT_TIMESTAMP   null on update CURRENT_TIMESTAMP comment '更新时间',
    is_deleted  tinyint     default 0                   null comment '是否删除 0-未删除 1-已删除',
    bind_status tinyint     default 0                   null comment '绑定状态 1-已绑定 0-未绑定',
    constraint uk_thread
        unique (thread_id)
)
    comment '用户-智能体会话绑定表';

create index idx_user
    on xr_user_thread_bind (user_id);

create table xr_code_optimize_result
(
    code_optimize_history_id  varchar(36) default ''                not null comment '代码优化历史id'
        primary key,
    app_id                    varchar(36)                           not null comment '应用id',
    code_optimize_summary     text                                  not null comment '代码优化总结',
    code_optimize_issues      text                                  not null comment '代码优化问题',
    code_optimize_suggestions text                                  not null comment '代码优化建议',
    platform_experience       text                                  not null comment '平台经验',
    agent_confidence          double                                not null comment 'Agent 置信度',
    create_time               datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    is_deleted                tinyint     default 0                 null comment '是否删除 0-未删除 1-已删除'
)
    comment '代码优化结果表';

create index idx_app_id
    on xr_code_optimize_result (app_id);

create index idx_app_id_create_time
    on xr_code_optimize_result (app_id, create_time);

create index idx_create_time
    on xr_code_optimize_result (create_time);

