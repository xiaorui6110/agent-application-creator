package com.xiaorui.agentapplicationcreator.temp.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

/**
 * @description: mybatis-flex 代码生成器（<a href="https://mybatis-flex.com/zh/others/codegen.html#%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B">...</a>）
 * @author: xiaorui
 * @date: 2025-11-30 11:45
 **/

public class MyBatisCodeGenerator {

    /**
     * TODO 需要生成的表名（每次修改表名生成）
     */
    private static final String[] TABLE_NAMES = {"xr_chat_history"};

    public static void main(String[] args) {
        // 获取数据源信息（从yml文件中获取）
        Dict dict = YamlUtil.loadByPath("application-dev.yml");
        Map<String, Object> dataSourceConfig = dict.getByPath("spring.datasource");
        String url = String.valueOf(dataSourceConfig.get("url"));
        String username = String.valueOf(dataSourceConfig.get("username"));
        String password = String.valueOf(dataSourceConfig.get("password"));
        // 配置数据源
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        //创建配置内容，自定义风格
        GlobalConfig globalConfig = createGlobalConfigUseStyle();
        //通过 datasource 和 globalConfig 创建代码生成器
        Generator generator = new Generator(dataSource, globalConfig);
        //生成代码
        generator.generate();
    }

    /**
     * 自定义生成风格
     */
    public static GlobalConfig createGlobalConfigUseStyle() {
        // 创建配置内容
        GlobalConfig globalConfig = new GlobalConfig();
        // 设置根包（临时包）
        globalConfig.getPackageConfig().setBasePackage("com.xiaorui.agentapplicationcreator.temp.genresult");
        // 设置表前缀和只生成哪些表，setGenerateTable 未配置时，生成所有表
        globalConfig.getStrategyConfig().setTablePrefix("xr_").setGenerateTable(TABLE_NAMES).setLogicDeleteColumn("isDeleted");
        // 设置生成 entity 并启用 Lombok
        globalConfig.enableEntity().setWithLombok(true).setJdkVersion(21);
        // 设置生成 mapper
        globalConfig.enableMapper();
        globalConfig.enableMapperXml();
        // 设置生成 service
        globalConfig.enableService();
        globalConfig.enableServiceImpl();
        // 设置生成 controller
        globalConfig.enableController();
        // 设置生成时间和字符串为空，避免多余的代码改动
        globalConfig.getJavadocConfig().setAuthor("xiaorui").setSince("");
        return globalConfig;
    }

}
