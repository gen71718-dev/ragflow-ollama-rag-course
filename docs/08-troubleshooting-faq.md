# 08 · 故障排查与 FAQ

> 规则：先看“现象 → 原因 → 命令 → 修复”，一条条对。所有命令在 Linux 服务器终端执行。

## 1. ES 容器反复重启 / OOM（最常见）

**现象**：`docker ps` 里 es01 一直 `Restarting`；日志出现：

```text
Native memory allocation (mmap) failed to map ... bytes for committing reserved memory.
```

或容器 `Exited (137)`（被系统 OOM 杀掉）。

**原因**：ES 默认按内存限制的一半申请 JVM 堆，10G 小内存机器上不够分。

**修复**：

```bash
echo 'ES_JAVA_OPTS=-Xms1g -Xmx1g' >> <ragflow目录>/docker/.env
cd <ragflow目录>/docker && docker compose up -d es01
sleep 45 && docker ps | grep es01   # 期望 Up + healthy
```

## 2. Docker 端口映射失效：iptables 报错

**现象**：`docker start xxx` 报 `iptables: No chain/target/match by that name`；容器间 `No route to host`。

**原因**：firewalld 重启清掉了 Docker 的 iptables 链。

**修复**（课程环境）：

```bash
systemctl stop firewalld && systemctl disable firewalld && systemctl restart docker
```

## 3. ragflow 连不上 es01：NameResolutionError / Connection refused

**现象**：知识库解析报 `Failed to resolve 'es01'` 或 `Connection refused`。

**原因**：ES 容器没起/没在同一个 docker 网络/别名丢失。

**修复顺序**：先保证 ES `Up (healthy)`（见 #1）→ 检查别名：

```bash
docker exec <ragflow容器名> getent hosts es01
```

若解析不到，把 es01 重新连回 ragflow 网络并补别名：

```bash
docker network connect --alias es01 docker_ragflow docker-es01-1
```

然后回知识库点「重新解析」。

## 4. Ollama 端口连不上：11434 拒绝连接

**排查命令**（按顺序）：

```bash
docker ps | grep ollama
ss -tlnp | grep 11434
curl http://127.0.0.1:11434/api/tags
```

容器没起就 `docker start ollama`；起了但 127.0.0.1 通、内网 IP 不通 → 查防火墙（#2）。

## 5. RAGFlow 页面打不开 / 一直转圈

三层排查：

1. **页面层**：`Ctrl+F5` 强刷；换无痕窗口；报错信息截图
2. **服务层**：`docker ps`；看 ragflow 日志最后 30 行：
   ```bash
   docker logs --tail 30 <ragflow容器名>
   ```
   关注 `Elasticsearch ... is healthy`、`Use Elasticsearch ... as the doc engine` 是否出现
3. **网络层**：服务器上 `curl -I http://127.0.0.1`；Windows 上 `curl -I http://<SERVER_IP>`

页面里出现 JS 报错（如 `TypeError: Cannot read properties of undefined`）多为前端缓存/版本不一致，先强刷或重启 ragflow 容器再试。

## 6. 答非所问 / 编造数字（如答 256、512）

**原因**：检索没命中（知识库没解析成功 / TopK 太小）或提示词允许自由发挥。

**验证检索到底有没有命中**（直接查 ES，脱敏示例）：

```bash
curl -s -u elastic:<ES密码> 'http://127.0.0.1:<ES_HOST_PORT>/ragflow_*/_search?pretty' \
  -H 'Content-Type: application/json' \
  -d '{"query":{"match":{"content":"64"}},"size":3}'
```

有命中但模型乱编 → 换成 04 节的“严格依据参考资料”提示词；无命中 → 检查知识库解析状态与 chunk 数。

## 7. 应用报 SerpApi / Agent 变量错误

**现象**：运行时报 `ValueError('SerpApi API key does not support empty value.')` 或 `Can't find variable: 'Agent:xxx@content'`。

**原因**：应用模板/画布里带了“联网搜索”“Agent 工具”节点，需要外网与付费 Key。

**修复**：新建最简单的“聊天助手”，画布只保留「知识库检索 → 大语言模型」，删掉搜索/工具节点（见 04 节第 7 条）。

## 8. 概念 FAQ

- **embedding 和对话模型的区别？** embedding 生成向量用于“找相似”，对话模型负责“生成文字”。RAG 两个都要。
- **rerank 要不要上？** 可选精度优化。Ollama 不提供 rerank；10G 机器先别上。追求精度再单独部署 TEI + bge-reranker-v2-m3。
- **向量库为什么用 ES？** RAGFlow 内置默认，教程不需要另装 Weaviate/Infinity；换向量库是平台配置层面的事，原理一样。
- **文档更新了怎么办？** 知识库里重新上传/重新解析对应文件即可，旧切片会被替换。
- **本地 vs 云端 API？** 本教程全本地，数据不出内网，无按量费用；代价是需要一台配置尚可的服务器。
- **Windows 上能跑这些命令吗？** `ip`、`systemctl`、`docker compose` 是 Linux 侧的；Windows 本机只负责浏览器访问与（可选）写代码。