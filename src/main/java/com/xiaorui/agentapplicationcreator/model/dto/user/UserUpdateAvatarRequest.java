package com.xiaorui.agentapplicationcreator.model.dto.user;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

/**
 * @description: 用户更新头像请求
 * @author: xiaorui
 * @date: 2025-11-30 13:50
 **/
@Data
public class UserUpdateAvatarRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -7155010502755257432L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 用户头像文件
     */
    private MultipartFile userAvatar;

}
