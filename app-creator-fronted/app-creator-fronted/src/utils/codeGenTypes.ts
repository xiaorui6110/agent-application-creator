/**
 * 代码生成类型枚举
 */
export enum CodeGenTypeEnum {
  SINGLE_FILE = 'single_file',
  MULTI_FILE = 'multi_file',
  VUE_PROJECT = 'vue_project',
}

/**
 * 代码生成类型配置
 */
export const CODE_GEN_TYPE_CONFIG = {
  [CodeGenTypeEnum.SINGLE_FILE]: {
    label: '原生 HTML 模式',
    value: CodeGenTypeEnum.SINGLE_FILE,
  },
  [CodeGenTypeEnum.MULTI_FILE]: {
    label: '原生多文件模式',
    value: CodeGenTypeEnum.MULTI_FILE,
  },
  [CodeGenTypeEnum.VUE_PROJECT]: {
    label: 'Vue 项目模式',
    value: CodeGenTypeEnum.VUE_PROJECT,
  },
} as const

/**
 * 代码生成类型选项（用于下拉选择）
 */
export const CODE_GEN_TYPE_OPTIONS = Object.values(CODE_GEN_TYPE_CONFIG).map((config) => ({
  label: config.label,
  value: config.value,
}))

export const coerceCodeGenTypeValue = (type: string | undefined): CodeGenTypeEnum | undefined => {
  const lower = (type ?? '').trim().toLowerCase()
  if (!lower) return undefined
  if (lower === CodeGenTypeEnum.SINGLE_FILE) return CodeGenTypeEnum.SINGLE_FILE
  if (lower === CodeGenTypeEnum.MULTI_FILE) return CodeGenTypeEnum.MULTI_FILE
  if (lower === CodeGenTypeEnum.VUE_PROJECT) return CodeGenTypeEnum.VUE_PROJECT
  return undefined
}

/**
 * 格式化代码生成类型
 * @param type 代码生成类型
 * @returns 格式化后的类型描述
 */
export const formatCodeGenType = (type: string | undefined): string => {
  if (!type) return '未知类型'

  const coerced = coerceCodeGenTypeValue(type)
  if (!coerced) return '未知类型'
  return CODE_GEN_TYPE_CONFIG[coerced].label
}

export const normalizeCodeGenType = (type: string): string => {
  return coerceCodeGenTypeValue(type) ?? CodeGenTypeEnum.SINGLE_FILE
}

/**
 * 获取所有代码生成类型
 */
export const getAllCodeGenTypes = () => {
  return Object.values(CodeGenTypeEnum)
}

/**
 * 检查是否为有效的代码生成类型
 * @param type 待检查的类型
 */
export const isValidCodeGenType = (type: string): type is CodeGenTypeEnum => {
  return Object.values(CodeGenTypeEnum).includes(type as CodeGenTypeEnum)
}
