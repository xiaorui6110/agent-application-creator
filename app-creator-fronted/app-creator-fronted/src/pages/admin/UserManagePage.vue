<template>
  <div id="userManagePage">
    <div class="stats-grid">
      <div v-for="card in statsCards" :key="card.key" class="stats-card">
        <div class="stats-label">{{ card.label }}</div>
        <div class="stats-value">{{ card.value }}</div>
      </div>
    </div>

    <a-card class="page-card" :bordered="false">
      <a-form layout="inline" :model="searchParams" @finish="doSearch">
        <a-form-item label="用户 ID">
          <a-input v-model:value="searchParams.userId" placeholder="输入用户 ID" allow-clear />
        </a-form-item>
        <a-form-item label="昵称">
          <a-input v-model:value="searchParams.nickName" placeholder="输入昵称" allow-clear />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">搜索</a-button>
            <a-button @click="resetSearch">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <div class="toolbar">
        <div class="toolbar-tip">
          已选择 <span>{{ selectedRowKeys.length }}</span> 项
        </div>
        <a-popconfirm
          title="确认批量删除选中的用户吗？"
          ok-text="确认"
          cancel-text="取消"
          @confirm="doBatchDelete"
        >
          <a-button danger :disabled="selectedRowKeys.length === 0">批量删除</a-button>
        </a-popconfirm>
      </div>

      <a-table
        row-key="userId"
        :columns="columns"
        :data-source="data"
        :pagination="pagination"
        :row-selection="rowSelection"
        :scroll="{ x: 1280 }"
        @change="doTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'userId'">
            <span>{{ record.userId || '-' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'nickName'">
            <span>{{ record.nickName || '-' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'userAvatar'">
            <a-avatar :src="record.userAvatar" :size="44">
              {{ record.nickName?.slice(0, 1) || 'U' }}
            </a-avatar>
          </template>
          <template v-else-if="column.dataIndex === 'userEmail'">
            <span>{{ record.userEmail || '-' }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'userProfile'">
            <a-tooltip :title="record.userProfile || '-'">
              <div class="ellipsis-text">{{ record.userProfile || '-' }}</div>
            </a-tooltip>
          </template>
          <template v-else-if="column.dataIndex === 'userRole'">
            <a-select
              :value="normalizeRole(record.userRole)"
              size="small"
              style="width: 110px"
              :disabled="isCurrentAdmin(record) || isBanned(record)"
              @change="(value: string) => handleRoleChange(record, value)"
            >
              <a-select-option value="user">普通用户</a-select-option>
              <a-select-option value="admin">管理员</a-select-option>
            </a-select>
          </template>
          <template v-else-if="column.dataIndex === 'userStatus'">
            <a-tag :color="isBanned(record) ? 'red' : 'green'">
              {{ isBanned(record) ? '已封禁' : '正常' }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'createTime'">
            {{ record.createTime ? formatTime(record.createTime) : '-' }}
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space wrap>
              <a-button size="small" :disabled="isCurrentAdmin(record)" @click="toggleBan(record)">
                {{ isBanned(record) ? '解封' : '封禁' }}
              </a-button>
              <a-popconfirm
                title="确认删除该用户吗？"
                ok-text="确认"
                cancel-text="取消"
                @confirm="doDelete(record.userId)"
              >
                <a-button danger size="small" :disabled="isCurrentAdmin(record)">删除</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  banOrUnbanUser,
  deleteBatchUser,
  deleteUser,
  getUserManageStats,
  listUserInfoByPage,
  updateUserRole,
} from '@/api/userController'
import { useLoginUserStore } from '@/stores/loginUser'
import { isSuccessResponse } from '@/utils/apiResponse'
import { resolveTotalCount } from '@/utils/pagination'
import { formatTime } from '@/utils/time'

const loginUserStore = useLoginUserStore()

const columns = [
  { title: '用户 ID', dataIndex: 'userId', width: 190 },
  { title: '昵称', dataIndex: 'nickName', width: 140 },
  { title: '头像', dataIndex: 'userAvatar', width: 80 },
  { title: '邮箱', dataIndex: 'userEmail', width: 220 },
  { title: '简介', dataIndex: 'userProfile', ellipsis: true, width: 220 },
  { title: '角色', dataIndex: 'userRole', width: 120 },
  { title: '状态', dataIndex: 'userStatus', width: 100 },
  { title: '注册时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 170 },
]

const data = ref<API.UserVO[]>([])
const total = ref(0)
const stats = ref<API.UserManageStatsVO>({})
const selectedRowKeys = ref<string[]>([])

const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
  userId: '',
  nickName: '',
})

const statsCards = computed(() => [
  { key: 'total', label: '用户总数', value: stats.value.totalUserCount ?? 0 },
  { key: 'normal', label: '普通用户', value: stats.value.normalUserCount ?? 0 },
  { key: 'admin', label: '管理员', value: stats.value.adminUserCount ?? 0 },
  { key: 'banned', label: '封禁用户', value: stats.value.bannedUserCount ?? 0 },
  { key: 'today', label: '今日新增', value: stats.value.todayRegisterCount ?? 0 },
  { key: 'seven', label: '近 7 天新增', value: stats.value.recentSevenDayRegisterCount ?? 0 },
])

const pagination = computed(() => ({
  current: searchParams.current ?? 1,
  pageSize: searchParams.pageSize ?? 10,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value: number) => `共 ${value} 条`,
}))

const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: (string | number)[]) => {
    selectedRowKeys.value = keys.map((key) => String(key))
  },
  getCheckboxProps: (record: API.UserVO) => ({
    disabled: isCurrentAdmin(record),
  }),
}))

const normalizeRole = (userRole?: string) => (userRole === 'admin' ? 'admin' : 'user')

const isBanned = (record: API.UserVO) => record.userStatus === 2 || record.userRole === 'ban'

const isCurrentAdmin = (record: API.UserVO) => record.userId === loginUserStore.loginUser.userId

const fetchStats = async () => {
  const res = await getUserManageStats()
  if (isSuccessResponse(res.data) && res.data.data) {
    stats.value = res.data.data
    return
  }
  message.error(res.data.msg ?? '获取统计数据失败')
}

const normalizeUserRecord = (record: API.UserVO) => ({
  ...record,
  userId: record.userId || '-',
  nickName: record.nickName || '',
  userEmail: record.userEmail || '',
  userProfile: record.userProfile || '',
})

const fetchData = async () => {
  const res = await listUserInfoByPage({}, searchParams)
  if (isSuccessResponse(res.data) && res.data.data) {
    data.value = (res.data.data.records ?? []).map(normalizeUserRecord)
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
  message.error(res.data.msg ?? '获取用户列表失败')
}

const reloadPageData = async () => {
  await Promise.all([fetchStats(), fetchData()])
}

const doSearch = async () => {
  searchParams.current = 1
  selectedRowKeys.value = []
  await fetchData()
}

const resetSearch = async () => {
  searchParams.userId = ''
  searchParams.nickName = ''
  searchParams.current = 1
  selectedRowKeys.value = []
  await fetchData()
}

const doTableChange = async (page: { current: number; pageSize: number }) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  await fetchData()
}

const doDelete = async (userId?: string) => {
  if (!userId || userId === '-') return
  const res = await deleteUser({}, { id: userId })
  if (isSuccessResponse(res.data) && res.data.data !== false) {
    selectedRowKeys.value = selectedRowKeys.value.filter((id) => id !== userId)
    message.success('删除成功')
    await reloadPageData()
    return
  }
  message.error(res.data.msg ?? '删除失败')
}

const doBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) return
  const res = await deleteBatchUser(
    {},
    selectedRowKeys.value.filter((id) => id !== '-').map((id) => ({ id })),
  )
  if (isSuccessResponse(res.data) && res.data.data !== false) {
    message.success('批量删除成功')
    selectedRowKeys.value = []
    await reloadPageData()
    return
  }
  message.error(res.data.msg ?? '批量删除失败')
}

const toggleBan = async (record: API.UserVO) => {
  if (!record.userId || record.userId === '-') return
  const res = await banOrUnbanUser({}, {
    userId: record.userId,
    isUnban: isBanned(record),
  })
  if (isSuccessResponse(res.data) && res.data.data !== false) {
    message.success(isBanned(record) ? '解封成功' : '封禁成功')
    await reloadPageData()
    return
  }
  message.error(res.data.msg ?? '操作失败')
}

const handleRoleChange = async (record: API.UserVO, userRole: string) => {
  if (!record.userId || record.userId === '-' || normalizeRole(record.userRole) === userRole) return
  const res = await updateUserRole({}, { userId: record.userId, userRole })
  if (isSuccessResponse(res.data) && res.data.data !== false) {
    message.success('角色更新成功')
    await reloadPageData()
    return
  }
  message.error(res.data.msg ?? '角色更新失败')
}

onMounted(async () => {
  await reloadPageData()
})
</script>

<style scoped>
#userManagePage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
}

.stats-card {
  padding: 18px 20px;
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.98), rgba(245, 247, 250, 0.95)),
    linear-gradient(120deg, rgba(5, 150, 105, 0.08), rgba(59, 130, 246, 0.06));
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.06);
}

.stats-label {
  color: rgba(15, 23, 42, 0.62);
  font-size: 13px;
}

.stats-value {
  margin-top: 8px;
  color: #111827;
  font-size: 28px;
  font-weight: 700;
  line-height: 1;
}

.page-card {
  border-radius: 18px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 18px 0 14px;
}

.toolbar-tip {
  color: rgba(15, 23, 42, 0.62);
}

.toolbar-tip span {
  color: #111827;
  font-weight: 700;
}

.ellipsis-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.ant-table-tbody > tr > td) {
  vertical-align: middle;
}
</style>
