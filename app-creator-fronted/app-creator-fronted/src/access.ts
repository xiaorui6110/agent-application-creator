import { message } from 'ant-design-vue'
import router from '@/router'
import { useLoginUserStore } from '@/stores/loginUser'

let firstFetchLoginUser = true

const PUBLIC_PATHS = new Set([
  '/',
  '/user/login',
  '/user/register',
  '/user/reset-password',
])

router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()
  let loginUser = loginUserStore.loginUser

  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    loginUser = loginUserStore.loginUser
    firstFetchLoginUser = false
  }

  if (!loginUser?.userId && !PUBLIC_PATHS.has(to.path)) {
    message.warning('请先登录')
    next({
      path: '/user/login',
      query: {
        redirect: to.fullPath,
      },
    })
    return
  }

  if (to.path.startsWith('/admin')) {
    if (!loginUser?.userId || loginUser.userRole !== 'admin') {
      message.error('没有权限')
      next({
        path: loginUser?.userId ? '/' : '/user/login',
        query: !loginUser?.userId
          ? {
              redirect: to.fullPath,
            }
          : undefined,
      })
      return
    }
  }

  next()
})
