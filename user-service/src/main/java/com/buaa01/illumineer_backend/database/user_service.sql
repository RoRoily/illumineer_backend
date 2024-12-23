create DATABASE  userservice;

USE userservice;

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
                            `cid` INT(11) NOT NULL AUTO_INCREMENT COMMENT '领域ID',
                            `name` VARCHAR(255) NOT NULL COMMENT '领域名称',
                            PRIMARY KEY (`cid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='文章领域表';


DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
                        `uid` INT(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `avatar` VARCHAR(500) DEFAULT 'https://cube.elemecdn.com/9/c2/f0ee8a3c7c9638a54940382568c9dpng.png' COMMENT '用户头像URL',
                        `email` VARCHAR(100) NOT NULL COMMENT '用户邮箱',
                        `password` VARCHAR(255) NOT NULL COMMENT '用户密码（加密后）',
                        `nick_name` VARCHAR(32) NOT NULL COMMENT '用户昵称',
                        `description` VARCHAR(100) DEFAULT '该用户还未填写自我介绍' COMMENT '个性签名',
                        `status` TINYINT(4) NOT NULL DEFAULT 9 COMMENT '账号权限状态，0为管理员，1~9为普通用户权限',
                        `is_verify` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '是否已实名认证',
                        `stats` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '账号状态，0正常 1封禁 2注销',
                        `intention` JSON DEFAULT NULL COMMENT '用户意向',
                        `name` VARCHAR(50) DEFAULT NULL COMMENT '用户真实姓名',
                        `gender` TINYINT(4) DEFAULT NULL COMMENT '性别，0女 1男 2未知',
                        `background` VARCHAR(500) DEFAULT 'https://tinypic.host/images/2023/11/15/69PB2Q5W9D2U7L.png' COMMENT '主页背景图URL',
                        `field` JSON DEFAULT NULL COMMENT '相关领域（JSON格式存储）',
                        `institution` VARCHAR(100) DEFAULT NULL COMMENT '所在机构',
                        `create_date` DATETIME NOT NULL COMMENT '账号创建时间',
                        `delete_date` DATETIME DEFAULT NULL COMMENT '账号注销时间',
                        PRIMARY KEY (`uid`),
                        UNIQUE KEY `email` (`email`),
                        UNIQUE KEY `nick_name` (`nick_name`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


DROP TABLE IF EXISTS `user2paper`;

CREATE TABLE `user2paper` (
                              `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
                              `uid` INT(11) NOT NULL COMMENT '用户ID',
                              `pid` INT(11) NOT NULL COMMENT '论文ID',
                              `collect` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '收藏状态：0未收藏，1已收藏',
                              `acess_date` DATETIME DEFAULT NULL COMMENT '最近访问时间',
                              PRIMARY KEY (`id`),
                              KEY `idx_user_paper` (`uid`, `pid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户与论文访问行为表';

DROP TABLE IF EXISTS `history`;

CREATE TABLE `history` (
                           `hid` INT(11) NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
                           `uid` INT(11) NOT NULL COMMENT '所属用户ID',
                           `count` INT(11) NOT NULL DEFAULT 0 COMMENT '收藏夹中文章数量',
                           PRIMARY KEY (`hid`),
                           KEY `idx_user_history` (`uid`) #为 uid 字段添加了 idx_user_history 索引，这样查询用户历史记录时会更高效。
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='历史记录表';




DROP TABLE IF EXISTS `favorite`;

CREATE TABLE `favorite` (
                            `fid` INT(11) NOT NULL COMMENT '收藏夹ID',
                            `uid` INT(11) NOT NULL COMMENT '所属用户ID',
                            `type` TINYINT(1) NOT NULL COMMENT '收藏夹类型 1默认收藏夹 2用户创建',
                            `title` VARCHAR(20) NOT NULL COMMENT '收藏夹名称',
                            `count` INT(11) NOT NULL DEFAULT 0 COMMENT '收藏夹中文章数量',
                            `is_delete` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除，0未删除，1已删除',
                            PRIMARY KEY (`fid`),
                            KEY `idx_user_favorite` (`uid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏夹表';






DROP TABLE IF EXISTS `institutions`;

CREATE TABLE institutions (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              domain VARCHAR(255) UNIQUE NOT NULL,  -- 机构邮箱域名
                              name VARCHAR(255) NOT NULL            -- 机构名称
);

DROP TABLE IF EXISTS `appeal_entry`;

CREATE TABLE `appeal_entry` (
                                `appeal_id` INT NOT NULL AUTO_INCREMENT COMMENT 'Appeal ID',
                                `conflict_paper_entry` INT COMMENT 'Conflicting Paper ID',  -- 假设 conflictPaperEntry 对应 PaperAdo 实体的主键
                                `pid` BIGINT COMMENT 'Paper ID',
                                `appellant_id` INT COMMENT 'Appellant ID',  -- 假设 appellant 对应 User 实体的主键
                                `owner_id` INT NOT NULL COMMENT 'Owner ID' default 1,  -- 假设 owner 对应 User 实体的主键
                                `is_accepted_by_appellant` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether the appeal is accepted by the appellant',
                                `accomplish` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether the appeal has been accomplished',
                                `appeal_time` DATETIME COMMENT 'Appeal Time',
                                `handle_time` DATETIME NULL COMMENT 'Handle Time',
                                PRIMARY KEY (`appeal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Appeal Entry Table';



INSERT INTO institutions (domain, name) VALUES
                                            ('buaa.edu.cn', '北京航空航天大学'),
                                            ('tsinghua.edu.cn', '清华大学'),
                                            ('pku.edu.cn', '北京大学'),
                                            ('fudan.edu.cn', '复旦大学'),
                                            ('sjtu.edu.cn', '上海交通大学'),
                                            ('whu.edu.cn', '武汉大学'),

                                            ('hust.edu.cn', '华中科技大学'),
                                            ('nankai.edu.cn', '南开大学'),
                                            ('nju.edu.cn', '南京大学'),
                                            ('bjut.edu.cn', '北京工业大学'),
                                            ('ustc.edu.cn', '中国科学技术大学'),
                                            ('ecust.edu.cn', '东华大学'),
                                            ('scu.edu.cn', '四川大学'),
                                            ('dhu.edu.cn', '东华大学'),
                                            ('scut.edu.cn', '华南理工大学'),
                                            ('cumtb.edu.cn', '中国矿业大学（北京）'),
                                            ('xmu.edu.cn', '厦门大学'),
                                            ('whut.edu.cn', '武汉理工大学'),
                                            ('hhu.edu.cn', '河海大学'),
                                            ('zju.edu.cn', '浙江大学'),
                                            ('sdust.edu.cn', '山东科技大学'),

                                            ('hnu.edu.cn', '华南农业大学'),
                                            ('nwu.edu.cn', '西北大学'),
                                            ('njut.edu.cn', '南京工业大学'),
                                            ('gxu.edu.cn', '桂林理工大学'),
                                            ('cas.cn', '中国科学院'),  -- 中国科学院
                                            ('caas.cn', '中国农业科学院'),  -- 中国农业科学院
                                            ('chineseacademyofsciences.org', '中国科学院'),  -- Chinese Academy of Sciences
                                            ('nims.go.jp', '国家材料科学研究所'),  -- National Institute for Materials Science (Japan)
                                            ('nih.gov', '美国国立卫生研究院'),  -- National Institutes of Health (USA)
                                            ('harvard.edu', '哈佛大学'),  -- Harvard University (USA)
                                            ('stanford.edu', '斯坦福大学'),  -- Stanford University (USA)
                                            ('mit.edu', '麻省理工学院'),  -- Massachusetts Institute of Technology (USA)
                                            ('ox.ac.uk', '牛津大学'),  -- University of Oxford (UK)
                                            ('cam.ac.uk', '剑桥大学'),  -- University of Cambridge (UK)
                                            ('imperial.ac.uk', '帝国理工学院'),  -- Imperial College London (UK)
                                            ('ethz.ch', '瑞士联邦理工学院苏黎世分校'),  -- ETH Zurich (Switzerland)
                                            ('max-planck-gesellschaft.de', '马克斯·普朗克学会'),  -- Max Planck Society (Germany)
                                            ('fraunhofer.de', '弗劳恩霍夫协会'),  -- Fraunhofer Society (Germany)
                                            ('cnrs.fr', '国家科学研究中心'),  -- Centre National de la Recherche Scientifique (France)
                                            ('un.org', '联合国'),  -- United Nations
                                            ('who.int', '世界卫生组织'),  -- World Health Organization
                                            ('unesco.org', '联合国教科文组织') ; -- United Nations Educational, Scientific and Cultural Organization



