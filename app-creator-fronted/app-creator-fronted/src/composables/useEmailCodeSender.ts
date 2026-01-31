import { onUnmounted, ref } from 'vue'
import { isSuccessResponse } from '@/utils/apiResponse'

export const useEmailCodeSender = (options: {
  send: (email: string) => Promise<{ data: API.ServerResponseEntityBoolean }>
  cooldownSeconds?: number
}) => {
  const sending = ref(false)
  const countdown = ref(0)
  let timer: number | undefined

  const startCountdown = (seconds: number) => {
    countdown.value = seconds
    timer = window.setInterval(() => {
      countdown.value -= 1
      if (countdown.value <= 0) {
        if (timer) window.clearInterval(timer)
        timer = undefined
        countdown.value = 0
      }
    }, 1000)
  }

  const sendCode = async (email: string) => {
    const trimmed = String(email ?? '').trim()
    if (!trimmed) return false
    if (sending.value || countdown.value > 0) return false

    sending.value = true
    try {
      const res = await options.send(trimmed)
      if (isSuccessResponse(res.data) && res.data.data) {
        startCountdown(options.cooldownSeconds ?? 60)
        return true
      }
      return false
    } finally {
      sending.value = false
    }
  }

  onUnmounted(() => {
    if (timer) window.clearInterval(timer)
  })

  return { sending, countdown, sendCode }
}

