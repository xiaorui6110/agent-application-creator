<template>
  <a-modal v-model:open="visible" title="应用详情" :footer="null" width="500px">
    <div class="app-detail-content">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="base" tab="基础信息">
          <div class="app-basic-info">
            <div class="info-item">
              <span class="info-label">应用 ID：</span>
              <span>{{ app?.appId ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">应用名称：</span>
              <span>{{ app?.appName ?? '-' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">创建者：</span>
              <UserInfo :user="app?.userVO" size="small" />
            </div>
            <div class="info-item">
              <span class="info-label">创建时间：</span>
              <span>{{ formatTime(app?.createTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">更新时间：</span>
              <span>{{ formatTime(app?.updateTime) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">生成类型：</span>
              <a-tag v-if="app?.codeGenType" color="orange">
                {{ formatCodeGenType(app.codeGenType) }}
              </a-tag>
              <span v-else>未知类型</span>
            </div>
            <div class="info-item">
              <span class="info-label">优先级：</span>
              <span>{{ app?.appPriority ?? 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">部署时间：</span>
              <span>{{ app?.deployedTime ? formatTime(app.deployedTime) : '未部署' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">部署地址：</span>
              <a-button
                v-if="app?.deployUrl"
                type="link"
                @click="openDeployUrl"
                style="padding: 0; height: auto"
              >
                打开
              </a-button>
              <span v-else>未部署</span>
            </div>
          </div>

          <div v-if="showActions" class="app-actions">
            <a-space>
              <a-button type="primary" @click="handleEdit">
                <template #icon>
                  <EditOutlined />
                </template>
                修改
              </a-button>
              <a-popconfirm
                title="确定要删除这个应用吗？"
                @confirm="handleDelete"
                ok-text="确定"
                cancel-text="取消"
              >
                <a-button danger>
                  <template #icon>
                    <DeleteOutlined />
                  </template>
                  删除
                </a-button>
              </a-popconfirm>
            </a-space>
          </div>
        </a-tab-pane>

        <a-tab-pane key="comments" :tab="commentsTabLabel">
          <div class="comment-box">
            <a-textarea
              v-model:value="newComment"
              :rows="3"
              :maxlength="500"
              placeholder="写下你的评论..."
            />
            <div class="comment-actions">
              <a-button type="primary" :loading="submitting" @click="submitComment">
                发表评论
              </a-button>
            </div>
          </div>

          <div class="comment-list">
            <a-spin :spinning="loading">
              <a-empty v-if="!loading && comments.length === 0" description="还没有评论，来抢沙发吧" />
              <div v-else>
                <div v-for="item in comments" :key="item.commentId" class="comment-item">
                  <div class="comment-avatar">
                    <a-avatar :src="item.appCommentUserVO?.userAvatar" />
                  </div>
                  <div class="comment-main">
                    <div class="comment-head">
                      <div class="comment-meta">
                        <span class="comment-author">{{ item.appCommentUserVO?.nickName ?? '匿名' }}</span>
                        <span class="comment-time">{{ formatTime(item.createTime) }}</span>
                      </div>
                      <a-popconfirm
                        v-if="canDelete(item)"
                        title="确定要删除这条评论吗？"
                        ok-text="确定"
                        cancel-text="取消"
                        @confirm="removeComment(item.commentId)"
                      >
                        <a-button type="text" danger size="small" class="comment-delete-btn">
                          <template #icon>
                            <DeleteOutlined />
                          </template>
                        </a-button>
                      </a-popconfirm>
                    </div>
                    <div class="comment-content">{{ item.commentContent }}</div>
                  </div>
                </div>

                <div v-if="hasMore" class="load-more">
                  <a-button type="link" :loading="loadingMore" @click="loadMore">加载更多</a-button>
                </div>
              </div>
            </a-spin>
          </div>
        </a-tab-pane>
      </a-tabs>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import UserInfo from './UserInfo.vue'
import { formatTime } from '@/utils/time'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { useLoginUserStore } from '@/stores/loginUser'
import { addComment, commentedHistory, deleteComment } from '@/api/appCommentController'
import { isSuccessResponse } from '@/utils/apiResponse'
import { resolveTotalCount } from '@/utils/pagination'

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const activeKey = ref('base')

const loading = ref(false)
const loadingMore = ref(false)
const submitting = ref(false)
const comments = ref<API.AppCommentVO[]>([])
const hasMore = ref(false)
const pendingReload = ref<null | boolean>(null)
const commentPage = ref({
  current: 1,
  pageSize: 10,
  total: 0,
})

const newComment = ref('')

const commentsTabLabel = computed(() => {
  const base = Math.max(props.app?.commentCount ?? 0, commentPage.value.total)
  const num = base ? ` (${base})` : ''
  return `评论${num}`
})

const appId = computed(() => {
  if (!props.app?.appId) return ''
  return String(props.app.appId)
})

const requireLogin = async () => {
  if (loginUserStore.loginUser.userId) return true
  message.warning('请先登录')
  emit('update:open', false)
  await router.push({
    path: '/user/login',
    query: {
      redirect: route.fullPath,
    },
  })
  return false
}

const canDelete = (item: API.AppCommentVO) => {
  if (!loginUserStore.loginUser.userId) return false
  return String(item.userId ?? '') === String(loginUserStore.loginUser.userId)
}

const loadComments = async (reset: boolean) => {
  if (!appId.value) return

  if (loading.value) {
    pendingReload.value = pendingReload.value === true ? true : reset
    return
  }

  loading.value = true
  try {
    if (reset) {
      commentPage.value.current = 1
      comments.value = []
    }

    const res = await commentedHistory(
      {},
      {
        appId: appId.value,
        current: commentPage.value.current,
        pageSize: commentPage.value.pageSize,
        sortField: 'createTime',
        sortOrder: 'desc',
      },
    )

    if (!isSuccessResponse(res.data) || !res.data.data) {
      message.error(res.data.msg ?? '获取评论失败')
      hasMore.value = false
      return
    }

    const records = res.data.data.records ?? []
    comments.value = reset ? records : [...comments.value, ...records]
    const inferredTotal = resolveTotalCount({
      current: commentPage.value.current,
      pageSize: commentPage.value.pageSize,
      totalRow: res.data.data.totalRow,
      recordsLength: records.length,
    })
    commentPage.value.total = inferredTotal
    hasMore.value = records.length >= (commentPage.value.pageSize ?? 10)
  } finally {
    loading.value = false

    if (pendingReload.value !== null) {
      const nextReset = pendingReload.value
      pendingReload.value = null
      await loadComments(nextReset)
    }
  }
}

const loadMore = async () => {
  if (!hasMore.value) return
  if (loadingMore.value) return
  loadingMore.value = true
  try {
    commentPage.value.current += 1
    await loadComments(false)
  } finally {
    loadingMore.value = false
  }
}

const submitComment = async () => {
  const ok = await requireLogin()
  if (!ok) return
  if (!appId.value) return

  const content = newComment.value.trim()
  if (!content) {
    message.warning('请输入评论内容')
    return
  }

  submitting.value = true
  try {
    const res = await addComment({}, { appId: appId.value, commentContent: content })
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('评论成功')
      newComment.value = ''
      await loadComments(true)
      return
    }
    message.error(res.data.msg ?? '评论失败')
  } finally {
    submitting.value = false
  }
}

const removeComment = async (commentId?: string) => {
  const ok = await requireLogin()
  if (!ok) return
  if (!commentId) return

  const res = await deleteComment({}, { commentId })
  if (isSuccessResponse(res.data) && res.data.data) {
    message.success('删除成功')
    await loadComments(true)
    return
  }
  message.error(res.data.msg ?? '删除失败')
}

const handleEdit = () => {
  emit('edit')
}

const handleDelete = () => {
  emit('delete')
}

const openDeployUrl = () => {
  if (props.app?.deployUrl) {
    window.open(props.app.deployUrl, '_blank')
  }
}

watch(
  () => visible.value,
  async (v) => {
    if (!v) return
    activeKey.value = 'base'
    comments.value = []
    hasMore.value = false
    commentPage.value = { current: 1, pageSize: 10, total: 0 }
    newComment.value = ''
  },
)

watch(
  () => [activeKey.value, appId.value, visible.value] as const,
  async ([key, id, v]) => {
    if (!v) return
    if (key !== 'comments') return
    if (!id) return
    await loadComments(true)
  },
)
</script>

<style scoped>
.app-detail-content {
  padding: 8px 0;
}

.app-basic-info {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  width: 80px;
  color: #666;
  font-size: 14px;
  flex-shrink: 0;
}

.app-actions {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.comment-box {
  margin-top: 8px;
}

.comment-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.comment-list {
  margin-top: 14px;
}

.comment-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-main {
  flex: 1;
  min-width: 0;
}

.comment-head {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
}

.comment-delete-btn {
  opacity: 0;
  transition: opacity 0.15s ease;
}

.comment-item:hover .comment-delete-btn {
  opacity: 1;
}

.comment-meta {
  display: flex;
  gap: 10px;
  align-items: center;
  min-width: 0;
}

.comment-author {
  font-weight: 600;
  color: rgba(17, 24, 39, 0.92);
}

.comment-time {
  font-size: 12px;
  color: rgba(17, 24, 39, 0.55);
}

.comment-content {
  margin-top: 6px;
  white-space: pre-wrap;
  word-break: break-word;
  color: rgba(17, 24, 39, 0.85);
}

.load-more {
  display: flex;
  justify-content: center;
  padding: 8px 0 2px;
}
</style>
