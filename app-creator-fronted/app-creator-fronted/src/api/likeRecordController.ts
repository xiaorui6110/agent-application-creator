// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 点赞/取消点赞 点赞/取消点赞 POST /likeRecord/do */
export async function doLike(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.doLikeParams,
  body: API.LikeDoRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/likeRecord/do', {
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

/** 获取点赞历史 获取点赞历史 POST /likeRecord/history */
export async function getLikeHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLikeHistoryParams,
  body: API.LikeQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageLikeRecordVO>('/likeRecord/history', {
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

/** 获取我的点赞历史 获取我的点赞历史 POST /likeRecord/my/history */
export async function getMyLikeHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getMyLikeHistoryParams,
  body: API.LikeQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageLikeRecordVO>('/likeRecord/my/history', {
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

/** 获取点赞状态 获取点赞状态 GET /likeRecord/status/${param0} */
export async function getLikeStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getLikeStatusParams,
  options?: { [key: string]: any }
) {
  const { targetId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityBoolean>(`/likeRecord/status/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 获取未读点赞记录 获取未读点赞记录 GET /likeRecord/unread */
export async function getUnreadLikes(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityListLikeRecordVO>('/likeRecord/unread', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取未读点赞记录数量 获取未读点赞记录数量 GET /likeRecord/unread/count */
export async function getUnreadLikesCount(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityLong>('/likeRecord/unread/count', {
    method: 'GET',
    ...(options || {}),
  })
}
