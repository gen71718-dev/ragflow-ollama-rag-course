#!/usr/bin/env bash
# Dify 对话 API 调用示例（本教程实测通过）。
# 用法:
#   export API_URL="http://<SERVER_IP>/v1/chat-messages"
#   export API_KEY="<APP_API_KEY>"
#   bash dify-chat.sh "XH-200 智能网关最多能接几个传感器？"
set -euo pipefail

API_URL="${API_URL:?请先 export API_URL，例如 http://<SERVER_IP>/v1/chat-messages}"
API_KEY="${API_KEY:?请先 export API_KEY}"
QUERY="${1:-你好}"
CONVERSATION_ID="${CONVERSATION_ID:-}"   # 第二次提问时把上次返回的 id 填进来即多轮

curl -sS -X POST "$API_URL" \
  -H "Authorization: Bearer $API_KEY" \
  -H "Content-Type: application/json" \
  -d "{\"inputs\":{},\"query\":\"$QUERY\",\"response_mode\":\"blocking\",\"conversation_id\":\"$CONVERSATION_ID\",\"user\":\"course-demo\"}"