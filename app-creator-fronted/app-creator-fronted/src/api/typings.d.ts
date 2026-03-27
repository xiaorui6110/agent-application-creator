declare namespace API {
  type addCommentParams = {
    /** 添加评论请求 */
    appCommentAddRequest?: any
  }

  type AgentResponse = {
    /** Agent 最终给用户的自然语言回复 */
    reply?: string
    /** 结构化回复 */
    structuredReply?: StructuredReply
    /** Agent 在本次对话中调用的工具信息 */
    toolCalls?: ToolCallResponse[]
    /** Agent 执行的代码修改计划 */
    codeModificationPlan?: CodeModificationPlan
    /** 主 Agent 生成的应用信息，副 Agent 执行的代码优化输入 */
    codeOptimizationInput?: CodeOptimizationInput
    /** Agent 生成的应用名称 */
    appName?: string
    /** Agent 根据用户提示判断的代码生成类型 */
    codeGenType?: string
    /** Agent 对用户意图的理解摘要 */
    intentSummary?: string
    /** Agent 自评置信度 */
    confidence?: number
    /** 扩展元数据 */
    metadata?: Record<string, any>
  }

  type AgentTask = {
    agentTaskId?: string
    taskId?: string
    threadId?: string
    appId?: string
    taskStatus?: string
    /** 浠诲姟鐘舵€佽鏄?*/
    message?: string
    message?: string
    taskError?: string
    taskResult?: AgentResponse
    taskError?: string
    retryCount?: number
    failType?: string
    nextRetryTime?: string
    createTime?: string
    updateTime?: string
    isDeleted?: number
  }

  type AgentTaskStatus = {
    /** 任务id */
    taskId?: string
    /** 对话线程id */
    threadId?: string
    /** 应用id */
    appId?: string
    /** 任务状态 */
    taskStatus?: string
    /** 给用户看的信息 */
    message?: string
    /** 重试次数 */
    retryCount?: number
    /** 失败类型 */
    failType?: string
    /** 下次重试时间 */
    nextRetryTime?: string
    /** 创建时间 */
    createTime?: string
  }

  type AdminDashboardStatsVO = {
    totalUserCount?: number
    todayRegisterCount?: number
    totalAppCount?: number
    todayAppCount?: number
    deployedAppCount?: number
    featuredAppCount?: number
    totalChatCount?: number
    todayChatCount?: number
    totalTaskCount?: number
    todayTaskCount?: number
    runningTaskCount?: number
    waitingTaskCount?: number
    succeededTaskCount?: number
    failedTaskCount?: number
  }

  type ModelCallLog = {
    modelCallLogId?: string
    userId?: string
    appId?: string
    threadId?: string
    agentName?: string
    provider?: string
    modelName?: string
    callType?: string
    callStatus?: string
    promptTokens?: number
    completionTokens?: number
    totalTokens?: number
    latencyMs?: number
    errorMessage?: string
    createTime?: string
    updateTime?: string
    isDeleted?: number
  }

  type ModelCallLogQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    userId?: string
    appId?: string
    threadId?: string
    agentName?: string
    modelName?: string
    callStatus?: string
  }

  type ModelCallStatsVO = {
    totalCallCount?: number
    todayCallCount?: number
    successCallCount?: number
    failedCallCount?: number
    totalPromptTokens?: number
    totalCompletionTokens?: number
    totalTokens?: number
    avgLatencyMs?: number
  }

  type AppAdminUpdateInfoRequest = {
    /** 应用id */
    appId: string
    /** 应用名称 */
    appName?: string
    /** 应用封面 */
    appCover?: string
    /** 应用描述 */
    appDescription?: string
    /** 应用排序优先级 */
    appPriority?: number
    appCategory?: string
    /** 代码生成类型（枚举） */
    codeGenType?: string
  }

  type AppCommentAddRequest = {
    userId?: string
    appId?: string
    commentContent?: string
    parentId?: string
  }

  type AppCommentDeleteRequest = {
    commentId?: string
  }

  type AppCommentLikeRequest = {
    commentId?: string
    userId?: string
    likeType?: 'LIKE' | 'CANCEL_LIKE'
    likeCount?: number
    dislikeCount?: number
  }

  type AppCommentQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    appId?: string
  }

  type AppCommentUserVO = {
    /** 用户id */
    userId?: string
    /** 用户昵称 */
    nickName?: string
    /** 用户头像 */
    userAvatar?: string
  }

  type AppCommentVO = {
    /** 评论id */
    commentId?: string
    /** 评论用户id */
    userId?: string
    /** 被评论应用id */
    appId?: string
    /** 被评论应用所属用户id */
    appUserId?: string
    /** 评论内容 */
    commentContent?: string
    /** 父评论id，null表示顶级评论 */
    parentId?: string
    /** 点赞数 */
    likeCount?: number
    /** 点踩数 */
    dislikeCount?: number
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
    /** 子评论列表 */
    childCommentList?: any
    /** 评论用户信息 */
    appCommentUserVO?: AppCommentUserVO
    /** 被评论应用信息 */
    appVO?: AppVO
  }

  type AppCreateRequest = {
    /** 应用初始化的 prompt */
    appInitPrompt: string
  }

  type AppDeployRequest = {
    /** 应用id */
    appId: string
  }

  type AppQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 应用id */
    appId?: string
    /** 应用名称 */
    appName?: string
    /** 代码生成类型（枚举） */
    codeGenType?: string
    /** 应用排序优先级 */
    appPriority?: number
    /** 应用分类 */
    appCategory?: string
    /** 排行类型 */
    rankType?: string
  }

  type AppUpdateInfoRequest = {
    /** 应用id */
    appId: string
    /** 应用名称 */
    appName?: string
    /** 应用封面 */
    appCover?: string
    /** 应用描述 */
    appDescription?: string
    /** 代码生成类型（枚举） */
    codeGenType?: string
  }

  type AppVO = {
    /** 应用id（雪花算法） */
    appId?: string
    /** 应用名称 */
    appName?: string
    /** 应用封面 */
    appCover?: string
    /** 应用初始化的 prompt */
    appInitPrompt?: string
    /** 应用描述 */
    appDescription?: string
    /** 代码生成类型（枚举） */
    codeGenType?: 'single_file' | 'multi_file' | 'vue_project'
    /** 应用排序优先级 */
    appPriority?: number
    /** 应用分类 */
    appCategory?: string
    /** 部署访问地址 */
    deployUrl?: string
    /** 部署时间 */
    deployedTime?: string
    /** 评论数 */
    commentCount?: number
    /** 点赞数 */
    likeCount?: number
    /** 分享数 */
    shareCount?: number
    /** 浏览量 */
    viewCount?: number
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
    /** 创建用户信息 */
    userVO?: UserVO
  }

  type banOrUnbanUserParams = {
    /** 封禁或解封用户请求参数 */
    userUnbanRequest?: any
  }

  type BaseResponseString = {
    code?: string
    data?: string
    msg?: string
  }

  type CallAgentRequest = {
    /** 消息 */
    message: string
    /** 线程id */
    threadId?: string
    /** 应用id */
    appId?: string
  }

  type changeUserPasswordParams = {
    /** 修改用户密码请求参数 */
    userChangePasswordRequest?: any
  }

  type changUserEmailParams = {
    /** 修改用户邮箱请求参数 */
    userChangeEmailRequest?: any
  }

  type ChatHistory = {
    chatHistoryId?: string
    chatMessage?: string
    chatMessageType?: string
    appId?: string
    userId?: string
    parentId?: string
    createTime?: string
    updateTime?: string
    isDeleted?: number
  }

  type ChatHistoryQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 对话历史id（雪花算法） */
    chatHistoryId: string
    /** 对话消息 */
    chatMessage?: string
    /** 消息类型：user/ai */
    chatMessageType?: string
    /** 应用id */
    appId?: string
    /** 创建用户id */
    userId?: string
    /** 游标查询 - 最后一条记录的创建时间 */
    lastCreateTime?: string
  }

  type chatParams = {
    /** 智能体对话请求参数 */
    callAgentRequest?: any
  }

  type CodeChange = {
    path?: string
    type?: string
    diff?: string
  }

  type CodeModificationPlan = {
    planType?: string
    rootDir?: string
    operations?: FileOperationPlan[]
    verification?: VerificationPlan
  }

  type CodeOptimizationInput = {
    /** 应用id */
    appId?: string
    /** 应用目标 */
    appGoal?: string
    /** 技术栈 */
    techStack?: string[]
    /** 文件树 */
    fileTree?: string[]
    /** 文件内容 */
    files?: Record<string, any>
    /** 最近一次用户触发的改动 */
    recentChanges?: CodeChange[]
    /** 平台的长期记忆 */
    platformMemory?: string[]
  }

  type commentedHistoryParams = {
    /** 获取评论历史请求 */
    appCommentQueryRequest?: any
  }

  type createAppParams = {
    /** 应用创建请求 */
    appCreateRequest?: any
  }

  type deleteAppByAdminParams = {
    /** 删除请求 */
    deleteRequest?: any
  }

  type deleteAppParams = {
    /** 删除请求 */
    deleteRequest?: any
  }

  type deleteBatchUserParams = {
    /** 批量删除请求参数 */
    deleteRequestList?: any
  }

  type deleteCommentParams = {
    /** 删除评论请求 */
    appCommentDeleteRequest?: any
  }

  type DeleteRequest = {
    id?: string
  }

  type deleteUserParams = {
    /** 删除请求参数 */
    deleteRequest?: any
  }

  type deleteChatHistoryByAdminParams = {
    /** 删除请求参数 */
    deleteRequest?: any
  }

  type deployAppParams = {
    /** 应用部署请求 */
    appDeployRequest?: any
  }

  type doLikeParams = {
    /** 点赞/取消点赞请求 */
    likeDoRequest?: any
  }

  type doShareParams = {
    /** 分享/取消分享请求 */
    shareDoRequest?: any
  }

  type downloadAppCodeParams = {
    /** 应用id */
    appId: string
  }

  type ExpectedCondition = {
    type?: 'CONTENT_EQUALS' | 'FILE_EXISTS' | 'FILE_NOT_EXISTS'
    value?: string
  }

  type FileOperationPlan = {
    operationType?:
      | 'CREATE_FILE'
      | 'OVERWRITE_FILE'
      | 'APPEND_FILE'
      | 'READ_FILE'
      | 'DELETE_FILE'
      | 'MOVE_FILE'
      | 'RENAME_FILE'
      | 'CREATE_DIRECTORY'
      | 'DELETE_EMPTY_DIRECTORY'
      | 'DELETE_DIRECTORY_RECURSIVE'
      | 'LIST_DIRECTORY_TREE'
      | 'EXISTS'
      | 'SEARCH_BY_NAME'
      | 'SEARCH_BY_CONTENT'
      | 'VERIFY_CONTENT_EQUALS'
      | 'VERIFY_CONTENT_CONTAINS'
    path?: string
    expected?: ExpectedCondition
    content?: string
    targetPath?: string
  }

  type getAppInfoByIdByAdminParams = {
    /** 应用id */
    appId: string
  }

  type getAppInfoByIdParams = {
    /** 应用id */
    appId: string
  }

  type getInfoParams = {
    id: string
    /** 任务ID */
    taskId?: any
  }

  type getLikeHistoryParams = {
    /** 获取点赞历史请求 */
    likeQueryRequest?: any
  }

  type getLikeStatusParams = {
    /** 目标ID */
    targetId: string
  }

  type getMyLikeHistoryParams = {
    /** 获取我的点赞历史请求 */
    likeQueryRequest?: any
  }

  type getMyShareHistoryParams = {
    /** 分享历史查询请求 */
    shareQueryRequest?: any
  }

  type getShareStatusParams = {
    /** 目标ID */
    targetId: string
  }

  type getTaskStateParams = {
    /** 任务ID */
    taskId: string
  }

  type getUserByIdParams = {
    /** 用户id */
    userId: string
  }

  type getUserInfoByIdOrNameParams = {
    /** 用户查询请求参数 */
    userQueryRequest?: any
  }

  type getUserShareHistoryParams = {
    /** 分享历史查询请求 */
    shareQueryRequest?: any
  }

  type likeCommentParams = {
    /** 点赞评论请求 */
    appCommentLikeRequest?: any
  }

  type LikeDoRequest = {
    targetId?: string
    isLiked?: number
  }

  type LikeQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    targetId?: string
  }

  type LikeRecordVO = {
    /** 点赞记录id */
    likeId?: string
    /** 用户id */
    userId?: string
    /** 被点赞内容id */
    targetId?: string
    /** 被点赞内容所属用户id */
    targetUserId?: string
    /** 是否点赞 0-取消 1-点赞 */
    isLiked?: number
    /** 最近一次点赞时间 */
    lastLikeTime?: string
    /** 点赞用户信息 */
    userVO?: UserVO
    /** 被点赞内容信息 */
    appVO?: AppVO
  }

  type listAllChatHistoryByPageForAdminParams = {
    /** 对话历史查询请求参数 */
    chatHistoryQueryRequest?: any
  }

  type listAppChatHistoryParams = {
    appId: string
    pageSize?: number
    lastCreateTime?: string
    /** 对话历史查询请求参数 */
    chatHistoryQueryRequest?: any
  }

  type listAppInfoByPageByAdminParams = {
    /** 应用查询请求 */
    appQueryRequest?: any
  }

  type listAppInfoByPageParams = {
    /** 应用查询请求 */
    appQueryRequest?: any
  }

  type listGoodAppInfoByPageParams = {
    /** 应用查询请求 */
    appQueryRequest?: any
  }

  type listParams = {
    /** 任务ID */
    taskId?: any
  }

  type listUserInfoByPageParams = {
    /** 用户查询请求参数 */
    userQueryRequest?: any
  }

  type myHistoryParams = {
    /** 获取我的评论历史请求 */
    appCommentQueryRequest?: any
  }

  type PageAgentTask = {
    records?: AgentTask[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAppCommentVO = {
    records?: AppCommentVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageAppVO = {
    records?: AppVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageChatHistory = {
    records?: ChatHistory[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageLikeRecordVO = {
    records?: LikeRecordVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type pageParams = {
    /** AgentTask分页对象 */
    page: PageAgentTask
  }

  type PageShareRecordVO = {
    records?: ShareRecordVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type PageModelCallLog = {
    records?: ModelCallLog[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type queryCommentParams = {
    /** 查询评论请求 */
    appCommentQueryRequest?: any
  }

  type resetUserPasswordParams = {
    /** 重置用户密码请求参数 */
    userResetPasswordRequest?: any
  }

  type retryTaskParams = {
    /** 任务ID */
    taskId: string
  }

  type sendEmailCodeParams = {
    /** 发送邮箱验证码请求参数 */
    userSendEmailCodeRequest?: any
  }

  type ServerResponseEntityAgentTaskStatus = {
    code?: string
    msg?: string
    data?: AgentTaskStatus
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityAppVO = {
    code?: string
    msg?: string
    data?: AppVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityBoolean = {
    code?: string
    msg?: string
    data?: boolean
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityListAppCommentVO = {
    code?: string
    msg?: string
    data?: AppCommentVO[]
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityListLikeRecordVO = {
    code?: string
    msg?: string
    data?: LikeRecordVO[]
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityListShareRecordVO = {
    code?: string
    msg?: string
    data?: ShareRecordVO[]
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityLong = {
    code?: string
    msg?: string
    data?: number
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityMapStringString = {
    code?: string
    msg?: string
    data?: Record<string, any>
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityPageAppCommentVO = {
    code?: string
    msg?: string
    data?: PageAppCommentVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityPageAppVO = {
    code?: string
    msg?: string
    data?: PageAppVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityPageChatHistory = {
    code?: string
    msg?: string
    data?: PageChatHistory
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityPageLikeRecordVO = {
    code?: string
    msg?: string
    data?: PageLikeRecordVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityPageShareRecordVO = {
    code?: string
    msg?: string
    data?: PageShareRecordVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityPageUserVO = {
    code?: string
    msg?: string
    data?: PageUserVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityString = {
    code?: string
    msg?: string
    data?: string
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntitySystemOutput = {
    code?: string
    msg?: string
    data?: SystemOutput
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityUser = {
    code?: string
    msg?: string
    data?: User
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityUserVO = {
    code?: string
    msg?: string
    data?: UserVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityListString = {
    code?: string
    msg?: string
    data?: string[]
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityAdminDashboardStatsVO = {
    code?: string
    msg?: string
    data?: AdminDashboardStatsVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityModelCallStatsVO = {
    code?: string
    msg?: string
    data?: ModelCallStatsVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityPageModelCallLog = {
    code?: string
    msg?: string
    data?: PageModelCallLog
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityUserManageStatsVO = {
    code?: string
    msg?: string
    data?: UserManageStatsVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityVoid = {
    code?: string
    msg?: string
    data?: Record<string, any>
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ShareDoRequest = {
    shareId?: string
    isShared?: number
  }

  type ShareQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    targetId?: string
  }

  type ShareRecordVO = {
    /** 分享记录id */
    shareId?: string
    /** 用户id */
    userId?: string
    /** 被分享内容id */
    targetId?: string
    /** 分享时间 */
    shareTime?: string
    /** 分享用户信息 */
    userVO?: UserVO
    /** 被分享内容信息 */
    appVO?: AppVO
  }

  type StructuredReply = {
    /** 应用生成模式 */
    type?: string
    /** 是否可直接运行 */
    runnable?: boolean
    /** 入口文件 */
    entry?: string
    /** 生成的文件内容 */
    files?: Record<string, any>
    /** 对本次生成结果的简要说明 */
    description?: string
  }

  type SystemOutput = {
    /** 对话线程 ID */
    threadId?: string
    /** 当前用户 ID */
    userId?: string
    /** 应用 ID */
    appId?: string
    /** 任务 ID */
    taskId?: string
    /** 任务状态 */
    taskStatus?: string
    /** Agent 名称 / 标识 */
    agentName?: string
    /** Agent 结构化回复 */
    agentResponse?: AgentResponse
    /** 本次回复是否命中历史 / 缓存 */
    fromMemory?: boolean
    /** 澶辫触绫诲瀷 */
    failType?: string
    /** 澶辫触鍘熷洜 */
    taskError?: string
    /** 閲嶈瘯娆℃暟 */
    retryCount?: number
    /** 涓嬫閲嶈瘯鏃堕棿 */
    nextRetryTime?: string
    /** 本次回复时间戳（秒） */
    timestamp?: number
  }

  type ToolCallResponse = {
    /** 工具名称 */
    toolName?: string
    /** 执行的动作语义 */
    action?: string
    /** 操作目标 */
    target?: string
    /** 工具输入参数 */
    input?: Record<string, any>
    /** 工具原始输出 */
    result?: Record<string, any>
    /** Tool 是否在技术层面执行成功（无异常） */
    invokedSuccessfully?: boolean
    /** 结果是否已被验证（例如是否 readFile 确认） */
    verified?: boolean
    /** 若未验证，给 Agent 的下一步建议 */
    nextActionHint?: string
    /** 执行耗时 */
    costMs?: number
  }

  type updateAppByAdminParams = {
    /** 管理员更新请求 */
    appAdminUpdateRequest?: any
  }

  type updateAppParams = {
    /** 应用信息更新请求 */
    appUpdateRequest?: any
  }

  type updateUserAvatarParams = {
    /** 头像文件 */
    头像文件?: any
  }

  type updateUserInfoParams = {
    /** 更新用户信息请求参数 */
    userUpdateInfoRequest?: any
  }

  type updateUserRoleParams = {
    userRoleUpdateRequest?: any
  }

  type User = {
    userId?: string
    nickName?: string
    userEmail?: string
    loginPassword?: string
    userPhone?: string
    userAvatar?: string
    userSex?: string
    userBirthday?: string
    userProfile?: string
    userRole?: string
    userStatus?: number
    userRegip?: string
    userLastip?: string
    userLasttime?: string
    userScore?: number
    createTime?: string
    updateTime?: string
    isDeleted?: number
  }

  type UserChangeEmailRequest = {
    /** 新邮箱 */
    newEmail: string
    /** 验证码（邮箱验证码） */
    emailVerifyCode: string
  }

  type UserChangePasswordRequest = {
    /** 原密码 */
    oldPassword: string
    /** 新密码 */
    newPassword: string
    /** 确认密码 */
    checkPassword: string
  }

  type userLoginParams = {
    /** 用户登录请求参数 */
    userLoginRequest?: any
  }

  type UserLoginRequest = {
    /** 用户邮箱 */
    userEmail: string
    /** 登录密码 */
    loginPassword: string
    /** 验证码（图形数字验证码-用户输入的） */
    verifyCode: string
    /** 验证码（数字验证码-服务器存储的） */
    serverVerifyCode: string
  }

  type UserQueryRequest = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    /** 用户id */
    userId?: string
    /** 用户昵称 */
    nickName?: string
  }

  type userRegisterParams = {
    /** 用户注册请求参数 */
    userRegisterRequest?: any
  }

  type UserRegisterRequest = {
    /** 用户邮箱 */
    userEmail: string
    /** 登录密码 */
    loginPassword: string
    /** 确认密码 */
    checkPassword: string
    /** 验证码（邮箱验证码） */
    emailVerifyCode: string
  }

  type UserResetPasswordRequest = {
    /** 用户邮箱 */
    userEmail: string
    /** 新密码 */
    newPassword: string
    /** 确认密码 */
    checkPassword: string
    /** 验证码（邮箱验证码） */
    emailVerifyCode: string
  }

  type UserSendEmailCodeRequest = {
    /** 用户邮箱 */
    userEmail: string
    /** 验证码用途：register-注册，resetPassword-重置密码，changeEmail-修改邮箱 */
    type: string
  }

  type UserUnbanRequest = {
    /** 用户id */
    userId: string
    /** 操作类型：true-解禁，false-封禁 */
    isUnban: boolean
  }

  type UserRoleUpdateRequest = {
    userId: string
    userRole: string
  }

  type UserUpdateInfoRequest = {
    /** 用户id */
    userId: string
    /** 用户昵称 */
    nickName?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户性别 m-男 f-女 */
    userSex?: string
    /** 用户生日 yyyy-mm-dd */
    userBirthday?: string
    /** 用户简介 */
    userProfile?: string
  }

  type UserVO = {
    /** 用户id */
    userId?: string
    /** 用户昵称 */
    nickName?: string
    /** 用户头像 */
    userAvatar?: string
    /** 用户邮箱 */
    userEmail?: string
    /** 用户性别 m-男 f-女 */
    userSex?: string
    /** 用户生日 yyyy-mm-dd */
    userBirthday?: string
    /** 用户备注 */
    userProfile?: string
    /** 用户角色 */
    userRole?: string
    /** 用户状态 */
    userStatus?: number
    /** 创建时间 */
    createTime?: string
  }

  type UserManageStatsVO = {
    totalUserCount?: number
    normalUserCount?: number
    adminUserCount?: number
    bannedUserCount?: number
    todayRegisterCount?: number
    recentSevenDayRegisterCount?: number
  }

  type VerificationPlan = {
    type?: 'CONTENT_EQUALS'
    path?: string
    value?: string
  }

  type getSharePreviewParams = {
    targetId: string
  }

  type SharePreviewVO = {
    appId?: string
    appName?: string
    shareUrl?: string
    qrCodeDataUrl?: string
  }

  type ServerResponseEntitySharePreviewVO = {
    code?: string
    msg?: string
    data?: SharePreviewVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }
  type AppTemplateCreateRequest = {
    appId?: string
    templateName?: string
    templateDescription?: string
  }

  type AppTemplateUseRequest = {
    templateId?: string
    appName?: string
    appDescription?: string
  }

  type AppTemplateVO = {
    templateId?: string
    templateName?: string
    templateDescription?: string
    codeGenType?: string
    entryFile?: string
    sourceAppId?: string
    createdBy?: string
    createdTime?: string
  }

  type AppVersionRestoreRequest = {
    appId?: string
    appVersionId?: string
  }

  type AppVersionVO = {
    appVersionId?: string
    appId?: string
    versionNumber?: number
    versionSource?: string
    versionNote?: string
    snapshotPath?: string
    entryFile?: string
    deployUrl?: string
    createdBy?: string
    createTime?: string
  }

  type createTemplateParams = {
    appTemplateCreateRequest?: any
  }

  type createAppFromTemplateParams = {
    appTemplateUseRequest?: any
  }

  type listAppVersionsParams = {
    appId: string
  }

  type restoreAppVersionParams = {
    appVersionRestoreRequest?: any
  }

  type ServerResponseEntityAppTemplateVO = {
    code?: string
    msg?: string
    data?: AppTemplateVO
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityListAppTemplateVO = {
    code?: string
    msg?: string
    data?: AppTemplateVO[]
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }

  type ServerResponseEntityListAppVersionVO = {
    code?: string
    msg?: string
    data?: AppVersionVO[]
    version?: string
    timestamp?: number
    sign?: string
    fail?: boolean
    success?: boolean
  }
}
