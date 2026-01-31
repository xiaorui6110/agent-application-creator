export const SUCCESS_CODE = '00000'
export const UNAUTHORIZED_CODE = '40100'

export type ApiResponseBase = {
  code?: string
  msg?: string
  success?: boolean
}

export function isSuccessResponse(res: ApiResponseBase | undefined | null): boolean {
  if (!res) return false
  if (typeof res.code === 'string') return res.code === SUCCESS_CODE
  return res.success === true
}

export function isUnauthorizedResponse(res: ApiResponseBase | undefined | null): boolean {
  if (!res) return false
  return res.code === UNAUTHORIZED_CODE
}
