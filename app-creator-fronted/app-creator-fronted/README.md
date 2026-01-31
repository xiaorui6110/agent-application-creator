# AI 智能体应用生成平台（前端）

基于 Vue 3 + TypeScript + Ant Design Vue 的前端工程。

核心能力：输入一句话创建应用、在对话页驱动生成与预览、部署与下载、管理员管理。

## 快速开始

```bash
npm install
npm run dev
```

## 环境变量

默认后端地址为 `http://localhost:8123/api`。

可在 `.env` 中覆盖：

```bash
VITE_API_BASE_URL=http://localhost:8123/api
VITE_DEPLOY_DOMAIN=http://localhost
```

## 常用脚本

```bash
npm run dev
npm run type-check
npm run build
npm run lint
```

## 功能概览

### 用户侧

- 应用创建：主页输入提示词创建应用
- 应用对话：对话页轮询任务状态并展示回复
- 应用预览：右侧 iframe / srcdoc 预览后端静态资源
- 应用部署：对话页一键部署
- 应用下载：下载生成代码包
- 应用编辑：修改应用名称、查看应用详情

### 管理员侧

- 应用管理：分页查询/编辑/删除/设置精选
- 用户管理：管理员可见

## 页面路由

- `/`：主页（我的作品 + 精选案例）
- `/app/chat/:id`：应用对话页（生成、预览、部署、下载）
- `/app/edit/:id`：应用编辑页
- `/admin/appManage`：应用管理（管理员）
- `/admin/userManage`：用户管理（管理员）

## 代码生成类型（重要）

前端对 `codeGenType` 的约定：

- 仅使用后端/数据库枚举 `value` 小写：`single_file` / `multi_file` / `vue_project`
- 展示层允许兼容后端返回的大写值（例如 `MULTI_FILE`），但不会把大写/UNKNOWN 回传给后端

相关实现：

- [codeGenTypes.ts](src/utils/codeGenTypes.ts)
- [AppChatPage.vue](src/pages/app/AppChatPage.vue)

## 项目结构

```
src/
  api/                  # OpenAPI 生成的接口与类型（typings.d.ts）
  components/           # 通用组件（Header、Card、Modal 等）
  config/               # 环境配置（API_BASE_URL、静态预览地址等）
  pages/                # 页面（Home / app / admin / user）
  stores/               # Pinia stores（loginUser 等）
  utils/                # 工具函数（codeGenTypes、apiResponse、时间格式化等）
```