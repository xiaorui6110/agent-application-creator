package com.xiaorui.agentapplicationcreator.manager.minio;

import cn.hutool.core.io.FileUtil;
import io.minio.*;
import io.minio.http.Method;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * @description: MinIO 工具类
 * @author: xiaorui
 * @date: 2025-12-02 10:53
 **/
@Slf4j
@Component
public class MinioManager {

    @Value("${minio.default-bucket}")
    private String defaultBucket;

    private final MinioClient minioClient;

    /**
     *  构造器注入 MinioClient
     */
    public MinioManager(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 1. 创建存储桶（若不存在）
     */
    public void createBucket(String bucketName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("桶 " + bucketName + " 创建成功");
            }
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("创建桶失败", e);
        }
    }

    /**
     * 2. 上传 File 到 MinIO
     *
     * @param file 本地文件/临时文件
     * @param objectName MinIO 存储的对象名（如 "test/2025/test.jpg"）
     * @return 临时访问链接
     */
    public String uploadFile(File file, String objectName) {
        return uploadFile(file, objectName, defaultBucket);
    }

    public String uploadFile(File file, String objectName, String bucketName) {
        // 1. 校验文件
        if (file == null || !file.exists()) {
            throw new RuntimeException("上传文件不存在！");
        }
        if (objectName == null || objectName.isEmpty()) {
            throw new RuntimeException("MinIO 对象名不能为空！");
        }
        if (bucketName == null || bucketName.isEmpty()) {
            throw new RuntimeException("MinIO 桶名不能为空！");
        }
        try {
            // 2. 确保桶存在
            createBucket(bucketName);
            // 3. 上传 File 到 MinIO（直接用 FileInputStream，无类型转换）
            try (FileInputStream fis = new FileInputStream(file)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(objectName)
                                // 文件流 + 大小
                                .stream(fis, file.length(), -1)
                                // 自动识别类型
                                .contentType(getContentType(file.getName()))
                                .build()
                );
            }
            // 4. 生成临时访问链接
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("File 上传到 MinIO 失败", e);
        }
    }

    /**
     * 上传 MultipartFile（Web 端上传适配）
     *
     * @param multipartFile 前端上传的文件
     * @param objectName MinIO 存储的对象名
     * @return 临时访问链接
     */
    public String uploadFile(MultipartFile multipartFile, String objectName) {
        return uploadFile(multipartFile, objectName, defaultBucket);
    }

    public String uploadFile(MultipartFile multipartFile, String objectName, String bucketName) {
        // 1. 校验 MultipartFile
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new RuntimeException("上传的 MultipartFile 为空！");
        }

        // 2. 将 MultipartFile 转为临时 File（Spring Boot 3 原生方式，无废弃类）
        File tempFile = null;
        try {
            // 创建临时文件（JVM 退出时自动删除）
            tempFile = Files.createTempFile(
                    "minio_temp_",
                    "." + getFileSuffix(multipartFile.getOriginalFilename())
            ).toFile();
            // 标记为临时文件，JVM 关闭时删除
            tempFile.deleteOnExit();
            // 将 MultipartFile 写入临时 File
            multipartFile.transferTo(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("MultipartFile 转 File 失败", e);
        }
        // 3. 调用核心方法上传
        String url = uploadFile(tempFile, objectName, bucketName);
        // 4. 手动删除临时文件（可选，防止磁盘占用）
        if (FileUtil.isEmpty(tempFile) && tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                // 仅打印警告，不影响主流程
                System.err.println("临时文件删除失败：" + tempFile.getAbsolutePath());
            }
        }

        return url;
    }

    /**
     * 获取文件后缀（无第三方依赖，当然hutool的FileUtil有对应方法）
     */
    private String getFileSuffix(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * 获取文件 ContentType
     */
    private String getContentType(String fileName) {
        String suffix = getFileSuffix(fileName);
        return switch (suffix) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png", "gif", "bmp" -> "image/" + suffix;
            case "txt" -> "text/plain";
            case "pdf" -> "application/pdf";
            case "doc", "docx" -> "application/msword";
            case "xls", "xlsx" -> "application/vnd.ms-excel";
            // 默认二进制流
            default -> "application/octet-stream";
        };
    }


    /**
     * 3. 下载文件（返回字节数组，可写入本地或响应给前端）
     */
    public byte[] downloadFile(String objectName) {
        return downloadFile(objectName, defaultBucket);
    }

    public byte[] downloadFile(String objectName, String bucketName) {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        ); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toByteArray();
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 4. 删除文件
     */
    public void deleteFile(String objectName) {
        deleteFile(objectName, defaultBucket);
    }

    public void deleteFile(String objectName, String bucketName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            System.out.println("文件 " + objectName + " 删除成功");
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 5. 获取文件临时访问链接（默认 7 天有效期）
     */
    public String getFileUrl(String objectName) {
        return getFileUrl(objectName, defaultBucket);
    }

    public String getFileUrl(String objectName, String bucketName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("获取文件链接失败", e);
        }
    }
}