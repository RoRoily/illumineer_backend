CREATE DATABASE docservice;

USE docservice;

DROP TABLE IF EXISTS `paper`;

CREATE TABLE `paper` (
                        `pid` INT(100) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
                        `title` VARCHAR(100) NOT NULL COMMENT '文章题目',
                        `theme` VARCHAR(100) NOT NULL COMMENT '文章主题',
                        `essabs` VARCHAR(255) NOT NULL COMMENT '内容摘要',
                        `keywords` VARCHAR(255) NOT NULL COMMENT '关键词',
                        `auths` VARCHAR(255) NOT NULL COMMENT '文章作者',
                        `derivation` VARCHAR(255) NOT NULL COMMENT '文章来源',
                        `type` VARCHAR(100) NOT NULL COMMENT '文章类型',
                        `publish_date` DATETIME NOT NULL COMMENT '发布时间',
                        `field` VARCHAR(100) NOT NULL COMMENT '相关领域',
                        `fav_time` INT(11) DEFAULT 0 COMMENT '收藏次数',
                        `ref_times` INT(11) DEFAULT 0 COMMENT '被引用次数',
                        `refs` VARCHAR(255) NOT NULL COMMENT '引用文献',
                        `content_url` VARCHAR(255) DEFAULT NULL COMMENT '文章链接',
                        `stats` TINYINT(4) DEFAULT 0 COMMENT '状态: 0 正常 1 已删除 2 审核中',
                        `category_id` INT(11) NOT NULL COMMENT '类别的id',
                        PRIMARY KEY (`pid`)
) ENGINE=InnoDB AUTO_INCREMENT=0 DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

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

