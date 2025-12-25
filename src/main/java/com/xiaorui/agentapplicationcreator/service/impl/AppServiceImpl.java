package com.xiaorui.agentapplicationcreator.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import com.xiaorui.agentapplicationcreator.execption.ThrowUtil;
import com.xiaorui.agentapplicationcreator.mapper.AppMapper;
import com.xiaorui.agentapplicationcreator.model.dto.app.AppQueryRequest;
import com.xiaorui.agentapplicationcreator.model.entity.App;
import com.xiaorui.agentapplicationcreator.model.entity.User;
import com.xiaorui.agentapplicationcreator.model.vo.AppVO;
import com.xiaorui.agentapplicationcreator.service.AppService;
import com.xiaorui.agentapplicationcreator.service.UserService;
import com.xiaorui.agentapplicationcreator.util.SecurityUtil;
import jakarta.annotation.Resource;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static com.xiaorui.agentapplicationcreator.constants.AppConstant.*;

/**
 * 应用表 服务层实现。
 *
 * @author xiaorui
 */
@Slf4j
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    private final static Integer MAX_INPUT_LENGTH = 2000;

    @Resource
    private UserService userService;

    /**
     * 创建应用
     *
     * @param appInitPrompt 应用初始化prompt(UserPrompt)
     * @return 应用id
     */
    @Override
    public String appCreate(String appInitPrompt) {
        // 获取当前用户信息
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        // 用户输入 prompt 校验
        validateUserInput(appInitPrompt);
        // 构造入库对象
        App app = new App();
        // 应用名称暂时为 appInitPrompt 前 12 位 TODO 后期由 AI 进行描述生成
        app.setAppName(appInitPrompt.substring(0, Math.min(appInitPrompt.length(), 12)));
        app.setUserId(userId);
        app.setAppInitPrompt(appInitPrompt);
        // 随机获取应用封面 TODO 待优化
        String coverUrl = "https://picsum.photos/1200/600";
        app.setAppCover(coverUrl);
        app.setDeployKey(RandomUtil.randomString(6));
        boolean result = this.save(app);
        ThrowUtil.throwIf(!result, ErrorCode.OPERATION_ERROR, "应用创建失败");
        log.info("app create success，app_id: {}, user_id: {}", app.getAppId(), userId);
        return app.getAppId();
    }


    /**
     * 获取查询条件（通过appId、appName和codeGenType查询）
     *
     * @param appQueryRequest 应用查询请求
     * @return 查询条件
     */
    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        ThrowUtil.throwIf(appQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        // 获取查询参数
        String userId = appQueryRequest.getAppId();
        String nickName = appQueryRequest.getAppName();
        String codeGenType = appQueryRequest.getCodeGenType();
        ThrowUtil.throwIf(StrUtil.isBlank(userId) && StrUtil.isBlank(nickName) && StrUtil.isBlank(codeGenType),
                ErrorCode.PARAMS_ERROR, "查询条件为空");
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        // 构造查询条件
        return QueryWrapper.create()
                .eq("app_id", userId)
                .like("app_name", nickName)
                // TODO 这个 codeGenType 好像有点问题，现在的 AI 不回复这个类型，一直为 null
                .eq("code_gen_type", codeGenType)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }


    /**
     * 获取应用信息
     *
     * @param appId 应用id
     * @return 应用信息vo
     */
    @Override
    public AppVO getAppInfo(String appId) {
        App app = this.mapper.selectOneById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        return appVO;
    }


    /**
     * 部署应用
     *
     * @param appId 应用id
     * @return 部署结果url
     */
    @Override
    public String appDeploy(String appId) {
        // 获取当前用户信息
        String userId = SecurityUtil.getUserInfo().getUserId();
        User loginUser = userService.getById(userId);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户不存在");
        }
        // 查询应用信息
        App app = this.mapper.selectOneById(appId);
        if (app == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        }
        // 权限校验，只能部署自己的应用
        if (!app.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限部署该应用");
        }
        // 检查是否已有 deployKey，否则生成 6 位 deployKey（字母 + 数字）
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            app.setDeployKey(RandomUtil.randomString(6));
        }
        // 获取源目录(code_output)
        Path sourceDirName = Paths.get(appId);
        String sourceDirPath = CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 复制文件到部署目录(code_deploy)
        String deployDirPath = CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setAppId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        updateApp.setUpdateTime(LocalDateTime.now());
        updateApp.setDeployUrl(String.format("%s/%s/", CODE_DEPLOY_HOST, deployKey));
        boolean updateResult = this.updateById(updateApp);
        ThrowUtil.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 返回可访问的 URL
        return String.format("%s/%s/", CODE_DEPLOY_HOST, deployKey);
    }


    /**
     * 用户输入校验：敏感词检测 ...
     */
    private void validateUserInput(String input) {
        if (StringUtil.isBlank(input)) {
            throw new BusinessException("输入不能为空", ErrorCode.PARAMS_ERROR);
        }
        if (input.length() > MAX_INPUT_LENGTH) {
            throw new BusinessException("输入过长，请分段发送", ErrorCode.PARAMS_ERROR);
        }
        // 验证字符串是否包含敏感词（目前先使用第三方框架简单实现）
        if (SensitiveWordHelper.contains(input)) {
            throw new BusinessException("输入包含不适宜内容", ErrorCode.PARAMS_ERROR);
        }
    }

}
