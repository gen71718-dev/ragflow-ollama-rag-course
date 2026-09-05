# 07 · 路线 B：Dify（对比章节，全链路已验证）

> 本仓库主线是 RAGFlow。本章保留 **Dify 已跑通** 的完整记录作为对照——同一份 Ollama、同一份业务文档，用 Dify 也能全链路跑通，方便你理解“平台换、原理不变”。

## 1. Dify 里的步骤摘要（与 RAGFlow 一一对应）

| # | RAGFlow（主线） | Dify（本路线） |
|---|---|---|
| 1 | 部署 RAGFlow（compose 一键起） | `docker compose up -d`（官方 docker/dify 目录） |
| 2 | 模型供应商 → Ollama | 设置 → 模型供应商 → Ollama，Base URL `http://<SERVER_IP>:11434`，添加 qwen2.5:7b / bge-m3 |
| 3 | 知识库解析（bge-m3 向量化） | 知识库 → 上传文档 → 分段与索引（选高质量/向量检索） |
| 4 | 聊天助手（检索→LLM） | 创建“聊天助手”应用，关联知识库，模型选 qwen2.5:7b |
| 5 | UI 提问验证 | UI 提问验证，问题同上，回答 “64 个” |
| 6 | 服务 API | 「API 访问」页取 Key，端点 `/v1/chat-messages` |

## 2. Dify Web API 实测记录（脱敏）

端点格式：

```text
POST http://<SERVER_IP>/v1/chat-messages
Authorization: Bearer <APP_API_KEY>
Content-Type: application/json
```

请求体（新会话）：

```json
{"inputs":{},"query":"XH-200 智能网关最多能接几个传感器？","response_mode":"blocking","conversation_id":"","user":"course-demo"}
```

请求体（多轮：把上次返回的 conversation_id 原样带回）：

```json
{"inputs":{},"query":"我刚才问的是哪个型号？","response_mode":"blocking","conversation_id":"<上次返回的id>","user":"course-demo"}
```

实测要点：新会话能答“XH-200 … 64 个传感器节点”；带 conversation_id 的追问能结合上文回答。失败排查：先 `curl -I http://<SERVER_IP>` 确认服务在，再看返回码与 `message` 字段。

## 3. RAGFlow vs Dify（选择建议）

| 维度 | RAGFlow | Dify |
|---|---|---|
| 定位 | 深度 RAG / 知识库引擎 | 通用 LLM 应用平台 |
| 文档解析/切块 | 更强（表格、版式、引用溯源） | 够用（基础分段+向量检索） |
| 编排 | 检索→生成为主 | 工作流/Agent/插件生态丰富 |
| 对接 Ollama | ✅ | ✅ |
| 本教程角色 | **主线** | 路线 B 对比 |

面试口径：选型不是“谁更好”，而是“我的场景是深度知识库问答 → RAGFlow；若要快速搭多工具工作流 → Dify”。两套都部署过 = 你懂原理而非只会点按钮。

## 4. Dify 常见坑（本教程踩过）

- **别用带联网搜索的“高级编排”模板**：会去调 SerpApi 等外网工具，无外网/无 Key 时报 `SerpApi API key does not support empty value`。改为简单应用或删掉搜索节点。
- **embedding 模型没配上**：知识库解析报错，先在模型供应商确认 bge-m3 可用。
- **`/v1/chat-messages` 拒绝连接**：确认 Dify 容器映射的端口（通常 80/8080），`ss -tlnp | grep :80` 看监听。

## 5. 验证结论

- 同一文档，Dify 与 RAGFlow 都能答出“64 个”，说明**两条路线共用同一套 Ollama + 知识**，区别只在平台层。
- 想要引用可点、解析更专业 → 主线 RAGFlow 体验更佳。