import { normalizeCodeGenType } from '@/utils/codeGenTypes'

export const DEPLOY_DOMAIN = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://172.19.48.249'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'

export const STATIC_BASE_URL = `${API_BASE_URL}/static`

export const getDeployUrl = (deployKey: string) => {
  return `${DEPLOY_DOMAIN}/${deployKey}`
}

export const getStaticPreviewUrl = (codeGenType: string, appId: string) => {
  normalizeCodeGenType(codeGenType)
  const baseUrl = `${STATIC_BASE_URL}/preview/${appId}/`
  return baseUrl
}
