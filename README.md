# 校园自习室预约系统（含 AI 对话智能体）

一个前后端分离的校园自习室 / 座位预约系统，包含 **Spring Boot 后端**、**Android 客户端**，并基于大模型 **Function Calling** 实现了一个可对话操作的 AI 预约智能体。支持房间与座位浏览、预约 / 取消、**高德地图可视化拍照签到**、签到前震动提醒、**WebSocket 实时消息推送**、**学生与管理员在线咨询私信**、**评价拍照 / 录像**、超时自动处理、多轮对话预约等完整业务闭环；客户端采用**底部导航 + 多 Fragment** 架构，各列表支持下拉刷新。


## 技术栈

| 层级 | 技术 |
| --- | --- |
| 后端 | Spring Boot、MyBatis、MySQL、Redis、JWT、Maven、Knife4j、Hutool、WebSocket |
| Android | Java 11、Retrofit、OkHttp、Gson、Material Components（BottomNavigationView）、SwipeRefreshLayout、Glide、Lottie、高德地图 SDK、AlarmManager |
| AI 智能体 | DeepSeek（Function Calling）、Redis 多轮记忆、提示词工程 |
| 部署 | cpolar 内网穿透（无需公网服务器） |

## 功能模块

- **用户端**：注册登录（JWT 鉴权）、自习室与座位浏览、预约 / 取消、我的预约、定位签到与签退、超时自动处理。
- **地图可视化签到**：基于高德地图展示房间位置、签到半径圆圈与当前位置蓝点，统一使用 GCJ-02 坐标系；进入范围后调起相机拍照，照片随坐标上传完成签到。
- **签到提醒**：预约创建后通过 AlarmManager 注册，在开始时间前 15 分钟触发震动并发送通知。
- **实时消息推送**：后端基于 WebSocket 维护在线连接，管理员单发 / 群发消息后实时推送到客户端，App 断线自动重连；不在线时消息仍入库，可稍后拉取。
- **在线咨询私信**：学生可在消息页发起与管理员的会话，基于 WebSocket 实时双向收发，支持离线消息与未读角标；会话与私信分别由 `conversation`、`private_message` 表存储，与公告功能并存。
- **全局导航与交互**：采用单 Activity + 多 Fragment 与底部导航栏（首页 / 预约 / 消息 / 我的），各列表支持下拉刷新，破坏性操作有确认提示。
- **多媒体评价**：评价支持拍照和录像，文件经 Multipart 上传，通过 `review_media` 一对多表存储，并在评论列表中回显（图片直接展示、视频点击播放）。
- **AI 对话智能体**：自然语言完成查房间、查座位、预约、查询我的预约、取消、签到、签退等操作，支持多轮上下文。
- **管理端**：房间、座位、预约、用户与消息的管理维护。

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
│       │   ├── entity/                 # 实体（含 ReviewMedia）
│       │   ├── config/                 # 配置（拦截器、Web、WebSocket、AI 等）
│       │   ├── ws/                     # WebSocket 服务端点
│       │   └── util/                   # 工具（JWT、定位距离等）
│       └── resources/
│           ├── mapper/                 # XML 映射
│           └── application-example.yml # 配置模板（真实配置不入库）
└── campus study room booking system/   # Android 工程
    └── app/src/main/
        ├── java/com/example/.../
        │   ├── network/                # Retrofit / OkHttp / 接口
        │   ├── model/                  # 数据模型
        │   ├── SignMapActivity.java    # 高德地图拍照签到页
        │   ├── HomeFragment.java 等        # 底部导航各 Fragment（首页/预约/消息/我的）
        │   └── util/                   # WebSocket、震动提醒等工具
        ├── jniLibs/                    # 高德 native 库（arm64-v8a / armeabi-v7a）
        └── AndroidManifest.xml
```

## 快速开始

### 1. 后端

环境要求：JDK 17+、Maven、MySQL 8、Redis。

1. 建库并导入表结构（数据库名 `study_room_reserve`，含评价媒体表 `review_media`）。
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
2. 在 `RetrofitClient` 中将 `BASE_URL` 改为后端服务地址（本地调试或内网穿透地址）；高德地图 Key 在 `AndroidManifest.xml` 中配置。
3. 高德 SDK 以 jar 形式发布，其 native 库已手动置于 `app/src/main/jniLibs/`，无需额外处理。
4. 连接设备 / 模拟器后编译运行。同 WiFi 下可将 `BASE_URL` 指向电脑内网 IP 直连，速度更快。

## 部署说明

演示环境可使用 cpolar 等内网穿透工具，将本地后端暴露为公网地址，Android 端配置该地址即可访问。免费版地址在重启后可能变化，变更后需重新配置 `BASE_URL`。

## 后续计划

- [x] 高德地图可视化拍照签到，并在真机完成"范围内允许、范围外拒绝"验证
- [x] 签到前震动提醒、WebSocket 实时消息推送、多媒体评价
- [x] 底部导航重构、下拉刷新、学生与管理员在线咨询私信
- [ ] 后端统一坐标系转换（WGS-84 / GCJ-02 / BD-09）
- [ ] 后端部署到云服务器，替代内网穿透
- [ ] 收藏、管理员数据看板等扩展功能
