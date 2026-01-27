# 项目初始化模块设计文档
> xiaorui，项目初始化的代码可以作为任意项目复用的基础代码，与业务无关。

## 总览
基于 Spring Boot 3 构建的标准化项目初始化骨架，已完成常见后端开发必备的基础能力搭建，
如常见依赖整合、通用基础代码、自定义异常、响应包装类、全局异常处理器、全局跨域配置等。
最终启动项目，测试健康检查接口，验证项目是否正常运行，项目完成初始化。

---

## 项目结构

```plaintext
src/main/
├── java/com/xiaorui/
│   ├── common/                                  # 公共模块
│   ├── config/                                  # 配置类
|   ├── constants/                               # 常量类
│   ├── controller/                              # 控制器层
|   ├── enums/                                   # 枚举类
│   ├── exception/                               # 异常处理
│   ├── generator/                               # 数据访问层代码生成工具
│   ├── model/                                   # 数据模型
│   ├── util/                                    # 工具类
│   └── AgentApplicationCreatorApplication.java  # 启动类
└── resources/
    ├── application.yml                          # 主配置文件（环境选择）
    ├── application-dev.yml                      # 开发环境配置
    ├── logback-spring.xml                       # 日志配置（暂未配置）
    └── mapper/                                  # MyBatis Flex Mapper文件
```

---

## 初始化流程

### 环境准备

- Spring Boot 3.5.6
- JDK 21
- MySQL 8.0
- Redis 7.4
- Maven 3.8.8

### 依赖整合

- Hutool 工具库 5.8.38
- knife4j 接口文档 4.4.0
- AOP、Redisson、JavaxMail、Sa-Token 等

### 通用代码

- 常见响应包装、异常处理、全局异常处理器、全局跨域配置等
- MyBatis Flex 数据访问层代码生成工具，替换 MyBatis Plus 插件
- 工具类封装、常量类、枚举类等

### 启动项目

```plaintext
com.xiaorui.agentapplicationcreator.controller.HealthController
```

1. 启动项目，测试健康检查接口，验证项目是否正常运行 
2. 项目完成初始化

---
