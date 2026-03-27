<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { ArrowRightOutlined, AppstoreAddOutlined, CopyOutlined, RocketOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  createApp as createAppApi,
  createAppFromTemplate,
  listAppInfoByPage,
  listAppCategories,
  listGoodAppInfoByPage,
  listTemplates,
} from '@/api/AppController'
import AppCard from '@/components/AppCard.vue'
import { isSuccessResponse } from '@/utils/apiResponse'
import { resolveTotalCount } from '@/utils/pagination'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { formatRelativeTime, formatTime } from '@/utils/time'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const userPrompt = ref('')
const creating = ref(false)
const templateCreating = ref(false)
const templateModalOpen = ref(false)
const selectedTemplate = ref<API.AppTemplateVO>()

const myApps = ref<API.AppVO[]>([])
const featuredApps = ref<API.AppVO[]>([])
const templates = ref<API.AppTemplateVO[]>([])
const categories = ref<string[]>([])

const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

const featuredFilter = reactive({
  appName: '',
  appCategory: '',
})

const templateForm = reactive({
  appName: '',
  appDescription: '',
})

const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

const requireLogin = async () => {
  if (loginUserStore.loginUser.userId) return true
  message.warning('请先登录')
  await router.push('/user/login')
  return false
}

const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用需求描述')
    return
  }

  const ok = await requireLogin()
  if (!ok) return

  creating.value = true
  try {
    const res = await createAppApi({}, { appInitPrompt: userPrompt.value.trim() })
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('应用创建成功')
      await router.push(`/app/chat/${String(res.data.data)}`)
      return
    }
    message.error(res.data.msg ?? '创建失败')
  } finally {
    creating.value = false
  }
}

const loadMyApps = async () => {
  if (!loginUserStore.loginUser.userId) return
  const res = await listAppInfoByPage(
    {},
    {
      current: Math.max(myAppsPage.current, 1),
      pageSize: Math.max(myAppsPage.pageSize, 6),
      sortField: 'createTime',
      sortOrder: 'desc',
    },
  )

  if (isSuccessResponse(res.data) && res.data.data) {
    myApps.value = res.data.data.records || []
    myAppsPage.total = resolveTotalCount({
      current: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      totalRow: res.data.data.totalRow,
      recordsLength: myApps.value.length,
    })
  }
}

const loadFeaturedApps = async () => {
  const res = await listGoodAppInfoByPage(
    {},
    {
      current: Math.max(featuredAppsPage.current, 1),
      pageSize: Math.max(featuredAppsPage.pageSize, 6),
      appName: featuredFilter.appName || undefined,
      appCategory: featuredFilter.appCategory || undefined,
      sortField: 'createTime',
      sortOrder: 'desc',
    },
  )

  if (isSuccessResponse(res.data) && res.data.data) {
    featuredApps.value = res.data.data.records || []
    featuredAppsPage.total = resolveTotalCount({
      current: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      totalRow: res.data.data.totalRow,
      recordsLength: featuredApps.value.length,
    })
  }
}

const loadCategories = async () => {
  const res = await listAppCategories()
  if (isSuccessResponse(res.data) && res.data.data) {
    categories.value = res.data.data
  }
}

const handleFeaturedSearch = async () => {
  featuredAppsPage.current = 1
  await loadFeaturedApps()
}

const loadTemplates = async () => {
  const res = await listTemplates()
  if (isSuccessResponse(res.data) && res.data.data) {
    templates.value = res.data.data
  }
}

const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

const viewWork = (app: API.AppVO) => {
  if (app.deployUrl) {
    window.open(app.deployUrl, '_blank')
  }
}

const openTemplateModal = async (template: API.AppTemplateVO) => {
  const ok = await requireLogin()
  if (!ok) return

  selectedTemplate.value = template
  templateForm.appName = `${template.templateName || '模板'} - 副本`
  templateForm.appDescription = template.templateDescription || ''
  templateModalOpen.value = true
}

const handleCreateFromTemplate = async () => {
  if (!selectedTemplate.value?.templateId) return
  if (!templateForm.appName.trim()) {
    message.warning('请输入新应用名称')
    return
  }

  templateCreating.value = true
  try {
    const res = await createAppFromTemplate(
      {},
      {
        templateId: selectedTemplate.value.templateId,
        appName: templateForm.appName.trim(),
        appDescription: templateForm.appDescription.trim(),
      },
    )

    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('已基于模板创建新应用')
      templateModalOpen.value = false
      await loadMyApps()
      await router.push(`/app/chat/${String(res.data.data)}`)
      return
    }
    message.error(res.data.msg ?? '模板创建失败')
  } finally {
    templateCreating.value = false
  }
}

onMounted(() => {
  loadMyApps()
  loadFeaturedApps()
  loadTemplates()
  loadCategories()
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <section class="hero">
        <div class="hero-copy">
          <div class="kicker">Agent Application Creator</div>
          <h1 class="hero-title">
            一句话生成应用
            <br />
            也能一键复用模板
          </h1>
          <p class="hero-desc">
            你可以从零创建，也可以直接从已有优秀应用模板出发，更快落地新的产品原型。
          </p>

          <div class="hero-notes">
            <div class="note">[VERSION] 历史版本</div>
            <div class="note">[TEMPLATE] 可复用模板</div>
            <div class="note">[DEPLOY] 一键部署</div>
          </div>
        </div>

        <div class="hero-panel">
          <div class="prompt-card">
            <a-textarea
              v-model:value="userPrompt"
              placeholder="例如：做一个适合独立开发者的产品展示页，带定价、FAQ 和用户评价。"
              :rows="5"
              :maxlength="1000"
              class="prompt-input"
            />
            <div class="prompt-actions">
              <a-button type="primary" size="large" @click="createApp" :loading="creating">
                开始创建
                <template #icon>
                  <ArrowRightOutlined />
                </template>
              </a-button>
            </div>
          </div>

          <div class="chips">
            <button class="chip" type="button" @click="setPrompt('做一个个人博客，包含文章列表、详情页、标签、搜索和归档。')">
              个人博客
            </button>
            <button class="chip" type="button" @click="setPrompt('做一个企业官网，要有产品介绍、案例展示、客户评价和联系表单。')">
              企业官网
            </button>
            <button class="chip" type="button" @click="setPrompt('做一个在线商城首页，强调活动氛围、分类导航和商品推荐。')">
              在线商城
            </button>
            <button class="chip" type="button" @click="setPrompt('做一个作品集网站，要有项目卡片、筛选、详情和关于我。')">
              作品集
            </button>
          </div>
        </div>
      </section>

      <section class="section template-section">
        <div class="section-head">
          <div>
            <h2 class="section-title">模板广场</h2>
            <div class="section-sub">把优秀应用沉淀成模板，再用模板快速创建新的应用。</div>
          </div>
          <a-tag color="orange">{{ templates.length }} 个模板</a-tag>
        </div>

        <a-empty v-if="templates.length === 0" description="暂时还没有可用模板" />

        <div v-else class="template-grid">
          <article v-for="template in templates" :key="template.templateId" class="template-card">
            <div class="template-card__icon">
              <AppstoreAddOutlined />
            </div>
            <div class="template-card__body">
              <div class="template-card__title-row">
                <h3>{{ template.templateName || '未命名模板' }}</h3>
                <a-tag>{{ formatCodeGenType(template.codeGenType) || template.codeGenType || '未知类型' }}</a-tag>
              </div>
              <p class="template-card__desc">
                {{ template.templateDescription || '这个模板暂时还没有补充描述。' }}
              </p>
              <div class="template-card__meta">
                <span><CopyOutlined /> 来源应用：{{ template.sourceAppId || '-' }}</span>
                <span>
                  <RocketOutlined />
                  {{ formatTime(template.createdTime) || formatRelativeTime(template.createdTime) || '刚刚创建' }}
                </span>
              </div>
            </div>
            <div class="template-card__actions">
              <a-button type="primary" @click="openTemplateModal(template)">使用模板</a-button>
            </div>
          </article>
        </div>
      </section>

      <section class="section">
        <div class="section-head">
          <div>
            <h2 class="section-title">我的作品</h2>
            <div class="section-sub">你最近创建和维护的应用都在这里。</div>
          </div>
        </div>
        <div class="app-grid">
          <AppCard v-for="app in myApps" :key="app.appId" :app="app" @view-chat="viewChat" @view-work="viewWork" />
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
      </section>

      <section class="section section-featured">
        <div class="section-head">
          <div>
            <h2 class="section-title">精选案例</h2>
            <div class="section-sub">平台里已经沉淀下来的高优先级应用。</div>
          </div>
        </div>
        <a-form layout="inline" class="featured-filter" @finish="handleFeaturedSearch">
          <a-form-item>
            <a-input v-model:value="featuredFilter.appName" placeholder="按应用名称搜索" allow-clear />
          </a-form-item>
          <a-form-item>
            <a-select
              v-model:value="featuredFilter.appCategory"
              placeholder="按分类筛选"
              style="width: 180px"
              allow-clear
            >
              <a-select-option v-for="category in categories" :key="category" :value="category">
                {{ category }}
              </a-select-option>
            </a-select>
          </a-form-item>
          <a-form-item>
            <a-space>
              <a-button type="primary" html-type="submit">搜索</a-button>
              <a-button
                @click="featuredFilter.appName = ''; featuredFilter.appCategory = ''; handleFeaturedSearch()"
              >
                重置
              </a-button>
            </a-space>
          </a-form-item>
        </a-form>
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
      </section>
    </div>

    <a-modal
      v-model:open="templateModalOpen"
      title="基于模板创建应用"
      :confirm-loading="templateCreating"
      ok-text="创建应用"
      cancel-text="取消"
      @ok="handleCreateFromTemplate"
    >
      <a-form layout="vertical">
        <a-form-item label="模板">
          <a-input :value="selectedTemplate?.templateName || '-'" disabled />
        </a-form-item>
        <a-form-item label="新应用名称">
          <a-input v-model:value="templateForm.appName" :maxlength="50" show-count />
        </a-form-item>
        <a-form-item label="新应用描述">
          <a-textarea v-model:value="templateForm.appDescription" :rows="4" :maxlength="300" show-count />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<style scoped>
#homePage {
  min-height: 100vh;
  background:
    radial-gradient(900px 420px at 12% 8%, rgba(242, 140, 40, 0.14), transparent 60%),
    radial-gradient(760px 360px at 92% 18%, rgba(242, 140, 40, 0.08), transparent 62%),
    linear-gradient(180deg, #fbfbf9 0%, #f6f2ea 55%, #f1ece2 100%);
}

.container {
  max-width: 1180px;
  margin: 0 auto;
  padding: 26px 22px 42px;
}

.hero {
  display: grid;
  grid-template-columns: 1.2fr 0.9fr;
  gap: 24px;
  padding: 54px 0 36px;
}

.kicker {
  display: inline-flex;
  padding: 8px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(17, 24, 39, 0.1);
  color: rgba(17, 24, 39, 0.74);
  font-size: 13px;
}

.hero-title {
  margin: 18px 0 12px;
  font-size: 62px;
  line-height: 0.98;
  letter-spacing: -1px;
  color: #171717;
}

.hero-desc {
  margin: 0;
  max-width: 620px;
  color: rgba(17, 24, 39, 0.66);
  line-height: 1.7;
  font-size: 16px;
}

.hero-notes {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 22px;
}

.note {
  padding: 8px 10px;
  border-radius: 10px;
  border: 1px dashed rgba(17, 24, 39, 0.14);
  background: rgba(255, 255, 255, 0.45);
  color: rgba(17, 24, 39, 0.56);
  font-size: 12px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
    monospace;
}

.hero-panel {
  transform: translateY(16px);
}

.prompt-card {
  position: relative;
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(17, 24, 39, 0.12);
  box-shadow: 0 20px 44px rgba(17, 24, 39, 0.1);
  backdrop-filter: blur(16px);
}

.prompt-input {
  border-radius: 14px;
  padding: 14px 14px 58px !important;
  background: rgba(255, 255, 255, 0.86) !important;
}

.prompt-actions {
  position: absolute;
  right: 14px;
  bottom: 14px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 14px;
}

.chip {
  border: 1px solid rgba(17, 24, 39, 0.12);
  background: rgba(255, 255, 255, 0.62);
  color: rgba(17, 24, 39, 0.78);
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(17, 24, 39, 0.08);
}

.section {
  margin: 20px 0 54px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 16px;
}

.section-title {
  margin: 0;
  font-size: 24px;
  color: rgba(17, 24, 39, 0.92);
}

.section-sub {
  margin-top: 4px;
  color: rgba(17, 24, 39, 0.55);
  font-size: 13px;
}

.template-section {
  padding: 22px;
  border-radius: 28px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.72), rgba(255, 250, 243, 0.92));
  border: 1px solid rgba(225, 147, 64, 0.12);
  box-shadow: 0 18px 46px rgba(161, 106, 42, 0.08);
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.template-card {
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(17, 24, 39, 0.08);
}

.template-card__icon {
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(246, 173, 85, 0.18), rgba(251, 191, 36, 0.12));
  color: #b45309;
  font-size: 20px;
}

.template-card__title-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.template-card__title-row h3 {
  margin: 0;
  font-size: 18px;
  line-height: 1.4;
}

.template-card__desc {
  margin: 10px 0 0;
  color: rgba(17, 24, 39, 0.68);
  line-height: 1.7;
  min-height: 72px;
}

.template-card__meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 13px;
  color: rgba(17, 24, 39, 0.54);
}

.template-card__actions {
  margin-top: auto;
}

.app-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.featured-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 24px;
  margin-bottom: 30px;
}

.featured-filter {
  margin-bottom: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-start;
}

@media (max-width: 1024px) {
  .hero,
  .template-grid,
  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .container {
    padding: 20px 14px 30px;
  }

  .hero,
  .template-grid,
  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 42px;
  }

  .hero-panel {
    transform: none;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
