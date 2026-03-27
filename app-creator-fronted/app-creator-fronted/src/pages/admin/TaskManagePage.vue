<template>
  <div id="taskManagePage">
    <div class="stats-grid">
      <div v-for="card in statsCards" :key="card.key" class="stats-card">
        <div class="stats-label">{{ card.label }}</div>
        <div class="stats-value">{{ card.value }}</div>
      </div>
    </div>

    <a-card class="page-card" :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="任务 ID">
          <a-input v-model:value="searchParams.taskId" placeholder="输入任务 ID" allow-clear />
        </a-form-item>
        <a-form-item label="应用 ID">
          <a-input v-model:value="searchParams.appId" placeholder="输入应用 ID" allow-clear />
        </a-form-item>
        <a-form-item label="线程 ID">
          <a-input v-model:value="searchParams.threadId" placeholder="输入线程 ID" allow-clear />
        </a-form-item>
        <a-form-item label="任务状态">
          <a-select
            v-model:value="searchParams.taskStatus"
            placeholder="选择任务状态"
            style="width: 160px"
            allow-clear
          >
            <a-select-option v-for="status in TASK_STATUS_OPTIONS" :key="status" :value="status">
              {{ status }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">搜索</a-button>
            <a-button @click="resetSearch">重置</a-button>
            <a-button :loading="loading" @click="fetchTasks">刷新</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="taskId"
        :columns="columns"
        :data-source="pagedData"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1500 }"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'taskStatus'">
            <a-tag :color="getTaskStatusColor(record.taskStatus)">
              {{ normalizeTaskStatus(record.taskStatus) }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'taskError'">
            <a-tooltip :title="record.taskError || '-'">
              <div class="ellipsis-text error-text">{{ record.taskError || '-' }}</div>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'taskResult'">
            <a-tooltip :title="record.taskResult?.reply || record.taskResult?.intentSummary || '-'">
              <div class="ellipsis-text">
                {{ record.taskResult?.reply || record.taskResult?.intentSummary || '-' }}
              </div>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'nextRetryTime'">
            <span>{{ record.nextRetryTime ? formatTime(record.nextRetryTime) : '-' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            <span>{{ record.createTime ? formatTime(record.createTime) : '-' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'updateTime'">
            <span>{{ record.updateTime ? formatTime(record.updateTime) : '-' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <a-button size="small" @click="openTaskDetail(record)">详情</a-button>
              <a-button
                v-if="isRetryable(record)"
                size="small"
                type="primary"
                ghost
                :loading="retryingTaskId === record.taskId"
                @click="handleRetry(record)"
              >
                重试
              </a-button>
              <a-button v-if="record.appId" size="small" @click="viewApp(record.appId)">
                查看应用
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="detailVisible"
      title="任务详情"
      width="860px"
      :footer="null"
      destroy-on-close
    >
      <template v-if="selectedTask">
        <a-descriptions bordered :column="2" size="small">
          <a-descriptions-item label="任务 ID">{{ selectedTask.taskId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getTaskStatusColor(selectedTask.taskStatus)">
              {{ normalizeTaskStatus(selectedTask.taskStatus) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="应用 ID">{{ selectedTask.appId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="线程 ID">{{ selectedTask.threadId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="失败类型">{{ selectedTask.failType || '-' }}</a-descriptions-item>
          <a-descriptions-item label="重试次数">{{ selectedTask.retryCount ?? 0 }}</a-descriptions-item>
          <a-descriptions-item label="下次重试">
            {{ selectedTask.nextRetryTime ? formatTime(selectedTask.nextRetryTime) : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ selectedTask.createTime ? formatTime(selectedTask.createTime) : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ selectedTask.updateTime ? formatTime(selectedTask.updateTime) : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="错误信息" :span="2">
            <pre class="detail-block">{{ selectedTask.taskError || '-' }}</pre>
          </a-descriptions-item>
          <a-descriptions-item label="任务结果" :span="2">
            <pre class="detail-block">{{ formatTaskResult(selectedTask.taskResult) }}</pre>
          </a-descriptions-item>
        </a-descriptions>
      </template>
    </a-modal>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { list, retryTask } from '@/api/agentTaskController'
import { formatTime } from '@/utils/time'

const router = useRouter()

const TASK_STATUS_OPTIONS = ['WAITING', 'RUNNING', 'RETRY_WAITING', 'SUCCEEDED', 'FAILED']

const columns = [
  { title: '任务 ID', dataIndex: 'taskId', width: 220, fixed: 'left' },
  { title: '应用 ID', dataIndex: 'appId', width: 160 },
  { title: '线程 ID', dataIndex: 'threadId', width: 220 },
  { title: '状态', dataIndex: 'taskStatus', width: 120 },
  { title: '重试次数', dataIndex: 'retryCount', width: 100 },
  { title: '失败类型', dataIndex: 'failType', width: 120 },
  { title: '错误信息', dataIndex: 'taskError', width: 260 },
  { title: '任务结果', dataIndex: 'taskResult', width: 260 },
  { title: '下次重试', dataIndex: 'nextRetryTime', width: 180 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '更新时间', dataIndex: 'updateTime', width: 180 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' },
]

const loading = ref(false)
const retryingTaskId = ref<string>()
const taskList = ref<API.AgentTask[]>([])
const detailVisible = ref(false)
const selectedTask = ref<API.AgentTask>()

const searchParams = reactive({
  current: 1,
  pageSize: 10,
  taskId: '',
  appId: '',
  threadId: '',
  taskStatus: undefined as string | undefined,
})

const normalizeTaskStatus = (status?: string) => {
  const upper = String(status ?? '').trim().toUpperCase()
  if (!upper || upper === 'INIT' || upper === 'QUEUED') {
    return 'WAITING'
  }
  return upper
}

const getTaskStatusColor = (status?: string) => {
  switch (normalizeTaskStatus(status)) {
    case 'SUCCEEDED':
      return 'green'
    case 'FAILED':
      return 'red'
    case 'RETRY_WAITING':
      return 'orange'
    case 'RUNNING':
      return 'blue'
    default:
      return 'default'
  }
}

const isRetryable = (task: API.AgentTask) => {
  const status = normalizeTaskStatus(task.taskStatus)
  return status === 'FAILED' || status === 'RETRY_WAITING'
}

const filteredData = computed(() => {
  return taskList.value.filter((task) => {
    const matchesTaskId =
      !searchParams.taskId || String(task.taskId ?? '').includes(searchParams.taskId.trim())
    const matchesAppId =
      !searchParams.appId || String(task.appId ?? '').includes(searchParams.appId.trim())
    const matchesThreadId =
      !searchParams.threadId || String(task.threadId ?? '').includes(searchParams.threadId.trim())
    const matchesStatus =
      !searchParams.taskStatus || normalizeTaskStatus(task.taskStatus) === searchParams.taskStatus
    return matchesTaskId && matchesAppId && matchesThreadId && matchesStatus
  })
})

const pagedData = computed(() => {
  const current = Math.max(searchParams.current, 1)
  const pageSize = Math.max(searchParams.pageSize, 1)
  const start = (current - 1) * pageSize
  return filteredData.value.slice(start, start + pageSize)
})

const todayTaskCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return taskList.value.filter((task) => String(task.createTime ?? '').startsWith(today)).length
})

const successCount = computed(() => {
  return taskList.value.filter((task) => normalizeTaskStatus(task.taskStatus) === 'SUCCEEDED').length
})

const failedCount = computed(() => {
  return taskList.value.filter((task) => normalizeTaskStatus(task.taskStatus) === 'FAILED').length
})

const runningCount = computed(() => {
  return taskList.value.filter((task) => normalizeTaskStatus(task.taskStatus) === 'RUNNING').length
})

const waitingCount = computed(() => {
  return taskList.value.filter((task) => {
    const status = normalizeTaskStatus(task.taskStatus)
    return status === 'WAITING' || status === 'RETRY_WAITING'
  }).length
})

const successRate = computed(() => {
  const total = taskList.value.length
  if (!total) return '0%'
  return `${((successCount.value / total) * 100).toFixed(1)}%`
})

const failureRate = computed(() => {
  const total = taskList.value.length
  if (!total) return '0%'
  return `${((failedCount.value / total) * 100).toFixed(1)}%`
})

const statsCards = computed(() => [
  { key: 'total', label: '任务总数', value: taskList.value.length },
  { key: 'today', label: '今日任务', value: todayTaskCount.value },
  { key: 'running', label: '运行中', value: runningCount.value },
  { key: 'waiting', label: '等待中', value: waitingCount.value },
  { key: 'successRate', label: '成功率', value: successRate.value },
  { key: 'failureRate', label: '失败率', value: failureRate.value },
])

const pagination = computed(() => ({
  current: searchParams.current,
  pageSize: searchParams.pageSize,
  total: filteredData.value.length,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条`,
}))

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await list({})
    const records = Array.isArray(res.data) ? res.data : []
    taskList.value = records.slice().sort((a: API.AgentTask, b: API.AgentTask) => {
      return String(b.createTime ?? '').localeCompare(String(a.createTime ?? ''))
    })
  } catch (error) {
    console.error('获取任务列表失败', error)
    message.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

const doSearch = () => {
  searchParams.current = 1
}

const resetSearch = () => {
  searchParams.taskId = ''
  searchParams.appId = ''
  searchParams.threadId = ''
  searchParams.taskStatus = undefined
  searchParams.current = 1
}

const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
}

const openTaskDetail = (task: API.AgentTask) => {
  selectedTask.value = task
  detailVisible.value = true
}

const formatTaskResult = (taskResult?: API.AgentResponse) => {
  if (!taskResult) return '-'
  try {
    return JSON.stringify(taskResult, null, 2)
  } catch {
    return String(taskResult)
  }
}

const handleRetry = async (task: API.AgentTask) => {
  if (!task.taskId) return
  retryingTaskId.value = task.taskId
  try {
    const res = await retryTask({ taskId: task.taskId })
    if (res.data?.success || res.data?.code === '0') {
      message.success('任务已重新加入执行队列')
      await fetchTasks()
      return
    }
    message.error(res.data?.msg ?? '任务重试失败')
  } catch (error) {
    console.error('任务重试失败', error)
    message.error('任务重试失败')
  } finally {
    retryingTaskId.value = undefined
  }
}

const viewApp = (appId?: string) => {
  if (!appId) return
  router.push(`/app/chat/${appId}`)
}

onMounted(() => {
  fetchTasks()
})
</script>

<style scoped>
#taskManagePage {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
}

.stats-card {
  padding: 18px 20px;
  border-radius: 14px;
  background: linear-gradient(135deg, #f7fafc 0%, #eef3f8 100%);
  border: 1px solid #e7edf4;
}

.stats-label {
  font-size: 13px;
  color: #5b6573;
  margin-bottom: 8px;
}

.stats-value {
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
  color: #1f2937;
}

.page-card {
  border-radius: 16px;
}

.ellipsis-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.error-text {
  color: #b91c1c;
}

.detail-block {
  margin: 0;
  max-height: 280px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px;
}
</style>
