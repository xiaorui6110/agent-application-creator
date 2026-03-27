<template>
  <div id="userLoginPage">
    <div class="auth-card">
      <div class="auth-head">
        <div class="auth-kicker">来吧，三分钟搞定</div>
        <h2 class="title">登录</h2>
        <div class="desc">你写一句话，我去折腾。</div>
      </div>

      <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
        <a-form-item name="userEmail" :rules="[{ required: true, message: '请输入邮箱' }]">
          <a-input v-model:value="formState.userEmail" placeholder="邮箱，比如：me@xx.com" />
        </a-form-item>
        <a-form-item
          name="loginPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, message: '密码长度不能小于 8 位' },
          ]"
        >
          <a-input-password v-model:value="formState.loginPassword" placeholder="密码（至少 8 位）" />
        </a-form-item>
        <a-form-item name="verifyCode" :rules="[{ required: true, message: '请输入验证码' }]">
          <a-row :gutter="8" :wrap="false" style="width: 100%">
            <a-col flex="auto">
              <a-input v-model:value="formState.verifyCode" placeholder="验证码" />
            </a-col>
            <a-col flex="140px">
              <a-button block @click="refreshCaptcha">换一张</a-button>
            </a-col>
          </a-row>
          <div v-if="captchaSrc" class="captcha-img">
            <img :src="captchaSrc" alt="captcha" />
          </div>
        </a-form-item>

        <div class="tips-row">
          <div class="tip-left">
            没账号？
            <RouterLink to="/user/register">去注册</RouterLink>
          </div>
          <RouterLink class="tip-right" to="/user/reset-password">忘记密码</RouterLink>
        </div>

        <a-form-item>
          <a-button type="primary" html-type="submit" class="submit-btn">进来</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { getPictureVerifyCode, userLogin } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { isSuccessResponse } from '@/utils/apiResponse'

const formState = reactive<API.UserLoginRequest>({
  userEmail: '',
  loginPassword: '',
  verifyCode: '',
  serverVerifyCode: '',
})

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const captchaBase64 = ref('')
const serverVerifyCode = ref('')

const captchaSrc = computed(() => {
  if (!captchaBase64.value) return ''
  if (captchaBase64.value.startsWith('data:')) return captchaBase64.value
  return `data:image/png;base64,${captchaBase64.value}`
})

const refreshCaptcha = async () => {
  const res = await getPictureVerifyCode()
  if (isSuccessResponse(res.data) && res.data.data) {
    const data = res.data.data as unknown as { base64Captcha?: string; encryptedCaptcha?: string }
    captchaBase64.value = data.base64Captcha ?? ''
    serverVerifyCode.value = data.encryptedCaptcha ?? ''
    formState.serverVerifyCode = serverVerifyCode.value
    return
  }
  message.error(res.data.msg ?? '获取验证码失败')
}

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: Record<string, unknown>) => {
  const loginBody: API.UserLoginRequest = {
    userEmail: String(values.userEmail ?? '').trim(),
    loginPassword: String(values.loginPassword ?? ''),
    verifyCode: String(values.verifyCode ?? '').trim(),
    serverVerifyCode: serverVerifyCode.value,
  }
  const res = await userLogin({}, loginBody)
  // 登录成功，把登录态保存到全局状态中
  if (isSuccessResponse(res.data)) {
    if (res.data.data) {
      loginUserStore.setLoginUser(res.data.data)
    } else {
      await loginUserStore.fetchLoginUser()
    }
    message.success('登录成功')

    const redirect = route.query.redirect
    const redirectPath = typeof redirect === 'string' && redirect.startsWith('/') ? redirect : '/'
    router.replace({
      path: redirectPath,
    })
  } else {
    message.error('登录失败，' + (res.data.msg ?? ''))
    await refreshCaptcha()
  }
}

onMounted(async () => {
  await refreshCaptcha()
})
</script>

<style scoped>
#userLoginPage {
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

.captcha-img {
  margin-top: 10px;
  border-radius: 12px;
  border: 1px dashed rgba(17, 24, 39, 0.14);
  background: rgba(255, 255, 255, 0.7);
  overflow: hidden;
}

.captcha-img img {
  display: block;
  width: 100%;
  height: 56px;
  object-fit: contain;
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
