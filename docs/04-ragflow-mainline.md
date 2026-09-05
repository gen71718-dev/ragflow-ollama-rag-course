# 04 · 主线：RAGFlow 部署 + 知识库 + 问答

> 目标：把 RAGFlow 跑起来，导入示例业务文档，让它在对话里回答出文档里的准确答案（如“XH-200 最多接入 64 个传感器”）。

## 0. RAGFlow 一启动会拉起哪些容器

| 容器 | 角色 |
|---|---|
| ragflow | 主服务（Web + 编排 + API），对外 80/443/9380-9384 |
| elasticsearch(es01) | 向量库 + 全文检索（数据引擎） |
| mysql | 元数据（知识库/应用/会话） |
| minio | 文件/对象存储（原始文档与切片文件） |
| redis(valkey) | 缓存/队列 |

## 1. 下载并解压部署包（服务器上执行）

```bash
mkdir -p /software && cd /software
# 国内网络建议用镜像加速，或使用官方 release 包
wget -q https://github.com/infiniflow/ragflow/releases/download/v0.27.1/ragflow-0.27.1.tar.gz
tar xf ragflow-0.27.1.tar.gz
cd ragflow-0.27.1/docker
cp .env .env.bak          # 先备份
```

## 2. 低内存优化（10G 机器实测必需）

**背景**：RAGFlow 默认让 ES 按“内存限制的一半”申请 JVM 堆。若 `.env` 里 `MEM_LIMIT=8073741824`(约 7.5G)，ES 就会去申请约 3.7G，10G 机器上分分钟 OOM，表现是 ES 容器反复 `Restarting`，日志报：

```text
Native memory allocation (mmap) failed to map ... bytes for committing reserved memory.
```

**修法**：给 ES 的 JVM 堆设上限 1G。

```bash
echo 'ES_JAVA_OPTS=-Xms1g -Xmx1g' >> /software/ragflow/ragflow-0.27.1/docker/.env
```

如需更稳可把 `.env` 里的 `MEM_LIMIT` 也改小（如 2G = `2147483648`）。

## 3. 启动 RAGFlow

```bash
cd /software/ragflow/ragflow-0.27.1/docker
docker compose up -d
```

等待镜像拉取与初始化（几分钟），然后：

```bash
docker ps
```

期望看到 ragflow / es01 / mysql / minio / redis 全部 `Up`，其中 mysql、redis 为 `healthy`。ES 若还是反复重启，执行 `docker compose up -d es01` 重建并查看 `docker logs --tail 50 docker-es01-1`。

浏览器打开 `http://<SERVER_IP>`（默认 80 端口），注册一个账号进入系统。

> 打不开？依次看：`docker ps` 是否都在；本机防火墙（02 节）；浏览器 `Ctrl+F5` 强刷。仍不行按 08 节查 ragflow 容器日志。

## 4. 配置模型供应商（对接本地 Ollama）

在 RAGFlow 界面：**头像菜单 → 模型供应商 → 添加供应商 → Ollama**：

| 配置项 | 填什么 |
|---|---|
| API Base | `http://<SERVER_IP>:11434` |
| 模型类型 | 分别添加 qwen2.5:7b（对话）、bge-m3（Embedding） |

保存后能看到两个模型已注册。（部分版本没有“测试”按钮，直接保存即可，是否可用由下一步知识库解析来验证。）

> 若 RAGFlow 与 Ollama 不在同一台机，把 `<SERVER_IP>` 换成能互通的内网 IP 即可；都在 Docker 里时也可以用同网络的服务名。

## 5. 创建知识库并导入示例文档

1. 左侧「知识库」→「新建知识库」，Embedding 模型选 `bge-m3`
2. 上传本仓库 [example-data/星火智造业务手册.md](../example-data/星火智造业务手册.md)
3. 触发「解析」（chunk 切块 + 向量化入库），等待状态变为**正常**，能看到 chunk 数量 > 0

解析失败时报 `es01 ... Connection refused / Name resolution` 之类 → ES 没就绪，回到第 2、3 步或查 08 节。

## 6. 创建问答应用（关键：用最简单的模板）

1. 左侧「聊天助手」→「新建聊天助手」，命名如“星火产品客服”
2. 画布默认通常是两条节点：**知识库检索 → 大语言模型**
   - 知识库检索节点：选择第 5 步的知识库，TopN 可设 3-5
   - 大语言模型节点：模型选 `qwen2.5:7b`，并填系统提示词
3. 保存后在右侧聊天窗提问：

```text
XH-200 智能网关最多能接几个传感器？
```

期望回答 **“最多 64 个传感器节点”**，且能展开引用来源（手册文档）。

**推荐系统提示词模板**：

```text
你是“星火智造”的产品客服。
回答必须严格依据【参考资料】中的内容，禁止编造数字、章节号、文件名或引用。
参考资料不足时，直接回答“资料中未找到相关信息”。
```

## 7. 避坑记录（本教程实战踩过）

- **不要选带“搜索 / Agent”节点的模板**：它们会尝试调用外网搜索（SerpApi）等工具，没有外网与付费 Key 时运行报错：
  - `ValueError('SerpApi API key does not support empty value.')`
  - `Can't find variable: 'Agent:xxx@content'`
  解决办法：新建“聊天助手”，画布里只保留「知识库检索 → 大语言模型」，删掉搜索/工具类节点。
- **rerank 模型**：Ollama 不提供 rerank，主线不填该项即可；填了反而可能连不上。进阶再考虑 TEI + bge-reranker-v2-m3（10G 内存不建议同机跑）。
- **回答出现 256/512 等编造数字**：说明检索没命中或提示词太松。先看回答的引用/检索日志是否为空；再用 08 节方法直接查 ES 里到底有没有“64”。

## 8. 本节验收清单

- [ ] `docker ps`：五个容器 Up
- [ ] 模型供应商里 qwen2.5:7b、bge-m3 已添加
- [ ] 知识库解析成功，chunk > 0
- [ ] 应用提问 “XH-200 最多能接几个传感器？” 回答 “64 个” 且带引用