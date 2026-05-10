# 多渠道告警系统

## How to Run

```bash
# 克隆项目后，在项目根目录执行
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```
访问地址: **http://localhost:8081**

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| Frontend | 8081 | Vue 3 前端应用 |
| Backend | 8080 (内部) | Spring Boot 后端API |
| MySQL | 3306 (内部) | 数据库 |
| Redis | 6379 (内部) | 缓存服务 |


## 测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |

## 题目内容

1、设计一个支持多种通知平台的告警系统，包括邮件、短信、钉钉、企业微信、飞书等，采用前后端分离架构。2、前端系统 采用Vue 3.x3、后端系统SpringBoot 2.x、Java 8 、MyBatisPlus 、redis(可选)4、采用规则引擎模式，支持灵活的告警条件配置5、要求通知平台可扩展6、支持从多种数据源获取数据进行告警7、接口认证采用JWT进行接口认证8、基于角色的权限控制9、敏感数据（如通知平台密钥）加密存储10、记录系统操作日志，便于追溯11、支持动态配置更新，无需重启服务

---

## 技术栈

### 后端
- **框架**: Spring Boot 2.7.x + Java 8
- **ORM**: MyBatis-Plus
- **缓存**: Redis
- **认证**: JWT
- **数据库**: MySQL 8.0

### 前端
- **框架**: Vue 3.x + Vite
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router

## 功能特性

- ✅ 多通知渠道支持 (邮件/短信/钉钉/企业微信/飞书)
- ✅ 可扩展的通知策略模式
- ✅ 基于 SpEL 的规则引擎
- ✅ 多数据源支持 (API/Mock)
- ✅ JWT 接口认证
- ✅ 基于角色的权限控制 (RBAC)
- ✅ 敏感数据 AES 加密存储
- ✅ 操作日志记录
- ✅ 动态配置更新（无需重启）

## 项目结构

```
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/alert/
│   │   ├── controller/         # 控制器层
│   │   ├── service/            # 服务层
│   │   ├── entity/             # 实体类
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── notify/             # 通知策略 (策略模式)
│   │   ├── rule/               # 规则引擎
│   │   ├── datasource/         # 数据源
│   │   ├── security/           # JWT 安全配置
│   │   ├── aop/                # 日志切面
│   │   └── config/             # 配置类
│   └── Dockerfile
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── api/                # API 请求
│   │   ├── router/             # 路由配置
│   │   └── stores/             # Pinia 状态管理
│   └── Dockerfile
├── docker-compose.yml          # Docker Compose 配置
└── README.md
```

## API 接口

### 认证
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 用户管理
- `GET /api/users/me` - 获取当前用户
- `GET /api/users` - 用户列表 (管理员)
- `PUT /api/users/{id}/status` - 修改用户状态
- `PUT /api/users/{id}/role` - 修改用户角色

### 通知渠道
- `GET /api/channels` - 渠道列表
- `POST /api/channels` - 创建渠道
- `PUT /api/channels/{id}` - 更新渠道
- `DELETE /api/channels/{id}` - 删除渠道

### 告警规则
- `GET /api/rules` - 规则列表
- `POST /api/rules` - 创建规则
- `PUT /api/rules/{id}` - 更新规则
- `DELETE /api/rules/{id}` - 删除规则
- `POST /api/rules/{id}/check` - 手动触发检查

### 告警记录
- `GET /api/records` - 告警记录列表

### 系统配置
- `GET /api/configs` - 配置列表
- `PUT /api/configs/{key}` - 更新配置

### 操作日志
- `GET /api/logs` - 日志列表

## 规则表达式示例

使用 Spring Expression Language (SpEL):

```
#cpu > 80                      # CPU 超过 80%
#memory > 90                   # 内存超过 90%
#responseTime > 3000           # 响应时间超过 3 秒
#cpu > 80 and #memory > 90     # 组合条件
#cpu > 95 or #memory > 95      # 或条件
```

## 扩展通知渠道

实现 `NotifyStrategy` 接口即可添加新的通知渠道：

```java
@Component
public class CustomNotifyStrategy implements NotifyStrategy {
    @Override
    public String getType() {
        return "CUSTOM";
    }

    @Override
    public boolean send(Map<String, Object> config, String title, String content) {
        // 实现通知发送逻辑
        return true;
    }
}
```

## License

MIT License
