-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                       null comment '用户账号',
    username     varchar(256)                       null comment '用户昵称',
    avatarUrl    varchar(1024)                      null comment '头像',
    userPassword varchar(512)                       not null comment '密码',
    gender       tinyint                            null comment '性别',
    phone        varchar(512)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 null comment '用户状态',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     int      default 0                 not null comment '是否删除',
    userRole     tinyint  default 0                 not null comment '0: 普通用户 1：管理员',
    planetCode   int                                null comment '星球编号（权限验证）'
);

