# 摇去哪 Fate Wheel · uni-app 工程

面向大学生的「决策转盘 + 地点标记」App。
**选什么 / 去不去 / 买不买** 三种上头决策工具 + 把想去的地方钉在地图上 + 小组 / 校友共享。

> 核心策略：**小程序版只做「私密 + 小组」**（微信个人开发者类目限制），**App 版放开做「小组 + 校友共享」**（不受微信类目限制）。共享层级在小程序端可用条件编译收口。

---

## 一、快速运行

```bash
cd fate-wheel-app
npm install          # 首次
npm run dev:h5       # H5 预览（浏览器打开 http://localhost:5173/）
npm run build:h5     # 打包 H5 产物（dist/build/h5）
npm run dev:mp-weixin    # 微信小程序预览（需微信开发者工具导入 dist/dev/mp-weixin）
npm run build:mp-weixin  # 打包小程序
```

- 需 Node.js ≥ 18（本机 v22 已验证）。
- 打包 App（iOS/Android）：用 HBuilderX 导入本工程 → 云打包；或按 uni-app 官方 CLI 配置原生工程。

## 二、页面清单（12 页）

| 页面 | 路径 | 说明 |
|---|---|---|
| 引导页 | pages/guide/guide | 三步玩法介绍 |
| 登录页 | pages/login/login | 微信一键授权，**无需手机号**（个人开发者无法用手机号快速验证组件） |
| 首页 | pages/index/index | 三决策入口 + 今日命运卡 + 连签 |
| 选什么 | pages/wheel/wheel | 四色转盘（conic-gradient + 3.6s 缓动），从标记库抽取 |
| 去不去 | pages/scale/scale | 利弊天平 / 命运硬币 / 冷静计时 三模式 |
| 买不买 | pages/buy/buy | 四维打分 / 拥有成本 / 冷静期 |
| 我的标记地图 | pages/map/map | 地图 + 列表双视图，四分类筛选，统计 |
| 标记地点 | pages/mark/mark | 富字段表单 + **共享范围三选一**（仅自己 / 小组 / 本校） |
| 决策档案 | pages/archive/archive | 统计 + 最近决策 + 趣味统计 |
| 宿舍小组 | pages/group/group | ≤5 人邀请制小组，组内排行，追加信息 |
| 本校地图 | pages/alumni/alumni | App 端放开的校友共享：收藏=复制到个人库、举报，无评论无聊天 |
| 我的 | pages/mine/mine | 用户区 + PRO 卡（¥25/年，支付为 UI 占位） |

## 三、共享层级与权限（对齐《地点共享机制设计方案》）

- **L0 私密**：默认，仅本人可见
- **L1 小组**：≤5 人邀请制，成员可追加信息（补充备注/评分/图片），不可覆盖创建者内容
- **L3 校友**：App 端开放；同校用户可查看 / 收藏 / 加入转盘 / 举报；无评论、无聊天、无个人主页
- **L4 公开**：未实现（需企业主体）

标记页 `shareScope` 已实现三选一，默认私密；共享范围可随时修改 / 撤回，撤回不影响他人已收藏副本。

**小程序端收口方式**：在 `pages/mark/mark.vue` 中，用条件编译把「共享到本校」选项在小程序端隐藏：
```html
<!-- #ifndef MP-WEIXIN -->
<view class="scope-opt" ...>共享到本校</view>
<!-- #endif -->
```

## 四、数据层：对接若依后端（fate-wheel-ruoyi）

当前接口已全部对接 **若依后端**（`fate-wheel-ruoyi/` 工程，`/api/**`）：

1. 后端地址：`src/utils/request.js` 顶部 `BASE_URL`（默认 `http://localhost:8080`），部署后改成你服务器地址。
2. 请求封装：`src/utils/request.js` 统一注入 `Authorization: Bearer <token>`，401 自动清登录态并跳登录页。
3. 接口层：`src/api/index.js` 已按后端契约实现（登录/资料/标记/转盘池/决策/小组/校友/举报/统计/订单），并在该层完成后端字段 → 前端 store 的映射（`markId→id`、`normal→想去` 等），页面层无需改动。
4. 数据装载：登录成功后自动 `api.loadAll()` 拉取标记/小组/决策填充 `store`（reactive，视图自动更新）。
5. 登录：`api.login` 优先用 `uni.login()` 的 code（App/小程序真机环境），未配置微信开放平台时自动回退到本地设备 openid（`fw_openid`），保证联调可跑通。
6. 数据库：后端 SQL 见 `fate-wheel-ruoyi/sql/`（`ry_20260417.sql` + `fw_business.sql`），部署见 `fate-wheel-ruoyi/DEPLOY.md`。

> 旧 `src/utils/mock.js` 已不再被 store 引用（保留作参考）；如需纯前端离线演示，可在 `store.init()` 临时注入 mock 数据。

## 五、合规要点（来自产品与共享设计方案）

- 个人虚拟支付：仅工具类目可开，**禁付费随机**（抽卡/盲盒）、禁迷信、禁诱导分享、禁社交类虚拟道具。本工程所有付费点均为确定性权益（无限标记 / 皮肤 / 去广告 / 高级分析）。
- 校友共享在小程序端为 🟡 中高风险，需先向微信官方核实；**App 端不受此类目限制**，这是 App 版的核心差异价值。
- 所有 UGC 字段（标记名称/备注/图片）上线前需接微信内容安全 API（文本 msgSecCheck + 图片 imgSecCheck）。

## 六、已知限制（当前为可运行 MVP）

- 登录已对接后端（openid 自动建号）；正式上线建议配置微信开放平台 appid/secret 换成真实 code2session（后端 `FwUserService` 留注释位）
- 地图选点为占位弹窗，未接腾讯位置服务真实 SDK（标记页「选择地图位置」）
- 支付未接入（Pro 仅 UI，后端 `fw_order` 表已建好，`FwOrderService.confirmPay` 留 TODO 接微信虚拟支付回调发放权益）
- 决策 / 统计已持久化到后端（MySQL）

## 七、preview 目录

`preview/` 存放 11 张 H5 实际运行渲染截图（引导/登录/首页/转盘/转盘结果/标记共享/地图/天平/小组/校友/我的），可直接查看界面效果。
