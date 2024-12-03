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


DROP TABLE IF EXISTS 'institutions';

CREATE TABLE institutions (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              domain VARCHAR(255) UNIQUE NOT NULL,  -- 机构邮箱域名
                              name VARCHAR(255) NOT NULL            -- 机构名称
);

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



