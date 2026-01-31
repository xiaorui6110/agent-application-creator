export type ChatMessageRole = 'user' | 'ai'

export const getChatMessageRole = (chatMessageType?: string): ChatMessageRole => {
  const raw = (chatMessageType ?? '').trim()
  const upper = raw.toUpperCase()
  if (upper === 'USER') return 'user'
  if (upper === 'AI') return 'ai'

  const lower = raw.toLowerCase()
  if (lower === 'user') return 'user'
  if (lower === 'ai') return 'ai'
  if (lower === 'assistant') return 'ai'

  return 'ai'
}

export const isUserChatMessage = (chatMessageType?: string) => {
  return getChatMessageRole(chatMessageType) === 'user'
}

export const formatChatMessageType = (chatMessageType?: string) => {
  return isUserChatMessage(chatMessageType) ? '用户消息' : 'AI消息'
}

