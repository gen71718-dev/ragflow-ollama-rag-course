# RAGFlow 部署速记（含 10G 内存机器的必要补丁）

```bash
mkdir -p /software && cd /software
wget -q https://github.com/infiniflow/ragflow/releases/download/v0.27.1/ragflow-0.27.1.tar.gz
tar xf ragflow-0.27.1.tar.gz
cd ragflow-0.27.1/docker
cp .env .env.bak

# 补丁1：ES JVM 堆上限 1G（小内存机器防 OOM，必做）
echo 'ES_JAVA_OPTS=-Xms1g -Xmx1g' >> .env

# 补丁2（可选）：MEM_LIMIT 调小到 2G，数值单位为字节 2*1024^3
# 编辑 .env 中 MEM_LIMIT=2147483648

docker compose up -d
docker ps
```

等待容器 healthy 后访问 `http://<SERVER_IP>`，注册账号。

## 若 ES 反复重启或解析报 es01 连不上

```bash
cd /software/ragflow/ragflow-0.27.1/docker && docker compose up -d es01
docker exec <ragflow容器名> getent hosts es01        # 应能解析出 es01
docker network connect --alias es01 docker_ragflow docker-es01-1   # 别名丢失时补回
```

## firewalld 冲突（CentOS/RHEL）

```bash
systemctl stop firewalld && systemctl disable firewalld && systemctl restart docker
```

> 完整图文步骤见 docs/04-ragflow-mainline.md；故障排查见 docs/08-troubleshooting-faq.md