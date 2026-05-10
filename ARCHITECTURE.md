# 多渠道智能告警系统 — 代码理解与结构梳理

## 一、项目总览

| 维度 | 说明 |
|---|---|
| **项目定位** | 多渠道智能告警平台，支持规则配置、定时检查、多渠道通知分发 |
| **技术栈** | 前端 Vue3 + Vite + Pinia + Element Plus；后端 Spring Boot 2.7 + MyBatis-Plus + Spring Security + JWT |
| **部署方式** | Docker Compose 编排 MySQL 8 + Redis 7 + 后端 JAR + 前端 Nginx |
| **Java 版本** | JDK 8（pom.xml `java.version=1.8`） |

---

## 二、目录结构

```
lable-00774-e/
├── docker-compose.yml          # 四容器编排: mysql / redis / backend / frontend
├── README.md
├── backend/
│   ├── Dockerfile              # 多阶段构建: maven打包 → jre运行
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/alert/
│       │   ├── AlertApplication.java       # 启动类(@EnableScheduling)
│       │   ├── common/Result.java          # 统一响应体 {code, message, data}
│       │   ├── config/
│       │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│       │   │   └── MybatisPlusConfig.java       # 分页插件 + 自动填充
│       │   ├── security/
│       │   │   ├── SecurityConfig.java     # Spring Security配置
│       │   │   ├── JwtAuthFilter.java      # JWT认证过滤器
│       │   │   └── UserPrincipal.java      # 认证主体(userId, username, role)
│       │   ├── util/
│       │   │   ├── JwtUtil.java            # JWT令牌生成/解析
│       │   │   └── EncryptUtil.java        # AES加解密(渠道配置)
│       │   ├── aop/
│       │   │   ├── OperationLog.java       # 自定义注解@OperationLog
│       │   │   └── LogAspect.java          # AOP切面记录操作日志
│       │   ├── entity/                     # 6个实体: User, AlertRule, AlertRecord, NotifyChannel, SysConfig, SysLog
│       │   ├── mapper/                     # 6个Mapper接口(继承BaseMapper)
│       │   ├── controller/                 # 7个Controller
│       │   ├── service/                    # 7个Service(继承ServiceImpl)
│       │   ├── datasource/                 # 数据源获取层(策略模式)
│       │   │   ├── DataSourceFetcher.java       # 接口
│       │   │   ├── DataSourceService.java       # 路由分发
│       │   │   ├── ApiDataSourceFetcher.java    # HTTP API获取
│       │   │   └── MockDataSourceFetcher.java   # Mock随机数据
│       │   ├── rule/
│       │   │   └── RuleEngine.java         # SpEL表达式规则引擎
│       │   └── notify/                     # 通知发送层(策略模式)
│       │       ├── NotifyStrategy.java          # 接口
│       │       ├── NotifyService.java           # 路由分发+解密
│       │       ├── EmailNotifyStrategy.java     # 邮件(仅日志)
│       │       ├── SmsNotifyStrategy.java       # 短信(仅日志)
│       │       ├── DingTalkNotifyStrategy.java  # 钉钉Webhook
│       │       ├── WeComNotifyStrategy.java     # 企业微信Webhook
│       │       └── FeishuNotifyStrategy.java    # 飞书Webhook
│       └── resources/
│           ├── application.yml             # 主配置(连接Docker内MySQL/Redis)
│           ├── application-local.yml       # 本地开发配置(localhost)
│           └── schema.sql                  # 建表+初始化数据
├── frontend/
│   ├── Dockerfile              # 多阶段构建: npm build → nginx
│   ├── nginx.conf              # 前端SPA路由 + /api反向代理
│   ├── vite.config.js          # Vite配置 + /api代理到localhost:8080
│   ├── package.json
│   └── src/
│       ├── main.js             # 入口: Pinia + Router + ElementPlus(中文)
│       ├── App.vue             # 根组件(仅<router-view>)
│       ├── style.css           # 全局样式(CSS变量主题)
│       ├── api/index.js        # Axios封装 + 全部API方法
│       ├── router/index.js     # 路由 + 导航守卫
│       ├── stores/user.js      # Pinia用户状态(token/user/登录/登出)
│       └── views/
│           ├── Login.vue       # 登录页(左品牌右表单)
│           ├── Layout.vue      # 主布局(侧边栏+顶栏+内容区)
│           ├── Dashboard.vue   # 仪表盘(统计+最近告警+渠道状态)
│           ├── Rules.vue       # 告警规则CRUD
│           ├── Channels.vue    # 通知渠道CRUD
│           ├── Records.vue     # 告警记录查询
│           ├── Users.vue       # 用户管理(仅ADMIN)
│           ├── Configs.vue     # 系统配置(仅ADMIN)
│           └── Logs.vue        # 操作日志(仅ADMIN)
```

---

## 三、后端核心架构与分层

### 3.1 分层结构

```
Controller → Service → Mapper(MyBatis-Plus)
                ↓
         DataSourceService → DataSourceFetcher(策略)
         RuleEngine(SpEL)
         NotifyService → NotifyStrategy(策略)
```

典型的 Controller-Service-Mapper 三层，辅以策略模式实现数据源获取和通知发送的解耦。

### 3.2 启动入口

`AlertApplication.java:11` — `@SpringBootApplication` + `@MapperScan("com.alert.mapper")` + `@EnableScheduling`。定时任务由 `@Scheduled` 驱动。

### 3.3 统一响应体

`common/Result.java` — 所有接口返回 `Result<T>`，结构为 `{code:200, message:"success", data:T}`。前端在 Axios 拦截器中统一判断 `code !== 200` 则弹错误消息。

### 3.4 全局异常处理

`config/GlobalExceptionHandler.java` 处理四类异常：
- `RuntimeException` → 400
- `AccessDeniedException` → 403
- `MethodArgumentNotValidException` / `BindException` → 400（校验失败）
- `Exception` → 500

---

## 四、鉴权与权限控制

### 4.1 JWT 鉴权流程

```
1. 用户POST /api/auth/login → UserService.login()
   → 验证用户名密码(BCrypt) → 生成JWT(JwtUtil.generateToken)
   → 返回 {token, user}

2. 后续请求 → JwtAuthFilter.doFilterInternal()
   → 从 Authorization: Bearer xxx 提取token
   → JwtUtil.validateToken() → 解析出 userId/username/role
   → 构造 UserPrincipal + UsernamePasswordAuthenticationToken
   → 写入 SecurityContext

3. 接口鉴权:
   - @PreAuthorize("hasRole('ADMIN')")  → 仅ADMIN角色可访问
   - /api/auth/** → permitAll (登录注册免鉴权)
   - 其余所有接口 → authenticated
```

**关键文件**：
- `security/SecurityConfig.java:30` — SecurityFilterChain 配置，无状态Session，CORS 白名单
- `security/JwtAuthFilter.java:28` — OncePerRequestFilter，从Header提取Token
- `util/JwtUtil.java:24` — HMAC-SHA 签名，Token 中携带 userId/username/role 三个 claim

### 4.2 权限模型

采用简单的**二角色模型**：`ADMIN` / `USER`，存储在 `sys_user.role` 字段。

| 接口 | 权限 |
|---|---|
| 用户管理(增删改) | `@PreAuthorize("hasRole('ADMIN')")` |
| 通知渠道(增删改) | ADMIN |
| 告警规则(增删改) | ADMIN |
| 系统配置/操作日志 | ADMIN |
| 告警记录查看 | 任何已认证用户 |
| 手动触发检查 | 任何已认证用户 |

### 4.3 前端权限控制

`router/index.js:27` — 路由守卫 `beforeEach`：
- 无 token → 跳转 `/login`
- 有 token 但无 user → 调 `fetchUser()` 获取用户信息
- `meta.admin: true` 的路由 → 非ADMIN跳转回首页
- Layout.vue 中侧边栏根据 `userStore.isAdmin()` 控制管理员菜单显示

---

## 五、核心业务链路

### 5.1 告警检查与通知分发（最核心链路）

这是系统最核心的定时任务链路，由 `AlertCheckService.java` 驱动：

```
@Scheduled(fixedDelay=30000)  ← 每30秒执行
AlertCheckService.check()
  │
  ├─ 读取系统配置 alert.check.enabled → 是否继续
  │
  ├─ AlertRuleService.getActiveRules() → 获取所有 status=1 的规则
  │
  └─ 对每条规则调用 checkRule(rule):
      │
      ├─ 1. DataSourceService.fetch(type, config)
      │     └─ 根据 type 路由到 ApiDataSourceFetcher 或 MockDataSourceFetcher
      │        → 返回 Map<String, Object> 数据
      │
      ├─ 2. RuleEngine.evaluate(rule, data)
      │     └─ 使用 SpEL 表达式解析器评估 rule.getRuleExpression()
      │        data.forEach(context::setVariable) 将数据放入SpEL上下文
      │        变量需通过 #变量名 引用，如 #cpu > 80
      │
      ├─ 3. [若触发] RuleEngine.formatMessage(template, data)
      │     └─ 将 ${cpu}, ${memory} 等占位符替换为实际数据
      │
      ├─ 4. 遍历 rule.getChannelIds() (逗号分隔的渠道ID)
      │     └─ NotifyChannelService.getDecrypted(id) → 解密渠道配置
      │        → NotifyService.send(channel, title, content)
      │           └─ 根据 channel.getType() 路由到对应 NotifyStrategy
      │              → 钉钉/飞书/企微: HTTP POST webhook
      │              → 邮件/短信: 仅打日志(未实现)
      │
      └─ 5. 保存 AlertRecord 到数据库
            → notifyStatus: 1=已通知, 2=通知失败, 0=未通知
```

**关键设计**：
- 数据源获取和通知发送均采用**策略模式**，Spring 自动注入所有实现类
- `DataSourceService.java:22` 和 `NotifyService.java:27` 在 `@PostConstruct` 中将策略注册到 Map
- 规则表达式使用 **Spring SpEL**，变量通过 `context::setVariable` 注册，需使用 `#变量名` 引用

### 5.2 手动触发检查

`AlertRuleController.java:66` — `POST /api/rules/{id}/check` → `alertCheckService.manualCheck(id)`，直接对该规则执行一次 `checkRule()`。

### 5.3 操作日志

`aop/LogAspect.java:31` — `@Around("@annotation(operationLog)")` 切面：
- 拦截所有标注了 `@OperationLog("操作描述")` 的方法
- 记录：操作人、操作描述、方法签名、请求参数、IP地址、执行时长、成功/失败
- 写入 `sys_log` 表

---

## 六、数据库设计（6张表）

| 表名 | 用途 | 关键字段 |
|---|---|---|
| `sys_user` | 用户 | username, password(BCrypt), role(ADMIN/USER), status(0/1) |
| `notify_channel` | 通知渠道 | name, type(EMAIL/SMS/DINGTALK/WECOM/FEISHU), config(AES加密JSON), status |
| `alert_rule` | 告警规则 | name, dataSourceType, dataSourceConfig, ruleExpression(SpEL), severity, channelIds(逗号分隔), notifyTemplate, checkInterval(秒), status |
| `alert_record` | 告警记录 | ruleId, ruleName, severity, content, notifyStatus(0/1/2), notifyResult, triggeredAt |
| `sys_config` | 系统配置 | configKey(UNIQUE), configValue, description |
| `sys_log` | 操作日志 | userId, username, operation, method, params, ip, duration, status |

**初始化数据** (`schema.sql`)：
- 默认管理员: `admin/admin123`（BCrypt加密存储）
- 3个通知渠道: 企业邮箱、钉钉告警群、飞书告警群（**config为明文JSON，未加密**）
- 4条告警规则: CPU/内存/磁盘/服务健康检查（**表达式与数据源变量名不匹配**）
- 5条告警记录样例

---

## 七、前端页面与接口对应关系

| 页面 | Vue文件 | 调用API | 说明 |
|---|---|---|---|
| 登录 | Login.vue | `POST /api/auth/login` | 返回token+user存Pinia+localStorage |
| 仪表盘 | Dashboard.vue | `GET /api/rules`, `GET /api/channels/all`, `GET /api/records` | 统计卡片+最近告警+渠道状态 |
| 告警规则 | Rules.vue | `CRUD /api/rules` + `POST /api/rules/{id}/check` | 列表/新建/编辑/删除/手动测试 |
| 通知渠道 | Channels.vue | `CRUD /api/channels` | 列表/新建/编辑/删除; config列显示为`******` |
| 告警记录 | Records.vue | `GET /api/records` | 只读列表,支持按severity筛选 |
| 用户管理 | Users.vue | `CRUD /api/users` | 仅ADMIN可见,支持状态/角色修改 |
| 系统配置 | Configs.vue | `GET /api/configs` + `PUT /api/configs/{key}` | 仅ADMIN可见,在线编辑配置值 |
| 操作日志 | Logs.vue | `GET /api/logs` | 仅ADMIN可见,支持按用户名搜索 |

**前端Axios封装** (`api/index.js`)：
- `baseURL: '/api'`，开发时 Vite 代理到 `localhost:8080`，生产时 Nginx 代理到 `backend:8080`
- 请求拦截：自动附加 `Authorization: Bearer {token}`
- 响应拦截：`code !== 200` 弹错误；`401` 自动清除token跳转登录；`403` 弹无权限

---

## 八、配置与敏感信息处理

### 8.1 配置管理

| 层级 | 机制 | 说明 |
|---|---|---|
| 静态配置 | `application.yml` / `application-local.yml` | 数据库/Redis连接、JWT密钥、加密密钥 |
| 动态配置 | `sys_config` 表 + `SysConfigService` | 三级缓存: ConcurrentHashMap → Redis → DB |
| 前端代理 | `vite.config.js` proxy / `nginx.conf` | `/api` 统一代理到后端 |

`SysConfigService.java:24` — 启动时从DB加载到本地缓存+Redis；读取时按 localCache → Redis → DB 降级；更新时同时写DB+本地缓存+Redis。

### 8.2 敏感信息处理

| 数据 | 处理方式 | 位置 |
|---|---|---|
| 用户密码 | BCrypt加密存储 | `UserService.java:26` |
| 通知渠道配置 | 写入时AES加密，但初始化数据为明文(详见§九.2) | `EncryptUtil.java` + `NotifyChannelService.java:20` |
| 渠道配置展示 | 返回前端时替换为 `******` | `NotifyChannelService.java:32` / `NotifyChannelController.java:33` |
| JWT密钥 | 配置文件明文 | `application.yml: jwt.secret` |
| 数据库密码 | 配置文件明文 + docker-compose环境变量 | `application.yml:8` / `docker-compose.yml:48-49` |

---

## 九、已知缺陷与不匹配问题

### 9.1 规则表达式与初始化 SQL 不匹配

**问题**：`RuleEngine.evaluate()` 通过 `data.forEach(context::setVariable)` 将数据放入 SpEL 上下文，变量需通过 `#变量名` 引用。但 `schema.sql` 中写入的规则表达式与实际数据源返回的变量名不一致：

| 规则 | SQL中的表达式 | 正确的SpEL写法 | 数据源返回的变量 |
|---|---|---|---|
| CPU使用率告警 | `value > 80` | `#cpu > 80` 或 `#value > 80` | Mock返回 `cpu`; API取决于响应JSON |
| 内存使用率告警 | `value > 90` | `#memory > 90` 或 `#value > 90` | Mock返回 `memory`; API取决于响应JSON |
| 磁盘空间告警 | `value > 85` | `#disk > 85` 或 `#value > 85` | Mock返回 `disk`; API取决于响应JSON |
| 服务健康检查 | `status != "UP"` | `#status != "UP"` | 无对应Mock变量; API取决于响应JSON |

**不匹配的具体表现**：

1. **缺少 `#` 前缀**：SpEL 中通过 `setVariable` 注册的变量必须用 `#变量名` 引用，SQL 中 `value > 80` 会尝试直接引用属性而非变量，导致解析失败返回 false。

2. **变量名不一致**：SQL 统一使用 `value`，但 MockDataSourceFetcher 返回的 key 是 `cpu`、`memory`、`disk`、`responseTime`，即使加上了 `#` 前缀，`#value > 80` 也无法匹配到 Mock 数据中的 `cpu` 字段。

3. **API数据源返回的 key 不可控**：`ApiDataSourceFetcher` 直接将 HTTP 响应 JSON 反序列化为 `Map<String, Object>`，返回什么 key 取决于外部 API 的响应格式。

4. **前端提示文字与初始化数据矛盾**：`Rules.vue:74` 提示"使用SpEL表达式，变量使用#前缀，如: `#cpu > 80 and #memory > 90`"，但 SQL 初始化数据中用的都是裸变量名 `value`、`status`，没有 `#` 前缀。

**结论**：SQL 初始化的规则表达式在实际运行中无法正确评估，属于**初始化数据与引擎运行机制的不一致 bug**。

### 9.2 通知渠道配置不是全量 AES 密文

**问题**：渠道配置的加密状态在数据库中不一致——通过前端创建的记录是密文，而 `schema.sql` 初始化的记录是明文。

**写入路径（加密）**：
- `NotifyChannelService.saveChannel()` (`NotifyChannelService.java:20`)：前端通过 POST/PUT 创建或更新渠道时，`config` 字段会经过 `encryptUtil.encrypt()` 加密后存入数据库。

**初始化数据（明文）**：
- `schema.sql:106-109`：三条初始化渠道的 `config` 字段是**明文 JSON**：
  ```sql
  '{"host":"smtp.company.com","port":465,"username":"alert@company.com","password":"xxx","ssl":true}'
  '{"webhook":"https://oapi.dingtalk.com/robot/send?access_token=xxx","secret":"xxx"}'
  '{"webhook":"https://open.feishu.cn/open-apis/bot/v2/hook/xxx"}'
  ```

**读取路径的容错**：
- `NotifyChannelService.getDecrypted()` (`NotifyChannelService.java:39`)：读取时调用 `encryptUtil.decrypt()`。
- `EncryptUtil.decrypt()` (`EncryptUtil.java:30`)：如果解密失败（即遇到明文），`catch` 块会**静默返回原始内容**。

**影响**：

1. 系统首次启动后，三条初始化渠道的 `config` 在 DB 中是明文 JSON，不是 AES 密文。
2. 只有通过前端界面**重新编辑保存**过的渠道，`config` 才会变成 AES 密文。
3. `EncryptUtil.decrypt()` 的容错设计掩盖了这个问题——对明文输入解密失败时直接返回原文，所以 `getDecrypted()` 既能处理密文也能处理明文，两种状态在运行时都能工作，但**数据存储形态不一致**。
4. 直接查数据库会发现部分渠道配置是可读明文、部分是十六进制密文，明文记录中的 webhook 地址、密码等敏感信息直接暴露。

---

## 十、扩展点

### 10.1 数据源扩展

`DataSourceFetcher` 接口（`datasource/DataSourceFetcher.java`），当前实现了 `API` 和 `MOCK` 两种。扩展方式：
1. 新建类实现 `DataSourceFetcher` 接口，加 `@Component`
2. 实现 `getType()` 返回唯一类型标识（如 `PROMETHEUS`、`DATABASE`）
3. 实现 `fetch()` 从数据源拉取数据
4. Spring 自动注入到 `DataSourceService.fetchers` 列表，`@PostConstruct` 自动注册

> 注：`AlertRule.dataSourceType` 字段注释提到了 `DATABASE/PROMETHEUS`，但代码中未实现。

### 10.2 通知渠道扩展

`NotifyStrategy` 接口（`notify/NotifyStrategy.java`），当前5种实现。扩展方式同上：
1. 新建类实现 `NotifyStrategy`，加 `@Component`
2. 实现 `getType()` 和 `send()`
3. 自动注册到 `NotifyService.strategyMap`

### 10.3 规则引擎扩展

当前使用 SpEL 表达式。`RuleEngine.java:17` 的 `evaluate()` 方法是唯一入口，如需支持更复杂的规则（如多条件组合、时间窗口、阈值趋势），需在此处扩展。

### 10.4 系统配置扩展

`sys_config` 表支持动态键值对，添加新配置项只需 INSERT 新行，无需改代码。前端 Configs.vue 会自动展示。

---

## 十一、关键依赖

| 依赖 | 版本 | 作用 | 风险 |
|---|---|---|---|
| Spring Boot | 2.7.18 | 核心框架 | 已停止免费维护(2023.11 EOL) |
| MyBatis-Plus | 3.5.3.1 | ORM + 分页 + 代码生成 | 稳定 |
| jjwt | 0.11.5 | JWT令牌 | 稳定 |
| hutool-all | 5.8.22 | AES加密 + HTTP工具 | 引入全量包，体积大 |
| fastjson | 2.0.40 | JSON序列化 | 2.x已修复1.x安全漏洞，但仍需关注 |
| MySQL Connector | 8.0.33 | 数据库驱动 | 注意License |
| Spring Data Redis | (Boot管理) | 缓存 | 配置异常时静默降级 |
| Vue3 | 3.4.21 | 前端框架 | 稳定 |
| Element Plus | 2.6.1 | UI组件库 | 稳定 |
| Axios | 1.6.8 | HTTP客户端 | 稳定 |

---

## 十二、风险点与改进建议

### 🔴 高风险

1. **SpEL表达式注入**（`RuleEngine.java:21`）：规则表达式直接从数据库取出用 `SpelExpressionParser` 执行，如果管理员账号被攻破，攻击者可构造恶意SpEL表达式执行任意代码。**建议**：限制SpEL可调用的类和方法，或改用自定义的DSL/规则解析器。

2. **JWT密钥硬编码**（`application.yml:23`）：`jwt.secret` 为明文硬编码在代码仓库中，且强度不足。**建议**：使用环境变量注入，生产环境使用强随机密钥。

3. **AES加密密钥硬编码**（`application.yml:27`）：`encrypt.key: AlertEncryptKey!` 明文存储，所有渠道配置的加密形同虚设。**建议**：密钥通过环境变量或密钥管理服务注入。

4. **数据库/Redis密码硬编码**：`application.yml` 和 `docker-compose.yml` 中均为明文 `alert123456`。

5. **初始化渠道配置为明文**（§9.2）：`schema.sql` 中的通知渠道 config 字段未加密，敏感信息（webhook、密码）直接暴露在数据库中。**建议**：将 schema.sql 中的 config 字段预加密，或在应用启动时对明文记录做一次迁移加密。

### 🟡 中风险

6. **初始化规则表达式与SpEL引擎不匹配**（§9.1）：SQL 中的表达式 `value > 80` 缺少 `#` 前缀且变量名与数据源不一致，导致规则永远无法正确触发。**建议**：修正 schema.sql 中的表达式为 `#cpu > 80` 等正确格式。

7. **注册接口无防护**（`AuthController.java:27`）：`/api/auth/register` 允许任何人注册，无验证码/邀请码机制，可被批量注册。

8. **通知发送无重试机制**：`AlertCheckService.checkRule()` 中通知失败仅记录，未实现 `notify.retry.count` 配置的重试逻辑。

9. **定时检查间隔固定**：`@Scheduled(fixedDelay=30000)` 固定30秒，但每条规则有自己的 `checkInterval` 字段，实际并未按规则的 `checkInterval` 分别调度，所有规则统一30秒检查一次。

10. **全局异常处理返回非标准HTTP状态码**：`GlobalExceptionHandler` 对 `RuntimeException` 返回 HTTP 400，但业务异常（如"用户名或密码错误"）也是 RuntimeException，语义不准确。**建议**：自定义业务异常类，区分400和500。

11. **Redis异常静默吞掉**（`SysConfigService.java:29`）：`catch (Exception ignored) {}` 吞掉所有Redis异常，可能导致配置缓存不一致而难以排查。

12. **CORS白名单硬编码**（`SecurityConfig.java:46`）：允许的域名列表硬编码，包含内网IP `192.168.1.3`，生产部署需修改。

### 🟢 低风险/改进建议

13. **Email/SMS通知未实现**：`EmailNotifyStrategy` 和 `SmsNotifyStrategy` 仅打日志，未实际发送。生产需接入JavaMail和短信SDK。

14. **没有Mapper XML**：`application.yml` 配置了 `mapper-locations: classpath:/mapper/*.xml`，但实际项目中无XML文件，全部使用MyBatis-Plus注解方式。

15. **告警记录无去重/抑制**：同一规则持续触发会不断产生告警记录，缺少告警抑制（如同一规则5分钟内不重复告警）。

16. **前端todayAlertCount不准确**（`Dashboard.vue:103`）：今日告警数实际取的是最近10条记录数，而非按日期筛选。

17. **用户列表排除admin**（`UserService.java:55`）：`wrapper.ne(User::getUsername, "admin")` 硬编码排除admin，但没有禁止通过 `PUT /api/users/{id}` 修改admin自身。

---

## 十三、核心类速查表

| 类 | 位置 | 职责 |
|---|---|---|
| `AlertApplication` | `AlertApplication.java:11` | 启动入口，开启定时任务 |
| `SecurityConfig` | `security/SecurityConfig.java:22` | Security配置，JWT过滤链，CORS |
| `JwtAuthFilter` | `security/JwtAuthFilter.java:18` | 请求级JWT验证 |
| `JwtUtil` | `util/JwtUtil.java:12` | JWT生成/解析/验证 |
| `EncryptUtil` | `util/EncryptUtil.java:10` | AES加解密(渠道配置) |
| `AuthController` | `controller/AuthController.java:14` | 登录/注册 |
| `UserService` | `service/UserService.java:15` | 用户CRUD + 登录逻辑 |
| `AlertCheckService` | `service/AlertCheckService.java:20` | **核心** 定时检查+通知分发编排 |
| `RuleEngine` | `rule/RuleEngine.java:14` | SpEL规则评估 + 消息模板渲染 |
| `DataSourceService` | `datasource/DataSourceService.java:12` | 数据源获取路由 |
| `NotifyService` | `notify/NotifyService.java:15` | 通知发送路由 + 配置解密 |
| `LogAspect` | `aop/LogAspect.java:23` | 操作日志切面 |
| `SysConfigService` | `service/SysConfigService.java:14` | 系统配置三级缓存 |
