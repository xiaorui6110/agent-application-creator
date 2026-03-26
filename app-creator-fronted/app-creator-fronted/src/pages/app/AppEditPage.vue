<template>
  <div id="appEditPage">
    <section class="hero">
      <div class="hero__copy">
        <div class="hero__eyebrow">App Studio</div>
        <h1>{{ appInfo?.appName || '应用工作台' }}</h1>
        <p>在这里维护应用基础信息，查看历史版本，并将当前应用沉淀为可复用模板。</p>
      </div>
      <div class="hero__stats">
        <div class="stat-card">
          <span class="stat-card__label">版本数</span>
          <strong>{{ versions.length }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-card__label">部署状态</span>
          <strong>{{ appInfo?.deployUrl ? '已部署' : '未部署' }}</strong>
        </div>
        <div class="stat-card">
          <span class="stat-card__label">当前类型</span>
          <strong>{{ formatCodeGenType(appInfo?.codeGenType) || '未知' }}</strong>
        </div>
      </div>
    </section>

    <a-row :gutter="[20, 20]">
      <a-col :xs="24" :xl="16">
        <a-card :bordered="false" class="panel-card">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="basic" tab="基础信息">
              <a-form ref="formRef" :model="formData" :rules="rules" layout="vertical" @finish="handleSubmit">
                <a-form-item label="应用名称" name="appName">
                  <a-input v-model:value="formData.appName" :maxlength="50" show-count />
                </a-form-item>

                <a-form-item
                  v-if="isAdmin"
                  label="应用封面"
                  name="appCover"
                  extra="支持图片链接，建议使用横向 4:3 尺寸。"
                >
                  <a-input v-model:value="formData.appCover" placeholder="https://example.com/cover.png" />
                  <div v-if="formData.appCover" class="cover-preview">
                    <a-image :src="formData.appCover" :width="240" :height="160" style="object-fit: cover" />
                  </div>
                </a-form-item>

                <a-form-item label="初始化提示词">
                  <a-textarea
                    v-model:value="formData.appInitPrompt"
                    :rows="5"
                    :maxlength="1000"
                    show-count
                    disabled
                  />
                  <div class="field-tip">初始化提示词当前仅支持查看，不支持直接编辑。</div>
                </a-form-item>

                <a-form-item label="代码生成类型">
                  <a-input :value="formatCodeGenType(formData.codeGenType) || '-'" disabled />
                </a-form-item>

                <a-space>
                  <a-button type="primary" html-type="submit" :loading="submitting">保存修改</a-button>
                  <a-button @click="resetForm">重置</a-button>
                  <a-button type="link" @click="goToChat">进入对话</a-button>
                  <a-button v-if="appInfo?.deployUrl" type="link" @click="openPreview">查看预览</a-button>
                </a-space>
              </a-form>
            </a-tab-pane>

            <a-tab-pane key="versions" :tab="`版本管理 (${versions.length})`">
              <div class="section-header">
                <div>
                  <h3>历史版本</h3>
                  <p>支持查看系统自动快照，并恢复到指定版本。</p>
                </div>
                <a-button @click="fetchVersions" :loading="versionsLoading">刷新</a-button>
              </div>

              <a-empty v-if="!versionsLoading && versions.length === 0" description="当前还没有历史版本" />

              <div v-else class="version-list">
                <div v-for="version in versions" :key="version.appVersionId" class="version-card">
                  <div class="version-card__head">
                    <div>
                      <div class="version-card__title">
                        <span>v{{ version.versionNumber ?? '-' }}</span>
                        <a-tag :color="versionSourceColor(version.versionSource)">
                          {{ version.versionSource || 'UNKNOWN' }}
                        </a-tag>
                      </div>
                      <div class="version-card__meta">
                        <span>{{ formatTime(version.createTime) }}</span>
                        <span v-if="version.createdBy">创建人：{{ version.createdBy }}</span>
                        <span v-if="version.entryFile">入口：{{ version.entryFile }}</span>
                      </div>
                    </div>
                    <a-popconfirm
                      title="确认恢复到这个版本吗？"
                      ok-text="恢复"
                      cancel-text="取消"
                      @confirm="handleRestoreVersion(version)"
                    >
                      <a-button type="primary" ghost :loading="restoringVersionId === version.appVersionId">
                        恢复此版本
                      </a-button>
                    </a-popconfirm>
                  </div>
                  <p class="version-card__note">
                    {{ version.versionNote || '该版本暂无备注信息。' }}
                  </p>
                  <div v-if="version.deployUrl" class="version-card__footer">
                    <a-button type="link" @click="openVersionDeploy(version.deployUrl)">打开当时部署地址</a-button>
                  </div>
                </div>
              </div>
            </a-tab-pane>

            <a-tab-pane key="template" tab="模板化">
              <div class="section-header">
                <div>
                  <h3>从当前应用创建模板</h3>
                  <p>把当前应用沉淀为模板，后续可以在首页直接复用创建新应用。</p>
                </div>
              </div>

              <a-form layout="vertical">
                <a-form-item label="模板名称">
                  <a-input
                    v-model:value="templateForm.templateName"
                    :maxlength="50"
                    show-count
                    placeholder="例如：极简博客模板"
                  />
                </a-form-item>
                <a-form-item label="模板描述">
                  <a-textarea
                    v-model:value="templateForm.templateDescription"
                    :rows="4"
                    :maxlength="300"
                    show-count
                    placeholder="描述这个模板适合什么场景、风格和结构。"
                  />
                </a-form-item>
                <a-space>
                  <a-button type="primary" :loading="templateSubmitting" @click="handleCreateTemplate">
                    保存为模板
                  </a-button>
                  <a-button @click="fillTemplateDefaults">一键带入当前应用信息</a-button>
                </a-space>
              </a-form>

              <a-divider />

              <div class="template-tip">
                <span>模板创建完成后，会出现在首页模板区中，可直接用于快速创建应用。</span>
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>

      <a-col :xs="24" :xl="8">
        <a-card :bordered="false" class="panel-card side-card">
          <template #title>应用信息</template>
          <a-descriptions :column="1" size="small" bordered>
            <a-descriptions-item label="应用 ID">{{ appInfo?.appId || '-' }}</a-descriptions-item>
            <a-descriptions-item label="创建者">
              <UserInfo :user="appInfo?.userVO" size="small" />
            </a-descriptions-item>
            <a-descriptions-item label="创建时间">
              {{ formatTime(appInfo?.createTime) || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="更新时间">
              {{ formatTime(appInfo?.updateTime) || '-' }}
            </a-descriptions-item>
            <a-descriptions-item label="部署时间">
              {{ appInfo?.deployedTime ? formatTime(appInfo.deployedTime) : '未部署' }}
            </a-descriptions-item>
            <a-descriptions-item label="访问地址">
              <a-button v-if="appInfo?.deployUrl" type="link" @click="openPreview">打开预览</a-button>
              <span v-else>未部署</span>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import {
  createTemplate,
  getAppInfoById,
  getAppInfoByIdByAdmin,
  listAppVersions,
  restoreAppVersion,
  updateApp,
  updateAppByAdmin,
} from '@/api/AppController'
import { isSuccessResponse } from '@/utils/apiResponse'
import { formatCodeGenType } from '@/utils/codeGenTypes'
import { formatTime } from '@/utils/time'
import UserInfo from '@/components/UserInfo.vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()

const appInfo = ref<API.AppVO>()
const versions = ref<API.AppVersionVO[]>([])
const submitting = ref(false)
const versionsLoading = ref(false)
const templateSubmitting = ref(false)
const restoringVersionId = ref<string>()
const formRef = ref<FormInstance>()
const activeTab = ref('basic')

const formData = reactive({
  appName: '',
  appCover: '',
  appInitPrompt: '',
  codeGenType: '',
})

const templateForm = reactive({
  templateName: '',
  templateDescription: '',
})

const isAdmin = computed(() => loginUserStore.loginUser.userRole === 'admin')
const currentAppId = computed(() => String(route.params.id || ''))

const rules = {
  appName: [
    { required: true, message: '请输入应用名称', trigger: 'blur' },
    { min: 1, max: 50, message: '应用名称长度需在 1 到 50 个字符之间', trigger: 'blur' },
  ],
  appCover: [{ type: 'url', message: '请输入有效的图片地址', trigger: 'blur' }],
}

const fetchAppInfo = async () => {
  if (!currentAppId.value) {
    message.error('应用 ID 不存在')
    await router.push('/')
    return
  }

  const res = isAdmin.value
    ? await getAppInfoByIdByAdmin({ appId: currentAppId.value })
    : await getAppInfoById({ appId: currentAppId.value })

  if (!isSuccessResponse(res.data) || !res.data.data) {
    message.error(res.data.msg ?? '获取应用信息失败')
    await router.push('/')
    return
  }

  appInfo.value = res.data.data
  if (!isAdmin.value && appInfo.value.userVO?.userId !== loginUserStore.loginUser.userId) {
    message.error('您没有权限编辑此应用')
    await router.push('/')
    return
  }

  formData.appName = appInfo.value.appName || ''
  formData.appCover = appInfo.value.appCover || ''
  formData.appInitPrompt = appInfo.value.appInitPrompt || ''
  formData.codeGenType = appInfo.value.codeGenType || ''
}

const fetchVersions = async () => {
  if (!currentAppId.value) return
  versionsLoading.value = true
  try {
    const res = await listAppVersions({ appId: currentAppId.value })
    if (isSuccessResponse(res.data) && res.data.data) {
      versions.value = res.data.data
      return
    }
    message.error(res.data.msg ?? '获取版本列表失败')
  } finally {
    versionsLoading.value = false
  }
}

const handleSubmit = async () => {
  if (!appInfo.value?.appId) return
  submitting.value = true
  try {
    const res = isAdmin.value
      ? await updateAppByAdmin(
          {},
          {
            appId: appInfo.value.appId,
            appName: formData.appName,
            appCover: formData.appCover,
          },
        )
      : await updateApp(
          {},
          {
            appId: appInfo.value.appId,
            appName: formData.appName,
          },
        )

    if (isSuccessResponse(res.data) && res.data.data !== false) {
      message.success('应用信息已更新')
      await fetchAppInfo()
      return
    }
    message.error(res.data.msg ?? '更新失败')
  } finally {
    submitting.value = false
  }
}

const handleRestoreVersion = async (version: API.AppVersionVO) => {
  if (!appInfo.value?.appId || !version.appVersionId) return
  restoringVersionId.value = version.appVersionId
  try {
    const res = await restoreAppVersion({}, { appId: appInfo.value.appId, appVersionId: version.appVersionId })
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success(`已恢复到 v${version.versionNumber}`)
      await Promise.all([fetchAppInfo(), fetchVersions()])
      return
    }
    message.error(res.data.msg ?? '恢复版本失败')
  } finally {
    restoringVersionId.value = undefined
  }
}

const fillTemplateDefaults = () => {
  templateForm.templateName = appInfo.value?.appName ? `${appInfo.value.appName} 模板` : ''
  templateForm.templateDescription =
    appInfo.value?.appDescription || appInfo.value?.appInitPrompt || '基于当前应用沉淀的可复用模板'
}

const handleCreateTemplate = async () => {
  if (!appInfo.value?.appId) return
  if (!templateForm.templateName.trim()) {
    message.warning('请输入模板名称')
    return
  }

  templateSubmitting.value = true
  try {
    const res = await createTemplate(
      {},
      {
        appId: appInfo.value.appId,
        templateName: templateForm.templateName.trim(),
        templateDescription: templateForm.templateDescription.trim(),
      },
    )
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('模板创建成功')
      templateForm.templateName = ''
      templateForm.templateDescription = ''
      return
    }
    message.error(res.data.msg ?? '模板创建失败')
  } finally {
    templateSubmitting.value = false
  }
}

const resetForm = () => {
  formData.appName = appInfo.value?.appName || ''
  formData.appCover = appInfo.value?.appCover || ''
  formRef.value?.clearValidate()
}

const goToChat = () => {
  if (appInfo.value?.appId) {
    router.push(`/app/chat/${appInfo.value.appId}`)
  }
}

const openPreview = () => {
  if (appInfo.value?.deployUrl) {
    window.open(appInfo.value.deployUrl, '_blank')
  }
}

const openVersionDeploy = (url?: string) => {
  if (url) {
    window.open(url, '_blank')
  }
}

const versionSourceColor = (source?: string) => {
  if (source === 'DEPLOYED') return 'green'
  if (source === 'RESTORED') return 'blue'
  if (source === 'GENERATED') return 'orange'
  return 'default'
}

onMounted(async () => {
  await Promise.all([fetchAppInfo(), fetchVersions()])
  fillTemplateDefaults()
})
</script>

<style scoped>
#appEditPage {
  max-width: 1180px;
  margin: 0 auto;
  padding: 28px 22px 40px;
}

.hero {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 18px;
  margin-bottom: 22px;
}

.hero__copy,
.hero__stats {
  border-radius: 24px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(255, 248, 238, 0.96), rgba(255, 255, 255, 0.92)), #fff;
  border: 1px solid rgba(225, 147, 64, 0.16);
  box-shadow: 0 18px 48px rgba(158, 98, 28, 0.08);
}

.hero__eyebrow {
  display: inline-flex;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(235, 137, 24, 0.1);
  color: #a65d0e;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero__copy h1 {
  margin: 16px 0 10px;
  font-size: 34px;
  line-height: 1.1;
  color: #1f2937;
}

.hero__copy p {
  margin: 0;
  color: rgba(31, 41, 55, 0.72);
  line-height: 1.7;
}

.hero__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.stat-card {
  border-radius: 18px;
  padding: 18px 16px;
  background: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(17, 24, 39, 0.06);
}

.stat-card__label {
  display: block;
  margin-bottom: 10px;
  font-size: 12px;
  color: rgba(31, 41, 55, 0.56);
}

.stat-card strong {
  font-size: 18px;
  color: #111827;
}

.panel-card {
  border-radius: 24px;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
}

.section-header h3 {
  margin: 0 0 6px;
  font-size: 18px;
}

.section-header p {
  margin: 0;
  color: rgba(17, 24, 39, 0.58);
}

.cover-preview {
  margin-top: 12px;
  padding: 12px;
  border-radius: 18px;
  background: #faf7f2;
}

.field-tip,
.template-tip {
  margin-top: 8px;
  font-size: 13px;
  color: rgba(17, 24, 39, 0.56);
}

.version-list {
  display: grid;
  gap: 14px;
}

.version-card {
  border: 1px solid rgba(17, 24, 39, 0.08);
  border-radius: 18px;
  padding: 18px;
  background: linear-gradient(180deg, #fff, #fcfaf7);
}

.version-card__head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.version-card__title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.version-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 10px;
  font-size: 13px;
  color: rgba(17, 24, 39, 0.58);
}

.version-card__note {
  margin: 14px 0 0;
  line-height: 1.7;
  color: rgba(17, 24, 39, 0.74);
}

.version-card__footer {
  margin-top: 10px;
}

.side-card {
  position: sticky;
  top: 20px;
}

@media (max-width: 992px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .hero__stats {
    grid-template-columns: 1fr 1fr 1fr;
  }

  .side-card {
    position: static;
  }
}

@media (max-width: 640px) {
  #appEditPage {
    padding: 20px 14px 32px;
  }

  .hero__stats {
    grid-template-columns: 1fr;
  }

  .version-card__head {
    flex-direction: column;
  }
}
</style>
