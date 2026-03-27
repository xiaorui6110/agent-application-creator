// @ts-ignore
/* eslint-disable */
import request from '@/request'

export async function deleteAppByAdmin(
  params: API.deleteAppByAdminParams,
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityBoolean>('/app/admin/delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function getAppInfoByIdByAdmin(
  params: API.getAppInfoByIdByAdminParams,
  options?: { [key: string]: any },
) {
  const { appId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityAppVO>(`/app/admin/get/info/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

export async function listAppInfoByPageByAdmin(
  params: API.listAppInfoByPageByAdminParams,
  body: API.AppQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityPageAppVO>('/app/admin/list/page/info', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function updateAppByAdmin(
  params: API.updateAppByAdminParams,
  body: API.AppAdminUpdateInfoRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityBoolean>('/app/admin/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function createApp(
  params: API.createAppParams,
  body: API.AppCreateRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityString>('/app/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function listTemplates(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityListAppTemplateVO>('/app/template/list', {
    method: 'GET',
    ...(options || {}),
  })
}

export async function createTemplate(
  params: API.createTemplateParams,
  body: API.AppTemplateCreateRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityAppTemplateVO>('/app/template/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function createAppFromTemplate(
  params: API.createAppFromTemplateParams,
  body: API.AppTemplateUseRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityString>('/app/template/use', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function deleteApp(
  params: API.deleteAppParams,
  body: API.DeleteRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityBoolean>('/app/delete', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function deployApp(
  params: API.deployAppParams,
  body: API.AppDeployRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityString>('/app/deploy', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function downloadAppCode(
  params: API.downloadAppCodeParams,
  options?: { [key: string]: any },
) {
  const { appId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityBoolean>(`/app/download/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

export async function getAppInfoById(
  params: API.getAppInfoByIdParams,
  options?: { [key: string]: any },
) {
  const { appId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityAppVO>(`/app/get/info/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

export async function listAppCategories(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityListString>('/app/category/list', {
    method: 'GET',
    ...(options || {}),
  })
}

export async function listGoodAppInfoByPage(
  params: API.listGoodAppInfoByPageParams,
  body: API.AppQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityPageAppVO>('/app/good/list/page/info', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function listAppInfoByPage(
  params: API.listAppInfoByPageParams,
  body: API.AppQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityPageAppVO>('/app/list/page/info', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function updateApp(
  params: API.updateAppParams,
  body: API.AppUpdateInfoRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityBoolean>('/app/update', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}

export async function listAppVersions(
  params: API.listAppVersionsParams,
  options?: { [key: string]: any },
) {
  const { appId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityListAppVersionVO>(`/app/version/list/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

export async function restoreAppVersion(
  params: API.restoreAppVersionParams,
  body: API.AppVersionRestoreRequest,
  options?: { [key: string]: any },
) {
  return request<API.ServerResponseEntityBoolean>('/app/version/restore', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    params: { ...params },
    data: body,
    ...(options || {}),
  })
}
