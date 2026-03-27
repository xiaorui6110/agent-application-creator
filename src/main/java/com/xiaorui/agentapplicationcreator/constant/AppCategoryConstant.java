package com.xiaorui.agentapplicationcreator.constant;

import java.util.List;

/**
 * @author xiaorui
 */
public interface AppCategoryConstant {

    String GENERAL = "general";
    String TOOL = "tool";
    String PRODUCTIVITY = "productivity";
    String EDUCATION = "education";
    String ENTERTAINMENT = "entertainment";
    String BUSINESS = "business";

    /**
     * 应用分类列表，这里实现的不是特别好，先不删除了吧
     */
    List<String> CATEGORY_LIST = List.of(
            GENERAL,
            TOOL,
            PRODUCTIVITY,
            EDUCATION,
            ENTERTAINMENT,
            BUSINESS
    );
}
