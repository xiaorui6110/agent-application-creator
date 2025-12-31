package com.xiaorui.agentapplicationcreator.constants;

/**
 * @description: 爬虫计数常量
 * @author: xiaorui
 * @date: 2025-12-31 16:39
 **/
public interface CrawlerConstant {

    /**
     * 警告计数
     */
    Integer WARN_COUNT = 15;

    /**
     * 封禁计数
     */
    Integer BAN_COUNT = 25;

    /**
     * 封禁角色
     */
    String BAN_ROLE = "ban";

    /**
     * 过期时间
     */
    Integer EXPIRE_TIME = 120;

}
