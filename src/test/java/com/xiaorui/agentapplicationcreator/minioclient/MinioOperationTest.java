package com.xiaorui.agentapplicationcreator.minioclient;

import com.xiaorui.agentapplicationcreator.manager.minio.MinioManager;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @description: MinIO 操作测试
 * @author: xiaorui
 * @date: 2025-12-02 10:59
 **/
@SpringBootTest
public class MinioOperationTest {

    // 测试专用参数
    private static final String TEST_BUCKET_NAME = "test.txt-minio-bucket";
    private static final String TEST_OBJECT_NAME = "test.txt-file.txt";
    private static final String TEST_FILE_CONTENT = "Hello MinIO Test!";
    private static final String DOWNLOAD_FILE_PATH = "./test.txt-download.txt";

    @Resource
    private MinioManager minioManager;

    @Resource
    private MinioClient minioClient;

    /**
     * 测试前置操作：创建测试桶 + 清理残留测试文件
     */
    @Test
    public void initTestEnv() {
        // 1. 创建测试桶
        minioManager.createBucket(TEST_BUCKET_NAME);
        System.out.println("测试前置：创建测试桶 " + TEST_BUCKET_NAME + " 成功");

        // 2. 清理残留的测试文件
        try {
            minioManager.deleteFile(TEST_OBJECT_NAME, TEST_BUCKET_NAME);
            System.out.println("测试前置：清理残留测试文件 " + TEST_OBJECT_NAME);
        } catch (RuntimeException e) {
            System.out.println("测试前置：无残留测试文件，无需清理");
        }
    }

    /**
     * 测试1：创建桶（覆盖已存在/新创建两种场景）
     */
    @Test
    public void testCreateBucket() {
        // 场景1：创建已存在的桶
        minioManager.createBucket(TEST_BUCKET_NAME);
        // 场景2：创建新的临时桶
        String tempBucket = "temp-test.txt-bucket-" + System.currentTimeMillis();
        minioManager.createBucket(tempBucket);
        // 验证桶是否存在
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(tempBucket).build()
            );
            assert exists : "临时桶 " + tempBucket + " 创建失败！";
            System.out.println("测试1：创建桶成功，临时桶名称：" + tempBucket);
            // 清理临时桶
            minioClient.removeBucket(
                    io.minio.RemoveBucketArgs.builder().bucket(tempBucket).build()
            );
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("测试1：创建桶验证失败", e);
        }
    }

    /**
     * 测试2：上传文件（修复 IOException 未处理问题）
     * 方式1：测试方法声明 throws IOException（推荐，简洁）
     */
    @Test
    public void testUploadFile() throws IOException { // 声明抛出 IOException
        // 1. 构造测试 MultipartFile（模拟前端上传的文件）
        MultipartFile testFile = new MockMultipartFile(
                "testFile",
                TEST_OBJECT_NAME,
                "text/plain",
                new ByteArrayInputStream(TEST_FILE_CONTENT.getBytes(StandardCharsets.UTF_8))
        );

        // 2. 调用上传方法
        String fileUrl = minioManager.uploadFile(testFile, TEST_OBJECT_NAME, TEST_BUCKET_NAME);

        // 3. 验证上传结果
        assert fileUrl != null && fileUrl.contains(TEST_OBJECT_NAME) : "文件上传失败，链接为空或异常！";
        System.out.println("测试2：文件上传成功，临时访问链接：" + fileUrl);
    }

    /**
     * 测试3：获取文件临时访问链接
     */
    @Test
    public void testGetFileUrl() throws IOException {
        // 先确保文件已上传
        testUploadFile();

        // 调用获取链接方法
        String fileUrl = minioManager.getFileUrl(TEST_OBJECT_NAME, TEST_BUCKET_NAME);

        // 验证链接有效性
        assert fileUrl.startsWith("http") && fileUrl.contains(TEST_BUCKET_NAME) : "临时链接生成失败！";
        System.out.println("测试3：获取临时链接成功：" + fileUrl);
    }

    /**
     * 测试4：下载文件
     */
    @Test
    public void testDownloadFile() throws IOException {
        // 先确保文件已上传
        testUploadFile();

        // 1. 调用下载方法
        byte[] fileBytes = minioManager.downloadFile(TEST_OBJECT_NAME, TEST_BUCKET_NAME);

        // 2. 验证文件内容
        String content = new String(fileBytes, StandardCharsets.UTF_8);
        assert content.equals(TEST_FILE_CONTENT) : "文件内容不一致，下载失败！";

        // 3. 将下载的文件写入本地
        try (FileOutputStream fos = new FileOutputStream(DOWNLOAD_FILE_PATH)) {
            fos.write(fileBytes);
            System.out.println("测试4：文件下载成功，本地保存路径：" + DOWNLOAD_FILE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("测试4：文件写入本地失败", e);
        }
    }

    /**
     * 测试5：删除文件
     */
    @Test
    public void testDeleteFile() throws IOException, MinioException, InvalidKeyException, NoSuchAlgorithmException {
        // 先确保文件已上传
        testUploadFile();

        // 1. 调用删除方法
        minioManager.deleteFile(TEST_OBJECT_NAME, TEST_BUCKET_NAME);

        // 2. 验证文件是否删除
        try {
            minioClient.getObject(
                    io.minio.GetObjectArgs.builder()
                            .bucket(TEST_BUCKET_NAME)
                            .object(TEST_OBJECT_NAME)
                            .build()
            );
            assert false : "文件未被删除，删除操作失败！";
        } catch (io.minio.errors.ErrorResponseException e) {
            assert e.errorResponse().code().equals("NoSuchKey") : "删除异常，错误码不符！";
            System.out.println("测试5：文件删除成功");
        }
    }

    /**
     * 测试后置操作：清理测试文件/目录
     */
    @Test
    public void cleanTestEnv() {
        // 1. 删除测试桶中的测试文件
        try {
            minioManager.deleteFile(TEST_OBJECT_NAME, TEST_BUCKET_NAME);
            System.out.println("测试后置：删除测试文件 " + TEST_OBJECT_NAME);
        } catch (RuntimeException e) {
            System.out.println("测试后置：测试文件已删除，无需重复清理");
        }

        // 2. 删除本地下载的测试文件
        File downloadFile = new File(DOWNLOAD_FILE_PATH);
        if (downloadFile.exists()) {
            boolean deleted = downloadFile.delete();
            System.out.println("测试后置：删除本地下载文件 " + (deleted ? "成功" : "失败"));
        }
    }

}
