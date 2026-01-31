<template>
  <div id="userResetPasswordPage">
    <div class="auth-card">
      <div class="auth-head">
        <div class="auth-kicker">别慌，能找回来</div>
        <h2 class="title">重置密码</h2>
        <div class="desc">邮箱验证码，60 秒内搞定。</div>
      </div>

      <a-form :model="formState" layout="vertical" @finish="handleSubmit">
        <a-form-item label="邮箱" name="userEmail" :rules="[{ required: true, message: '请输入邮箱' }]">
          <a-input v-model:value="formState.userEmail" placeholder="邮箱，比如：me@xx.com" />
        </a-form-item>

        <a-form-item
          label="邮箱验证码"
          name="emailVerifyCode"
          :rules="[{ required: true, message: '请输入邮箱验证码' }]"
        >
          <a-row :gutter="8" :wrap="false" style="width: 100%">
            <a-col flex="auto">
              <a-input v-model:value="formState.emailVerifyCode" placeholder="邮箱验证码" />
            </a-col>
            <a-col flex="160px">
              <a-button
                block
                :disabled="emailCode.sending.value || emailCode.countdown.value > 0"
                @click="handleSendEmailCode"
              >
                {{
                  emailCode.countdown.value > 0
                    ? `${emailCode.countdown.value}s 后重试`
                    : emailCode.sending.value
                      ? '发送中...'
                      : '发验证码'
                }}
              </a-button>
            </a-col>
          </a-row>
        </a-form-item>

        <a-form-item
          label="新密码"
          name="newPassword"
          :rules="[
            { required: true, message: '请输入新密码' },
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.newPassword" placeholder="新密码（至少 8 位）" />
        </a-form-item>

        <a-form-item
          label="确认密码"
          name="checkPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: validateCheckPassword, trigger: 'blur' },
          ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="再输一遍，别手滑" />
        </a-form-item>

        <div class="tips-row">
          <RouterLink to="/user/login">返回登录</RouterLink>
          <RouterLink class="tip-right" to="/user/register">去注册</RouterLink>
        </div>

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="submitting" class="submit-btn">
            重新上路
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { resetUserPassword, sendEmailCode } from '@/api/userController.ts'
import { isSuccessResponse } from '@/utils/apiResponse'
import { useEmailCodeSender } from '@/composables/useEmailCodeSender'

const router = useRouter()

const submitting = ref(false)
const formState = reactive<API.UserResetPasswordRequest>({
  userEmail: '',
  newPassword: '',
  checkPassword: '',
  emailVerifyCode: '',
})

const emailCode = useEmailCodeSender({
  send: async (email) => sendEmailCode({}, { userEmail: email, type: 'resetPassword' }),
})

const handleSendEmailCode = async () => {
  const email = String(formState.userEmail ?? '').trim()
  if (!email) {
    message.error('请先输入邮箱')
    return
  }
  const ok = await emailCode.sendCode(email)
  if (ok) {
    message.success('验证码已发送')
  }
}

const validateCheckPassword = (_: unknown, value: string) => {
  if (value && value !== formState.newPassword) {
    return Promise.reject(new Error('两次输入密码不一致'))
  }
  return Promise.resolve()
}

const handleSubmit = async () => {
  const email = String(formState.userEmail ?? '').trim()
  if (!email) {
    message.error('请输入邮箱')
    return
  }

  submitting.value = true
  try {
    const body: API.UserResetPasswordRequest = {
      userEmail: email,
      newPassword: String(formState.newPassword ?? ''),
      checkPassword: String(formState.checkPassword ?? ''),
      emailVerifyCode: String(formState.emailVerifyCode ?? '').trim(),
    }
    const res = await resetUserPassword({}, body)
    if (isSuccessResponse(res.data) && res.data.data) {
      message.success('密码重置成功，请登录')
      router.replace('/user/login')
      return
    }
    message.error(res.data.msg ?? '密码重置失败')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
#userResetPasswordPage {
  min-height: calc(100vh - 72px);
  padding: 32px 16px 48px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(17, 24, 39, 0.10);
  border-radius: 18px;
  padding: 22px 20px 18px;
  box-shadow:
    0 1px 0 rgba(255, 255, 255, 0.8) inset,
    0 20px 40px rgba(17, 24, 39, 0.08);
  backdrop-filter: blur(10px);
}

.auth-head {
  margin-bottom: 16px;
}

.auth-kicker {
  display: inline-flex;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(17, 24, 39, 0.05);
  color: rgba(17, 24, 39, 0.7);
  font-size: 12px;
}

.title {
  margin: 10px 0 6px;
  font-size: 28px;
  letter-spacing: -0.03em;
  color: rgba(17, 24, 39, 0.92);
}

.desc {
  color: rgba(17, 24, 39, 0.56);
  margin-bottom: 0;
}

.tips-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: rgba(17, 24, 39, 0.56);
  font-size: 13px;
  margin: 8px 0 14px;
}

.tips-row a {
  color: var(--brand);
}

.tip-right {
  white-space: nowrap;
}

.submit-btn {
  width: 100%;
  height: 42px;
  border-radius: 12px;
}
</style>
