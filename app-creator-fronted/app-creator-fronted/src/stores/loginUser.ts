import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/userController.ts'
import { isSuccessResponse } from '@/utils/apiResponse'

/**
 * 登录用户信息
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref<API.UserVO>({})

  // 获取登录用户信息
  async function fetchLoginUser() {
    const res = await getUserInfo()
    if (isSuccessResponse(res.data) && res.data.data) {
      loginUser.value = res.data.data
    }
  }

  // 更新登录用户信息
  function setLoginUser(newLoginUser: API.UserVO) {
    loginUser.value = newLoginUser
  }

  function clearLoginUser() {
    loginUser.value = {}
  }

  return { loginUser, fetchLoginUser, setLoginUser, clearLoginUser }
})
