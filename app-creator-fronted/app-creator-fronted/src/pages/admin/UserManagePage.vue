<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="用户 ID">
        <a-input v-model:value="searchParams.userId" placeholder="输入用户 ID" />
      </a-form-item>
      <a-form-item label="昵称">
        <a-input v-model:value="searchParams.nickName" placeholder="输入昵称" />
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
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image
            :src="record.userAvatar"
            :width="48"
            :height="48"
            style="object-fit: cover; border-radius: 6px"
          />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
          <a-tag color="orange">普通用户</a-tag>
          </div>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button danger @click="doDelete(record.userId)">删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserInfoByPage } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { isSuccessResponse } from '@/utils/apiResponse'
import { resolveTotalCount } from '@/utils/pagination'

const columns = [
  {
    title: '用户ID',
    dataIndex: 'userId',
  },
  {
    title: '昵称',
    dataIndex: 'nickName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserInfoByPage({}, searchParams)
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
}

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

// 表格分页变化时的操作
const doTableChange = (page: { current: number; pageSize: number }) => {
  searchParams.current = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.current = 1
  fetchData()
}

// 删除数据
const doDelete = async (userId?: string) => {
  if (!userId) {
    return
  }
  const res = await deleteUser({}, { id: userId })
  if (isSuccessResponse(res.data) && res.data.data !== false) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error(res.data.msg ?? '删除失败')
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
#userManagePage {
  padding: 24px;
  background: white;
  margin-top: 16px;
}
</style>
