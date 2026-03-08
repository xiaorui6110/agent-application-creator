// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** 封禁或解封用户 管理员封禁或解封用户 POST /user/ban */
export async function banOrUnbanUser(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.banOrUnbanUserParams,
  body: API.UserUnbanRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/user/ban', {
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

/** 修改用户邮箱 修改当前用户邮箱 POST /user/change/email */
export async function changUserEmail(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.changUserEmailParams,
  body: API.UserChangeEmailRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/user/change/email', {
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

/** 修改用户密码 修改当前用户密码 POST /user/change/password */
export async function changeUserPassword(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.changeUserPasswordParams,
  body: API.UserChangePasswordRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/user/change/password', {
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

/** 删除用户 管理员删除指定用户 POST /user/delete */
export async function deleteUser(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteUserParams,
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/user/delete', {
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

/** 批量删除用户 管理员批量删除用户 POST /user/delete/batch */
export async function deleteBatchUser(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.deleteBatchUserParams,
  body: API.DeleteRequest[],
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/user/delete/batch', {
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

/** 根据 id 获取用户信息 管理员根据 id 获取用户信息 GET /user/get/${param0} */
export async function getUserById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserByIdParams,
  options?: { [key: string]: any }
) {
  const { userId: param0, ...queryParams } = params
  return request<API.ServerResponseEntityUser>(`/user/get/${param0}`, {
    method: 'GET',
    params: { ...queryParams },
    ...(options || {}),
  })
}

/** 获取用户信息 获取当前用户信息 GET /user/getInfo */
export async function getUserInfo(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityUserVO>('/user/getInfo', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 用户查询 用户根据用户id、昵称，分页获取查询用户信息 POST /user/getInfoList */
export async function getUserInfoByIdOrName(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getUserInfoByIdOrNameParams,
  body: API.UserQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageUserVO>('/user/getInfoList', {
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

/** 获取图形验证码 获取图形验证码 GET /user/getPictureVerifyCode */
export async function getPictureVerifyCode(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityMapStringString>('/user/getPictureVerifyCode', {
    method: 'GET',
    ...(options || {}),
  })
}

/** 分页查询用户信息 管理员分页查询用户信息 POST /user/list/page/info */
export async function listUserInfoByPage(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.listUserInfoByPageParams,
  body: API.UserQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityPageUserVO>('/user/list/page/info', {
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

/** 用户登录 用户使用邮箱 + 密码登录 POST /user/login */
export async function userLogin(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.userLoginParams,
  body: API.UserLoginRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityUserVO>('/user/login', {
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

/** 用户登出 当前用户登出 POST /user/logout */
export async function userLogout(options?: { [key: string]: any }) {
  return request<API.ServerResponseEntityBoolean>('/user/logout', {
    method: 'POST',
    ...(options || {}),
  })
}

/** 用户注册 用户使用邮箱验证码注册 POST /user/register */
export async function userRegister(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.userRegisterParams,
  body: API.UserRegisterRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityString>('/user/register', {
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

/** 重置用户密码 重置当前用户密码 POST /user/reset/password */
export async function resetUserPassword(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.resetUserPasswordParams,
  body: API.UserResetPasswordRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/user/reset/password', {
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

/** 发送邮箱验证码 向目标邮箱发送验证码 POST /user/sendEmailCode */
export async function sendEmailCode(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.sendEmailCodeParams,
  body: API.UserSendEmailCodeRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/user/sendEmailCode', {
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

/** 更新用户头像 更新当前用户头像 POST /user/update/avatar */
export async function updateUserAvatar(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateUserAvatarParams,
  body: {},
  options?: { [key: string]: any }
) {
  const file = (body as any)?.multipartFile ?? body
  const formData = new FormData()
  formData.append('multipartFile', file)
  return request<API.ServerResponseEntityString>('/user/update/avatar', {
    method: 'POST',
    params: {
      ...params,
    },
    data: formData,
    ...(options || {}),
  })
}

/** 更新用户信息 更新当前用户信息 POST /user/update/info */
export async function updateUserInfo(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.updateUserInfoParams,
  body: API.UserUpdateInfoRequest,
  options?: { [key: string]: any }
) {
  return request<API.ServerResponseEntityBoolean>('/user/update/info', {
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
