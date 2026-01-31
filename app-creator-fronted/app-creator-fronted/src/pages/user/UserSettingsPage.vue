<template>
  <div id="userSettingsPage">
    <a-card class="settings-card" title="账号设置" :loading="loading">
      <a-tabs v-model:activeKey="activeTab" class="settings-tabs">
        <a-tab-pane key="profile" tab="个人资料">
          <div class="tab-body">
            <a-form layout="vertical" :model="profileForm" @finish="handleUpdateProfile" class="settings-form">
            <a-form-item label="昵称" name="nickName" :rules="[{ required: true, message: '请输入昵称' }]">
              <a-input v-model:value="profileForm.nickName" placeholder="请输入昵称" :maxlength="50" />
            </a-form-item>

            <a-form-item label="性别" name="userSex">
              <a-select v-model:value="profileForm.userSex" allow-clear placeholder="请选择性别">
                <a-select-option value="m">男</a-select-option>
                <a-select-option value="f">女</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="生日" name="userBirthday">
              <a-date-picker v-model:value="profileForm.userBirthday" style="width: 100%" />
            </a-form-item>

            <a-form-item label="简介" name="userProfile">
              <a-textarea v-model:value="profileForm.userProfile" :rows="4" :maxlength="200" show-count />
            </a-form-item>

            <a-button type="primary" html-type="submit" :loading="savingProfile">保存资料</a-button>
            </a-form>

            <a-divider class="soft-divider" />

            <a-form layout="vertical" class="settings-form">
              <a-form-item label="头像">
                <a-space class="avatar-row">
                  <a-avatar :src="loginUserStore.loginUser.userAvatar" :size="64" />
                  <a-upload
                    :showUploadList="false"
                    :beforeUpload="beforeAvatarUpload"
                    :customRequest="handleUploadAvatar"
                  >
                    <a-button :loading="uploadingAvatar">换一张</a-button>
                  </a-upload>
                </a-space>
              </a-form-item>
            </a-form>
          </div>
        </a-tab-pane>

        <a-tab-pane key="email" tab="修改邮箱">
          <div class="tab-body">
            <a-form layout="vertical" :model="emailForm" @finish="handleChangeEmail" class="settings-form">
            <a-form-item
              label="新邮箱"
              name="newEmail"
              :rules="[{ required: true, message: '请输入新邮箱' }, { type: 'email', message: '邮箱格式不正确' }]"
            >
              <a-input v-model:value="emailForm.newEmail" placeholder="请输入新邮箱" />
            </a-form-item>

            <a-form-item
              label="邮箱验证码"
              name="emailVerifyCode"
              :rules="[{ required: true, message: '请输入邮箱验证码' }]"
            >
              <a-row :gutter="8" :wrap="false" style="width: 100%">
                <a-col flex="auto">
                  <a-input v-model:value="emailForm.emailVerifyCode" placeholder="请输入邮箱验证码" />
                </a-col>
                <a-col flex="160px">
                  <a-button
                    block
                    :disabled="emailCode.sending.value || emailCode.countdown.value > 0"
                    @click="handleSendChangeEmailCode"
                  >
                    {{
                      emailCode.countdown.value > 0
                        ? `${emailCode.countdown.value}s 后重试`
                        : emailCode.sending.value
                          ? '发送中...'
                          : '发送验证码'
                    }}
                  </a-button>
                </a-col>
              </a-row>
            </a-form-item>

            <a-button type="primary" html-type="submit" :loading="savingEmail">修改邮箱</a-button>
            </a-form>
          </div>
        </a-tab-pane>

        <a-tab-pane key="password" tab="修改密码">
          <div class="tab-body">
            <a-form layout="vertical" :model="passwordForm" @finish="handleChangePassword" class="settings-form">
            <a-form-item label="原密码" name="oldPassword" :rules="[{ required: true, message: '请输入原密码' }]">
              <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" />
            </a-form-item>

            <a-form-item
              label="新密码"
              name="newPassword"
              :rules="[
                { required: true, message: '请输入新密码' },
                { min: 8, message: '密码长度不能小于 8 位' },
              ]"
            >
              <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
            </a-form-item>

            <a-form-item
              label="确认密码"
              name="checkPassword"
              :rules="[
                { required: true, message: '请确认密码' },
                { validator: validatePasswordCheck, trigger: 'blur' },
              ]"
            >
              <a-input-password v-model:value="passwordForm.checkPassword" placeholder="请确认密码" />
            </a-form-item>

            <a-alert
              type="warning"
              show-icon
              message="修改密码成功后将强制退出登录"
              style="margin-bottom: 12px"
            />

            <a-button type="primary" danger html-type="submit" :loading="savingPassword">修改密码</a-button>
            </a-form>
          </div>
        </a-tab-pane>
      </a-tabs>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import dayjs, { type Dayjs } from 'dayjs'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import {
  changUserEmail,
  changeUserPassword,
  sendEmailCode,
  updateUserAvatar,
  updateUserInfo,
  userLogout,
} from '@/api/userController.ts'
import { isSuccessResponse } from '@/utils/apiResponse'
import { useEmailCodeSender } from '@/composables/useEmailCodeSender'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const activeTab = ref('profile')
const loading = ref(true)

const savingProfile = ref(false)
const uploadingAvatar = ref(false)
const savingEmail = ref(false)
const savingPassword = ref(false)

type ProfileFormState = {
  nickName: string
  userSex?: string
  userBirthday?: Dayjs
  userProfile?: string
}

const profileForm = reactive<ProfileFormState>({
  nickName: '',
  userSex: undefined,
  userBirthday: undefined,
  userProfile: '',
})

const emailForm = reactive<API.UserChangeEmailRequest>({
  newEmail: '',
  emailVerifyCode: '',
})

const passwordForm = reactive<API.UserChangePasswordRequest>({
  oldPassword: '',
  newPassword: '',
  checkPassword: '',
})

const currentUserId = computed(() => loginUserStore.loginUser.userId)

const emailCode = useEmailCodeSender({
  send: async (email) => sendEmailCode({}, { userEmail: email, type: 'changeEmail' }),
})

const hydrateFromLoginUser = async () => {
  await loginUserStore.fetchLoginUser()

  profileForm.nickName = loginUserStore.loginUser.nickName ?? ''
  profileForm.userSex = loginUserStore.loginUser.userSex
  profileForm.userProfile = loginUserStore.loginUser.userProfile ?? ''

  const birthday = loginUserStore.loginUser.userBirthday
  profileForm.userBirthday = birthday ? dayjs(birthday) : undefined
}

const handleUpdateProfile = async () => {
  if (!currentUserId.value) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  savingProfile.value = true
  try {
    const body: API.UserUpdateInfoRequest = {
      userId: currentUserId.value,
      nickName: String(profileForm.nickName ?? '').trim(),
      userSex: profileForm.userSex,
      userBirthday: profileForm.userBirthday ? profileForm.userBirthday.format('YYYY-MM-DD') : undefined,
      userProfile: profileForm.userProfile,
    }
    const res = await updateUserInfo({}, body)
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('资料已更新')
      await hydrateFromLoginUser()
      return
    }
    message.error(res.data.msg ?? '更新失败')
  } finally {
    savingProfile.value = false
  }
}

const beforeAvatarUpload = () => {
  return true
}

type UploadRequestOptions = {
  file?: File
  onSuccess?: (value: unknown) => void
  onError?: (error: unknown) => void
}

const handleUploadAvatar = async (options: UploadRequestOptions) => {
  const file = options?.file
  if (!file) return

  uploadingAvatar.value = true
  try {
    const res = await updateUserAvatar({}, { multipartFile: file })

    if (isSuccessResponse(res.data)) {
      message.success('头像已更新')
      await hydrateFromLoginUser()
      options?.onSuccess?.(res.data)
      return
    }
    options?.onError?.(new Error(res.data.msg ?? '上传失败'))
    message.error(res.data.msg ?? '上传失败')
  } catch (e) {
    options?.onError?.(e)
    message.error('上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

const handleSendChangeEmailCode = async () => {
  const email = String(emailForm.newEmail ?? '').trim()
  if (!email) {
    message.error('请先输入新邮箱')
    return
  }
  const ok = await emailCode.sendCode(email)
  if (ok) {
    message.success('验证码已发送')
  }
}

const handleChangeEmail = async () => {
  const email = String(emailForm.newEmail ?? '').trim()
  if (!email) {
    message.error('请输入新邮箱')
    return
  }

  savingEmail.value = true
  try {
    const body: API.UserChangeEmailRequest = {
      newEmail: email,
      emailVerifyCode: String(emailForm.emailVerifyCode ?? '').trim(),
    }
    const res = await changUserEmail({}, body)
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('邮箱已修改')
      emailForm.emailVerifyCode = ''
      await hydrateFromLoginUser()
      return
    }
    message.error(res.data.msg ?? '修改失败')
  } finally {
    savingEmail.value = false
  }
}

const validatePasswordCheck = (_: unknown, value: string) => {
  if (value && value !== passwordForm.newPassword) {
    return Promise.reject(new Error('两次输入密码不一致'))
  }
  return Promise.resolve()
}

const handleChangePassword = async () => {
  savingPassword.value = true
  try {
    const body: API.UserChangePasswordRequest = {
      oldPassword: String(passwordForm.oldPassword ?? ''),
      newPassword: String(passwordForm.newPassword ?? ''),
      checkPassword: String(passwordForm.checkPassword ?? ''),
    }
    const res = await changeUserPassword({}, body)
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('密码已修改，请重新登录')
      await userLogout()
      loginUserStore.clearLoginUser()
      await router.replace('/user/login')
      return
    }
    message.error(res.data.msg ?? '修改失败')
  } finally {
    savingPassword.value = false
  }
}

onMounted(async () => {
  await hydrateFromLoginUser()
  loading.value = false
})
</script>

<style scoped>
#userSettingsPage {
  max-width: 1040px;
  margin: 24px auto;
  padding: 0 16px;
}

.settings-card {
  border-radius: 18px;
  border: 1px solid rgba(17, 24, 39, 0.10);
  background: rgba(255, 255, 255, 0.86);
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.8) inset,
    0 20px 48px rgba(17, 24, 39, 0.08);
  backdrop-filter: blur(10px);
}

:deep(.settings-card .ant-card-head) {
  border-bottom: 1px solid rgba(17, 24, 39, 0.08);
}

:deep(.settings-card .ant-card-head-title) {
  font-size: 16px;
  color: rgba(17, 24, 39, 0.92);
}

.settings-tabs {
  margin-top: 4px;
}

:deep(.settings-tabs .ant-tabs-nav) {
  margin: 0;
}

:deep(.settings-tabs .ant-tabs-ink-bar) {
  background: var(--brand);
}

:deep(.settings-tabs .ant-tabs-tab) {
  padding: 10px 0;
  font-size: 14px;
}

:deep(.settings-tabs .ant-tabs-tab + .ant-tabs-tab) {
  margin-left: 22px;
}

:deep(.settings-tabs .ant-tabs-tab.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: var(--brand);
}

.tab-body {
  padding-top: 12px;
}

.settings-form {
  max-width: 520px;
}

:deep(.settings-form .ant-input),
:deep(.settings-form .ant-input-affix-wrapper),
:deep(.settings-form .ant-select-selector),
:deep(.settings-form .ant-picker) {
  border-radius: 12px;
}

:deep(.settings-form .ant-btn) {
  border-radius: 12px;
}

.soft-divider {
  border-top-color: rgba(17, 24, 39, 0.08);
}

.avatar-row {
  align-items: center;
}
</style>
