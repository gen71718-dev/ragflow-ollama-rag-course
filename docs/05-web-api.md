# 05 · 打通 Web API（curl 验证）

> 目标：把“网页里能聊”变成“程序能调”，即业务系统只发一个 HTTP POST 就能拿到 AI 回答。

## 1. 思路：应用平台 = 帮你把对话应用“发布成 API”

你不需要自己实现 RAG。平台在背后帮你完成：向量化问题 → 检索 → 组装上下文 → 调 Ollama 生成 → 返回。

两个平台通用字段：

| 字段 | 含义 |
|---|---|
| `query` | 用户问题 |
| `conversation_id` | 会话编号；**传上次返回的 id 即多轮会话**；新会话传空串 |
| `response_mode` | `blocking` 等回答完一次性返回（另有 streaming 流式） |
| `user` | 调用方标识（随便填） |
| `Authorization: Bearer <APP_API_KEY>` | 鉴权，由平台生成，相当于应用密码 |

## 2. 已验证示例（路线 B：Dify，本教程实测通过）

Dify：在应用的「API 访问 / Access API」页复制 API Key，端点固定为 `POST http://<SERVER_IP>/v1/chat-messages`。

```bash
curl -X POST http://<SERVER_IP>/v1/chat-messages \
  -H "Authorization: Bearer <APP_API_KEY>" \
  -H "Content-Type: application/json" \
  -d '{"inputs":{},"query":"XH-200 智能网关最多能接几个传感器？","response_mode":"blocking","conversation_id":"","user":"course-demo"}'
```

预期返回 JSON 中含 `"answer": "XH-200 智能网关最多可接入 64 个传感器节点"` 和 `conversation_id`。

**多轮会话验证**：把返回的 `conversation_id` 原样放进第二次请求，再问“我刚才问的是哪个型号？”，模型应能结合上文回答——这就是业务系统实现“上下文连贯”的方式。

> 完整可执行脚本见 [api-examples/dify-chat.sh](../api-examples/dify-chat.sh)（占位符替换后即可运行）。

## 3. RAGFlow 服务 API

RAGFlow 的对话应用同样可以发布：进入应用的「服务 API / 发布」页 → 生成/复制 API Key，并按界面展示的 **Base URL 与请求格式** 调用（RAGFlow 提供 OpenAI 兼容风格的端点；不同版本界面略异，以你页面上显示的地址为准）。

模板见 [api-examples/ragflow-openai.sh](../api-examples/ragflow-openai.sh)：把 `<BASE_URL>`、`<MODEL>`（通常为应用名）、`<APP_API_KEY>` 替换后运行。

> 判断是否调通的通用标准：HTTP 200 + 返回里含 `answer` / `choices[].message.content` 且能回答出“64 个”。

## 4. 谁有资格调用（安全红线）

- **密钥只放后端**：业务系统的服务端持有 `<APP_API_KEY>`，前端页面绝不直接带 key 调平台
- 平台端口只暴露在内网；跨网调用走网关/VPN 并做鉴权
- 本仓库所有示例的 key 均为占位符，替换前不可用，也禁止把真实 key 提交进仓库

## 5. 本节验收清单

- [ ] curl 一次调用返回 HTTP 200 + 正确 answer
- [ ] 携带 conversation_id 的第二次调用能记住上文