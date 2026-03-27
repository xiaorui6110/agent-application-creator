import axios from 'axios'
import { message } from 'ant-design-vue'
import { API_BASE_URL } from '@/config/env'
import { isUnauthorizedResponse } from '@/utils/apiResponse'
import { LOGIN_USER_STORAGE_KEY, useLoginUserStore } from '@/stores/loginUser'
import { pinia } from '@/stores/pinia'

const clearAuthState = () => {
  localStorage.removeItem(LOGIN_USER_STORAGE_KEY)
  useLoginUserStore(pinia).clearLoginUser()
}

const myAxios = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  withCredentials: true,
})

myAxios.interceptors.request.use(
  function (config) {
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

myAxios.interceptors.response.use(
  function (response) {
    const { data, config } = response
    if (isUnauthorizedResponse(data)) {
      clearAuthState()
      if (!(config as Record<string, any>)?.skipAuthRedirect && !window.location.pathname.includes('/user/login')) {
        message.warning('请先登录')
        const redirectPath = `${window.location.pathname}${window.location.search}${window.location.hash}`
        window.location.href = `/user/login?redirect=${encodeURIComponent(redirectPath)}`
      }
    }
    return response
  },
  function (error) {
    return Promise.reject(error)
  },
)

export default myAxios
