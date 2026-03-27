// @ts-ignore
/* eslint-disable */
import request from '@/request'

export async function getAdminDashboardStats(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityAdminDashboardStatsVO>('/admin/dashboard/stats', {
    method: 'GET',
    ...(options || {}),
  })
}
