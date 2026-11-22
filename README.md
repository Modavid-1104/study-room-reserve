# 校园自习室预约系统（含 AI 对话智能体）

一个前后端分离的校园自习室 / 座位预约系统，包含 **Spring Boot 后端**、**Android 客户端**，并基于大模型 **Function Calling** 实现了一个可对话操作的 AI 预约智能体。支持房间与座位浏览、预约 / 取消、定位签到、超时自动处理、多轮对话预约等完整业务闭环。

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot、MyBatis、MySQL、Redis、JWT、Maven、Knife4j、Hutool |
| Android | Java 11、Retrofit、OkHttp、Gson、Material Components、Glide、Lottie |
| AI 智能体 | DeepSeek（Function Calling）、Redis 多轮记忆、提示词工程 |
| 部署 | cpolar 内网穿透（无需公网服务器） |

## 功能模块

- **用户端**：注册登录（JWT 鉴权）、自习室与座位浏览、预约 / 取消、我的预约、定位签到与签退、超时自动处理。
- **AI 对话智能体**：自然语言完成查房间、查座位、预约、查询我的预约、取消、签到、签退等操作，支持多轮上下文。
- **管理端**：房间、座位、预约与用户的管理维护。

## AI 智能体架构（核心亮点）

系统把大模型作为"大脑"负责理解意图与决策，把后端 Service 作为"手"负责真正执行，二者解耦：

```
用户自然语言
   │
   ▼
DeepSeek 大模型（结合 system prompt 与历史上下文决策）
   │  需要调用工具
   ▼
Function Calling（7 个工具）
   │  tool_calls
   ▼
后端 Service 执行（复用同一套业务逻辑 / 定位校验）
   │  role=tool 回传结果
   ▼
大模型整理成自然语言回复用户
```

- **7 个工具函数**：`get_room_list`、`get_seats_by_room`、`quick_reserve`、`get_my_reservations`、`cancel_reservation`、`sign_reservation`、`sign_out_reservation`，覆盖预约全生命周期。
- **工具调用循环**：支持多轮 `tool_calls → 执行 → 回传 → 再推理`，可自动串联多个工具。
- **多轮记忆**：基于 Redis 按 `conversationId` 持久化会话历史，TTL 24 小时。
- **提示词工程**：通过 system prompt 约束角色、时间推理与对外口径，避免内部状态、工具名等信息泄露。
- **定位透明注入**：客户端经定位权限获取坐标后随请求传入，后端用 Haversine 公式计算距离并校验是否在允许范围内。

## 目录结构

```
study-room-booking-system/
├── study-room-reserve/                 # 后端工程（Spring Boot）
│   └── src/main/
│       ├── java/com/studyroom/.../
│       │   ├── controller/             # 接口层
│       │   ├── service/                # 业务逻辑（含 AI 服务）
│       │   ├── mapper/                 # MyBatis 映射
│       │   ├── entity/                 # 实体
│       │   ├── config/                 # 配置（拦截器、Web、AI 等）
│       │   └── util/                   # 工具（JWT、定位距离等）
│       └── resources/
│           ├── mapper/                 # XML 映射
│           └── application-example.yml # 配置模板（真实配置不入库）
└── campus study room booking system/   # Android 工程
    └── app/src/main/
        ├── java/com/example/.../
        │   ├── network/                # Retrofit / OkHttp / 接口
        │   ├── model/                  # 数据模型
        │   ├── ui/                     # 界面
        │   └── util/                   # 定位等工具
        └── AndroidManifest.xml
```

## 快速开始

### 1. 后端

环境要求：JDK 17+、Maven、MySQL 8、Redis。

1. 建库并导入表结构（数据库名 `study_room`）。
2. 复制配置模板并填写真实信息：

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
```

在 `application.yml` 中配置数据库连接、Redis 以及 DeepSeek API Key 等。

3. 启动运行：

```bash
mvn spring-boot:run
```

> 真实 `application.yml` 已在 `.gitignore` 中忽略，不会被提交，请注意保管密钥。

### 2. Android

环境要求：Android Studio、minSdk 24。

1. 用 Android Studio 打开 `campus study room booking system` 目录。
2. 在 `RetrofitClient` 中将 `BASE_URL` 改为后端服务地址（本地调试或内网穿透地址）。
3. 连接设备 / 模拟器后编译运行。

## 部署说明

演示环境可使用 cpolar 等内网穿透工具，将本地后端暴露为公网地址，Android 端配置该地址即可访问。免费版地址在重启后可能变化，变更后需重新配置 `BASE_URL`。

## 后续计划

- [ ] 定位签到在真机上完成"范围内允许、范围外拒绝"验证
- [ ] 后端统一坐标系转换（WGS-84 / GCJ-02 / BD-09）
- [ ] 收藏、消息推送、管理员数据看板等扩展功能
