// @ts-ignore
/* eslint-disable */
import request from '@/request'

export async function getModelCallStats(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityModelCallStatsVO>('/modelCallLog/admin/stats', {
    method: 'GET',
    ...(options || {}),
  })
}

export async function listModelCallLogByPage(
  body: API.ModelCallLogQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityPageModelCallLog>('/modelCallLog/admin/list/page', {
    method: 'POST',
    data: body,
    ...(options || {}),
  })
}
