# 06 · 业务系统接入示例

> 目标：用一个“网页对话框”模拟你自己的业务系统，调通平台的 Web API，体验多轮对话。

## 1. 两种接入姿势

| 姿势 | 适用 | 说明 |
|---|---|---|
| 网页 Demo（本仓库） | 演示、给面试官看 | `web-demo/index.html`，纯前端调用（演示用） |
| 后端调用（推荐生产） | 真实业务系统 | 密钥放后端，用 curl/Python/Java 等调 API（见 api-examples） |

> 红线：前端直接带 API Key 只能用于本地演示；真实系统务必走后端代理。

## 2. 运行网页 Demo

1. 修改 [web-demo/index.html](../web-demo/index.html) 顶部配置区：
   - `API_URL`：替换为你的平台端点（见 05 节）
   - `API_KEY`：替换为你的应用密钥
2. 任意方式打开页面（二选一）：
   - 直接双击用浏览器打开（简单，但部分浏览器对跨域有要求）
   - 或起个静态服务：`python3 -m http.server 8080` 后访问 `http://localhost:8080/web-demo/`
3. 提问“XH-200 智能网关最多能接几个传感器？”，确认能回答，且连续追问可记住上文（页面已自动保存 `conversation_id`）。

## 3. 后端最小示例（Python）

见 [api-examples/chat.py](../api-examples/chat.py)：

```bash
export API_URL="http://<SERVER_IP>/v1/chat-messages"   # Dify 格式（RAGFlow 见其发布页地址）
export API_KEY="<APP_API_KEY>"
python api-examples/chat.py "XH-200 智能网关最多能接几个传感器？"
```

脚本要点：
- 第一次提问后保存 `conversation_id`
- 第二次提问自动带上它 → 多轮会话
- 只依赖标准库 `urllib`，无第三方依赖，可直接跑

## 4. 接到“你自己的业务系统”

把 Demo 里的 HTTP 调用换成你的业务语言即可（Java/Go/.NET 都一样），核心只有一件事：

```text
组装请求 → POST 平台对话 API(Bearer key) → 解析 answer 展示给用户 → 记住 conversation_id
```

## 5. 本节验收清单

- [ ] 网页 Demo 能问答、能多轮
- [ ] Python/curl 脚本在命令行同样能问答
- [ ] 已理解“业务系统 = API 客户端”，平台负责 RAG