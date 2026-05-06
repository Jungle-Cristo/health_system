#!/bin/bash
# 一键启动脚本 - 健康管理系统
# 用法: bash start.sh

echo "=== 健康管理系统启动 ==="

# 清理旧进程
echo "清理旧进程..."
for pid in $(netstat -ano 2>/dev/null | grep -E ":8080 |:5173 " | grep LISTENING | awk '{print $5}'); do
  taskkill -PID $pid -F 2>/dev/null
done
sleep 1

# 启动后端
echo "启动后端 (DeepSeek 模式)..."
cd "$(dirname "$0")/health-management-system"
mvn spring-boot:run -q &
BACKEND_PID=$!

# 等后端就绪
echo "等待后端就绪..."
for i in $(seq 1 20); do
  sleep 1
  if curl -s http://localhost:8080/api/ai/provider/current > /dev/null 2>&1; then
    echo "后端已就绪: $(curl -s http://localhost:8080/api/ai/provider/current)"
    break
  fi
done

# 录入测试数据
echo "录入30天健康数据..."
cd "$(dirname "$0")"
node seed-data.js 2>&1 | tail -3

# 启动前端
echo "启动前端..."
cd health-management-web
npx vite --port 5173 &
FRONTEND_PID=$!

echo ""
echo "=== 启动完成 ==="
echo "前端: http://localhost:5173"
echo "后端: http://localhost:8080/api"
echo "测试账号: testuser / Test123456"
echo ""
echo "按 Ctrl+C 停止所有服务"
wait
