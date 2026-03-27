<template>
  <div id="chatManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="消息内容">
        <a-input v-model:value="searchParams.chatMessage" placeholder="输入消息内容" />
      </a-form-item>
      <a-form-item label="消息类型">
        <a-select
          v-model:value="searchParams.chatMessageType"
          placeholder="选择消息类型"
          style="width: 120px"
        >
          <a-select-option value="">全部</a-select-option>
          <a-select-option value="USER">用户消息</a-select-option>
          <a-select-option value="AI">AI消息</a-select-option>
        </a-select>
      </a-form-item>
      <a-form-item label="应用ID">
        <a-input v-model:value="searchParams.appId" placeholder="输入应用ID" />
      </a-form-item>
      <a-form-item label="用户ID">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户ID" />
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
      :scroll="{ x: 1400 }"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'chatMessage'">
          <a-tooltip :title="record.chatMessage">
            <div class="message-text">{{ record.chatMessage }}</div>
          </a-tooltip>
        </template>
        <template v-else-if="column.dataIndex === 'chatMessageType'">
          <a-tag :color="isUserChatMessage(record.chatMessageType) ? 'blue' : 'green'">
            {{ formatChatMessageType(record.chatMessageType) }}
          </a-tag>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="primary" size="small" @click="viewAppChat(record.appId)">
              查看对话
            </a-button>
            <a-popconfirm title="确定要删除这条消息吗？" @confirm="deleteMessage(record.chatHistoryId)">
              <a-button danger size="small">删除</a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { deleteChatHistoryByAdmin, listAllChatHistoryByPageForAdmin } from '@/api/chatHistoryController'
import { formatTime } from '@/utils/time'
import { isSuccessResponse } from '@/utils/apiResponse'
import { resolveTotalCount } from '@/utils/pagination'
import { formatChatMessageType, isUserChatMessage } from '@/utils/chatMessageTypes'

const router = useRouter()

const columns = [
  {
    title: 'ID',
    dataIndex: 'chatHistoryId',
    width: 80,
    fixed: 'left',
  },
  {
    title: '消息内容',
    dataIndex: 'chatMessage',
    width: 300,
  },
  {
    title: '消息类型',
    dataIndex: 'chatMessageType',
    width: 100,
  },
  {
    title: '应用ID',
    dataIndex: 'appId',
    width: 80,
  },
  {
    title: '用户ID',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    width: 160,
  },
  {
    title: '操作',
    key: 'action',
    width: 180,
    fixed: 'right',
  },
]

// 数据
const data = ref<API.ChatHistory[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.ChatHistoryQueryRequest>({
  chatHistoryId: '',
  current: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  try {
    const res = await listAllChatHistoryByPageForAdmin({}, searchParams)
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

// 查看应用对话
const viewAppChat = (appId: string | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}`)
  }
}

// 删除消息
const deleteMessage = async (id: string | undefined) => {
  if (!id) return

  try {
    const res = await deleteChatHistoryByAdmin({}, { id })
    if (isSuccessResponse(res.data) && res.data.data !== false) {
      message.success('删除成功')
      await fetchData()
      return
    }
    message.error(res.data.msg ?? '删除失败')
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}
</script>

<style scoped>
#chatManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}

.message-text {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
