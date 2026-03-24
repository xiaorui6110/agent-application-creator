# Bug 遇见并解决
> xiaorui，记录遇到的 Bug 和解决方案，有些可能还需要更多的操作才能解决，但是不放进文档中了。

### 解决 IDEA 连接 Mongodb 失败问题：
https://stackoverflow.com/questions/79843528/intellij-idea-driver-class-com-dbschema-mongojdbcdriver-is-incompatible-with


### minIO 社区版设置桶权限为 public

https://blog.csdn.net/lhq15222807611/article/details/151361609

```bash
wget https://dl.min.io/client/mc/release/linux-amd64/mc

chmod +x mc

sudo mv mc /usr/local/bin/

mc alias set myminio http://127.0.0.1:9000 xiaorui 18656412886ty

mc anonymous set public myminio/agent-app-creator-bucket
```

Ubuntu 默认不让 root 远程登录，

1 分钟解决（只需要在 Linux 里运行 3 条命令）
你现在登录你的 Linux 终端（已经是 root 了），直接复制运行这 3 行：
1. 开启 root 密码登录
   bash
   运行
   sed -i 's/^#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config
2. 开启密码认证
   bash
   运行
   sed -i 's/^PasswordAuthentication no/PasswordAuthentication yes/' /etc/ssh/sshd_config
3. 重启 SSH 服务（生效）
   bash
   运行
   service ssh restart
   然后你再测试
   Windows 终端输入：
   bash
   运行
   ssh root@172.19.48.249
   密码：123456


### 等等吧，后续再补充呗


没问题！我们全新创建一个专用 Nginx 配置，干净、安全、不影响默认配置，我一步步带你做，复制粘贴就能用。
一、创建新的 Nginx 配置文件
bash
运行
nano /etc/nginx/sites-available/agent-app.conf
二、把下面完整配置直接粘贴进去
nginx
server {
listen 80;
server_name 172.19.48.249;

    # 你上传的项目根目录（就是你之前创建的目录）
    root /home/xiaorui/nginx/code_deploy;
    index index.html index.htm;

    # 前端项目访问规则
    location / {
        try_files $uri $uri/ /index.html;
        expires 1h;
    }

    # 静态资源缓存
    location ~* \.(css|js|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf)$ {
        expires 7d;
    }
}
三、保存并退出
Ctrl + O → 回车Ctrl + X → 退出
四、启用这个配置（建立软链接）
bash
运行
ln -s /etc/nginx/sites-available/agent-app.conf /etc/nginx/sites-enabled/
五、测试 Nginx 配置是否正确
bash
运行
nginx -t
出现 successful 就是没问题 ✅
六、重启 Nginx 生效
bash
运行
systemctl restart nginx
七、给目录正确权限（必须）
bash
运行
chmod -R 755 /home/xiaorui
chown -R www-data:www-data /home/xiaorui/nginx
现在访问你的项目
plaintext
http://172.19.48.249/VYgRWb/
404 直接消失，页面正常显示！

