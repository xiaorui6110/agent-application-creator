export const extractAgentReplyFromText = (raw: string): string => {
  const text = (raw ?? '').trim()
  if (!text) return ''

  const firstChar = text[0]
  if (firstChar !== '{' && firstChar !== '[') {
    return raw
  }

  try {
    const obj = JSON.parse(text)
    if (typeof obj === 'string') {
      return obj
    }

    const isRecord = (value: unknown): value is Record<string, unknown> => {
      return typeof value === 'object' && value !== null
    }

    const get = (value: unknown, key: string): unknown => {
      return isRecord(value) ? value[key] : undefined
    }

    const getString = (value: unknown, key: string): string | undefined => {
      const v = get(value, key)
      return typeof v === 'string' && v.trim() ? v : undefined
    }

    const directReply = getString(obj, 'reply')
    if (directReply) return directReply

    const nestedReply = getString(get(obj, 'agentResponse'), 'reply')
    if (nestedReply) return nestedReply

    const structuredDescription = getString(get(obj, 'structuredReply'), 'description')
    if (structuredDescription) return structuredDescription

    const nestedStructuredDescription = getString(
      get(get(obj, 'agentResponse'), 'structuredReply'),
      'description',
    )
    if (nestedStructuredDescription) return nestedStructuredDescription

    const intentSummary = getString(obj, 'intentSummary')
    if (intentSummary) return intentSummary

    const nestedIntentSummary = getString(get(obj, 'agentResponse'), 'intentSummary')
    if (nestedIntentSummary) return nestedIntentSummary
  } catch {
    return raw
  }

  return raw
}
