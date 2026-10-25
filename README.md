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
