"""业务系统接入最小示例（Python 标准库，无第三方依赖）。

演示多轮会话：第一次提问拿 conversation_id，第二次自动带上，模型能记住上文。
用法:
    export API_URL="http://<SERVER_IP>/v1/chat-messages"   # Dify 格式(实测通过)
    export API_KEY="<APP_API_KEY>"
    python chat.py "XH-200 智能网关最多能接几个传感器？"
    python chat.py "我刚才问的是哪个型号？"
"""
import json
import os
import sys
import urllib.request

API_URL = os.environ["API_URL"]
API_KEY = os.environ["API_KEY"]
conversation_id = os.environ.get("CONVERSATION_ID", "")


def ask(query: str) -> dict:
    body = json.dumps({
        "inputs": {},
        "query": query,
        "response_mode": "blocking",
        "conversation_id": conversation_id,
        "user": "business-demo",
    }).encode("utf-8")
    req = urllib.request.Request(
        API_URL,
        data=body,
        headers={
            "Authorization": f"Bearer {API_KEY}",
            "Content-Type": "application/json",
        },
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=120) as resp:
        return json.loads(resp.read().decode("utf-8"))


if __name__ == "__main__":
    query = sys.argv[1] if len(sys.argv) > 1 else "XH-200 智能网关最多能接几个传感器？"
    data = ask(query)
    print("回答:", data.get("answer", "(无 answer 字段，请检查返回结构)"))
    print("conversation_id:", data.get("conversation_id", ""))