# Bug 遇见并解决

> xiaorui

### 解决 IDEA 连接 Mongodb 失败问题：
https://stackoverflow.com/questions/79843528/intellij-idea-driver-class-com-dbschema-mongojdbcdriver-is-incompatible-with


### minIO 社区版设置桶权限为 public

https://blog.csdn.net/lhq15222807611/article/details/151361609

修改【avatar 目录所有图片】批量预览（递归）
```bash
# 核心：同时配置 预览模式 + JPG图片标准MIME类型，递归修改所有头像
mc cp --attr "Content-Disposition=inline;Content-Type=image/jpeg" --recursive myminio/agent-app-creator-bucket/avatar/ myminio/agent-app-creator-bucket/avatar/
```
无痕模式访问（最稳妥，彻底绕过缓存）
重启生效
```bash
mc admin service restart myminio/
```
