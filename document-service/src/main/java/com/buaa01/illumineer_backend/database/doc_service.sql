CREATE DATABASE docService;

USE docService;

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
                            `main_class_id` varchar(20) NOT NULL COMMENT '主分区ID',
                            `sub_class_id` varchar(20) NOT NULL COMMENT '子分区ID',
                            `main_class_name` varchar(20) NOT NULL COMMENT '主分区名称',
                            `sub_class_name` varchar(20) NOT NULL COMMENT '子分区名称',
                            `description` varchar(200) DEFAULT NULL COMMENT '描述',
                            `rcm_tag` varchar(500) DEFAULT NULL COMMENT '推荐标签',
                            PRIMARY KEY (`main_class_id`,`sub_class_id`),
                            KEY `main_class_id` (`main_class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='分区表';


DROP TABLE IF EXISTS `paper`;

CREATE TABLE `paper` (
                         `pid` BIGINT(20) NOT NULL COMMENT '文章ID',
                         `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
                         `keywords` JSON DEFAULT NULL COMMENT '文章关键词（JSON格式存储）',
                         `content_url` VARCHAR(500) DEFAULT NULL COMMENT '文章内容URL',
                         `auths` JSON DEFAULT NULL COMMENT '文章作者（JSON格式存储，键为作者名，值为顺序编号）',
                         `category` VARCHAR(255) DEFAULT NULL COMMENT '相关领域',
                         `type` VARCHAR(50) DEFAULT NULL COMMENT '文章类型（期刊、论文、会议、报纸等）',
                         `theme` VARCHAR(255) DEFAULT NULL COMMENT '文章主题',
                         `publish_date` DATETIME DEFAULT NULL COMMENT '出版时间',
                         `derivation` VARCHAR(255) DEFAULT NULL COMMENT '来源',
                         `ref_times` INT(11) DEFAULT 0 COMMENT '引用次数',
                         `fav_times` INT(11) DEFAULT 0 COMMENT '收藏次数',
                         `refs` JSON DEFAULT NULL COMMENT '引用文献（存储引用的文献ID列表）',
                         `stats` TINYINT(1) DEFAULT 0 COMMENT '文章状态（0 正常，1 已删除）',
                         `ess_abs` TEXT DEFAULT NULL COMMENT '文章摘要',
                         PRIMARY KEY (`pid`),
                         KEY `idx_category` (`category`),
                         KEY `idx_author` (`auths`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

DROP TABLE IF EXISTS `patents`;

CREATE TABLE `patents` (
                           `pid` INT(11) NOT NULL AUTO_INCREMENT COMMENT '专利ID',
                           `applicants` JSON DEFAULT NULL COMMENT '申请人（JSON格式存储，键为申请人名，值为顺序编号）',
                           `name` VARCHAR(255) NOT NULL COMMENT '专利名',
                           `number` VARCHAR(255) NOT NULL COMMENT '专利编号',
                           `ipc_classification` VARCHAR(255) DEFAULT NULL COMMENT 'IPC分类号',
                           `abstracts` TEXT DEFAULT NULL COMMENT '内容摘要',
                           `grant_date` DATETIME DEFAULT NULL COMMENT '授权时间',
                           `keywords` JSON DEFAULT NULL COMMENT '关键词（JSON格式存储）',
                           `publish_date` DATETIME DEFAULT NULL COMMENT '发布时间',
                           `fav_time` INT(11) DEFAULT 0 COMMENT '收藏次数',
                           `stats` TINYINT(1) DEFAULT 0 COMMENT '状态: 0 正常 1 已删除 2 审核中',
                           PRIMARY KEY (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='专利表';

DROP TABLE IF EXISTS  `project`;

CREATE TABLE `project` (
                           `pid` INT(11) NOT NULL AUTO_INCREMENT COMMENT '科研项目ID',
                           `presider` JSON DEFAULT NULL COMMENT '主持人（JSON格式存储，键为主持人名，值为顺序编号）',
                           `affiliation` INT(11) DEFAULT NULL COMMENT '所属机构ID',
                           `keywords` JSON DEFAULT NULL COMMENT '关键词（JSON格式存储）',
                           `ess_abs` TEXT DEFAULT NULL COMMENT '内容摘要',
                           `publish_date` DATETIME DEFAULT NULL COMMENT '发布时间',
                           `fav_time` INT(11) DEFAULT 0 COMMENT '收藏次数',
                           `stats` TINYINT(1) DEFAULT 0 COMMENT '状态: 0 正常 1 已删除 2 审核中',
                           PRIMARY KEY (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='科研项目表';
