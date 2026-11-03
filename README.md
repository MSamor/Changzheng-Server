# Changzheng Server

长征计步微信小程序后端，Spring Boot + MyBatis-Plus，默认使用 SQLite，支持切换 MySQL。

## 环境

- Java 17+
- Maven 3.9+

## 启动

```bash
mvn spring-boot:run
```

默认端口 `8080`，SQLite 数据文件为 `./changzheng.db`。默认 `WECHAT_MOCK=true`，适合本地联调；生产环境请设置：

```bash
WECHAT_MOCK=false WECHAT_APP_ID=xxx WECHAT_APP_SECRET=xxx mvn spring-boot:run
```

切换 MySQL：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

并设置 `MYSQL_HOST`、`MYSQL_PORT`、`MYSQL_DATABASE`、`MYSQL_USERNAME`、`MYSQL_PASSWORD`。

## 兼容接口

- `GET /home/getUserIdByCode/{code}`
- `POST /home/steps`
- `POST /home/saveUserInfo`
- `GET /map/getAllData/{userId}`
- `PUT /map/exchangeSteps/{userId}`
- `GET /city/getDetail/{place}`
- `GET /rank/getAllRank?userId={userId}`
- `GET /rank/getTodayRank?userId={userId}`

所有接口返回 `{code,message,data}`，旧小程序读取的字段保留在 `data` 内。

## 注意

当前小程序会把登录 `code` 再传给步数接口。服务端会把首次登录得到的 `session_key` 持久化到 `wechat_sessions`，后续使用缓存会话完成微信运动数据解密。


### 排行榜头像昵称

- `GET /home/userInfo/{userId}`：读取头像、昵称和用户ID（不返回 openid / unionid）。
- `POST /home/avatar`：multipart，字段 `userId`、`file`，返回持久 `avatarUrl`。支持5MB以内PNG/JPG图片。
- `POST /home/saveUserInfo`：JSON `{ "userId": 1, "nickname": "长征参与者", "avatarUrl": "https://你的域名/uploads/avatars/xxx.png" }`，成功后返回保存的资料。省略的其他资料字段保持原值。

头像保存到 `APP_UPLOAD_DIRECTORY`（默认 `data/uploads`）下的 `avatars` 目录，部署时请为此目录挂载持久存储。`APP_PUBLIC_BASE_URL` 可设置外网 HTTPS 服务地址（包含反向代理前缀，如有），否则使用请求的服务地址。新头像使用独立文件名，避免旧缓存。图片上传成功后需保存资料才会更新用户记录。

小程序上线时将后端 HTTPS 域名加入微信后台的 request、uploadFile、downloadFile 合法域名；本机 127.0.0.1 地址仅供电脑开发工具调试。资料接口沿用现有 userId 协议，目前并无服务端会话鉴权；若公网部署，需要统一补充登录凭证校验，前端仅允许点击自己的资料并不能代替服务端鉴权。
