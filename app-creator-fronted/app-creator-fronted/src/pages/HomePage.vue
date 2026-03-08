<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { createApp as createAppApi, listAppInfoByPage, listGoodAppInfoByPage } from '@/api/appController'
import AppCard from '@/components/AppCard.vue'
import { isSuccessResponse } from '@/utils/apiResponse'
import { resolveTotalCount } from '@/utils/pagination'
import { ArrowRightOutlined } from '@ant-design/icons-vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.userId) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await createAppApi({}, { appInitPrompt: userPrompt.value.trim() })

    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + (res.data.msg ?? ''))
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.userId) {
    return
  }

  try {
    const res = await listAppInfoByPage({}, {
      current: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (isSuccessResponse(res.data) && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = resolveTotalCount({
        current: myAppsPage.current,
        pageSize: myAppsPage.pageSize,
        totalRow: res.data.data.totalRow,
        recordsLength: myApps.value.length,
      })
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppInfoByPage({}, {
      current: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (isSuccessResponse(res.data) && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = resolveTotalCount({
        current: featuredAppsPage.current,
        pageSize: featuredAppsPage.pageSize,
        totalRow: res.data.data.totalRow,
        recordsLength: featuredApps.value.length,
      })
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployUrl) {
    window.open(app.deployUrl, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <div class="hero">
        <div class="hero-copy">
          <div class="kicker">把想法交给我</div>
          <h1 class="hero-title">
            一句话
            <br />
            做个站
          </h1>
          <div class="hero-desc">
            <div>别写代码。</div>
            <div>写清需求就行。</div>
          </div>

          <div class="hero-notes">
            <div class="note">[ .ZIP ] 代码</div>
            <div class="note">[ .HTML ] 预览</div>
            <div class="note">{ DEPLOY } 一键</div>
          </div>
        </div>

        <div class="hero-panel">
          <div class="prompt-card">
            <a-textarea
              v-model:value="userPrompt"
              placeholder="比如：个人博客"
              :rows="4"
              :maxlength="1000"
              class="prompt-input"
            />
            <div class="prompt-actions">
              <a-button type="primary" size="large" @click="createApp" :loading="creating">
                开跑
                <template #icon>
                  <ArrowRightOutlined />
                </template>
              </a-button>
            </div>
          </div>

          <div class="chips">
            <button
              class="chip"
              type="button"
              @click="
                setPrompt(
                  '做个个人博客。要有文章列表、详情页、标签、搜索。风格干净，手机也好看。',
                )
              "
            >
              个人博客
            </button>
            <button
              class="chip"
              type="button"
              @click="
                setPrompt(
                  '做个企业官网。要有介绍、产品、新闻、联系。风格稳一点。',
                )
              "
            >
              企业官网
            </button>
            <button
              class="chip"
              type="button"
              @click="
                setPrompt(
                  '做个在线商城。要有商品、购物车、下单。页面要清爽。',
                )
              "
            >
              在线商城
            </button>
            <button
              class="chip"
              type="button"
              @click="
                setPrompt(
                  '做个作品集。要有画廊、项目详情、联系方式。图片要大。',
                )
              "
            >
              作品集
            </button>
          </div>
        </div>
      </div>

      <div class="section">
        <div class="section-head">
          <h2 class="section-title">我的作品</h2>
          <div class="section-sub">你做过的，都在这。</div>
        </div>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.appId"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <div class="section section-featured">
        <div class="section-head">
          <h2 class="section-title">精选案例</h2>
          <div class="section-sub">挑几个好看的。</div>
        </div>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.appId"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background:
    radial-gradient(900px 420px at 12% 8%, rgba(242, 140, 40, 0.14), transparent 60%),
    radial-gradient(760px 360px at 92% 18%, rgba(242, 140, 40, 0.08), transparent 62%),
    linear-gradient(180deg, #fbfbf9 0%, #f6f2ea 55%, #f1ece2 100%);
  position: relative;
  overflow: hidden;
}

/* 科技感网格背景 */
#homePage::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(rgba(17, 24, 39, 0.05) 1px, transparent 1px),
    linear-gradient(90deg, rgba(17, 24, 39, 0.05) 1px, transparent 1px),
    linear-gradient(rgba(17, 24, 39, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(17, 24, 39, 0.035) 1px, transparent 1px);
  background-size:
    120px 120px,
    120px 120px,
    24px 24px,
    24px 24px;
  pointer-events: none;
  transform: translateY(-12px) rotate(-0.2deg);
}

#homePage::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg%20xmlns='http://www.w3.org/2000/svg'%20width='140'%20height='140'%20viewBox='0%200%20140%20140'%3E%3Cfilter%20id='n'%3E%3CfeTurbulence%20type='fractalNoise'%20baseFrequency='.9'%20numOctaves='2'%20stitchTiles='stitch'/%3E%3C/filter%3E%3Crect%20width='140'%20height='140'%20filter='url(%23n)'%20opacity='.06'/%3E%3C/svg%3E");
  background-size: 180px 180px;
  pointer-events: none;
  mix-blend-mode: multiply;
}

.container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 26px 22px 34px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

.hero {
  display: grid;
  grid-template-columns: 1.25fr 0.95fr;
  gap: 24px;
  align-items: start;
  padding: 56px 0 40px;
}

.hero-copy {
  padding-left: 8px;
}

.kicker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(17, 24, 39, 0.10);
  color: rgba(17, 24, 39, 0.72);
  font-size: 13px;
  box-shadow: 0 10px 30px rgba(17, 24, 39, 0.08);
}

.hero-title {
  margin: 18px 0 10px;
  font-size: 64px;
  letter-spacing: -1px;
  line-height: 0.98;
  color: var(--ink);
}

.hero-desc {
  margin-top: 10px;
  color: rgba(17, 24, 39, 0.66);
  font-size: 16px;
  line-height: 1.6;
}

.hero-notes {
  margin-top: 22px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.note {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
  font-size: 12px;
  color: rgba(17, 24, 39, 0.55);
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px dashed rgba(17, 24, 39, 0.14);
  background: rgba(255, 255, 255, 0.45);
}

.hero-panel {
  transform: translateY(18px);
}

.prompt-card {
  position: relative;
  padding: 16px 16px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(17, 24, 39, 0.12);
  box-shadow: var(--shadow-m);
  backdrop-filter: blur(16px);
}

.prompt-input {
  border-radius: 14px;
  border: 1px solid rgba(17, 24, 39, 0.10) !important;
  background: rgba(255, 255, 255, 0.86) !important;
  padding: 14px 14px 58px 14px !important;
  font-size: 15px;
  line-height: 1.55;
}

.prompt-actions {
  position: absolute;
  right: 14px;
  bottom: 14px;
}

.chips {
  margin-top: 14px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-start;
}

.chip {
  appearance: none;
  border: 1px solid rgba(17, 24, 39, 0.12);
  background: rgba(255, 255, 255, 0.62);
  color: rgba(17, 24, 39, 0.78);
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  transition:
    transform 180ms cubic-bezier(0.22, 1, 0.36, 1),
    box-shadow 180ms cubic-bezier(0.22, 1, 0.36, 1),
    border-color 180ms cubic-bezier(0.22, 1, 0.36, 1);
}

.chip:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-s);
  border-color: rgba(242, 140, 40, 0.35);
}

/* 区域标题 */
.section {
  margin: 18px 0 54px;
}

.section-featured {
  margin-left: 28px;
}

.section-head {
  display: flex;
  align-items: baseline;
  gap: 14px;
  padding-left: 6px;
  margin-bottom: 16px;
}

.section-title {
  font-size: 22px;
  font-weight: 650;
  margin: 0;
  color: rgba(17, 24, 39, 0.92);
}

.section-sub {
  font-size: 13px;
  color: rgba(17, 24, 39, 0.52);
}

/* 我的作品网格 */
.app-grid {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

/* 精选案例网格 */
.featured-grid {
  display: grid;
  grid-template-columns: 0.95fr 1.05fr;
  gap: 24px;
  margin-bottom: 32px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: flex-start;
  margin-top: 32px;
  padding-left: 6px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero {
    grid-template-columns: 1fr;
    padding: 34px 0 24px;
  }

  .hero-title {
    font-size: 44px;
  }

  .hero-panel {
    transform: none;
  }

  .section-featured {
    margin-left: 0;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }
}
</style>
