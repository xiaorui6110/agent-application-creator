import { API_BASE_URL } from '@/config/env'

export interface AgentTaskStreamEvent<T = API.SystemOutput> {
  event?: string
  taskId?: string
  threadId?: string
  appId?: string
  taskStatus?: string
  message?: string
  agentName?: string
  done?: boolean
  timestamp?: number
  result?: T
}

interface ConsumeAgentTaskStreamOptions<T = API.SystemOutput> {
  taskId: string
  signal?: AbortSignal
  onEvent?: (event: AgentTaskStreamEvent<T>) => void | Promise<void>
}

const parseSseChunk = async <T>(
  chunk: string,
  onEvent?: (event: AgentTaskStreamEvent<T>) => void | Promise<void>,
) => {
  const lines = chunk
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter(Boolean)

  if (!lines.length) return

  let eventName = ''
  const dataLines: string[] = []

  for (const line of lines) {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trim())
    }
  }

  if (!dataLines.length) return

  const rawData = dataLines.join('\n')
  let payload: AgentTaskStreamEvent<T>
  try {
    payload = JSON.parse(rawData) as AgentTaskStreamEvent<T>
  } catch {
    payload = { event: eventName, message: rawData }
  }
  if (!payload.event && eventName) {
    payload.event = eventName
  }
  await onEvent?.(payload)
}

export const consumeAgentTaskStream = async <T = API.SystemOutput>({
  taskId,
  signal,
  onEvent,
}: ConsumeAgentTaskStreamOptions<T>) => {
  const response = await fetch(`${API_BASE_URL}/agentTask/stream/${encodeURIComponent(taskId)}`, {
    method: 'GET',
    credentials: 'include',
    headers: {
      Accept: 'text/event-stream',
    },
    signal,
  })

  if (!response.ok) {
    throw new Error(`SSE request failed: ${response.status}`)
  }

  if (!response.body) {
    throw new Error('SSE response body is empty')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  try {
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })

      const normalizedBuffer = buffer.replace(/\r\n/g, '\n')
      let boundaryIndex = normalizedBuffer.indexOf('\n\n')
      while (boundaryIndex >= 0) {
        const chunk = normalizedBuffer.slice(0, boundaryIndex)
        buffer = normalizedBuffer.slice(boundaryIndex + 2)
        await parseSseChunk(chunk, onEvent)
        boundaryIndex = buffer.indexOf('\n\n')
      }
    }

    const tail = buffer.trim()
    if (tail) {
      await parseSseChunk(tail, onEvent)
    }
  } finally {
    reader.releaseLock()
  }
}
