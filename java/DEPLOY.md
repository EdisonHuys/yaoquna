# 摇去哪 · 若依后端 + 管理端部署手册

> 本工程是「摇去哪」App 的**后端**（RuoYi-Vue 3.9.2）与**管理端**（RuoYi-Vue3 前端）。
> App 端 uni-app 前端见同目录的 `fate-wheel-app/`。
>
> 你在本机已完成**编译验证**（`mvn package` BUILD SUCCESS，产物 `ruoyi-admin.jar`），
> 以下步骤请在**目标服务器**（有 JDK17 / MySQL / Redis / Node 运行环境的那台机器）执行。

---

## 1. 项目结构

```
fate-wheel-ruoyi/                 # 后端 + 管理端工程（本目录）
├── sql/
│   ├── ry_20260417.sql           # 若依框架基础库（系统表、菜单、admin 账号）
│   └── fw_business.sql           # 摇去哪业务库（9 张业务表 + 菜单 2000-2051）
├── ruoyi-admin/                  # 启动模块（打成 ruoyi-admin.jar）
│   └── src/main/java/com/ruoyi/
│       ├── web/controller/fatewheel/   # 管理端接口（标记/举报/学校/用户/订单/统计）
│       └── fatewheel/                  # App 登录 + 用户服务
├── ruoyi-system/src/main/java/com/ruoyi/fatewheel/
│   ├── domain/                   # 9 个实体 + 3 个 VO
│   ├── mapper/                   # 8 个 MyBatis 注解 Mapper
│   └── service/                  # 标记/小组/决策/学校/举报/订单/统计 业务
├── ruoyi-ui/                     # 管理端前端（Vue3 + Element Plus）
│   └── src/views/fatewheel/      # 6 个管理页面
└── ruoyi-admin.jar               # 已构建产物（可直接 java -jar 运行）
```

---

## 2. 环境要求（目标机）

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 后端运行 |
| Maven | 3.6+ | 重新打包用（也可直接跑现成 jar） |
| MySQL | 8.0+ | 数据库 |
| Redis | 5.0+ | 缓存（登录态、验证码） |
| Node.js | 18+ | 构建管理端前端 |

---

## 3. 数据库初始化

```bash
mysql -uroot -p
# 1) 建库（库名与后端配置一致，默认 ry-vue）
CREATE DATABASE IF NOT EXISTS `ry-vue` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `ry-vue`;

# 2) 先导入若依框架库
source /path/to/fate-wheel-ruoyi/sql/ry_20260417.sql;

# 3) 再导入摇去哪业务库（9 张表 + 菜单权限）
source /path/to/fate-wheel-ruoyi/sql/fw_business.sql;
```

> `fw_business.sql` 内含：fw_school / fw_user_extend / fw_mark / fw_group / fw_group_member /
> fw_decision / fw_favorite / fw_report / fw_order 九张业务表，以及
> 「摇去哪管理」目录 + 6 个子菜单（ID 2000-2051）与 admin 角色（role_id=1）的授权。
> 重复执行会报错，可先 `DROP TABLE` 或仅在首次导入。

**演示数据（可选）**：在 `fw_school` 里插入你的学校，例如：

```sql
INSERT INTO fw_school (school_name, school_code, city, sort, status) VALUES
('苏州大学', 'school_suda', '苏州', 1, '0');
```

---

## 4. 后端配置与启动

### 4.1 修改数据源

编辑 `ruoyi-admin/src/main/resources/application-druid.yml`：

```yaml
master:
  url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
  username: root
  password: 你的数据库密码
```

### 4.2 修改 Redis

编辑 `ruoyi-admin/src/main/resources/application.yml`：

```yaml
redis:
  host: localhost
  port: 6379
  password:            # 无密码留空
  database: 0
```

### 4.3 打包（可选，已有现成 jar 可跳过）

```bash
cd fate-wheel-ruoyi
mvn clean package -DskipTests
# 产物：ruoyi-admin/target/ruoyi-admin.jar
```

### 4.4 启动

```bash
java -jar ruoyi-admin/target/ruoyi-admin.jar
# 后台运行：
nohup java -jar ruoyi-admin/target/ruoyi-admin.jar > app.log 2>&1 &
```

启动成功后：
- 管理端接口：`http://服务器IP:8080`
- **App 接口：`http://服务器IP:8080/api/**`**（登录 `/api/login` 已加入安全白名单）
- 默认账号：`admin / admin123`

---

## 5. 管理端前端部署

### 5.1 本地开发调试

```bash
cd fate-wheel-ruoyi/ruoyi-ui
npm install
npm run dev
# 打开 http://localhost:1024 （默认端口 1024，登录 admin/admin123）
```

### 5.2 生产构建 + Nginx 部署

```bash
cd fate-wheel-ruoyi/ruoyi-ui
npm install
npm run build:prod          # 产物在 dist/
```

把 `dist/` 部署到 Nginx，并**反向代理后端**：

```nginx
server {
    listen 80;
    server_name 你的域名或IP;

    location / {
        root /data/fatewheel-admin/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端接口代理（管理端 /dev-api 与 App /api 都转给后端）
    location /prod-api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
    }
}
```

> 若依 Vue3 前端默认请求前缀是 `/dev-api`（开发）/ `/prod-api`（生产），
> 后端 `context-path: /` 下 `/system/**` 等接口会被代理到 `/` 去掉前缀，无需改配置。
> 如需自定义，改 `ruoyi-ui/.env.production` 的 `VITE_APP_BASE_API`。

---

## 6. App 前端对接（fate-wheel-app）

`fate-wheel-app/src/utils/request.js` 顶部：

```js
export const BASE_URL = 'http://localhost:8080'
```

改成本机可访问的后端地址：

```js
export const BASE_URL = 'http://你的服务器IP:8080'
```

- **打包 App**：`npm run build:app` 后用 HBuilderX 云打包，App 无跨域限制，直接走 BASE_URL。
- **H5 演示**：`npm run dev:h5`，浏览器访问需后端允许跨域（若依已默认开启 CORS）。
- **微信小程序**：小程序后台把 `request 合法域名` 配成后端 https 域名；个人主体可用自建域名 + SSL。
- **登录说明**：未配置微信开放平台 appid 时，App 端自动使用本地设备 openid 演示登录；
  正式上线建议在后端 `application.yml` 配置微信 appid/secret，并在 `FwUserService` 里换成真实 code2session 换取 openid。

---

## 7. 功能接口一览

### App 端 `/api/**`（带 token）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/login | 微信登录（openid/code + 昵称头像） |
| GET | /api/profile | 我的资料 |
| PUT | /api/profile/school | 设置学校 |
| GET | /api/marks?scope&category | 标记列表（scope: mine/group/school） |
| POST/PUT/DELETE | /api/marks(/id) | 标记增删改 |
| GET | /api/wheel/pool | 转盘素材池 |
| POST | /api/marks/{id}/favorite | 收藏 / 取消 |
| POST | /api/marks/{id}/visited | 标记已去 |
| GET/POST | /api/decisions | 决策记录 |
| GET/POST | /api/groups | 小组列表 / 建组 |
| POST | /api/groups/join | 邀请码加组 |
| GET | /api/groups/{id}/members | 小组成员 |
| GET | /api/schools | 学校列表 |
| GET | /api/alumni/marks | 校友共享标记 |
| POST | /api/reports | 提交举报 |
| GET | /api/stats | 我的统计 |
| POST/GET | /api/orders | 创建订单 / 我的订单 |

### 管理端 `/system/**`（需管理员权限）

| 路径 | 功能 |
|------|------|
| /system/fwmark | 地点标记管理 |
| /system/fwreport | 举报处理 |
| /system/fwschool | 学校管理 |
| /system/fwuserext | App 用户管理 |
| /system/fworder | 订单管理 |
| /system/fwstat | 数据统计 |

---

## 8. 常见问题

| 问题 | 处理 |
|------|------|
| 启动报 MySQL 连接失败 | 检查 application-druid.yml 用户名/密码/库名，确认 `ry-vue` 库存在 |
| 登录验证码出不来 | Redis 未启动：`redis-server` |
| App 请求 401 | token 过期或未带；重新登录 |
| 管理端页面空白/404 | dist 未部署正确或路由未匹配；确认用 `build:prod` 构建，Nginx `try_files` 指向 index.html |
| 菜单看不到「摇去哪管理」 | 确认已导入 fw_business.sql，且用 admin 登录（role_id=1 已授权） |
| Android/iOS 支付 | fw_order 表已预留，`FwOrderService.confirmPay` 中 `TODO` 处对接微信虚拟支付回调发放权益 |
