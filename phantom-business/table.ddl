CREATE TABLE `c2c_msg` (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` varchar(32) NOT NULL COMMENT '发送者ID',
  `receiver_id` varchar(32) NOT NULL COMMENT '接收者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `timestamp` bigint(20) NOT NULL COMMENT '消息时间戳',
  `crc` varchar(32) NOT NULL COMMENT '客户端消息唯一标识',
  `delivery_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '投递状态 0-未投递 1-已投递',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `platform` tinyint(4) NOT NULL COMMENT '平台 1-Android 2-IOS 3-WEB',
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单聊消息';

CREATE TABLE `c2g_msg` (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `sender_id` varchar(32) NOT NULL COMMENT '发送者ID',
  `group_id` bigint(20) NOT NULL COMMENT '群组ID',
  `content` text NOT NULL COMMENT '消息内容',
  `timestamp` bigint(20) NOT NULL COMMENT '时间戳',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6597083600779350017 DEFAULT CHARSET=utf8mb4 COMMENT='群聊消息';

CREATE TABLE `group_info` (
  `group_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `group_name` varchar(32) NOT NULL COMMENT '会话标题',
  `group_avatar` varchar(255) NOT NULL COMMENT '会话头像',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`group_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='会话信息表';

CREATE TABLE `group_members` (
  `relationship_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关系ID，自增',
  `group_id` bigint(20) NOT NULL COMMENT '会话ID',
  `user_account` varchar(32) NOT NULL COMMENT '用户ID',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`relationship_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `user` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(32) NOT NULL COMMENT '用户名',
  `user_account` varchar(32) NOT NULL COMMENT '用户账户',
  `user_password` varchar(32) NOT NULL COMMENT '用户密码',
  `avatar` varchar(256) NOT NULL COMMENT '头像',
  `gmt_create` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
