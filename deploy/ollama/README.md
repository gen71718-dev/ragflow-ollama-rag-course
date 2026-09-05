# Ollama 部署速记

```bash
docker run -d \
  --name ollama \
  --restart unless-stopped \
  -v ollama:/root/.ollama \
  -p 11434:11434 \
  ollama/ollama

docker exec ollama ollama pull qwen2.5:7b   # 对话模型（生成回答）
docker exec ollama ollama pull bge-m3       # embedding 模型（向量化）

# 验证
curl http://<SERVER_IP>:11434/api/tags
```

> 详细说明见 docs/03-ollama-models.md