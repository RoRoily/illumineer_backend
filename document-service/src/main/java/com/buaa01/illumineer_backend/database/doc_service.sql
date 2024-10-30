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

