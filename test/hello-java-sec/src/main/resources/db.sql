CREATE DATABASE IF NOT EXISTS test default charset utf8 COLLATE utf8_general_ci;
use test;

-- SQL注入使用的数据
CREATE TABLE `users`
(
    `id`   int(11) unsigned NOT NULL AUTO_INCREMENT,
    `uuid` CHAR(36)         NOT NULL DEFAULT (UUID()) UNIQUE,
    `user` varchar(50)      NOT NULL,
    `pass` varchar(128)     NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uuid_unique` (`uuid`)
);

INSERT INTO `users`
values (1, UUID(), 'zhangwei', '123456');

INSERT INTO `users`
values (2, UUID(), 'Admin', 'password');

INSERT INTO `users`
values (3, UUID(), 'wangwei', '123123');

-- 登录日志记录
CREATE TABLE `auth`
(
    `id`   int(6) unsigned NOT NULL AUTO_INCREMENT,
    `user` varchar(50)     NOT NULL,
    `ip`   varchar(50)     NOT NULL,
    `date` varchar(60)     NOT NULL,
    PRIMARY KEY (`id`)
);

-- 存储型XSS
CREATE TABLE `xss`
(
    `id`      int(6) unsigned NOT NULL AUTO_INCREMENT,
    `user`    varchar(50)     NOT NULL,
    `content` TEXT            NOT NULL,
    `date`    varchar(60)     NOT NULL,
    PRIMARY KEY (`id`)
);