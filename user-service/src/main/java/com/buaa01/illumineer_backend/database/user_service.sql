create DATABASE  userservice;

USE userservice;

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
                        `uid` INT(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `avatar` VARCHAR(500) DEFAULT 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png' COMMENT '用户头像URL',
                        `email` VARCHAR(100) NOT NULL COMMENT '用户邮箱',
                        `password` VARCHAR(255) NOT NULL COMMENT '用户密码（加密后）',
                        `nick_name` VARCHAR(32) NOT NULL COMMENT '用户昵称',
                        `description` VARCHAR(100) DEFAULT NULL COMMENT '个性签名',
                        `background` VARCHAR(500) DEFAULT 'https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png' COMMENT '主页背景图URL',
                        `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '账号权限状态，0为管理员，1~9为普通用户权限',
                        `is_verify` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已实名认证',
                        `stats` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '账号状态，0正常 1封禁 2注销',
                        `name` VARCHAR(50) DEFAULT NULL COMMENT '用户真实姓名',
                        `auth_id` INT(11) DEFAULT NULL COMMENT '认证ID',
                        `gender` TINYINT(4) DEFAULT NULL COMMENT '性别，0女 1男 2未知',
                        `field` JSON DEFAULT NULL COMMENT '相关领域（JSON格式存储）',
                        `institution` VARCHAR(100) DEFAULT NULL COMMENT '所在机构',
                        `create_date` DATETIME NOT NULL COMMENT '账号创建时间',
                        `delete_date` DATETIME DEFAULT NULL COMMENT '账号注销时间',
                        PRIMARY KEY (`uid`),
                        UNIQUE KEY `email` (`email`),
                        UNIQUE KEY `nick_name` (`nick_name`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';



DROP TABLE IF EXISTS `favorite`;

CREATE TABLE `favorite` (
                            `fid` int(11) NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
                            `uid` int(11) NOT NULL COMMENT '所属用户ID',
                            `type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '收藏夹类型 1默认收藏夹 2用户创建',
                            `title` varchar(20) NOT NULL COMMENT '标题',
                            `count` int(11) NOT NULL DEFAULT '0' COMMENT '收藏夹中文章数量',
                            `is_delete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除 0否 1已删除',
                            PRIMARY KEY (`fid`),
                            UNIQUE KEY `fid` (`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹';