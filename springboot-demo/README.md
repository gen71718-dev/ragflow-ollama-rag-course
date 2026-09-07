# springboot-demo · 业务系统后端接入示例（Spring Boot）

> 为什么需要这一层：浏览器直接调用 RAGFlow/Dify 会把 API Key 暴露在前端，
> 生产上应由**后端统一持有密钥**并封装平台 API，再给内部业务系统提供干净的 REST 接口。
> 本模块就是仓库 `docs/06-business-integration.md` 里「后端调用（推荐生产）」的 Spring Boot 最小实现。

## 技术栈

- Java 21 + Spring Boot 3.3（Web）
- `RestTemplate` 发起 HTTP 调用，Jackson 解析 JSON
- 无数据库、无第三方中间件依赖（部署层的 RAGFlow/Dify 内部已有 ES/MySQL，业务封装层不需要再连库）

## 目录结构

```text
springboot-demo/
├── pom.xml
├── README.md
└── src/main/
    ├── java/com/example/ragdemo/
    │   ├── RagDemoApplication.java      # 启动类
    │   ├── config/                      # @ConfigurationProperties + RestTemplate 超时配置
    │   ├── client/                      # Dify / RAGFlow 两个平台的 HTTP 客户端（可插拔）
    │   ├── service/                     # 按 ai.platform 选择走哪个平台
    │   └── web/                         # REST Controller + 统一异常处理
    └── resources/application.yml        # 平台地址 / 密钥 / 模型配置
```

## 配置

编辑 `src/main/resources/application.yml`，替换占位符：

```yaml
ai:
  platform: dify          # dify | ragflow，二选一
  dify:
    url: http://<SERVER_IP>/v1/chat-messages   # 同 api-examples/chat.py 的 API_URL
    api-key: <APP_API_KEY>
    user: business-demo
  ragflow:
    url: http://<SERVER_IP>/v1/chat/completions # 同 api-examples/ragflow-openai.sh
    api-key: <APP_API_KEY>
    model: <RF_MODEL>
```

> 真实密钥不要写死在 yml 里提交仓库。进阶做法：用环境变量覆盖，
> 例如启动前 `export AI_DIFY_API_KEY=xxx`（Spring Boot 支持把配置项映射到环境变量）。

## 运行

```bash
cd springboot-demo
mvn spring-boot:run
```

启动后先看健康检查：`curl http://localhost:8080/api/health` 应返回 `{"status":"UP"}`。

## 调用示例

单轮问答（Dify 平台）：

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query":"XH-200 智能网关最多能接几个传感器？"}'
```

返回示例：

```json
{"answer":"XH-200 最多支持 64 个传感器节点……","conversationId":"xxxxxxxx","platform":"dify"}
```

多轮对话：把第一次返回的 `conversationId` 带进第二次请求：

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"query":"我刚才问的是哪个型号？","conversationId":"<上一步返回的 conversationId>"}'
```

## 两个平台的区别（面试口径）

- **dify（默认）**：走 `/v1/chat-messages`，请求体里传 `conversation_id` 即可续接多轮会话，仓库已实测该格式（见 `api-examples/chat.py`、`web-demo/`）。
- **ragflow**：服务 API 是 OpenAI 兼容格式（`/v1/chat/completions`），本客户端解析 `choices[0].message.content`。会话管理以发布页展示为准。
- 切换平台只需改 `ai.platform`，业务层代码不用动——这就是「封装一层」的价值。

## 常见问题

- **502 / 调用 AI 平台失败**：先 `curl` 平台地址确认服务可达、API Key 是否正确（参考 `scripts/check.sh`）。
- **连接超时**：大模型生成慢，默认读超时 120s，一般够用；可在 `RestClientConfig` 调整。
- **报错里出现占位符 `<SERVER_IP>`**：说明 application.yml 还没替换，部署前务必替换完整。
