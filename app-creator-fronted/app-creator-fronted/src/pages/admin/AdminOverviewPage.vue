<template>
  <div id="adminOverviewPage">
    <section class="hero-card">
      <div>
        <div class="hero-eyebrow">后台运营概览</div>
        <h1 class="hero-title">平台运行状态与核心数据</h1>
        <p class="hero-desc">集中查看用户增长、应用产出、对话活跃度和任务执行表现。</p>
      </div>
      <a-button type="primary" :loading="loading" @click="fetchStats">刷新数据</a-button>
    </section>

    <section class="overview-grid">
      <div v-for="card in summaryCards" :key="card.key" class="overview-card">
        <div class="overview-label">{{ card.label }}</div>
        <div class="overview-value">{{ card.value }}</div>
        <div class="overview-extra">{{ card.extra }}</div>
      </div>
    </section>

    <section class="panel-grid">
      <a-card title="用户与应用" :bordered="false" class="panel-card">
        <div class="metric-list">
          <div class="metric-item">
            <span>用户总数</span>
            <strong>{{ stats.totalUserCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>今日新增用户</span>
            <strong>{{ stats.todayRegisterCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>应用总数</span>
            <strong>{{ stats.totalAppCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>今日新增应用</span>
            <strong>{{ stats.todayAppCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>已部署应用</span>
            <strong>{{ stats.deployedAppCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>精选应用</span>
            <strong>{{ stats.featuredAppCount ?? 0 }}</strong>
          </div>
        </div>
        <div class="quick-actions">
          <a-button @click="router.push('/admin/userManage')">用户管理</a-button>
          <a-button @click="router.push('/admin/appManage')">应用管理</a-button>
        </div>
      </a-card>

      <a-card title="对话与任务" :bordered="false" class="panel-card">
        <div class="metric-list">
          <div class="metric-item">
            <span>对话消息总数</span>
            <strong>{{ stats.totalChatCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>今日对话消息</span>
            <strong>{{ stats.todayChatCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>任务总数</span>
            <strong>{{ stats.totalTaskCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>今日任务</span>
            <strong>{{ stats.todayTaskCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>运行中任务</span>
            <strong>{{ stats.runningTaskCount ?? 0 }}</strong>
          </div>
          <div class="metric-item">
            <span>等待中任务</span>
            <strong>{{ stats.waitingTaskCount ?? 0 }}</strong>
          </div>
        </div>
        <div class="quick-actions">
          <a-button @click="router.push('/admin/taskManage')">任务监控</a-button>
          <a-button @click="router.push('/admin/chatManage')">对话管理</a-button>
        </div>
      </a-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAdminDashboardStats } from '@/api/adminDashboardController'
import { isSuccessResponse } from '@/utils/apiResponse'

const router = useRouter()
const loading = ref(false)
const stats = reactive<API.AdminDashboardStatsVO>({})

const successRate = computed(() => {
  const total = Number(stats.totalTaskCount ?? 0)
  if (!total) return '0%'
  return `${((Number(stats.succeededTaskCount ?? 0) / total) * 100).toFixed(1)}%`
})

const failureRate = computed(() => {
  const total = Number(stats.totalTaskCount ?? 0)
  if (!total) return '0%'
  return `${((Number(stats.failedTaskCount ?? 0) / total) * 100).toFixed(1)}%`
})

const deploymentRate = computed(() => {
  const total = Number(stats.totalAppCount ?? 0)
  if (!total) return '0%'
  return `${((Number(stats.deployedAppCount ?? 0) / total) * 100).toFixed(1)}%`
})

const summaryCards = computed(() => [
  {
    key: 'users',
    label: '用户规模',
    value: stats.totalUserCount ?? 0,
    extra: `今日新增 ${stats.todayRegisterCount ?? 0}`,
  },
  {
    key: 'apps',
    label: '应用规模',
    value: stats.totalAppCount ?? 0,
    extra: `今日新增 ${stats.todayAppCount ?? 0}`,
  },
  {
    key: 'deploy',
    label: '部署率',
    value: deploymentRate.value,
    extra: `已部署 ${stats.deployedAppCount ?? 0}`,
  },
  {
    key: 'chat',
    label: '对话消息',
    value: stats.totalChatCount ?? 0,
    extra: `今日消息 ${stats.todayChatCount ?? 0}`,
  },
  {
    key: 'success',
    label: '任务成功率',
    value: successRate.value,
    extra: `成功任务 ${stats.succeededTaskCount ?? 0}`,
  },
  {
    key: 'failure',
    label: '任务失败率',
    value: failureRate.value,
    extra: `失败任务 ${stats.failedTaskCount ?? 0}`,
  },
])

const fetchStats = async () => {
  loading.value = true
  try {
    const res = await getAdminDashboardStats()
    if (isSuccessResponse(res.data) && res.data.data) {
      Object.assign(stats, res.data.data)
      return
    }
    message.error(res.data.msg ?? '获取运营概览失败')
  } catch (error) {
    console.error('获取运营概览失败', error)
    message.error('获取运营概览失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStats()
})
</script>

<style scoped>
#adminOverviewPage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  padding: 24px 28px;
  border-radius: 18px;
  background:
    radial-gradient(circle at top left, rgba(251, 191, 36, 0.18), transparent 36%),
    linear-gradient(135deg, #fffdf7 0%, #f6f9fc 100%);
  border: 1px solid #ece6d6;
}

.hero-eyebrow {
  font-size: 13px;
  color: #8b6f2c;
  margin-bottom: 8px;
}

.hero-title {
  margin: 0 0 8px;
  font-size: 28px;
  color: #1f2937;
}

.hero-desc {
  margin: 0;
  color: #5b6573;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.overview-card {
  padding: 18px 20px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e8edf3;
}

.overview-label {
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 10px;
}

.overview-value {
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
  color: #111827;
}

.overview-extra {
  margin-top: 10px;
  font-size: 12px;
  color: #64748b;
}

.panel-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.panel-card {
  border-radius: 16px;
}

.metric-list {
  display: grid;
  gap: 10px;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #edf2f7;
  color: #475569;
}

.metric-item strong {
  color: #111827;
  font-size: 16px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  margin-top: 18px;
}

@media (max-width: 960px) {
  .panel-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    flex-direction: column;
  }
}
</style>
