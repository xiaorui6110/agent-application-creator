import fs from 'node:fs'
import path from 'node:path'

const projectRoot = process.cwd()
const typingsPath = path.join(projectRoot, 'src', 'api', 'typings.d.ts')
const userControllerPath = path.join(projectRoot, 'src', 'api', 'userController.ts')

function ensureListAppChatHistoryParamsHasAppId(content) {
  const anchor = 'type listAppChatHistoryParams = {'
  const anchorIndex = content.indexOf(anchor)
  if (anchorIndex < 0) return { content, changed: false }

  const blockStart = anchorIndex + anchor.length
  const blockEnd = content.indexOf('}', blockStart)
  if (blockEnd < 0) return { content, changed: false }

  const block = content.slice(anchorIndex, blockEnd + 1)
  if (block.includes('appId:')) return { content, changed: false }

  const patchedBlock = block.replace(
    anchor,
    `${anchor}\n    /** 应用 id（路径参数） */\n    appId: string\n`,
  )
  return { content: content.replace(block, patchedBlock), changed: true }
}

function ensureUserUpdateAvatarRequestIsAny(content) {
  const anchor = 'type UserUpdateAvatarRequest = {'
  const anchorIndex = content.indexOf(anchor)
  if (anchorIndex < 0) return { content, changed: false }

  const blockStart = anchorIndex + anchor.length
  const blockEnd = content.indexOf('}', blockStart)
  if (blockEnd < 0) return { content, changed: false }

  const block = content.slice(anchorIndex, blockEnd + 1)
  if (block.includes('multipartFile: any')) return { content, changed: false }
  const patchedBlock = block.replace(/multipartFile:\s*string/g, 'multipartFile: any')
  return { content: content.replace(block, patchedBlock), changed: true }
}

function ensureAppVOCodeGenTypeLowercase(content) {
  const anchor = 'type AppVO = {'
  const anchorIndex = content.indexOf(anchor)
  if (anchorIndex < 0) return { content, changed: false }

  const blockStart = anchorIndex + anchor.length
  const blockEnd = content.indexOf('}', blockStart)
  if (blockEnd < 0) return { content, changed: false }

  const block = content.slice(anchorIndex, blockEnd + 1)
  const upperUnion =
    /codeGenType\?:\s*'SINGLE_FILE'\s*\|\s*'MULTI_FILE'\s*\|\s*'VUE_PROJECT'/
  const lowerUnion =
    /codeGenType\?:\s*'single_file'\s*\|\s*'multi_file'\s*\|\s*'vue_project'/
  if (lowerUnion.test(block)) return { content, changed: false }
  if (!upperUnion.test(block)) return { content, changed: false }

  const patchedBlock = block.replace(
    upperUnion,
    "codeGenType?: 'single_file' | 'multi_file' | 'vue_project'",
  )
  return { content: content.replace(block, patchedBlock), changed: true }
}

function patchUserControllerUpdateUserAvatar(content) {
  const fnAnchor = 'export async function updateUserAvatar('
  const fnIndex = content.indexOf(fnAnchor)
  if (fnIndex < 0) return { content, changed: false }

  const fnEnd = content.indexOf('\n}', fnIndex)
  if (fnEnd < 0) return { content, changed: false }

  const block = content.slice(fnIndex, fnEnd + 3)

  if (block.includes('const formData = new FormData()') && block.includes("formData.append('multipartFile'")) {
    return { content, changed: false }
  }

  const patched = block
    .replace(
      /return request<API\.ServerResponseEntityString>\('\/user\/update\/avatar',[\s\S]*?\}\)\n\}/,
      `const file = (body as any)?.multipartFile ?? body\n  const formData = new FormData()\n  formData.append('multipartFile', file)\n  return request<API.ServerResponseEntityString>('/user/update/avatar', {\n    method: 'POST',\n    params: {\n      ...params,\n    },\n    data: formData,\n    ...(options || {}),\n  })\n}`,
    )
    .replace(
      /headers:\s*\{[\s\S]*?'Content-Type':\s*'application\/json',[\s\S]*?\},\n/g,
      '',
    )

  return { content: content.replace(block, patched), changed: true }
}

if (!fs.existsSync(typingsPath)) {
  console.error(`[openapi-postprocess] missing: ${typingsPath}`)
  process.exit(1)
}

const raw = fs.readFileSync(typingsPath, 'utf-8')
const p1 = ensureListAppChatHistoryParamsHasAppId(raw)
const p2 = ensureUserUpdateAvatarRequestIsAny(p1.content)
const p3 = ensureAppVOCodeGenTypeLowercase(p2.content)

const patched = p3.content
const changed = p1.changed || p2.changed || p3.changed

if (changed) {
  fs.writeFileSync(typingsPath, patched, 'utf-8')
  console.log('[openapi-postprocess] patched src/api/typings.d.ts')
} else {
  console.log('[openapi-postprocess] no changes needed')
}

if (fs.existsSync(userControllerPath)) {
  const rawUserController = fs.readFileSync(userControllerPath, 'utf-8')
  const { content: patchedUserController, changed: changedUserController } =
    patchUserControllerUpdateUserAvatar(rawUserController)
  if (changedUserController) {
    fs.writeFileSync(userControllerPath, patchedUserController, 'utf-8')
    console.log('[openapi-postprocess] patched src/api/userController.ts')
  }
}
