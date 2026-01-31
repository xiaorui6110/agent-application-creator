// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 根据主键获取agent执行任务表 根据主键获取agent执行任务表 GET /agentTask/getInfo/${param0} */
export async function getInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getInfoParams,
  options?: { [key: string]: any }
) {
  const { id: param0, ...queryParams } = params
  return request<API.AgentTask>(`/agentTask/getInfo/${param0}`, {
    method: 'GET',
    params: {
      ...queryParams,
    },
    ...(options || {}),
  })
}

/** 轮询获取任务结果 前端轮询 GET /agentTask/getTask */
export async function getTaskState(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getTaskStateParams,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntitySystemOutput>('/agentTask/getTask', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 查询所有agent执行任务表 查询所有agent执行任务表 GET /agentTask/list */
export async function list(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listParams,
  options?: { [key: string]: any }
) {
  return request<API.AgentTask[]>('/agentTask/list', {
    method: 'GET',
    params: {
      ...params,
    },
    ...(options || {}),
  })
}

/** 分页查询agent执行任务表 分页查询agent执行任务表 GET /agentTask/page */
export async function page(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.pageParams,
  options?: { [key: string]: any }
) {
  return request<API.PageAgentTask>('/agentTask/page', {
    method: 'GET',
    params: {
      ...params,
      page: undefined,
      ...params['page'],
    },
    ...(options || {}),
  })
}

/** 手动重试任务 手动重试任务 POST /agentTask/retry/${param0} */
export async function retryTask(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.retryTaskParams,
  options?: { [key: string]: any }
) {
  const { taskId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityVoid>(`/agentTask/retry/${param0}`, {
    method: 'POST',
    params: { ...queryParams },
    ...(options || {}),
  })
}
