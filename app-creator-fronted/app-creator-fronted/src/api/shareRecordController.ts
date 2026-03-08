// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 分享/取消分享 分享/取消分享 POST /shareRecord/do */
export async function doShare(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.doShareParams,
  body: API.ShareDoRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/shareRecord/do', {
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

/** 获取分享历史 获取分享历史 POST /shareRecord/history */
export async function getUserShareHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserShareHistoryParams,
  body: API.ShareQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageShareRecordVO>('/shareRecord/history', {
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

/** 获取我的分享历史 获取我的分享历史 POST /shareRecord/my/history */
export async function getMyShareHistory(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getMyShareHistoryParams,
  body: API.ShareQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageShareRecordVO>('/shareRecord/my/history', {
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

/** 获取分享状态 获取分享状态 GET /shareRecord/status/${param0} */
export async function getShareStatus(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getShareStatusParams,
  options?: { [key: string]: any }
) {
  const { targetId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityBoolean>(`/shareRecord/status/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 获取未读分享记录 获取未读分享记录 GET /shareRecord/unread */
export async function getUnreadShares(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityListShareRecordVO>('/shareRecord/unread', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 获取未读分享记录数量 获取未读分享记录数量 GET /shareRecord/unread/count */
export async function getUnreadSharesCount(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityLong>('/shareRecord/unread/count', {
    method: 'GET',
    ...(options || {}),
  })
}
