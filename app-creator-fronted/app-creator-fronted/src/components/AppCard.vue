<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.appCover" :src="app.appCover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <span class="placeholder-text">AI</span>
      </div>
      <div class="app-overlay">
        <a-space>
          <a-button type="primary" @click="handleViewChat">查看对话</a-button>
          <a-button v-if="app.deployUrl" type="default" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :src="app.userVO?.userAvatar" :size="40">
          {{ app.userVO?.nickName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author">
          {{ app.userVO?.nickName || (featured ? '官方' : '未知用户') }}
        </p>
        <div class="app-actions">
          <a-space size="small">
            <a-button
              size="small"
              type="text"
              :disabled="!appId"
              :loading="likeLoading"
              @click="toggleLike"
            >
              <template #icon>
                <HeartFilled v-if="liked" class="active" />
                <HeartOutlined v-else />
              </template>
              {{ likeCount }}
            </a-button>
            <a-button
              size="small"
              type="text"
              :disabled="!appId"
              :loading="shareLoading"
              @click="toggleShare"
            >
              <template #icon>
                <ShareAltOutlined v-if="shared" class="active" />
                <ShareAltOutlined v-else />
              </template>
              {{ shareCount }}
            </a-button>
          </a-space>
        </div>
      </div>
    </div>
  </div>

  <a-modal
    v-model:open="sharePreviewVisible"
    title="分享应用"
    :footer="null"
    width="420px"
    @cancel="closeSharePreview"
  >
    <div class="share-preview">
      <div v-if="sharePreview?.qrCodeDataUrl" class="share-preview__qr">
        <img :src="sharePreview.qrCodeDataUrl" alt="share-qrcode" />
      </div>
      <div class="share-preview__link">
        <a-typography-paragraph :copyable="{ text: sharePreview?.shareUrl ?? '' }">
          {{ sharePreview?.shareUrl ?? '-' }}
        </a-typography-paragraph>
      </div>
      <a-button block type="primary" @click="openSharedUrl">打开分享链接</a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { isSuccessResponse } from '@/utils/apiResponse'
import { doLike, getLikeStatus } from '@/api/likeRecordController'
import { doShare, getSharePreview, getShareStatus } from '@/api/shareRecordController'
import { HeartFilled, HeartOutlined, ShareAltOutlined } from '@ant-design/icons-vue'

interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const appId = computed(() => {
  if (!props.app.appId) return ''
  return String(props.app.appId)
})

const liked = ref(false)
const shared = ref(false)
const likeLoading = ref(false)
const shareLoading = ref(false)
const likeCount = ref<number>(props.app.likeCount ?? 0)
const shareCount = ref<number>(props.app.shareCount ?? 0)
const sharePreviewVisible = ref(false)
const sharePreview = ref<API.SharePreviewVO>()

watch(
  () => props.app.likeCount,
  (v) => {
    likeCount.value = v ?? 0
  },
)

watch(
  () => props.app.shareCount,
  (v) => {
    shareCount.value = v ?? 0
  },
)

const requireLogin = async () => {
  if (loginUserStore.loginUser.userId) return true
  message.warning('请先登录')
  await router.push({
    path: '/user/login',
    query: {
      redirect: route.fullPath,
    },
  })
  return false
}

const refreshStates = async () => {
  if (!appId.value) return
  if (!loginUserStore.loginUser.userId) {
    liked.value = false
    shared.value = false
    return
  }

  const [likeRes, shareRes] = await Promise.all([
    getLikeStatus({ targetId: appId.value }),
    getShareStatus({ targetId: appId.value }),
  ])

  if (isSuccessResponse(likeRes.data)) {
    liked.value = likeRes.data.data === true
  }
  if (isSuccessResponse(shareRes.data)) {
    shared.value = shareRes.data.data === true
  }
}

const toggleLike = async () => {
  if (!appId.value) return
  const ok = await requireLogin()
  if (!ok) return

  likeLoading.value = true
  try {
    const next = liked.value ? 0 : 1
    const res = await doLike({}, { targetId: appId.value, isLiked: next })
    if (isSuccessResponse(res.data) && res.data.data) {
      liked.value = !liked.value
      likeCount.value = Math.max(0, likeCount.value + (next === 1 ? 1 : -1))
      return
    }
    message.error(res.data.msg ?? '操作失败')
  } finally {
    likeLoading.value = false
  }
}

const toggleShare = async () => {
  if (!appId.value) return
  const ok = await requireLogin()
  if (!ok) return

  shareLoading.value = true
  try {
    if (!shared.value) {
      const res = await doShare({}, { shareId: appId.value, isShared: 1 })
      if (isSuccessResponse(res.data) && res.data.data) {
        shared.value = true
        shareCount.value = Math.max(0, shareCount.value + 1)
      } else {
        message.error(res.data.msg ?? '操作失败')
        return
      }
    }

    const previewRes = await getSharePreview({ targetId: appId.value })
    if (isSuccessResponse(previewRes.data) && previewRes.data.data) {
      sharePreview.value = previewRes.data.data
      sharePreviewVisible.value = true
      return
    }
    message.error(previewRes.data.msg ?? '获取分享预览失败')
  } finally {
    shareLoading.value = false
  }
}

const openSharedUrl = () => {
  if (!sharePreview.value?.shareUrl) return
  window.open(sharePreview.value.shareUrl, '_blank')
}

const closeSharePreview = () => {
  sharePreviewVisible.value = false
}

const handleViewChat = () => {
  emit('view-chat', props.app.appId)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}

onMounted(async () => {
  await refreshStates()
})

watch(
  () => loginUserStore.loginUser.userId,
  async () => {
    await refreshStates()
  },
)
</script>

<style scoped>
.app-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition:
    transform 0.3s,
    box-shadow 0.3s;
  cursor: pointer;
}

.app-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.25);
}

.app-preview {
  height: 180px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-placeholder {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  color: rgba(17, 24, 39, 0.55);
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(240px 120px at 30% 30%, rgba(242, 140, 40, 0.1), transparent 60%),
    radial-gradient(220px 120px at 80% 70%, rgba(242, 140, 40, 0.08), transparent 60%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.75), rgba(255, 255, 255, 0.55));
}

.placeholder-text {
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(17, 24, 39, 0.12);
  background: rgba(255, 255, 255, 0.55);
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.app-info {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-author {
  font-size: 14px;
  color: #666;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-actions {
  margin-top: 8px;
  display: flex;
  align-items: center;
}

.active {
  color: #f28c28;
}

.share-preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.share-preview__qr {
  display: flex;
  justify-content: center;
}

.share-preview__qr img {
  width: 220px;
  height: 220px;
  object-fit: contain;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  padding: 10px;
  background: #fff;
}

.share-preview__link {
  padding: 12px;
  border-radius: 12px;
  background: #faf7f2;
}
</style>
