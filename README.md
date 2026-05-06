# 健康管理系统

基于 Vue 3 + Spring Boot 的个人健康数据管理平台，集成 DeepSeek AI 智能健康助手。

## 技术栈

| 前端 | 后端 |
|------|------|
| Vue 3 + TypeScript | Spring Boot 3 |
| Vite 5 | Spring Security + JWT |
| Element Plus | Spring Data JPA + H2 |
| Tailwind CSS | DeepSeek / OpenAI / 文心一言 |
| ECharts + GSAP | Swagger/OpenAPI |

## 快速启动

```bash
# 1. 启动后端（默认 DeepSeek 模式）
cd health-management-system
export DEEPSEEK_API_KEY=sk-your-key
mvn spring-boot:run

# 2. 录入30天测试数据
node seed-data.js

# 3. 启动前端
cd health-management-web
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`，使用 `testuser` / `Test123456` 登录。

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `DEEPSEEK_API_KEY` | DeepSeek API Key | 空（降级到 Mock 模式） |
| `OPENAI_API_KEY` | OpenAI API Key | 空 |
| `WENXIN_API_KEY` | 文心一言 API Key | 空 |
| `AI_DEFAULT_PROVIDER` | 默认 AI 提供商 | `deepseek` |
| `JWT_SECRET` | JWT 签名密钥 | 内置开发密钥 |

## API 端点

### 认证 `/api/auth`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/auth/register` | 用户注册 |
| POST | `/auth/login` | 用户登录 |
| POST | `/auth/logout` | 退出登录 |

### 健康数据 `/api/health`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/health/data` | 录入健康数据 |
| GET | `/health/data?type=&startDate=&endDate=` | 查询数据 |
| GET | `/health/trend?type=&period=` | 趋势数据 |
| DELETE | `/health/data/{id}` | 删除记录 |

### AI 助手 `/api/ai`
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/ai/chat` | 发送消息 |
| GET | `/ai/history` | 聊天历史 |
| DELETE | `/ai/history` | 清空历史 |
| GET | `/ai/provider/current` | 当前提供商 |
| GET | `/ai/provider/available` | 可用提供商列表 |
| POST | `/ai/provider/switch` | 切换提供商 |

## 健康数据类型

| 类型 | 单位 | 正常范围 |
|------|------|----------|
| 步数 (steps) | 步 | 5000-12000 |
| 心率 (heart_rate) | bpm | 60-100 |
| 睡眠 (sleep) | 小时 | 6-9 |
| 体重 (weight) | kg | - |
| 血压 (blood_pressure) | mmHg | 90-140 |
| 血糖 (blood_sugar) | mmol/L | 3.9-7.0 |

## 项目结构

```
├── seed-data.js                  # 数据录入脚本
├── health-management-system/     # Spring Boot 后端
│   └── src/main/java/com/health/
│       ├── ai/                   # AI 服务适配器
│       │   └── impl/             # OpenAI / DeepSeek / 文心一言 / Mock
│       ├── config/               # 安全配置、缓存配置
│       ├── controller/           # REST 控制器
│       ├── entity/               # JPA 实体
│       ├── service/              # 业务服务
│       └── utils/                # JWT、缓存工具
└── health-management-web/        # Vue 3 前端
    └── src/
        ├── api/                  # API 客户端
        ├── components/           # 组件（ai/、common/、health/）
        ├── config/               # 健康指标配置
        ├── views/                # 页面
        └── router/               # 路由
```

## AI 健康助手

AI 助手会自动读取用户过去 30 天的健康数据（步数、心率、睡眠、体重、血压、血糖），在对话中作为上下文提供给 AI 模型，从而实现基于真实健康指标的个性化分析和建议。

支持三种 AI 提供商切换，无 API Key 时自动使用 Mock 模式。
