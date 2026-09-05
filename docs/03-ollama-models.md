# 03 · Ollama：跑起本地大模型

> 目标：用 Docker 启动 Ollama，拉取 **1 个对话模型（生成回答）+ 1 个 Embedding 模型（向量化）**，并验证 API 可访问。

## 1. 为什么是这两个模型

| 模型 | 类型 | 作用 | 体积 |
|---|---|---|---|
| `qwen2.5:7b` | 对话/生成（LLM） | 理解问题、写回答 | 约 4.7G |
| `bge-m3` | Embedding | 把文字转成向量做检索 | 约 1.2G |

> 面试常问：**embedding 模型 ≠ 对话模型**。对话模型负责“说话”，embedding 负责“判断两段话语义像不像”，两个都要，缺一个 RAG 就断链。

## 2. 启动 Ollama 容器

```bash
docker run -d \
  --name ollama \
  --restart unless-stopped \
  -v ollama:/root/.ollama \
  -p 11434:11434 \
  ollama/ollama
```

参数说明：`-v ollama:/root/.ollama` 把模型存进数据卷（容器删了模型还在）；`-p 11434:11434` 对外提供 API。

## 3. 拉取模型（约几分钟到几十分钟，取决于网速）

```bash
docker exec ollama ollama pull qwen2.5:7b   # 对话模型
docker exec ollama ollama pull bge-m3       # embedding 模型
```

查看已装模型：

```bash
docker exec ollama ollama list
```

预期能看到两行：`qwen2.5:7b`（capabilities: completion/tools）和 `bge-m3`（capabilities: embedding）。

## 4. 验证 API 能被外部访问

在**服务器本机**执行：

```bash
curl http://127.0.0.1:11434/api/tags
curl http://<SERVER_IP>:11434/api/tags
```

两条都应返回包含两个模型的 JSON。再做一个真实生成测试：

```bash
curl http://<SERVER_IP>:11434/api/generate \
  -d '{"model":"qwen2.5:7b","prompt":"回复OK两个字","stream":false}'
```

预期返回 `"response":"OK"`。

## 5. 常见问题

- **`curl: 拒绝连接`**：依次查 `docker ps`（容器是否 Up）、`ss -tlnp | grep 11434`（是否监听）、防火墙（第 02 节是否停用）。
- **OLLAMA_HOST 要不要改？** 容器方案用 `-p 11434:11434` 已对外暴露，不需要改 `OLLAMA_HOST`。只有裸机（systemd 直装）才需要设置 `Environment=OLLAMA_HOST=0.0.0.0`。
- **内存**：qwen2.5:7b 推理时约占用 4-6G 内存，10G 的机器后面要让 ES 少占内存（见 04 节）。

## 6. 本节验收清单

- [ ] `docker exec ollama ollama list` 有两个模型
- [ ] `http://<SERVER_IP>:11434/api/tags` 返回 JSON
- [ ] 生成测试返回 “OK”