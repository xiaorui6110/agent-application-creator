// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 管理员删除应用 管理员删除应用 POST /app/admin/delete */
export async function deleteAppByAdmin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteAppByAdminParams,
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/app/admin/delete', {
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

/** 管理员根据 id 获取应用详情 管理员根据 id 获取应用详情 GET /app/admin/get/info/${param0} */
export async function getAppInfoByIdByAdmin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppInfoByIdByAdminParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityAppVO>(`/app/admin/get/info/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 管理员分页获取应用列表 管理员分页获取应用列表 POST /app/admin/list/page/info */
export async function listAppInfoByPageByAdmin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listAppInfoByPageByAdminParams,
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageAppVO>('/app/admin/list/page/info', {
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

/** 管理员更新应用 管理员更新应用 POST /app/admin/update */
export async function updateAppByAdmin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateAppByAdminParams,
  body: API.AppAdminUpdateInfoRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/app/admin/update', {
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

/** 应用创建 用户在主页输入提示词 POST /app/create */
export async function createApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.createAppParams,
  body: API.AppCreateRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityString>('/app/create', {
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

/** 删除应用 删除应用 POST /app/delete */
export async function deleteApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteAppParams,
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/app/delete', {
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

/** 应用部署 应用部署 POST /app/deploy */
export async function deployApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deployAppParams,
  body: API.AppDeployRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityString>('/app/deploy', {
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

/** 下载应用代码 下载应用代码 GET /app/download/${param0} */
export async function downloadAppCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.downloadAppCodeParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityBoolean>(`/app/download/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 获取应用信息 获取应用信息 GET /app/get/info/${param0} */
export async function getAppInfoById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAppInfoByIdParams,
  options?: { [key: string]: any }
) {
  const { appId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityAppVO>(`/app/get/info/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 分页获取精选应用列表 分页获取精选应用列表 POST /app/good/list/page/info */
export async function listGoodAppInfoByPage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listGoodAppInfoByPageParams,
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageAppVO>('/app/good/list/page/info', {
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

/** 分页获取应用列表 分页获取应用列表 POST /app/list/page/info */
export async function listAppInfoByPage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listAppInfoByPageParams,
  body: API.AppQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageAppVO>('/app/list/page/info', {
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

/** 应用信息更新 应用信息更新 POST /app/update */
export async function updateApp(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateAppParams,
  body: API.AppUpdateInfoRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/app/update', {
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
