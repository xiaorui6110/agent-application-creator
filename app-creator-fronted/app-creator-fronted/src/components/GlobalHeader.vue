<template>
  <a-layout-header class="header">
    <a-row :wrap="false" align="middle">
      <a-col flex="260px">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">AI 智能体应用生成平台</h1>
          </div>
        </RouterLink>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          class="nav-menu"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </a-col>
      <a-col>
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.userId" class="header-actions">
            <a-badge :count="unreadCount" :offset="[2, 2]">
              <a-button class="inbox-btn" @click="router.push('/user/community')">
                <BellOutlined />
                社区中心
              </a-button>
            </a-badge>
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                {{ loginUserStore.loginUser.nickName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="router.push('/user/community')">
                    <BellOutlined />
                    社区中心
                  </a-menu-item>
                  <a-menu-item @click="router.push('/user/settings')">
                    <SettingOutlined />
                    账号设置
                  </a-menu-item>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { getCommunityUnreadSummary } from '@/api/communityController'
import {
  BellOutlined,
  HomeOutlined,
  LogoutOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import { isSuccessResponse, isUnauthorizedResponse } from '@/utils/apiResponse'

const loginUserStore = useLoginUserStore()
const router = useRouter()
const selectedKeys = ref<string[]>(['/'])
const unreadCount = ref(0)
let pollTimer: number | undefined

router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '首页',
    title: '首页',
  },
  {
    key: '/user/community',
    label: '社区中心',
    title: '社区中心',
  },
  {
    key: '/admin/overview',
    label: '运营概览',
    title: '运营概览',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  },
  {
    key: '/admin/taskManage',
    label: '任务监控',
    title: '任务监控',
  },
  {
    key: '/admin/observability',
    label: '可观测性面板',
    title: '可观测性面板',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://www.promptingguide.ai/zh', target: '_blank' }, '提示工程指南'),
    title: '提示工程指南',
  },
  {
    key: 'github',
    label: h('a', { href: 'https://github.com/xiaorui6110/agent-application-creator', target: '_blank' }, 'GitHub'),
    title: 'GitHub',
  },
]

const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  if (key.startsWith('/')) {
    if (!loginUserStore.loginUser.userId && key !== '/') {
      router.push({
        path: '/user/login',
        query: {
          redirect: key,
        },
      })
      return
    }
    router.push(key)
  }
}

const loadUnreadSummary = async () => {
  if (!loginUserStore.loginUser.userId) {
    unreadCount.value = 0
    return
  }
  const res = await getCommunityUnreadSummary()
  if (isSuccessResponse(res.data) && res.data.data) {
    unreadCount.value = Number(res.data.data.totalUnreadCount ?? 0)
  }
}

const doLogout = async () => {
  const res = await userLogout()
  if ((isSuccessResponse(res.data) && res.data.data !== false) || isUnauthorizedResponse(res.data)) {
    loginUserStore.clearLoginUser()
    unreadCount.value = 0
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + (res.data.msg ?? ''))
  }
}

onMounted(async () => {
  await loadUnreadSummary()
  pollTimer = window.setInterval(loadUnreadSummary, 30000)
})

watch(
  () => loginUserStore.loginUser.userId,
  () => {
    loadUnreadSummary()
  },
  { immediate: false }
)

onUnmounted(() => {
  if (pollTimer) {
    window.clearInterval(pollTimer)
  }
})
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.78);
  padding: 0 32px;
  border-bottom: 1px solid rgba(17, 24, 39, 0.10);
  backdrop-filter: blur(14px);
  position: relative;
  overflow: hidden;
}

.header::before {
  content: '';
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(17, 24, 39, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(17, 24, 39, 0.04) 1px, transparent 1px);
  background-size: 120px 120px;
  pointer-events: none;
  transform: translateY(-10px) rotate(-0.15deg);
}

.header::after {
  content: '';
  position: absolute;
  inset: 0;
  background-image: url("data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20width='140'%20height='140'%20viewBox='0%200%20140%20140'%3E%3Cfilter%20id='n'%3E%3CfeTurbulence%20type='fractalNoise'%20baseFrequency='.9'%20numOctaves='2'%20stitchTiles='stitch'/%3E%3C/filter%3E%3Crect%20width='140'%20height='140'%20filter='url(%23n)'%20opacity='.05'/%3E%3C/svg%3E");
  background-size: 180px 180px;
  mix-blend-mode: multiply;
  pointer-events: none;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 2px;
}

.logo {
  height: 48px;
  width: 48px;
  transform: translateY(-2px);
  border-radius: 6px;
  object-fit: cover;
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.7) inset,
    0 10px 24px rgba(17, 24, 39, 0.08);
}

.site-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.1;
  color: var(--brand);
}

.nav-menu {
  margin-left: 14px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.inbox-btn {
  border-radius: 999px;
}

:deep(.ant-menu) {
  background: transparent;
}

:deep(.ant-menu-item),
:deep(.ant-menu-submenu-title) {
  color: rgba(17, 24, 39, 0.78) !important;
}

:deep(.ant-menu-item-selected) {
  color: var(--brand) !important;
}

:deep(.ant-menu-horizontal > .ant-menu-item::after),
:deep(.ant-menu-horizontal > .ant-menu-submenu::after) {
  border-bottom: 2px solid transparent;
}

:deep(.ant-menu-horizontal > .ant-menu-item-selected::after),
:deep(.ant-menu-horizontal > .ant-menu-submenu-selected::after) {
  border-bottom-color: var(--brand) !important;
}

:deep(.ant-menu-horizontal > .ant-menu-item::after) {
  border-bottom-color: var(--brand) !important;
}

.ant-menu-horizontal {
  border-bottom: none !important;
}
</style>
