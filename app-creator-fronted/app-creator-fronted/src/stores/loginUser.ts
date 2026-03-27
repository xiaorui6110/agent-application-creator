import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/userController.ts'
import { isSuccessResponse, isUnauthorizedResponse } from '@/utils/apiResponse'

export const LOGIN_USER_STORAGE_KEY = 'app_creator_login_user'

const loadCachedLoginUser = (): API.UserVO => {
  const raw = localStorage.getItem(LOGIN_USER_STORAGE_KEY)
  if (!raw) {
    return {}
  }
  try {
    return JSON.parse(raw) as API.UserVO
  } catch (error) {
    localStorage.removeItem(LOGIN_USER_STORAGE_KEY)
    return {}
  }
}

const persistLoginUser = (user: API.UserVO) => {
  if (user?.userId) {
    localStorage.setItem(LOGIN_USER_STORAGE_KEY, JSON.stringify(user))
    return
  }
  localStorage.removeItem(LOGIN_USER_STORAGE_KEY)
}

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.UserVO>(loadCachedLoginUser())
  const loginUserFetched = ref(false)

  async function fetchLoginUser() {
    try {
      const res = await getUserInfo({ skipAuthRedirect: true })
      if (isSuccessResponse(res.data) && res.data.data) {
        loginUser.value = res.data.data
        persistLoginUser(loginUser.value)
        loginUserFetched.value = true
        return
      }
      if (isUnauthorizedResponse(res.data)) {
        clearLoginUser()
      }
    } catch (error) {
      clearLoginUser()
    } finally {
      loginUserFetched.value = true
    }
  }

  function setLoginUser(newLoginUser: API.UserVO) {
    loginUser.value = newLoginUser
    loginUserFetched.value = true
    persistLoginUser(newLoginUser)
  }

  function clearLoginUser() {
    loginUser.value = {}
    persistLoginUser({})
  }

  return { loginUser, loginUserFetched, fetchLoginUser, setLoginUser, clearLoginUser }
})
