<template>
  <div id="appManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchParams.appName" placeholder="输入应用名称" />
      </a-form-item>
      <a-form-item label="生成类型">
        <a-select
          v-model:value="searchParams.codeGenType"
          placeholder="选择生成类型"
          style="width: 150px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option
            v-for="option in CODE_GEN_TYPE_OPTIONS"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />

    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 1200 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'appCover'">
          <a-image v-if="record.appCover" :src="record.appCover" :width="80" :height="60" />
          <div v-else class="no-cover">无封面</div>
        </template>
        <template v-else-if="column.dataIndex === 'appInitPrompt'">
          <a-tooltip :title="record.appInitPrompt">
            <div class="prompt-text">{{ record.appInitPrompt }}</div>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'codeGenType'">
          {{ formatCodeGenType(record.codeGenType) }}
        </template>
        <template v-else-if="column.dataIndex === 'appPriority'">
          <a-tag v-if="record.appPriority === 99" color="gold">精选</a-tag>
          <span v-else>{{ record.appPriority || 0 }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'deployedTime'">
          <span v-if="record.deployedTime">
            {{ formatTime(record.deployedTime) }}
          </span>
          <span v-else class="text-gray">未部署</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.dataIndex === 'userVO'">
          <UserInfo :user="record.userVO" size="small" />
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="viewAppDetail(record.appId)">详情</a-button>
            <a-button type="primary" size="small" @click="editApp(record)"> 编辑 </a-button>
            <a-button
              type="default"
              size="small"
              @click="toggleFeatured(record)"
              :class="{ 'featured-btn': record.appPriority === 99 }"
            >
              {{ record.appPriority === 99 ? '取消精选' : '精选' }}
            </a-button>
            <a-popconfirm title="确定要删除这个应用吗？" @confirm="deleteApp(record.appId)">
              <a-button danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>

    <AppDetailModal
      v-model:open="appDetailVisible"
      :app="selectedAppInfo"
      :show-actions="true"
      @edit="handleEditSelected"
      @delete="handleDeleteSelected"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  listAppInfoByPageByAdmin,
  deleteAppByAdmin,
  updateAppByAdmin,
  getAppInfoByIdByAdmin,
} from '@/api/appController'
import { CODE_GEN_TYPE_OPTIONS, formatCodeGenType } from '@/utils/codeGenTypes'
import { isValidCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import { isSuccessResponse } from '@/utils/apiResponse'
import { resolveTotalCount } from '@/utils/pagination'

const router = useRouter()

const appDetailVisible = ref(false)
const selectedAppInfo = ref<API.AppVO>()

const columns = [
  {
    title: 'ID',
    dataIndex: 'appId',
    width: 80,
    fixed: 'left',
  },
  {
    title: '应用名称',
    dataIndex: 'appName',
    width: 150,
  },
  {
    title: '封面',
    dataIndex: 'appCover',
    width: 100,
  },
  {
    title: '初始提示词',
    dataIndex: 'appInitPrompt',
    width: 200,
  },
  {
    title: '生成类型',
    dataIndex: 'codeGenType',
    width: 100,
  },
  {
    title: '优先级',
    dataIndex: 'appPriority',
    width: 80,
  },
  {
    title: '部署时间',
    dataIndex: 'deployedTime',
    width: 160,
  },
  {
    title: '创建者',
    dataIndex: 'userVO',
    width: 120,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 200,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.AppVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.AppQueryRequest>({
  current: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    if (searchParams.codeGenType && !isValidCodeGenType(searchParams.codeGenType)) {
      searchParams.codeGenType = undefined
    }
    const res = await listAppInfoByPageByAdmin({}, searchParams)
    if (isSuccessResponse(res.data) && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = resolveTotalCount({
        current: searchParams.current ?? 1,
        pageSize: searchParams.pageSize ?? 10,
        totalRow: res.data.data.totalRow,
        recordsLength: data.value.length,
      })
      if (res.data.data.pageNumber) {
        searchParams.current = res.data.data.pageNumber
      }
      return
    }
    message.error(res.data.msg ?? '获取数据失败')
  } catch (error) {
    console.error('获取数据失败：', error)
    message.error('获取数据失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.current ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 编辑应用
const editApp = (app: API.AppVO) => {
  router.push(`/app/edit/${app.appId}`)
}

// 切换精选状态
const toggleFeatured = async (app: API.AppVO) => {
  if (!app.appId) return

  const currentPriority = app.appPriority ?? 0
  const newPriority = currentPriority === 99 ? 0 : 99

  try {
    const res = await updateAppByAdmin({}, { appId: app.appId, appPriority: newPriority })

    if (isSuccessResponse(res.data) && res.data.data !== false) {
      message.success(newPriority === 99 ? '已设为精选' : '已取消精选')
      // 刷新数据
      fetchData()
    } else {
      message.error('操作失败：' + (res.data.msg ?? ''))
    }
  } catch (error) {
    console.error('操作失败：', error)
    message.error('操作失败')
  }
}

// 删除应用
const deleteApp = async (appId?: string) => {
  if (!appId) return

  try {
    const res = await deleteAppByAdmin({}, { id: appId })
    if (isSuccessResponse(res.data) && res.data.data !== false) {
      message.success('删除成功')
      // 刷新数据
      fetchData()
    } else {
      message.error('删除失败：' + (res.data.msg ?? ''))
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

const handleEditSelected = () => {
  if (selectedAppInfo.value?.appId) {
    router.push(`/app/edit/${selectedAppInfo.value.appId}`)
  }
}

const handleDeleteSelected = async () => {
  if (!selectedAppInfo.value?.appId) return
  await deleteApp(selectedAppInfo.value.appId)
  appDetailVisible.value = false
  selectedAppInfo.value = undefined
}

const viewAppDetail = async (appId?: string) => {
  if (!appId) return
  try {
    const res = await getAppInfoByIdByAdmin({ appId })
    if (isSuccessResponse(res.data) && res.data.data) {
      selectedAppInfo.value = res.data.data
      appDetailVisible.value = true
      return
    }
    message.error(res.data.msg ?? '获取应用详情失败')
  } catch (error) {
    console.error('获取应用详情失败：', error)
    message.error('获取应用详情失败')
  }
}
</script>

<style scoped>
#appManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}

.no-cover {
  width: 80px;
  height: 60px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 12px;
  border-radius: 4px;
}

.prompt-text {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-gray {
  color: #999;
}

.featured-btn {
  background: #faad14;
  border-color: #faad14;
  color: white;
}

.featured-btn:hover {
  background: #d48806;
  border-color: #d48806;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
