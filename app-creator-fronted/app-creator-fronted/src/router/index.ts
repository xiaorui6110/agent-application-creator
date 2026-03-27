import { createRouter, createWebHistory } from 'vue-router'
import HomePage from '@/pages/HomePage.vue'
import UserLoginPage from '@/pages/user/UserLoginPage.vue'
import UserRegisterPage from '@/pages/user/UserRegisterPage.vue'
import UserResetPasswordPage from '@/pages/user/UserResetPasswordPage.vue'
import UserSettingsPage from '@/pages/user/UserSettingsPage.vue'
import UserCommunityPage from '@/pages/user/UserCommunityPage.vue'
import AdminOverviewPage from '@/pages/admin/AdminOverviewPage.vue'
import UserManagePage from '@/pages/admin/UserManagePage.vue'
import AppManagePage from '@/pages/admin/AppManagePage.vue'
import TaskManagePage from '@/pages/admin/TaskManagePage.vue'
import AdminObservabilityPage from '@/pages/admin/AdminObservabilityPage.vue'
import AppChatPage from '@/pages/app/AppChatPage.vue'
import AppEditPage from '@/pages/app/AppEditPage.vue'
import ChatManagePage from '@/pages/admin/ChatManagePage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '首页',
      component: HomePage,
    },
    {
      path: '/user/login',
      name: '用户登录',
      component: UserLoginPage,
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: UserRegisterPage,
    },
    {
      path: '/user/reset-password',
      name: '重置密码',
      component: UserResetPasswordPage,
    },
    {
      path: '/user/settings',
      name: '账号设置',
      component: UserSettingsPage,
    },
    {
      path: '/user/community',
      name: '社区中心',
      component: UserCommunityPage,
    },
    {
      path: '/admin/overview',
      name: '运营概览',
      component: AdminOverviewPage,
    },
    {
      path: '/admin/userManage',
      name: '用户管理',
      component: UserManagePage,
    },
    {
      path: '/admin/appManage',
      name: '应用管理',
      component: AppManagePage,
    },
    {
      path: '/admin/chatManage',
      name: '对话管理',
      component: ChatManagePage,
    },
    {
      path: '/admin/taskManage',
      name: '任务监控',
      component: TaskManagePage,
    },
    {
      path: '/admin/observability',
      name: '可观测性面板',
      component: AdminObservabilityPage,
    },
    {
      path: '/app/chat/:id',
      name: '应用对话',
      component: AppChatPage,
    },
    {
      path: '/app/edit/:id',
      name: '编辑应用',
      component: AppEditPage,
    },
  ],
})

export default router
