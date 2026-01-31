# Redis 缓存反序列化问题解决方案

## 问题描述

### 错误信息
```
java.lang.ClassCastException: class java.util.LinkedHashMap cannot be cast to class com.xiaorui.agentapplicationcreator.response.ServerResponseEntity
```

### 根本原因

1. **泛型类型擦除**：
   - `@Cacheable` 注解缓存的是 `ServerResponseEntity<Page<AppVO>>` 类型
   - 由于 Java 泛型类型擦除，运行时只保留了原始类型 `ServerResponseEntity`
   - Jackson 反序列化时无法知道泛型参数是 `Page<AppVO>`
   - 因此将泛型字段反序列化为 `LinkedHashMap`

2. **旧缓存数据不兼容**：
   - 之前缓存的数据使用了不同的序列化配置
   - 新的 `RedisCacheManagerConfig` 尝试反序列化旧数据时失败

## 当前解决方案（临时）

### 禁用缓存

在 `AppController.listGoodAppInfoByPage` 方法中注释掉 `@Cacheable` 注解：

```java
@PostMapping("/good/list/page/info")
// 暂时禁用缓存，避免反序列化问题
// @Cacheable(
//         value = "good_app_page",
//         key = "T(com.xiaorui.agentapplicationcreator.util.CacheKeyUtil).generateKey(#appQueryRequest)",
//         condition = "#appQueryRequest.pageSize <= 20"
// )
public ServerResponseEntity<Page<AppVO>> listGoodAppInfoByPage(@RequestBody AppQueryRequest appQueryRequest) {
    // ...
}
```

### 优点
- 简单直接，立即解决问题
- 不影响核心功能

### 缺点
- 失去缓存性能优势
- 每次请求都需要查询数据库

## 完整解决方案（后续实施）

### 方案一：自定义缓存注解 + 类型安全的序列化

#### 1. 创建自定义缓存注解

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TypeSafeCacheable {
    String[] cacheNames();
    String key() default "";
    String condition() default "";
}
```

#### 2. 创建缓存切面

```java
@Aspect
@Component
public class TypeSafeCacheAspect {

    @Resource
    private CacheManager cacheManager;

    @Around("@annotation(typeSafeCacheable)")
    public Object around(ProceedingJoinPoint pjp, TypeSafeCacheable typeSafeCacheable) throws Throwable {
        // 获取方法返回类型的泛型信息
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Type returnType = signature.getGenericReturnType();

        // 检查缓存
        Cache cache = cacheManager.getCache(typeSafeCacheable.cacheNames()[0]);
        String cacheKey = generateKey(pjp, typeSafeCacheable);

        Object cachedValue = cache.get(cacheKey);
        if (cachedValue != null) {
            return deserialize(cachedValue, returnType);
        }

        // 执行方法
        Object result = pjp.proceed();

        // 序列化并缓存（保留类型信息）
        cache.put(cacheKey, serialize(result, returnType));

        return result;
    }

    private Object deserialize(Object value, Type type) {
        // 使用 Jackson 的 TypeReference 保留泛型信息
        TypeReference<?> typeRef = new TypeReference<Object>() {
            @Override
            public Type getType() {
                return type;
            }
        };
        return objectMapper.convertValue(value, typeRef);
    }
}
```

### 方案二：只缓存数据，不缓存包装对象

修改 `listGoodAppInfoByPage` 方法，只缓存 `Page<AppVO>` 数据：

```java
@PostMapping("/good/list/page/info")
public ServerResponseEntity<Page<AppVO>> listGoodAppInfoByPage(@RequestBody AppQueryRequest appQueryRequest) {
    // ... 查询逻辑 ...
    
    Page<AppVO> appInfoPage = buildPageResult(...);
    
    // 从缓存获取数据
    Page<AppVO> cachedData = cacheManager.getCache("good_app_page").get(cacheKey);
    if (cachedData != null) {
        return ServerResponseEntity.success(cachedData);
    }
    
    // 缓存数据（不缓存 ServerResponseEntity）
    cacheManager.getCache("good_app_page").put(cacheKey, appInfoPage);
    
    return ServerResponseEntity.success(appInfoPage);
}
```

### 方案三：使用 Spring Cache 抽象 + Redisson

使用 Redisson 的分布式缓存，它提供了更好的类型安全支持：

```java
@Configuration
public class RedissonCacheConfig {

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();
        config.put("good_app_page", 
            new CacheConfig()
                .setTtl(5, TimeUnit.MINUTES)
                .setMaxSize(1000));
        
        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
```

### 方案四：使用 Caffeine 本地缓存（推荐）

对于数据量不大的场景，使用 Caffeine 本地缓存：

```java
@Configuration
public class CaffeineCacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .recordStats());
        
        return cacheManager;
    }
}
```

**优点**：
- 不需要序列化/反序列化
- 类型安全
- 性能更好

**缺点**：
- 只能在单实例应用中使用
- 数据分布在各个实例中

### 方案五：修改 ObjectMapper 配置，启用默认类型信息

```java
@Configuration
public class RedisCacheManagerConfig {

    @Bean
    public CacheManager cacheManager() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 启用默认类型信息（每个 JSON 对象都会包含 @class 字段）
        objectMapper.enableDefaultTyping(
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        objectMapper.activateDefaultTyping(
            PolymorphicTypeValidator.builder().build(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // ... 其他配置 ...
        
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

**优点**：
- 保留完整的类型信息
- 解决泛型类型擦除问题

**缺点**：
- JSON 体积变大（包含类型信息）
- 安全性降低（反序列化时会加载任意类）

## 推荐方案

对于当前项目，**推荐使用方案二：只缓存数据，不缓存包装对象**

### 实施步骤

1. **重构 `listGoodAppInfoByPage` 方法**
   - 移除 `@Cacheable` 注解
   - 手动管理缓存
   - 只缓存 `Page<AppVO>` 数据

2. **清理旧缓存数据**
   - 使用 `CacheCleanupConfig` 在启动时清理
   - 或手动执行 Redis 命令清理

3. **测试验证**
   - 确认缓存正常工作
   - 确认类型转换错误不再出现

## 监控和告警

### 添加缓存命中率监控

```java
@Component
public class CacheMonitor {

    @Resource
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 60000) // 每分钟
    public void reportCacheStats() {
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
        Cache<Object, Object> cache = caffeineCacheManager.getCache("good_app_page").getNativeCache();
        CacheStats stats = ((Caffeine) cache).stats();
        
        log.info("Cache hit rate: {}", stats.hitRate());
        log.info("Cache miss count: {}", stats.missCount());
        log.info("Cache eviction count: {}", stats.evictionCount());
    }
}
```

### 添加反序列化异常告警

```java
@ExceptionHandler(ClassCastException.class)
public ServerResponseEntity<String> handleClassCastException(ClassCastException e) {
    if (e.getMessage().contains("ServerResponseEntity")) {
        log.error("Cache deserialization error detected, need to clear cache", e);
        // 自动清理缓存
        redisCacheUtil.clearCache("good_app_page");
    }
    return ServerResponseEntity.fail(ErrorCode.SYSTEM_ERROR, "服务暂时不可用");
}
```

## 总结

1. **短期方案**：禁用 `@Cacheable` 注解
2. **中期方案**：实施方案二（只缓存数据）
3. **长期方案**：考虑使用 Caffeine 或 Redisson

选择哪个方案取决于：
- 数据量大小
- 是否需要分布式缓存
- 对性能的要求
- 团队的技术栈熟悉程度
