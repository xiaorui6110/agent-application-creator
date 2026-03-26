package com.xiaorui.agentapplicationcreator.constant;

import java.util.List;

public interface AppCategoryConstant {

    String GENERAL = "general";
    String TOOL = "tool";
    String PRODUCTIVITY = "productivity";
    String EDUCATION = "education";
    String ENTERTAINMENT = "entertainment";
    String BUSINESS = "business";

    List<String> CATEGORY_LIST = List.of(
            GENERAL,
            TOOL,
            PRODUCTIVITY,
            EDUCATION,
            ENTERTAINMENT,
            BUSINESS
    );
}
