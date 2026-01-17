# vue_project_spec.md

（Vue 3 + Vite Project Generation Spec）

---

## Spec ID

`FRONTEND_VUE_PROJECT_V1`

---

## 适用范围（Scope）

本规范适用于 **VUE_PROJECT 模式** 的前端应用生成：

* 基于 **Vue 3.x + Vite**
* 工程化、多文件结构
* 组件化、路由驱动
* 面向中大型、可扩展的前端应用

---

## 技术栈约束（Tech Stack Constraints）

### 必选技术

* **Vue 3.x**
* **Vite**
* **Vue Router 4.x**
* **Composition API**
* **`<script setup>` 语法糖**

### 禁止项（Hard Prohibitions）

* 状态管理库（Vuex / Pinia 等）
* 类型系统（TypeScript / runtime 类型校验）
* 代码格式化与规范工具（ESLint / Prettier）
* UI 组件库
* CSS 预处理器（SCSS / LESS）
* 后端代码或 API 约定假设

> 目标是：**最小依赖 + 最大可运行性**

---

## 项目初始化规范（Project Initialization）

* 必须使用 **Vite + Vue 3** 创建项目
* 不得自定义端口号
* 必须支持子路径部署

---

## Vite 配置规范（vite.config.js）

### 强制要求

* 必须配置 `base` 路径以支持子路径部署
* 必须配置 `@` 别名指向 `/src`

### 允许配置

* 仅限与构建和路径解析相关的最小配置

### 禁止项

* 不必要的插件
* 与业务逻辑相关的配置

---

## 标准项目结构（Required Project Structure）

```
/
├── index.html
├── package.json
├── vite.config.js
├── src/
│   ├── main.js
│   ├── App.vue
│   ├── router/
│   │   └── index.js
│   ├── pages/
│   ├── components/
│   ├── styles/        # 可选
│   ├── utils/         # 可选
│   └── assets/        # 可选
└── public/            # 可选
```

### 结构原则

* `pages/`：路由页面级组件
* `components/`：可复用 UI / 逻辑组件
* `utils/`：与 UI 无关的通用函数
* 避免层级过深（≤ 3 层）

---

## 组件设计规范（Component Design）

* 必须遵循 **单一职责原则**
* 组件应：

    * 职责清晰
    * 依赖明确
    * 可被复用
* 页面组件（pages）：

    * 允许组合多个子组件
* 基础组件：

    * 不直接依赖路由

---

## 组件实现规范（Implementation Rules）

* 必须使用 `<script setup>`
* 必须使用 Composition API
* 禁止 Options API
* 禁止在组件中混入复杂业务逻辑

### 注释原则

* 避免“逐行解释式”注释
* 仅在关键逻辑处说明 **为什么这样设计**

---

## 路由规范（Routing Rules）

* 必须使用 **Vue Router 4.x**
* 路由配置集中在 `src/router/index.js`
* 路由规则应：

    * 清晰
    * 可读
    * 不嵌套过深

---

## 样式规范（Styling Rules）

* 使用 **原生 CSS**
* 支持响应式布局：

    * 桌面端
    * 平板端
    * 移动端
* 优先使用 Flexbox / Grid
* 样式保持简洁，避免复杂选择器

---

## 代码质量规范（Code Quality）

* 以 **“可运行”** 为第一优先级
* 避免不必要的抽象
* 避免为未来假想需求设计结构
* 保证新手开发者可快速理解项目

---

## 平台偏好（Platform Preferences）

* 简单实现 > 技术炫技
* 清晰结构 > 高度抽象
* 可维护性 > 理论最优解

---

## 反模式（Anti-Patterns）

* 在组件中堆叠大量逻辑
* 页面组件直接承担通用能力
* 为简单应用引入“企业级复杂度”
* 组件职责模糊、边界不清

---
