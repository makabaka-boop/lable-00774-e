-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    status TINYINT DEFAULT 1 COMMENT '0-禁用 1-启用',
    role VARCHAR(20) DEFAULT 'USER' COMMENT 'ADMIN/USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 通知渠道配置表
CREATE TABLE IF NOT EXISTS notify_channel (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL COMMENT 'EMAIL/SMS/DINGTALK/WECOM/FEISHU',
    config TEXT NOT NULL COMMENT '加密存储的配置JSON',
    status TINYINT DEFAULT 1,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 告警规则表
CREATE TABLE IF NOT EXISTS alert_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    data_source_type VARCHAR(50) NOT NULL COMMENT 'API/DATABASE/PROMETHEUS',
    data_source_config TEXT NOT NULL COMMENT '数据源配置JSON',
    rule_expression TEXT NOT NULL COMMENT '规则表达式',
    severity VARCHAR(20) DEFAULT 'WARNING' COMMENT 'INFO/WARNING/CRITICAL',
    channel_ids VARCHAR(500) COMMENT '通知渠道ID,逗号分隔',
    notify_template TEXT COMMENT '通知模板',
    check_interval INT DEFAULT 60 COMMENT '检查间隔(秒)',
    status TINYINT DEFAULT 1,
    created_by BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 告警记录表
CREATE TABLE IF NOT EXISTS alert_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT NOT NULL,
    rule_name VARCHAR(100),
    severity VARCHAR(20),
    content TEXT,
    notify_status TINYINT DEFAULT 0 COMMENT '0-未通知 1-已通知 2-通知失败',
    notify_result TEXT,
    triggered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rule_id (rule_id),
    INDEX idx_triggered_at (triggered_at)
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    duration BIGINT COMMENT '执行时长(ms)',
    status TINYINT DEFAULT 1 COMMENT '1-成功 0-失败',
    error_msg TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- 系统配置表(支持动态配置)
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(255),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 初始化管理员账号 (密码: admin123)
INSERT INTO sys_user (username, password, nickname, role) VALUES 
('admin', '$2a$10$8lnr25P6z40jMlcUSQ5Mcer7lgtQS/.oNOJDj0Cv0YI3Vk8n098c6', '管理员', 'ADMIN')
ON DUPLICATE KEY UPDATE password='$2a$10$8lnr25P6z40jMlcUSQ5Mcer7lgtQS/.oNOJDj0Cv0YI3Vk8n098c6';

-- 初始化普通用户
INSERT INTO sys_user (username, password, nickname, email, phone, role, status) VALUES 
('zhangsan', '$2a$10$8lnr25P6z40jMlcUSQ5Mcer7lgtQS/.oNOJDj0Cv0YI3Vk8n098c6', '张三', 'zhangsan@company.com', '13800138001', 'USER', 1),
('lisi', '$2a$10$8lnr25P6z40jMlcUSQ5Mcer7lgtQS/.oNOJDj0Cv0YI3Vk8n098c6', '李四', 'lisi@company.com', '13800138002', 'USER', 1),
('wangwu', '$2a$10$8lnr25P6z40jMlcUSQ5Mcer7lgtQS/.oNOJDj0Cv0YI3Vk8n098c6', '王五', 'wangwu@company.com', '13800138003', 'USER', 0),
('zhaoliu', '$2a$10$8lnr25P6z40jMlcUSQ5Mcer7lgtQS/.oNOJDj0Cv0YI3Vk8n098c6', '赵六', 'zhaoliu@company.com', '13800138004', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE username=username;

-- 初始化系统配置
INSERT INTO sys_config (config_key, config_value, description) VALUES
('alert.check.enabled', 'true', '是否启用告警检查'),
('alert.check.thread.pool.size', '5', '告警检查线程池大小'),
('notify.retry.count', '3', '通知重试次数')
ON DUPLICATE KEY UPDATE config_key=config_key;

-- 初始化通知渠道
INSERT INTO notify_channel (name, type, config, status, created_by) VALUES
('企业邮箱', 'EMAIL', '{"host":"smtp.company.com","port":465,"username":"alert@company.com","password":"xxx","ssl":true}', 1, 1),
('钉钉告警群', 'DINGTALK', '{"webhook":"https://oapi.dingtalk.com/robot/send?access_token=xxx","secret":"xxx"}', 1, 1),
('飞书告警群', 'FEISHU', '{"webhook":"https://open.feishu.cn/open-apis/bot/v2/hook/xxx"}', 1, 1)
ON DUPLICATE KEY UPDATE name=name;

-- 初始化告警规则
INSERT INTO alert_rule (name, description, data_source_type, data_source_config, rule_expression, severity, channel_ids, notify_template, check_interval, status, created_by) VALUES
('CPU使用率告警', '当CPU使用率超过80%时触发告警', 'API', '{"url":"http://monitor-api/metrics/cpu","method":"GET"}', 'value > 80', 'WARNING', '1,2', '【告警】${ruleName}: CPU使用率达到${value}%，请及时处理！', 60, 1, 1),
('内存使用率告警', '当内存使用率超过90%时触发严重告警', 'API', '{"url":"http://monitor-api/metrics/memory","method":"GET"}', 'value > 90', 'CRITICAL', '1,2,3', '【严重告警】${ruleName}: 内存使用率达到${value}%，请立即处理！', 30, 1, 1),
('磁盘空间告警', '当磁盘使用率超过85%时触发告警', 'API', '{"url":"http://monitor-api/metrics/disk","method":"GET"}', 'value > 85', 'WARNING', '1', '【告警】${ruleName}: 磁盘使用率达到${value}%', 300, 1, 1),
('服务健康检查', '检测核心服务是否正常运行', 'API', '{"url":"http://core-service/health","method":"GET"}', 'status != "UP"', 'CRITICAL', '1,2,3', '【严重告警】${ruleName}: 服务状态异常，当前状态: ${status}', 15, 0, 1)
ON DUPLICATE KEY UPDATE name=name;

-- 初始化告警记录
INSERT INTO alert_record (rule_id, rule_name, severity, content, notify_status, notify_result, triggered_at) VALUES
(1, 'CPU使用率告警', 'WARNING', 'CPU使用率达到85%', 1, '通知成功', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(2, '内存使用率告警', 'CRITICAL', '内存使用率达到92%', 1, '通知成功', DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(1, 'CPU使用率告警', 'WARNING', 'CPU使用率达到82%', 1, '通知成功', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
(3, '磁盘空间告警', 'WARNING', '磁盘使用率达到87%', 2, '通知失败: 邮件服务器连接超时', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
(2, '内存使用率告警', 'CRITICAL', '内存使用率达到95%', 0, NULL, NOW())
ON DUPLICATE KEY UPDATE rule_id=rule_id;
