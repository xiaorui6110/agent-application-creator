package com.xiaorui.agentapplicationcreator.handler;

import cn.hutool.core.util.CharsetUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.response.ServerResponseEntity;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * @description: http 请求处理
 * @author: xiaorui
 * @date: 2025-11-30 14:39
 **/
@Component
public class HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpHandler.class);

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 将服务器响应对象输出到Web页面
     * @param serverResponseEntity 服务器响应实体
     * @param <T> 响应数据的泛型类型
     */
    public <T> void printServerResponseToWeb(ServerResponseEntity<T> serverResponseEntity) {
        if (serverResponseEntity == null) {
            logger.info("print obj is null");
            return;
        }
        // 获取当前请求的属性
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            logger.error("requestAttributes is null, can not print to web");
            return;
        }
        // 获取HTTP响应对象
        HttpServletResponse response = requestAttributes.getResponse();
        if (response == null) {
            logger.error("httpServletResponse is null, can not print to web");
            return;
        }
        logger.error("response error: " + serverResponseEntity.getMsg());
        response.setCharacterEncoding(CharsetUtil.UTF_8);
        // 设置响应内容类型为JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        PrintWriter printWriter = null;
        try {
            printWriter = response.getWriter();
            // 将响应对象序列化为JSON字符串并写入输出流
            printWriter.write(objectMapper.writeValueAsString(serverResponseEntity));
        }
        catch (IOException e) {
            throw new BusinessException("io 异常", e);
        }
    }

    /**
     * 将业务异常输出到Web页面
     * @param businessException 业务异常对象
     * @param <T> 响应数据的泛型类型
     */
    public <T> void printServerResponseToWeb(BusinessException businessException) {
        if (businessException == null) {
            logger.info("print obj is null");
            return;
        }
        // 如果异常中包含服务器响应实体，则直接处理
        if (Objects.nonNull(businessException.getServerResponseEntity())) {
            printServerResponseToWeb(businessException.getServerResponseEntity());
            return;
        }
        ServerResponseEntity<T> serverResponseEntity = new ServerResponseEntity<>();
        serverResponseEntity.setCode(businessException.getCode());
        serverResponseEntity.setMsg(businessException.getMessage());
        printServerResponseToWeb(serverResponseEntity);
    }

}
