-- =============================================
-- 上门理发美甲预约系统数据库脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS home_beauty DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE home_beauty;

-- =============================================
-- 1. 用户表（顾客）
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '默认地址',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '默认地址经度',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '默认地址纬度',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表（顾客）';

-- =============================================
-- 2. 手艺人表
-- =============================================
DROP TABLE IF EXISTS `artisan`;
CREATE TABLE `artisan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `id_card` VARCHAR(18) NOT NULL COMMENT '身份证号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `certificate_no` VARCHAR(100) DEFAULT NULL COMMENT '资质证书编号',
    `certificate_img` VARCHAR(255) DEFAULT NULL COMMENT '资质证书图片',
    `work_years` INT DEFAULT 0 COMMENT '从业年限',
    `skill_desc` TEXT COMMENT '技能描述',
    `longitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '当前位置经度',
    `latitude` DECIMAL(10, 7) DEFAULT NULL COMMENT '当前位置纬度',
    `service_radius` INT DEFAULT 5 COMMENT '服务半径（公里）',
    `work_status` TINYINT DEFAULT 0 COMMENT '工作状态：0-离线，1-空闲，2-忙碌',
    `audit_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核，1-审核通过，2-审核拒绝',
    `audit_remark` VARCHAR(255) DEFAULT NULL COMMENT '审核备注',
    `rating` DECIMAL(3, 2) DEFAULT 5.00 COMMENT '综合评分',
    `order_count` INT DEFAULT 0 COMMENT '完成订单数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    KEY `idx_work_status` (`work_status`),
    KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='手艺人表';

-- =============================================
-- 3. 手艺人技能关联表
-- =============================================
DROP TABLE IF EXISTS `artisan_skill`;
CREATE TABLE `artisan_skill` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `artisan_id` BIGINT NOT NULL COMMENT '手艺人ID',
    `service_item_id` BIGINT NOT NULL COMMENT '服务项目ID',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '该技能的定价',
    `duration` INT NOT NULL COMMENT '服务时长（分钟）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_artisan_service` (`artisan_id`, `service_item_id`),
    KEY `idx_artisan_id` (`artisan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='手艺人技能关联表';

-- =============================================
-- 4. 服务项目表
-- =============================================
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `name` VARCHAR(100) NOT NULL COMMENT '服务项目名称',
    `description` TEXT COMMENT '服务描述',
    `cover_img` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `base_price` DECIMAL(10, 2) NOT NULL COMMENT '基础价格',
    `default_duration` INT NOT NULL COMMENT '默认时长（分钟）',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目表';

-- =============================================
-- 5. 服务分类表
-- =============================================
DROP TABLE IF EXISTS `service_category`;
CREATE TABLE `service_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务分类表';

-- =============================================
-- 6. 订单表
-- =============================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `artisan_id` BIGINT DEFAULT NULL COMMENT '手艺人ID（派单后填充）',
    `service_item_id` BIGINT NOT NULL COMMENT '服务项目ID',
    `service_name` VARCHAR(100) NOT NULL COMMENT '服务项目名称（快照）',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '服务价格',
    `duration` INT NOT NULL COMMENT '服务时长（分钟）',
    `appointment_date` DATE NOT NULL COMMENT '预约日期',
    `appointment_time` VARCHAR(20) NOT NULL COMMENT '预约时段，如：09:00-10:00',
    `address` VARCHAR(255) NOT NULL COMMENT '上门地址',
    `longitude` DECIMAL(10, 7) NOT NULL COMMENT '上门地址经度',
    `latitude` DECIMAL(10, 7) NOT NULL COMMENT '上门地址纬度',
    `contact_name` VARCHAR(50) NOT NULL COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) NOT NULL COMMENT '联系人电话',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `order_status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待派单，1-待接单，2-待服务，3-服务中，4-待结算，5-已完成，6-已取消',
    `pay_status` TINYINT DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-已退款',
    `pay_amount` DECIMAL(10, 2) DEFAULT NULL COMMENT '实付金额',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `check_in_code` VARCHAR(32) DEFAULT NULL COMMENT '到场核销码',
    `check_in_time` DATETIME DEFAULT NULL COMMENT '到场核销时间',
    `start_service_time` DATETIME DEFAULT NULL COMMENT '开始服务时间',
    `end_service_time` DATETIME DEFAULT NULL COMMENT '结束服务时间',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `dispatch_time` DATETIME DEFAULT NULL COMMENT '派单时间',
    `accept_time` DATETIME DEFAULT NULL COMMENT '接单时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_artisan_id` (`artisan_id`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_appointment` (`appointment_date`, `appointment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 7. 订单操作日志表
-- =============================================
DROP TABLE IF EXISTS `order_log`;
CREATE TABLE `order_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `operator_type` TINYINT NOT NULL COMMENT '操作人类型：1-用户，2-手艺人，3-系统',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作动作',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '操作备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';

-- =============================================
-- 8. 评价表
-- =============================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `artisan_id` BIGINT NOT NULL COMMENT '手艺人ID',
    `rating` TINYINT NOT NULL COMMENT '评分：1-5星',
    `content` VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
    `imgs` VARCHAR(1000) DEFAULT NULL COMMENT '评价图片（多个逗号分隔）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_artisan_id` (`artisan_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- =============================================
-- 9. 用户收藏表
-- =============================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `artisan_id` BIGINT NOT NULL COMMENT '手艺人ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_artisan` (`user_id`, `artisan_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收藏表';

-- =============================================
-- 10. 手艺人工作时段设置表
-- =============================================
DROP TABLE IF EXISTS `artisan_work_slot`;
CREATE TABLE `artisan_work_slot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `artisan_id` BIGINT NOT NULL COMMENT '手艺人ID',
    `week_day` TINYINT NOT NULL COMMENT '星期：1-周一，7-周日',
    `start_time` VARCHAR(10) NOT NULL COMMENT '开始时间，如：09:00',
    `end_time` VARCHAR(10) NOT NULL COMMENT '结束时间，如：18:00',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-休息，1-可接单',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_artisan_week` (`artisan_id`, `week_day`, `start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='手艺人工作时段设置表';

-- =============================================
-- 初始化数据
-- =============================================

-- 服务分类
INSERT INTO `service_category` (`id`, `name`, `icon`, `sort`, `status`) VALUES
(1, '理发', 'haircut.png', 1, 1),
(2, '美甲', 'manicure.png', 2, 1),
(3, '美容', 'beauty.png', 3, 1),
(4, '美发', 'hairdressing.png', 4, 1);

-- 服务项目
INSERT INTO `service_item` (`id`, `category_id`, `name`, `description`, `base_price`, `default_duration`, `sort`, `status`) VALUES
(1, 1, '男士剪发', '专业男士发型修剪，包含洗发和造型', 68.00, 60, 1, 1),
(2, 1, '女士剪发', '女士发型设计修剪，包含洗发和造型', 98.00, 90, 2, 1),
(3, 1, '儿童剪发', '专业儿童理发，适合3-12岁儿童', 58.00, 45, 3, 1),
(4, 1, '老人剪发', '专为老年人设计的理发服务', 48.00, 45, 4, 1),
(5, 2, '基础美甲', '指甲修剪、去死皮、上色', 88.00, 60, 1, 1),
(6, 2, '法式美甲', '经典法式美甲，优雅大方', 128.00, 90, 2, 1),
(7, 2, '光疗甲', '持久光疗美甲，包含款式设计', 198.00, 120, 3, 1),
(8, 2, '美甲卸除', '专业卸甲服务，不伤指甲', 38.00, 30, 4, 1),
(9, 3, '面部护理', '深层清洁、补水保湿护理', 168.00, 90, 1, 1),
(10, 3, '眼部护理', '眼部按摩、淡化黑眼圈', 98.00, 60, 2, 1),
(11, 4, '烫发', '专业烫发服务，多种卷度可选', 298.00, 180, 1, 1),
(12, 4, '染发', '专业染发服务，包含色板选择', 268.00, 180, 2, 1);

-- 测试用户（密码都是123456，BCrypt加密后的密码）
INSERT INTO `user` (`id`, `phone`, `password`, `nickname`, `gender`, `address`, `longitude`, `latitude`, `status`) VALUES
(1, '13800138001', '$2a$10$tLe/m.Jp6dSuVRdlk1zpVu1DZX3eg0HVqHQUzNtNmE.Jpqjw/Bla.', '张女士', 2, '北京市朝阳区建国路88号SOHO现代城A座', 116.467281, 39.908823, 1),
(2, '13800138002', '$2a$10$tLe/m.Jp6dSuVRdlk1zpVu1DZX3eg0HVqHQUzNtNmE.Jpqjw/Bla.', '李先生', 1, '北京市海淀区中关村大街1号', 116.317619, 39.983615, 1),
(3, '13800138003', '$2a$10$tLe/m.Jp6dSuVRdlk1zpVu1DZX3eg0HVqHQUzNtNmE.Jpqjw/Bla.', '王宝妈', 2, '北京市丰台区方庄小区1号楼', 116.437583, 39.872395, 1);

-- 测试手艺人
INSERT INTO `artisan` (`id`, `phone`, `password`, `real_name`, `id_card`, `gender`, `certificate_no`, `work_years`, `skill_desc`, `longitude`, `latitude`, `service_radius`, `work_status`, `audit_status`, `status`) VALUES
(1, '13900139001', '$2a$10$tLe/m.Jp6dSuVRdlk1zpVu1DZX3eg0HVqHQUzNtNmE.Jpqjw/Bla.', '理发师Tony', '110101199001011234', 1, 'HF202001001', 8, '擅长男士油头、女士时尚发型设计', 116.472810, 39.903823, 10, 1, 1, 1),
(2, '13900139002', '$2a$10$tLe/m.Jp6dSuVRdlk1zpVu1DZX3eg0HVqHQUzNtNmE.Jpqjw/Bla.', '美甲师Lily', '110102199202022234', 2, 'MJ202105002', 5, '擅长法式美甲、光疗甲、日式美甲', 116.470000, 39.905000, 8, 1, 1, 1),
(3, '13900139003', '$2a$10$tLe/m.Jp6dSuVRdlk1zpVu1DZX3eg0HVqHQUzNtNmE.Jpqjw/Bla.', '美容师小美', '110103199303033234', 2, 'MR201908003', 7, '擅长面部护理、身体护理、按摩', 116.465000, 39.910000, 12, 1, 1, 1);

-- 手艺人技能关联
INSERT INTO `artisan_skill` (`artisan_id`, `service_item_id`, `price`, `duration`, `status`) VALUES
(1, 1, 68.00, 60, 1),
(1, 2, 98.00, 90, 1),
(1, 3, 58.00, 45, 1),
(1, 4, 48.00, 45, 1),
(1, 11, 198.00, 180, 1),
(1, 12, 168.00, 180, 1),
(2, 5, 88.00, 60, 1),
(2, 6, 128.00, 90, 1),
(2, 7, 198.00, 180, 1),
(2, 8, 38.00, 30, 1),
(3, 9, 168.00, 90, 1),
(3, 10, 98.00, 60, 1);

-- 手艺人工作时段设置（默认周一到周日9:00-18:00）
INSERT INTO `artisan_work_slot` (`artisan_id`, `week_day`, `start_time`, `end_time`, `status`) VALUES
(1, 1, '09:00', '18:00', 1),
(1, 2, '09:00', '18:00', 1),
(1, 3, '09:00', '18:00', 1),
(1, 4, '09:00', '18:00', 1),
(1, 5, '09:00', '18:00', 1),
(1, 6, '09:00', '18:00', 1),
(1, 7, '09:00', '18:00', 1),
(2, 1, '09:00', '18:00', 1),
(2, 2, '09:00', '18:00', 1),
(2, 3, '09:00', '18:00', 1),
(2, 4, '09:00', '18:00', 1),
(2, 5, '09:00', '18:00', 1),
(2, 6, '09:00', '18:00', 1),
(2, 7, '09:00', '18:00', 1),
(3, 1, '09:00', '18:00', 1),
(3, 2, '09:00', '18:00', 1),
(3, 3, '09:00', '18:00', 1),
(3, 4, '09:00', '18:00', 1),
(3, 5, '09:00', '18:00', 1),
(3, 6, '09:00', '18:00', 1),
(3, 7, '09:00', '18:00', 1);
