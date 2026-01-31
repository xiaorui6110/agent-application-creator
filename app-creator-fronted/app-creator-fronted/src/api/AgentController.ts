// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 智能体对话接口 每个用户在 60؜ 秒内最多只能发起 5 次智能体对话请求 POST /agent/chat */
export async function chat(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.chatParams,
  body: API.CallAgentRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityAgentTaskStatus>('/agent/chat', {
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
