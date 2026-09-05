# 02 · 环境准备：Linux + Docker

> 目标：拿到一台能装 Docker 的 Linux 服务器，并把它配置成后面所有步骤的“家”。

## 1. 硬件建议

| 项目 | 建议 | 说明 |
|---|---|---|
| CPU | 4 核以上 | qwen2.5:7b 推理主要吃 CPU/GPU |
| 内存 | ≥ 16G | 本教程在 10G 机器实测可跑通，但必须做第 4 节的低内存优化 |
| 磁盘 | ≥ 60G | 两个模型约 6G + 各镜像 + 数据 |
| GPU（可选） | NVIDIA + nvidia-container-toolkit | 有 GPU 更快；无 GPU 纯 CPU 也能跑，慢而已 |

## 2. 安装 Docker

以 CentOS Stream 9 / RHEL 系为例（Ubuntu 请用官方源安装 docker-ce）：

```bash
curl -fsSL https://get.docker.com | bash -s docker --mirror Aliyun
systemctl enable --now docker
docker version        # 验证，能看到 client / server 两段
docker ps             # 能列出空列表即正常
```

## 3. 关闭防火墙对 Docker 的干扰（CentOS/RHEL 实测必做）

现象：Docker 容器起来后端口不通；重启后报 `iptables: No chain/target/match by that name`。
原因：Docker 用 iptables/nftables 管理端口转发，firewalld 重启会清掉 Docker 的规则链。

```bash
systemctl stop firewalld
systemctl disable firewalld
systemctl restart docker
```

> 课程环境直接禁用；生产环境建议按官方文档正确放行端口，而不是关闭防火墙。

## 4. 记录你的服务器内网 IP

```bash
ip addr show | grep inet
```

找到 `ens33`（或 eth0 等）那一行的 `inet 192.168.x.x/24`，记下来，本文档后面统一写成 `<SERVER_IP>`。

> 提示：`ip` 是 Linux 命令，Windows 的 CMD/PowerShell 里没有；如果你在自己的 Windows 电脑上敲，会提示“不是内部或外部命令”。

## 5. 检查资源余量

```bash
free -h      # 内存（total/available）
df -h        # 磁盘
```

## 6. 本节验收清单

- [ ] `docker ps` 正常返回（无报错）
- [ ] 已停用 firewalld 并重启 docker
- [ ] 已记录服务器内网 IP（替换文档里的 `<SERVER_IP>`）