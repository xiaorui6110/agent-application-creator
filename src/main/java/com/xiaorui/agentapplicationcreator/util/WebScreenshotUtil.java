package com.xiaorui.agentapplicationcreator.util;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaorui.agentapplicationcreator.execption.BusinessException;
import com.xiaorui.agentapplicationcreator.execption.ErrorCode;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Objects;

/**
 * @description: 生成应用封面图工具
 * @author: xiaorui
 * @date: 2026-01-02 13:22
 **/
@Slf4j
public class WebScreenshotUtil {

    private static volatile WebDriver webDriver = null;

    /**
     * 使用相对路径指向 resources 目录下的 ChromeDriver
     */
    private static final String CHROME_DRIVER_PATH = System.getProperty("user.dir") + "/src/main/resources/web_drivers/chromedriver.exe";
    /**
     * 默认页面宽高
     */
    private static final int DEFAULT_WIDTH = 1600;
    private static final int DEFAULT_HEIGHT = 900;
    /**
     * 临时截图根目录（抽成常量，统一维护）
     */
    private static final String TEMP_SCREENSHOT_ROOT = System.getProperty("user.dir") + "/tmp/screenshots";
    /**
     * 默认文件过期时间：24小时（单位：毫秒），可根据业务调整
     */
    private static final long DEFAULT_EXPIRE_TIME = 24 * 60 * 60 * 1000L;


    /**
     * 生成网页截图
     *
     * @param webUrl 网页URL
     * @return 压缩后的截图文件路径，失败返回null
     */
    public static String saveWebPageScreenshot(String webUrl) {
        if (StrUtil.isBlank(webUrl)) {
            log.error("网页URL不能为空");
            return null;
        }
        WebDriver driver = null;
        try {
            // 每次调用创建新的 WebDriver 实例（相较于 ThreadLocal 便于理解，但是重复初始化驱动会影响性能）
            driver = getWebDriver();
            // 创建临时目录（使用常量根目录，统一管理）
            String rootPath = TEMP_SCREENSHOT_ROOT + File.separator + UUID.randomUUID().toString().substring(0, 8);
            FileUtil.mkdir(rootPath);
            // 图片后缀
            final String imageSuffix = ".png";
            // 原始截图文件路径
            String imageSavePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + imageSuffix;
            // 访问网页
            driver.get(webUrl);
            // 等待页面加载完成
            waitForPageLoad(driver);
            // 截图
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            // 保存原始图片
            saveImage(screenshotBytes, imageSavePath);
            log.info("原始截图保存成功: {}", imageSavePath);
            // 压缩图片
            final String compressionSuffix = "_compressed.jpg";
            String compressedImagePath = rootPath + File.separator + RandomUtil.randomNumbers(5) + compressionSuffix;
            compressImage(imageSavePath, compressedImagePath);
            log.info("压缩图片保存成功: {}", compressedImagePath);
            // 删除原始图片，只保留压缩图片
            FileUtil.del(imageSavePath);
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败: {}", webUrl, e);
            return null;
        } finally {
            // 关闭当前 WebDriver 实例
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("关闭 WebDriver 时出现异常", e);
                }
            }
        }
    }


    /**
     * 初始化 Chrome 浏览器驱动
     * <a href="https://www.selenium.dev/zh-cn/documentation/webdriver/browsers/chrome/#options">...</a>
     */
    private static WebDriver getWebDriver() {
        try {
            // 检查 ChromeDriver 文件是否存在
            File driverFile = new File(CHROME_DRIVER_PATH);
            if (!driverFile.exists()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "ChromeDriver 未找到，请确保文件存在于: " + CHROME_DRIVER_PATH);
            }
            // 设置本地 ChromeDriver 路径
            System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式（新版Chrome推荐使用 --headless=new 替代旧的 --headless）
            options.addArguments("--headless=new");
            // 禁用 GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", DEFAULT_WIDTH, DEFAULT_HEIGHT));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理（chrome://version/ 查看）（selenium-java 依赖版本最好最新，与 chrome 浏览器保持一致）
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("初始化 Chrome 浏览器失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化 Chrome 浏览器失败");
        }
    }


    /**
     * 等待页面加载完成
     */
    private static void waitForPageLoad(WebDriver webDriver) {
        try {
            // 创建等待页面加载对象
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            // 等待 document.readyState 为 complete
            wait.until(driver -> Objects.equals(((JavascriptExecutor) driver)
                    .executeScript("return document.readyState"), "complete"));
            // 额外等待一段时间，确保动态内容加载完成
            Thread.sleep(2000);
            log.info("页面加载完成");
        } catch (Exception e) {
            log.error("等待页面加载时出现异常，继续执行截图", e);
        }
    }


    /**
     * 压缩图片
     */
    private static void compressImage(String originalImagePath, String compressedImagePath) {
        // 压缩图片质量（0.1 = 10% 质量）
        final float compressionQuality = 0.3f;
        try {
            ImgUtil.compress(FileUtil.file(originalImagePath), FileUtil.file(compressedImagePath), compressionQuality);
        } catch (Exception e) {
            log.error("压缩图片失败: {} -> {}", originalImagePath, compressedImagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩图片失败");
        }
    }

    /**
     * 保存图片到文件
     */
    private static void saveImage(byte[] imageBytes, String imagePath) {
        try {
            FileUtil.writeBytes(imageBytes, imagePath);
        } catch (Exception e) {
            log.error("保存图片失败: {}", imagePath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存图片失败");
        }
    }

    /**
     * 关闭浏览器（在项目停止前正确销毁驱动，释放资源）
     */
    @PreDestroy
    public static void destroy() {
        if (webDriver != null) {
            try {
                webDriver.quit();
            } catch (Exception e) {
                log.warn("关闭 WebDriver 时出现异常", e);
            } finally {
                webDriver = null;
            }
        }
    }

    /**
     * 清理过期的临时截图文件（默认清理24小时前的所有截图文件+空文件夹）
     */
    public static void cleanupTempFiles() {
        cleanupTempFiles(DEFAULT_EXPIRE_TIME);
    }

    /**
     * 重载方法：支持自定义过期时间，灵活适配不同业务场景
     *
     * @param expireTime 过期时间（单位：毫秒），例如 1小时=3600*1000
     */
    public static void cleanupTempFiles(long expireTime) {
        // 参数校验
        if (expireTime <= 0) {
            log.error("清理临时文件失败：过期时间必须大于0");
            return;
        }
        File rootDir = new File(TEMP_SCREENSHOT_ROOT);
        // 校验目录是否存在
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            log.info("临时截图目录不存在，无需清理：{}", TEMP_SCREENSHOT_ROOT);
            return;
        }

        // 计算过期时间阈值（当前时间 - 过期时长）
        long expireThreshold = System.currentTimeMillis() - expireTime;
        int deletedFileCount = 0;
        int deletedDirCount = 0;

        try {
            // 递归遍历目录下所有文件/子目录
            File[] childFiles = rootDir.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                log.info("临时截图目录为空，无需清理");
                return;
            }

            for (File file : childFiles) {
                if (file.isFile()) {
                    // 处理文件：判断最后修改时间是否过期
                    if (file.lastModified() < expireThreshold) {
                        boolean isDeleted = FileUtil.del(file);
                        if (isDeleted) {
                            deletedFileCount++;
                            log.debug("清理过期截图文件：{}", file.getAbsolutePath());
                        } else {
                            log.warn("清理文件失败，权限不足或文件被占用：{}", file.getAbsolutePath());
                        }
                    }
                } else if (file.isDirectory()) {
                    // 递归清理子目录内的过期文件
                    cleanDirRecursive(file, expireThreshold, deletedFileCount, deletedDirCount);
                    // 子目录清理完成后，判断是否为空，为空则删除
                    if (FileUtil.isEmpty(file)) {
                        boolean isDeleted = FileUtil.del(file);
                        if (isDeleted) {
                            deletedDirCount++;
                            log.debug("清理空的截图目录：{}", file.getAbsolutePath());
                        }
                    }
                }
            }

            log.info("临时截图文件清理完成 | 过期阈值：{}ms | 清理文件数：{} | 清理空目录数：{}",
                    expireTime, deletedFileCount, deletedDirCount);

        } catch (Exception e) {
            log.error("清理临时截图文件时发生异常", e);
        }
    }

    /**
     * 递归清理指定目录下的过期文件（内部工具方法）
     */
    private static void cleanDirRecursive(File dir, long expireThreshold, int deletedFileCount, int deletedDirCount) {
        File[] childFiles = dir.listFiles();
        if (childFiles == null) {
            return;
        }

        for (File file : childFiles) {
            if (file.isFile()) {
                // 文件过期则删除
                if (file.lastModified() < expireThreshold) {
                    boolean isDeleted = FileUtil.del(file);
                    if (isDeleted) {
                        deletedFileCount++;
                        log.debug("递归清理过期文件：{}", file.getAbsolutePath());
                    }
                }
            } else if (file.isDirectory()) {
                // 递归处理子目录
                cleanDirRecursive(file, expireThreshold, deletedFileCount, deletedDirCount);
                // 子目录为空则删除
                if (FileUtil.isEmpty(file)) {
                    boolean isDeleted = FileUtil.del(file);
                    if (isDeleted) {
                        deletedDirCount++;
                        log.debug("递归清理空目录：{}", file.getAbsolutePath());
                    }
                }
            }
        }
    }

}