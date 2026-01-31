<template>
  <div id="userRegisterPage">
    <div class="auth-card">
      <div class="auth-head">
        <div class="auth-kicker">先领一把钥匙</div>
        <h2 class="title">注册</h2>
        <div class="desc">邮箱验证码，60 秒有效。</div>
      </div>

      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userEmail" :rules="[{ required: true, message: '请输入邮箱' }]">
          <a-input v-model:value="formState.userEmail" placeholder="邮箱，比如：me@xx.com" />
        </a-form-item>
        <a-form-item
          name="loginPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.loginPassword" placeholder="密码（至少 8 位）" />
        </a-form-item>
        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { min: 8, message: '密码不能小于 8 位' },
            { validator: validateCheckPassword },
          ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="再输一遍，别手滑" />
        </a-form-item>
        <a-form-item name="emailVerifyCode" :rules="[{ required: true, message: '请输入邮箱验证码' }]">
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

        <div class="tips-row">
          <div class="tip-left">
            已有账号？
            <RouterLink to="/user/login">去登录</RouterLink>
          </div>
        </div>

        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-btn">创建账号</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { sendEmailCode, userRegister } from '@/api/userController.ts'
import { message } from 'ant-design-vue'
import { reactive } from 'vue'
import { isSuccessResponse } from '@/utils/apiResponse'
import { useEmailCodeSender } from '@/composables/useEmailCodeSender'

const router = useRouter()

const formState = reactive<API.UserRegisterRequest>({
  userEmail: '',
  loginPassword: '',
  checkPassword: '',
  emailVerifyCode: '',
})

const emailCode = useEmailCodeSender({
  send: async (email) => sendEmailCode({}, { userEmail: email, type: 'register' }),
})

const handleSendEmailCode = async () => {
  if (!formState.userEmail) {
    message.error('请先输入邮箱')
    return
  }
  const ok = await emailCode.sendCode(formState.userEmail)
  if (ok) {
    message.success('验证码已发送')
  } else {
    message.error('验证码发送失败')
  }
}

/**
 * 验证确认密码
 * @param rule
 * @param value
 * @param callback
 */
const validateCheckPassword = (rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (value && value !== formState.loginPassword) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await userRegister({}, values)
  // 注册成功，跳转到登录页面
  if (isSuccessResponse(res.data)) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败，' + (res.data.msg ?? ''))
  }
}

</script>

<style scoped>
#userRegisterPage {
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

.submit-btn {
  width: 100%;
  height: 42px;
  border-radius: 12px;
}
</style>
