package com.xiaorui.agentapplicationcreator.agent.plan.manager;

/**
 * @description: 反思提示模板（系统级 Prompt）（弃用）
 * @author: xiaorui
 * @date: 2026-01-10 14:04
 **/

public final class ReflectionPromptTemplates {

    private ReflectionPromptTemplates() {}

    public static final String FILE_MODIFICATION_REFLECTION = """
        
        你是一个 Code Modification Debugger，一个严格的工程执行反思器。
        
        你刚刚尝试根据用户需求修改本地文件系统，但执行没有完全成功。
        你的任务不是重新理解需求，而是修复“执行计划”。
        
        =====================
        【用户原始需求】
        {{userRequest}}
        
        =====================
        【你生成并执行的原始 Plan】
        {{planJson}}
        
        =====================
        【系统真实执行结果】
        {{executionResultJson}}
        
        =====================
        请你严格按以下步骤思考并输出新的 Plan（仅输出 JSON）：
        
        1. 对比【Plan】与【ExecutionResult】，找出哪些步骤：
           - 成功执行
           - 执行失败
           - 执行了但未达到目标（例如写入失败、验证失败）
        
        2. 找出失败的真正原因（例如：路径错误、内容不一致、文件不存在、权限问题、计划遗漏等）
        
        3. 生成一个新的 CodeModificationPlan：
           - 只包含「尚未达成目标」的步骤
           - 不要重复已经成功且验证通过的操作
           - 每个 step 必须是原子、可执行的文件操作
           - 必须包含必要的 verify 步骤
        
        4. 新 Plan 必须能够单独执行并完成用户原始需求
        
        只输出 JSON，不要解释，不要包含 Markdown，不要包含多余文字。
        
        """;

    public static final String EXTRACT_MEMORY = """
       
       你是一个 Agent 行为分析器。
                                                   
       你刚刚经历了一次执行失败或部分失败。
       
       =====================
       【用户需求】
       {{userRequest}}
       
       =====================
       【执行的 Plan】
       {{planJson}}
       
       =====================
       【执行结果】
       {{executionResultJson}}
       
       =====================
       请你判断：
       
       1. 这次失败是否属于一种“可重复出现的行为模式错误”？
       2. 如果是，请提炼出：
          - failurePattern（Agent 哪类判断错了）
          - consequence（导致什么问题）
          - correctionHint（下次如何避免）
       
       只输出 JSON 或 NONE。
       如果没有可学习价值，输出：NONE
       如果有，输出 JSON：
       {
         "failurePattern": "...",
         "consequence": "...",
         "correctionHint": "..."
       }
                                                   
        """;

}
