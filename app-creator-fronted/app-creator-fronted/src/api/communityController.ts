// @ts-ignore
/* eslint-disable */
import request from '@/request'

export async function getCommunityUnreadSummary(options?: { [key: string]: any }) {
  return request<any>('/community/unread/summary', {
    method: 'GET',
    ...(options || {}),
  })
}

export async function getCommunityUnreadFeed(
  params?: {
    limit?: number
  },
  options?: { [key: string]: any }
) {
  return request<any>('/community/unread/feed', {
    method: 'GET',
    params: {
      ...(params || {}),
    },
    ...(options || {}),
  })
}

export async function clearCommunityUnread(options?: { [key: string]: any }) {
  return request<any>('/community/unread/clear', {
    method: 'POST',
    ...(options || {}),
  })
}
