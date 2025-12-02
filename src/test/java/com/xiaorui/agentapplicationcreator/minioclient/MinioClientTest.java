package com.xiaorui.agentapplicationcreator.minioclient;

import io.minio.MinioClient;
import io.minio.errors.MinioException;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @description: MinIO 客户端测试
 * @author: xiaorui
 * @date: 2025-12-02 10:36
 **/
@SpringBootTest
public class MinioClientTest {

        // 全局客户端实例
        private static MinioClient minioClient;

        static {
            // 初始化客户端
            try {
                minioClient = MinioClient.builder()
                        // MinIO 服务地址
                        .endpoint("http://172.30.43.12:9000")
                        // Access Key/Secret Key
                        .credentials("xiaorui", "18656412886ty")
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("MinIO 客户端初始化失败", e);
            }
    }

    public static void main(String[] args) {
        try {
            // 测试：检查桶是否存在
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket("agent-app-creator-bucket").build()
            );
            System.out.println("桶是否存在：" + exists);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
