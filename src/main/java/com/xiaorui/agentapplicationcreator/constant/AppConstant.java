package com.xiaorui.agentapplicationcreator.constant;

/**
 * @author xiaorui
 */
public interface AppConstant {

    /**
     * 精选应用优先级
     */
    Integer GOOD_APP_PRIORITY = 99;

    /**
     * 默认应用优先级
     */
    Integer DEFAULT_APP_PRIORITY = 0;

    /**
     * 默认推荐分数（应用推荐分数这部分，没有实现完全，一些定时任务和分数的计算等，待实现）
     */
    Double DEFAULT_RECOMMEND_SCORE = 0D;
}
