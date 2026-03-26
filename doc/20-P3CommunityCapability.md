# P3-3 更完整的社区能力

这一阶段不再重复建设评论、点赞、分享三套独立 CRUD，而是把现有互动能力收口成统一的社区消息中心。

## 1. 能力目标

- 保留现有评论、点赞、分享接口
- 提供统一的未读消息汇总能力
- 提供统一的未读消息列表能力
- 提供统一的全部已读清理能力
- 修正评论通知和互动历史中的错误查询条件

## 2. 对外接口

- `GET /community/unread/summary`
- `GET /community/unread/feed`
- `POST /community/unread/clear`

## 3. 当前实现

- 评论未读基于 `xr_app_comment.is_read`
- 点赞未读基于 `xr_like_record.is_read`
- 分享未读基于 `xr_share_record.is_read`
- 社区消息中心不新增数据库表，而是聚合现有三类消息

## 4. 本次修正

- 修正评论未读查询应按 `app_user_id` 聚合，而不是错误按 `user_id`
- 修正“收到的评论历史”和“我发出的评论历史”查询方向
- 修正点赞控制器 `/history` 与 `/my/history` 的语义映射

## 5. 后续扩展

- 增加分页型社区消息中心
- 支持按消息类型筛选
- 增加评论回复提醒
- 增加站内信或消息面板 UI
