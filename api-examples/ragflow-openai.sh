#!/usr/bin/env bash
# RAGFlow「服务 API」调用模板（OpenAI 兼容风格，占位符）。
# 用法: 先在 RAGFlow 对话应用里进入「服务 API / 发布」页，复制界面展示的
#       Base URL / 模型名 / API Key，替换下面三个变量后运行。
#   export RF_BASE_URL="<RAGFlow服务API的Base URL>"
#   export RF_MODEL="<界面显示的模型/应用名>"
#   export RF_API_KEY="<APP_API_KEY>"
#   bash ragflow-openai.sh "XH-200 智能网关最多能接几个传感器？"
set -euo pipefail

RF_BASE_URL="${RF_BASE_URL:?请先 export RF_BASE_URL}"
RF_MODEL="${RF_MODEL:?请先 export RF_MODEL}"
RF_API_KEY="${RF_API_KEY:?请先 export RF_API_KEY}"
QUERY="${1:-你好}"

# 若你的版本是 Dify 风格端点(/v1/chat-messages)，请改用 dify-chat.sh；
# 以下按 RAGFlow 发布页展示的 OpenAI 兼容格式填写。
curl -sS -X POST "${RF_BASE_URL}/v1/chat/completions" \
  -H "Authorization: Bearer $RF_API_KEY" \
  -H "Content-Type: application/json" \
  -d "{\"model\":\"$RF_MODEL\",\"messages\":[{\"role\":\"user\",\"content\":\"$QUERY\"}]}"