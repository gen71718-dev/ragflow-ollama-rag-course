#!/usr/bin/env bash
# 一键巡检：服务器上执行，把结果贴给排查用
# 用法: bash check.sh <SERVER_IP>   （SERVER_IP 缺省取 127.0.0.1）
set -uo pipefail
IP="${1:-127.0.0.1}"

echo "===== 1) 内存/磁盘 ====="
free -h; df -h | head -5

echo; echo "===== 2) 容器状态 ====="
docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}' 2>&1

echo; echo "===== 3) Ollama 模型 ====="
curl -s --max-time 5 "http://$IP:11434/api/tags" 2>&1 | head -c 800; echo

echo; echo "===== 4) 平台首页 HTTP 状态 ====="
curl -sI --max-time 8 "http://$IP" 2>&1 | head -5

echo; echo "===== 5) 平台容器日志(尾部15行) ====="
L=$(docker ps --format '{{.Names}}' | grep -E 'ragflow' | head -1)
[ -n "$L" ] && docker logs --tail 15 "$L" 2>&1 || echo "未找到 ragflow 容器"