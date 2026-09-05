# 摇去哪

> 标记好地方、好吃的、好玩的 → 转盘随机决定 → 出发打卡

面向大学生的宝藏地点决策工具。把你收藏的美食、景点、好去处标记在地图上，纠结去哪的时候摇一摇转盘，让命运替你做决定。

## 目录结构

```
yaoquna/
├── app/          # 移动端前端（uni-app，一套代码编译小程序/App/H5）
├── web/          # 管理端 Web（Vue + Element UI）
├── java/         # 后端服务（若依 RuoYi-Vue，Spring Boot + MyBatis）
│   ├── ruoyi-admin/      # 启动模块
│   ├── ruoyi-framework/  # 框架核心
│   ├── ruoyi-system/     # 系统模块
│   ├── ruoyi-common/     # 通用模块
│   ├── ruoyi-quartz/     # 定时任务
│   ├── ruoyi-generator/  # 代码生成
│   ├── sql/               # 数据库初始化脚本
│   └── bin/               # 启停脚本
└── docs/         # 产品文档（产品方案、设计文档、部署报告）
```

## 技术栈

| 端 | 技术 |
|----|------|
| 移动端 | uni-app + Vue3 + Vite |
| 管理端 | Vue2 + Element UI |
| 后端 | Spring Boot + MyBatis + MySQL 5.7 |
| 地图 | 腾讯位置服务 |

## 核心功能

- **地点标记**：在地图上标记好吃的、好玩的、好地方，支持分类、评分、备注、图片
- **转盘决策**：从标记的地点中随机抽取，支持分类筛选、距离筛选
- **去不去 / 买不买**：利弊天平、命运硬币、价值打分等多种决策模式
- **小组共享**：≤5 人邀请制小组，共享宝藏地图
- **校友共享**：同校范围共享地点库（远期规划）
- **打卡记录**：决策档案、个人图鉴、成就系统

## 快速开始

### 移动端（app/）

```bash
cd app
npm install
npm run dev:mp-weixin   # 微信小程序
npm run dev:h5           # H5
```

### 管理端（web/）

```bash
cd web
npm install
npm run dev
```

### 后端（java/）

```bash
cd java
# 创建数据库并导入 sql/ry_2025.sql
# 修改 ruoyi-admin/src/main/resources/application-druid.yml 中的数据库配置
mvn clean package
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

默认账号：admin / admin123

## 品牌

- 名称：摇去哪
- Slogan：标记好地方，摇一摇就出发
- 主色：暖橙渐变

## 文档

- [产品方案](docs/产品方案-选择终结者.md)
- [地点共享机制设计](docs/地点共享机制设计方案.md)
- [线上部署验收报告](docs/线上部署验收报告.md)
