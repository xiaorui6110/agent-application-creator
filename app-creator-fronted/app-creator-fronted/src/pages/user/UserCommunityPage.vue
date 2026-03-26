<template>
  <div class="community-page">
    <div class="hero-card">
      <div>
        <p class="eyebrow">Community Inbox</p>
        <h1>社区中心</h1>
        <p class="hero-text">统一查看评论通知、点赞提醒、分享消息，以及你的互动历史。</p>
      </div>
      <a-space>
        <a-button @click="refreshAll" :loading="summaryLoading || feedLoading">刷新</a-button>
        <a-button type="primary" ghost @click="clearUnreadFeed" :loading="clearingLoading">
          全部标记已读
        </a-button>
      </a-space>
    </div>

    <a-row :gutter="[16, 16]" class="summary-grid">
      <a-col :xs="24" :md="6">
        <div class="summary-card accent-comment">
          <span class="summary-label">未读评论</span>
          <strong>{{ summary.unreadCommentCount }}</strong>
        </div>
      </a-col>
      <a-col :xs="24" :md="6">
        <div class="summary-card accent-like">
          <span class="summary-label">未读点赞</span>
          <strong>{{ summary.unreadLikeCount }}</strong>
        </div>
      </a-col>
      <a-col :xs="24" :md="6">
        <div class="summary-card accent-share">
          <span class="summary-label">未读分享</span>
          <strong>{{ summary.unreadShareCount }}</strong>
        </div>
      </a-col>
      <a-col :xs="24" :md="6">
        <div class="summary-card accent-total">
          <span class="summary-label">未读总数</span>
          <strong>{{ summary.totalUnreadCount }}</strong>
        </div>
      </a-col>
    </a-row>

    <a-tabs v-model:activeKey="activeTab" class="community-tabs">
      <a-tab-pane key="feed" tab="未读消息">
        <a-card :loading="feedLoading" class="panel-card">
          <template #title>统一消息流</template>
          <template #extra>
            <span class="panel-extra">按时间倒序展示最近 {{ unreadFeed.length }} 条未读消息</span>
          </template>
          <a-empty v-if="!unreadFeed.length" description="当前没有未读消息" />
          <div v-else class="feed-list">
            <div v-for="item in unreadFeed" :key="`${item.notificationType}-${item.notificationId}`" class="feed-item">
              <div class="feed-tag" :class="`tag-${item.notificationType.toLowerCase()}`">
                {{ typeTextMap[item.notificationType] ?? item.notificationType }}
              </div>
              <div class="feed-body">
                <div class="feed-head">
                  <span class="feed-actor">{{ item.actorUserName || '匿名用户' }}</span>
                  <span class="feed-time">{{ formatTime(item.actionTime) }}</span>
                </div>
                <div class="feed-content">{{ item.content || '有新的互动消息' }}</div>
                <div class="feed-app" v-if="item.appName">关联应用：{{ item.appName }}</div>
              </div>
            </div>
          </div>
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="comments" tab="收到的评论">
        <a-card class="panel-card" :loading="commentLoading">
          <template #title>评论通知历史</template>
          <a-empty v-if="!commentRecords.length" description="还没有收到评论" />
          <div v-else class="history-list">
            <div v-for="item in commentRecords" :key="item.commentId" class="history-item">
              <div class="history-head">
                <span>{{ item.appCommentUserVO?.nickName || '匿名用户' }}</span>
                <span>{{ formatTime(item.createTime) }}</span>
              </div>
              <div class="history-content">{{ item.commentContent }}</div>
              <div class="history-app" v-if="item.appVO?.appName">{{ item.appVO.appName }}</div>
            </div>
          </div>
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="likes" tab="收到的点赞">
        <a-card class="panel-card" :loading="likeLoading">
          <template #title>点赞历史</template>
          <a-empty v-if="!likeRecords.length" description="还没有收到点赞" />
          <div v-else class="history-list">
            <div v-for="item in likeRecords" :key="item.likeId" class="history-item">
              <div class="history-head">
                <span>{{ item.userVO?.nickName || '匿名用户' }}</span>
                <span>{{ formatTime(item.lastLikeTime) }}</span>
              </div>
              <div class="history-content">赞了你的应用</div>
              <div class="history-app" v-if="item.appVO?.appName">{{ item.appVO.appName }}</div>
            </div>
          </div>
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="shares" tab="收到的分享">
        <a-card class="panel-card" :loading="shareLoading">
          <template #title>分享历史</template>
          <a-empty v-if="!shareRecords.length" description="还没有收到分享" />
          <div v-else class="history-list">
            <div v-for="item in shareRecords" :key="item.shareId" class="history-item">
              <div class="history-head">
                <span>{{ item.userVO?.nickName || '匿名用户' }}</span>
                <span>{{ formatTime(item.shareTime) }}</span>
              </div>
              <div class="history-content">分享了你的应用</div>
              <div class="history-app" v-if="item.appVO?.appName">{{ item.appVO.appName }}</div>
            </div>
          </div>
        </a-card>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { commentedHistory } from '@/api/appCommentController'
import { getCommunityUnreadFeed, getCommunityUnreadSummary, clearCommunityUnread } from '@/api/communityController'
import { getLikeHistory } from '@/api/likeRecordController'
import { getUserShareHistory } from '@/api/shareRecordController'
import { isSuccessResponse } from '@/utils/apiResponse'

type CommunitySummary = {
  unreadCommentCount: number
  unreadLikeCount: number
  unreadShareCount: number
  totalUnreadCount: number
}

type CommunityNotification = {
  notificationType: string
  notificationId: string
  actorUserId?: string
  actorUserName?: string
  appId?: string
  appName?: string
  content?: string
  actionTime?: string
}

type CommentRecord = API.AppCommentVO
type LikeRecord = API.LikeRecordVO
type ShareRecord = API.ShareRecordVO

const activeTab = ref('feed')
const summaryLoading = ref(false)
const feedLoading = ref(false)
const commentLoading = ref(false)
const likeLoading = ref(false)
const shareLoading = ref(false)
const clearingLoading = ref(false)

const summary = ref<CommunitySummary>({
  unreadCommentCount: 0,
  unreadLikeCount: 0,
  unreadShareCount: 0,
  totalUnreadCount: 0,
})

const unreadFeed = ref<CommunityNotification[]>([])
const commentRecords = ref<CommentRecord[]>([])
const likeRecords = ref<LikeRecord[]>([])
const shareRecords = ref<ShareRecord[]>([])

const typeTextMap: Record<string, string> = {
  COMMENT: '评论',
  LIKE: '点赞',
  SHARE: '分享',
}

const formatTime = (time?: string) => {
  if (!time) return '--'
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const loadSummary = async () => {
  summaryLoading.value = true
  try {
    const res = await getCommunityUnreadSummary()
    if (isSuccessResponse(res.data) && res.data.data) {
      summary.value = {
        unreadCommentCount: Number(res.data.data.unreadCommentCount ?? 0),
        unreadLikeCount: Number(res.data.data.unreadLikeCount ?? 0),
        unreadShareCount: Number(res.data.data.unreadShareCount ?? 0),
        totalUnreadCount: Number(res.data.data.totalUnreadCount ?? 0),
      }
      return
    }
    message.error(res.data?.msg ?? '获取社区摘要失败')
  } finally {
    summaryLoading.value = false
  }
}

const loadUnreadFeed = async () => {
  feedLoading.value = true
  try {
    const res = await getCommunityUnreadFeed({ limit: 50 })
    if (isSuccessResponse(res.data)) {
      unreadFeed.value = Array.isArray(res.data.data) ? res.data.data : []
      return
    }
    message.error(res.data?.msg ?? '获取未读消息失败')
  } finally {
    feedLoading.value = false
  }
}

const loadCommentHistory = async () => {
  commentLoading.value = true
  try {
    const res = await commentedHistory({}, { current: 1, pageSize: 20 })
    if (isSuccessResponse(res.data)) {
      commentRecords.value = res.data.data?.records ?? []
      return
    }
    message.error(res.data?.msg ?? '获取评论历史失败')
  } finally {
    commentLoading.value = false
  }
}

const loadLikeHistory = async () => {
  likeLoading.value = true
  try {
    const res = await getLikeHistory({}, { current: 1, pageSize: 20 })
    if (isSuccessResponse(res.data)) {
      likeRecords.value = res.data.data?.records ?? []
      return
    }
    message.error(res.data?.msg ?? '获取点赞历史失败')
  } finally {
    likeLoading.value = false
  }
}

const loadShareHistory = async () => {
  shareLoading.value = true
  try {
    const res = await getUserShareHistory({}, { current: 1, pageSize: 20 })
    if (isSuccessResponse(res.data)) {
      shareRecords.value = res.data.data?.records ?? []
      return
    }
    message.error(res.data?.msg ?? '获取分享历史失败')
  } finally {
    shareLoading.value = false
  }
}

const refreshAll = async () => {
  await loadSummary()
  await Promise.all([loadUnreadFeed(), loadCommentHistory(), loadLikeHistory(), loadShareHistory()])
  await loadSummary()
}

const clearUnreadFeed = async () => {
  clearingLoading.value = true
  try {
    const res = await clearCommunityUnread()
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('未读消息已清空')
      unreadFeed.value = []
      await loadSummary()
      return
    }
    message.error(res.data?.msg ?? '清空未读消息失败')
  } finally {
    clearingLoading.value = false
  }
}

onMounted(async () => {
  await refreshAll()
})
</script>

<style scoped>
.community-page {
  max-width: 1180px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.hero-card {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: flex-end;
  padding: 28px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(242, 140, 40, 0.22), transparent 35%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(255, 247, 237, 0.92));
  border: 1px solid rgba(17, 24, 39, 0.08);
  box-shadow: 0 24px 60px rgba(17, 24, 39, 0.08);
}

.eyebrow {
  margin: 0 0 8px;
  color: var(--brand);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-card h1 {
  margin: 0;
  font-size: 32px;
  color: #1f2937;
}

.hero-text {
  margin: 10px 0 0;
  max-width: 680px;
  color: #6b7280;
  line-height: 1.7;
}

.summary-grid {
  margin-top: 18px;
}

.summary-card {
  padding: 18px 20px;
  border-radius: 18px;
  min-height: 116px;
  border: 1px solid rgba(17, 24, 39, 0.06);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 12px 30px rgba(17, 24, 39, 0.06);
}

.summary-label {
  display: block;
  color: #6b7280;
  margin-bottom: 10px;
}

.summary-card strong {
  font-size: 34px;
  color: #1f2937;
}

.accent-comment { background: linear-gradient(180deg, #fff8f2, #ffffff); }
.accent-like { background: linear-gradient(180deg, #fff4f4, #ffffff); }
.accent-share { background: linear-gradient(180deg, #f4fbff, #ffffff); }
.accent-total { background: linear-gradient(180deg, #fff9ec, #ffffff); }

.community-tabs {
  margin-top: 22px;
}

.panel-card {
  border-radius: 20px;
  border: 1px solid rgba(17, 24, 39, 0.08);
  box-shadow: 0 18px 48px rgba(17, 24, 39, 0.06);
}

.panel-extra {
  color: #9ca3af;
  font-size: 12px;
}

.feed-list,
.history-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.feed-item,
.history-item {
  display: flex;
  gap: 14px;
  padding: 16px 18px;
  border-radius: 16px;
  background: #fafaf9;
  border: 1px solid rgba(17, 24, 39, 0.06);
}

.feed-tag {
  flex: 0 0 auto;
  min-width: 54px;
  height: 28px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.tag-comment { background: rgba(245, 158, 11, 0.16); color: #b45309; }
.tag-like { background: rgba(239, 68, 68, 0.14); color: #b91c1c; }
.tag-share { background: rgba(14, 165, 233, 0.14); color: #0369a1; }

.feed-body,
.history-item {
  flex: 1;
}

.feed-head,
.history-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: #6b7280;
  font-size: 13px;
}

.feed-actor {
  color: #111827;
  font-weight: 700;
}

.feed-content,
.history-content {
  margin-top: 8px;
  color: #1f2937;
  line-height: 1.7;
}

.feed-app,
.history-app {
  margin-top: 8px;
  color: var(--brand-700);
  font-size: 13px;
}

@media (max-width: 768px) {
  .hero-card {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-card h1 {
    font-size: 26px;
  }
}
</style>
