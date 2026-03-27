<template>
  <div id="adminObservabilityPage">
    <section class="hero-card">
      <div>
        <div class="hero-eyebrow">可观测性面板</div>
        <h1 class="hero-title">模型调用消耗与稳定性</h1>
        <p class="hero-desc">集中查看模型调用次数、Token 消耗、失败情况和平均耗时。</p>
      </div>
      <a-button type="primary" :loading="loading" @click="refreshData">刷新数据</a-button>
    </section>

    <section class="stats-grid">
      <div v-for="card in statsCards" :key="card.key" class="stats-card">
        <div class="stats-label">{{ card.label }}</div>
        <div class="stats-value">{{ card.value }}</div>
        <div class="stats-extra">{{ card.extra }}</div>
      </div>
    </section>

    <a-card class="page-card" :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="handleSearch">
        <a-form-item label="用户 ID">
          <a-input v-model:value="searchParams.userId" placeholder="输入用户 ID" allow-clear />
        </a-form-item>
        <a-form-item label="应用 ID">
          <a-input v-model:value="searchParams.appId" placeholder="输入应用 ID" allow-clear />
        </a-form-item>
        <a-form-item label="线程 ID">
          <a-input v-model:value="searchParams.threadId" placeholder="输入线程 ID" allow-clear />
        </a-form-item>
        <a-form-item label="Agent">
          <a-input v-model:value="searchParams.agentName" placeholder="输入 Agent 名称" allow-clear />
        </a-form-item>
        <a-form-item label="模型">
          <a-input v-model:value="searchParams.modelName" placeholder="输入模型名称" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchParams.callStatus"
            placeholder="选择状态"
            style="width: 140px"
            allow-clear
          >
            <a-select-option value="SUCCESS">SUCCESS</a-select-option>
            <a-select-option value="FAILED">FAILED</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">搜索</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="modelCallLogId"
        :columns="columns"
        :data-source="records"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1600 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'callStatus'">
            <a-tag :color="record.callStatus === 'SUCCESS' ? 'green' : 'red'">
              {{ record.callStatus || '-' }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'latencyMs'">
            <span>{{ formatLatency(record.latencyMs) }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'errorMessage'">
            <a-tooltip :title="record.errorMessage || '-'">
              <div class="ellipsis-text">{{ record.errorMessage || '-' }}</div>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            <span>{{ record.createTime ? formatTime(record.createTime) : '-' }}</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { getModelCallStats, listModelCallLogByPage } from '@/api/modelCallLogController'
import { isSuccessResponse } from '@/utils/apiResponse'
import { formatTime } from '@/utils/time'

const loading = ref(false)
const stats = reactive<API.ModelCallStatsVO>({})
const records = ref<API.ModelCallLog[]>([])
const total = ref(0)

const searchParams = reactive<API.ModelCallLogQueryRequest>({
  current: 1,
  pageSize: 10,
  userId: '',
  appId: '',
  threadId: '',
  agentName: '',
  modelName: '',
  callStatus: undefined,
  sortField: 'create_time',
  sortOrder: 'descend',
})

const columns = [
  { title: '模型', dataIndex: 'modelName', width: 180, fixed: 'left' },
  { title: 'Agent', dataIndex: 'agentName', width: 180 },
  { title: '用户 ID', dataIndex: 'userId', width: 180 },
  { title: '应用 ID', dataIndex: 'appId', width: 180 },
  { title: '线程 ID', dataIndex: 'threadId', width: 220 },
  { title: '提供方', dataIndex: 'provider', width: 120 },
  { title: '调用类型', dataIndex: 'callType', width: 120 },
  { title: '调用状态', dataIndex: 'callStatus', width: 120 },
  { title: '输入 Token', dataIndex: 'promptTokens', width: 120 },
  { title: '输出 Token', dataIndex: 'completionTokens', width: 120 },
  { title: '总 Token', dataIndex: 'totalTokens', width: 120 },
  { title: '耗时', dataIndex: 'latencyMs', width: 120 },
  { title: '错误信息', dataIndex: 'errorMessage', width: 260 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
]

const successRate = computed(() => {
  const totalCount = Number(stats.totalCallCount ?? 0)
  if (!totalCount) return '0%'
  return `${((Number(stats.successCallCount ?? 0) / totalCount) * 100).toFixed(1)}%`
})

const failureRate = computed(() => {
  const totalCount = Number(stats.totalCallCount ?? 0)
  if (!totalCount) return '0%'
  return `${((Number(stats.failedCallCount ?? 0) / totalCount) * 100).toFixed(1)}%`
})

const statsCards = computed(() => [
  {
    key: 'total',
    label: '模型调用总数',
    value: stats.totalCallCount ?? 0,
    extra: `今日新增 ${stats.todayCallCount ?? 0}`,
  },
  {
    key: 'success',
    label: '成功率',
    value: successRate.value,
    extra: `成功 ${stats.successCallCount ?? 0}`,
  },
  {
    key: 'failure',
    label: '失败率',
    value: failureRate.value,
    extra: `失败 ${stats.failedCallCount ?? 0}`,
  },
  {
    key: 'prompt',
    label: '输入 Token',
    value: stats.totalPromptTokens ?? 0,
    extra: '累计输入消耗',
  },
  {
    key: 'completion',
    label: '输出 Token',
    value: stats.totalCompletionTokens ?? 0,
    extra: '累计输出消耗',
  },
  {
    key: 'latency',
    label: '平均耗时',
    value: formatLatency(stats.avgLatencyMs),
    extra: `总 Token ${stats.totalTokens ?? 0}`,
  },
])

const pagination = computed(() => ({
  current: searchParams.current,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条`,
}))

const formatLatency = (value?: number) => `${Number(value ?? 0)} ms`

const loadStats = async () => {
  const res = await getModelCallStats()
  if (isSuccessResponse(res.data) && res.data.data) {
    Object.assign(stats, res.data.data)
    return
  }
  throw new Error(res.data.msg ?? '获取模型调用统计失败')
}

const loadRecords = async () => {
  const res = await listModelCallLogByPage(searchParams)
  if (isSuccessResponse(res.data) && res.data.data) {
    records.value = res.data.data.records ?? []
    total.value = Number(res.data.data.totalRow ?? 0)
    return
  }
  throw new Error(res.data.msg ?? '获取模型调用记录失败')
}

const refreshData = async () => {
  loading.value = true
  try {
    await Promise.all([loadStats(), loadRecords()])
  } catch (error) {
    console.error('加载模型调用数据失败', error)
    message.error(error instanceof Error ? error.message : '加载模型调用数据失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = async () => {
  searchParams.current = 1
  await refreshData()
}

const resetSearch = async () => {
  Object.assign(searchParams, {
    current: 1,
    pageSize: 10,
    userId: '',
    appId: '',
    threadId: '',
    agentName: '',
    modelName: '',
    callStatus: undefined,
    sortField: 'create_time',
    sortOrder: 'descend',
  })
  await refreshData()
}

const handleTableChange = (page: any, _filters: any, sorter: any) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  searchParams.sortField = sorter?.field || 'create_time'
  searchParams.sortOrder = sorter?.order || 'descend'
  refreshData()
}

onMounted(() => {
  refreshData()
})
</script>

<style scoped>
#adminObservabilityPage {
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
    radial-gradient(circle at top left, rgba(14, 165, 233, 0.16), transparent 34%),
    linear-gradient(135deg, #f8fdff 0%, #f4f8fb 100%);
  border: 1px solid #dbe8ef;
}

.hero-eyebrow {
  margin-bottom: 8px;
  font-size: 13px;
  color: #0f6c8d;
}

.hero-title {
  margin: 0 0 8px;
  font-size: 28px;
  color: #12212e;
}

.hero-desc {
  margin: 0;
  color: #556371;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.stats-card {
  padding: 18px 20px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #e7edf2;
}

.stats-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 10px;
}

.stats-value {
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
  color: #111827;
}

.stats-extra {
  margin-top: 10px;
  font-size: 12px;
  color: #6b7280;
}

.page-card {
  border-radius: 16px;
}

.ellipsis-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 960px) {
  .hero-card {
    flex-direction: column;
  }
}
</style>
