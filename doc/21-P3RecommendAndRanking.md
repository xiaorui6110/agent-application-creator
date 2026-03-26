# P3-4 精选推荐机制、应用排行与分类

这一阶段延续现有人工精选能力，不单独引入复杂推荐系统，而是先做一套可控、可运营的轻量方案。

## 1. 设计原则

- 保留现有 `appPriority` 作为人工精选入口
- 新增 `appCategory` 作为应用分类字段
- 新增 `recommendScore` 作为人工可调推荐分
- 推荐列表优先综合 `recommendScore + appPriority + 互动数据`
- 排行列表支持 `recommend / hot / latest`

## 2. 对外接口

- `GET /app/category/list`
- `POST /app/recommend/list/page`
- `POST /app/rank/list/page`

## 3. 后台运营方式

- 管理员仍可通过 `/app/admin/update` 设置精选优先级
- 管理员可额外设置 `appCategory`
- 管理员可额外设置 `recommendScore`

## 4. 当前实现

- `appPriority >= 99` 视为精选应用
- 推荐榜按 `recommendScore -> appPriority -> viewCount -> likeCount -> shareCount -> createTime`
- 热门榜按 `appPriority -> viewCount -> likeCount -> shareCount -> commentCount -> createTime`
- 最新榜按 `createTime`

## 5. 后续扩展

- 增加自动回写推荐分的定时任务
- 增加分类维度下的推荐榜
- 增加首页运营位配置
- 增加真实推荐策略实验能力
