// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 添加评论 添加评论 POST /appComment/add */
export async function addComment(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.addCommentParams,
  body: API.AppCommentAddRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/appComment/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取评论历史 获取评论历史 POST /appComment/commented/history */
export async function commentedHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.commentedHistoryParams,
  body: API.AppCommentQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageAppCommentVO>('/appComment/commented/history', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 删除评论 删除评论 POST /appComment/delete */
export async function deleteComment(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteCommentParams,
  body: API.AppCommentDeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/appComment/delete', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 点赞评论 点赞评论 POST /appComment/like */
export async function likeComment(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.likeCommentParams,
  body: API.AppCommentLikeRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/appComment/like', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取我的评论历史 获取我的评论历史 POST /appComment/my/history */
export async function myHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.myHistoryParams,
  body: API.AppCommentQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageAppCommentVO>('/appComment/my/history', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 查询评论 查询评论 POST /appComment/query */
export async function queryComment(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.queryCommentParams,
  body: API.AppCommentQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageAppCommentVO>('/appComment/query', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    params: {
      ...params,
    },
    data: body,
    ...(options || {}),
  })
}

/** 获取未读评论 获取未读评论 GET /appComment/unread */
export async function unreadComment(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityListAppCommentVO>('/appComment/unread', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取未读评论数量 获取未读评论数量 GET /appComment/unread/count */
export async function unreadCommentCount(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityLong>('/appComment/unread/count', {
    method: 'GET',
    ...(options || {}),
  })
}
