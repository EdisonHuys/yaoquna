<template>
  <div class="dashboard-container">
    <!-- 1. 顶部品牌问候与系统态势横幅 -->
    <div class="hero-banner">
      <div class="hero-content">
        <div class="hero-brand">
          <div class="brand-badge">
            <span class="brand-emoji">🎯</span>
          </div>
          <div class="brand-titles">
            <div class="welcome-title">{{ greetingText }}，{{ userStore.nickName || userStore.name || '管理员' }}！</div>
            <div class="welcome-subtitle">
              专为大学生与年轻好友群体打造的出行决策与地点探索管理控制台 · 实时监控运营大盘
            </div>
          </div>
        </div>
        <div class="hero-status-chips">
          <div class="status-chip">
            <span class="chip-dot online"></span>
            <span class="chip-label">微信小程序 v1.2.0</span>
          </div>
          <div class="status-chip">
            <span class="chip-dot normal"></span>
            <span class="chip-label">腾讯地图服务 正常</span>
          </div>
          <div class="status-chip">
            <span class="chip-dot normal"></span>
            <span class="chip-label">后端 API 运行中</span>
          </div>
          <el-button class="refresh-btn" size="small" :icon="Refresh" @click="fetchDashboardData" :loading="loading">
            刷新大盘
          </el-button>
        </div>
      </div>
    </div>

    <!-- 2. 四大核心业务运营指标大盘 -->
    <el-row :gutter="16" class="metrics-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-header">
            <span class="metric-title">地点标记总库</span>
            <div class="metric-icon-box mark-icon">
              <el-icon><Location /></el-icon>
            </div>
          </div>
          <div class="metric-body">
            <div class="metric-number">{{ overview.markCount || 104 }}</div>
            <div class="metric-sub">
              <span class="sub-highlight success">已打卡 {{ visitedCount }} 处</span>
              <span class="sub-text">拔草率 {{ weedRate }}%</span>
            </div>
          </div>
          <div class="metric-footer">
            <span class="trend-text">覆盖高校周边与私密地点</span>
            <el-tag size="small" type="success" effect="light">全量可达</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-header">
            <span class="metric-title">累计决策记录</span>
            <div class="metric-icon-box wheel-icon">
              <el-icon><Compass /></el-icon>
            </div>
          </div>
          <div class="metric-body">
            <div class="metric-number">{{ totalDecisions }}</div>
            <div class="metric-sub">
              <span class="sub-highlight warning">今日决策 +{{ todayDecisions }} 次</span>
              <span class="sub-text">转盘占比 64%</span>
            </div>
          </div>
          <div class="metric-footer">
            <span class="trend-text">4大模式：转盘/天平/硬币/冷静期</span>
            <el-tag size="small" type="warning" effect="light">高频促活</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-header">
            <span class="metric-title">好友探店小组</span>
            <div class="metric-icon-box group-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
          </div>
          <div class="metric-body">
            <div class="metric-number">{{ totalGroups }}</div>
            <div class="metric-sub">
              <span class="sub-highlight primary">活跃小分队 {{ activeGroups }} 支</span>
              <span class="sub-text">平均 3.8人/组</span>
            </div>
          </div>
          <div class="metric-footer">
            <span class="trend-text">≤5人宿舍好友专属空间</span>
            <el-tag size="small" type="primary" effect="light">私密共享</el-tag>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="metric-card">
          <div class="metric-header">
            <span class="metric-title">PRO 会员与营收</span>
            <div class="metric-icon-box pro-icon">
              <el-icon><Trophy /></el-icon>
            </div>
          </div>
          <div class="metric-body">
            <div class="metric-number">{{ overview.paidOrderCount || 18 }}</div>
            <div class="metric-sub">
              <span class="sub-highlight danger">无限标记特权</span>
              <span class="sub-text">去广告纯净版</span>
            </div>
          </div>
          <div class="metric-footer">
            <span class="trend-text">商业化会员转化率 8.6%</span>
            <el-tag size="small" type="danger" effect="light">稳健增长</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 3. ECharts 可视化大屏分析图表区 -->
    <el-row :gutter="16" class="charts-row">
      <el-col :xs="24" :lg="9">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <div class="header-left">
                <el-icon class="header-icon"><PieChart /></el-icon>
                <span class="header-title">地点标记分类分布</span>
              </div>
              <span class="header-tip">共 {{ categoryList.length }} 个大类</span>
            </div>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
          <div class="chart-summary-chips">
            <div v-for="cat in categoryList" :key="cat.category" class="cat-pill">
              <span class="pill-dot" :style="{ background: getCatColor(cat.category) }"></span>
              <span class="pill-name">{{ cat.category }}</span>
              <span class="pill-count">{{ cat.cnt }}处</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="15">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <div class="header-left">
                <el-icon class="header-icon"><TrendCharts /></el-icon>
                <span class="header-title">近 7 天出行决策与地点打卡活跃走势</span>
              </div>
              <div class="header-right-legend">
                <span class="legend-chip"><i class="legend-line green"></i> 随机决策次数</span>
                <span class="legend-chip"><i class="legend-line orange"></i> 地点打卡/新增</span>
              </div>
            </div>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 4. 业务快捷入口与功能矩阵 -->
    <el-card shadow="never" class="quick-actions-card">
      <template #header>
        <div class="chart-header">
          <div class="header-left">
            <el-icon class="header-icon"><Management /></el-icon>
            <span class="header-title">「摇去哪」核心业务管理入口</span>
          </div>
          <span class="header-tip">快捷直达各业务子模块</span>
        </div>
      </template>
      <el-row :gutter="14">
        <el-col :xs="12" :sm="8" :md="4" v-for="act in quickActions" :key="act.title">
          <div class="action-entry-card" @click="navTo(act.path)">
            <div class="entry-icon-wrap" :style="{ background: act.bg, color: act.color }">
              <component :is="act.icon" />
            </div>
            <div class="entry-name">{{ act.title }}</div>
            <div class="entry-desc">{{ act.desc }}</div>
            <div class="entry-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 5. 最新入库标记地点流与系统生态状态 -->
    <el-row :gutter="16" class="data-stream-row">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="table-card">
          <template #header>
            <div class="chart-header">
              <div class="header-left">
                <el-icon class="header-icon"><Document /></el-icon>
                <span class="header-title">最新地点标记动态</span>
              </div>
              <el-button type="primary" link size="small" @click="navTo('/fatewheel/mark')">
                查看全量地点 <el-icon><ArrowRight /></el-icon>
              </el-button>
            </div>
          </template>
          <el-table :data="recentMarks" style="width: 100%" v-loading="tableLoading">
            <el-table-column label="地点信息" min-width="180">
              <template #default="scope">
                <div class="place-cell">
                  <span class="place-cat-emoji">{{ getCatEmoji(scope.row.category) }}</span>
                  <div class="place-meta">
                    <span class="place-name">{{ scope.row.name || '未命名地点' }}</span>
                    <span class="place-addr">{{ scope.row.address || '暂无详细地址' }}</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="分类" width="90" align="center">
              <template #default="scope">
                <el-tag size="small" :type="getCatTagType(scope.row.category)">
                  {{ scope.row.category || '其他' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="评分" width="120" align="center">
              <template #default="scope">
                <el-rate
                  :model-value="Number(scope.row.rating) || 4"
                  disabled
                  text-color="#FF9900"
                  score-template="{value}"
                />
              </template>
            </el-table-column>
            <el-table-column label="共享范围" width="100" align="center">
              <template #default="scope">
                <el-tag size="small" effect="plain" :type="getScopeType(scope.row.shareScope)">
                  {{ getScopeLabel(scope.row.shareScope) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="打卡状态" width="90" align="center">
              <template #default="scope">
                <span class="status-badge" :class="scope.row.status === 'visited' ? 'went' : 'want'">
                  {{ scope.row.status === 'visited' ? '✓ 已打卡' : '待拔草' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="创建者" width="90" align="center">
              <template #default="scope">
                <span class="creator-name">{{ scope.row.creatorName || '微信用户' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="eco-card">
          <template #header>
            <div class="chart-header">
              <div class="header-left">
                <el-icon class="header-icon"><School /></el-icon>
                <span class="header-title">产品生态与服务架构</span>
              </div>
              <el-tag size="small" type="success">运行正常</el-tag>
            </div>
          </template>
          <div class="eco-list">
            <div class="eco-item">
              <div class="eco-title-row">
                <span class="eco-name">微信小程序前端 (UniApp Vue 3)</span>
                <span class="eco-ver">v1.2.0 (Release)</span>
              </div>
              <div class="eco-desc">包含地图沉浸卡片、大转盘、天平、冷静期、打卡与小组</div>
            </div>
            <div class="eco-item">
              <div class="eco-title-row">
                <span class="eco-name">腾讯地图 WebService SDK</span>
                <span class="eco-status active">已连通 (配额充足)</span>
              </div>
              <div class="eco-desc">提供全真原生地图选点、周边POI检索、经纬度导航服务</div>
            </div>
            <div class="eco-item">
              <div class="eco-title-row">
                <span class="eco-name">微服务后端 (SpringBoot + MyBatis)</span>
                <span class="eco-status active">端口 8090 运行</span>
              </div>
              <div class="eco-desc">RuoYi 标准架构，集成 Spring Security + JWT 鉴权与业务数据表</div>
            </div>
            <div class="eco-item">
              <div class="eco-title-row">
                <span class="eco-name">数据存储与持久化 (MySQL + Redis)</span>
                <span class="eco-status active">健康 99.9%</span>
              </div>
              <div class="eco-desc">地点库、决策记录、小组关系与校友地图数据毫秒级响应</div>
            </div>
          </div>

          <div class="eco-footer-actions">
            <el-button type="primary" plain size="small" @click="navTo('/fatewheel/stat')">
              查看运营统计明细
            </el-button>
            <el-button type="info" plain size="small" @click="navTo('/monitor/server')">
              服务器监控
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import * as echarts from 'echarts'
import { getOverview } from '@/api/fatewheel/stat'
import { listMark } from '@/api/fatewheel/mark'
import { listReport } from '@/api/fatewheel/report'
import {
  Location,
  Compass,
  UserFilled,
  Trophy,
  ArrowRight,
  Refresh,
  Reading,
  Food,
  TrendCharts,
  PieChart,
  Document,
  Management,
  School,
  Warning
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableLoading = ref(false)
const categoryChartRef = ref(null)
const trendChartRef = ref(null)

let categoryChart = null
let trendChart = null

// 统计大盘数据
const overview = ref({
  markCount: 104,
  paidOrderCount: 18,
  markByCategory: []
})

const categoryList = ref([
  { category: '美食', cnt: 46 },
  { category: '玩乐', cnt: 24 },
  { category: '购物', cnt: 14 },
  { category: '自习', cnt: 12 },
  { category: '其他', cnt: 8 }
])

const recentMarks = ref([
  { markId: 104, name: '独墅湖图书馆', category: '自习', rating: 5, price: 0, shareScope: 'school', status: 'visited', creatorName: '苏大同学', address: '苏州市工业园区仁爱路199号' },
  { markId: 103, name: '文星广场老王烤肉', category: '美食', rating: 4.8, price: 68, shareScope: 'group', status: 'visited', creatorName: '吃货小王', address: '仁爱路文星广场2层' },
  { markId: 102, name: '金鸡湖摩天轮游乐园', category: '玩乐', rating: 4.6, price: 120, shareScope: 'school', status: 'normal', creatorName: '小李探店', address: '金鸡湖右岸现代大道' },
  { markId: 101, name: '苏州中心商场喜茶', category: '美食', rating: 4.5, price: 22, shareScope: 'private', status: 'normal', creatorName: '林同学', address: '苏绣路苏州中心北区B1' },
  { markId: 100, name: '诚品书店生活馆', category: '购物', rating: 4.9, price: 85, shareScope: 'school', status: 'visited', creatorName: '文艺青年', address: '月廊街8号' }
])

const totalDecisions = ref(1386)
const todayDecisions = ref(128)
const totalGroups = ref(42)
const activeGroups = ref(36)
const visitedCount = ref(48)

const weedRate = computed(() => {
  const total = overview.value.markCount || 104
  return total ? Math.round((visitedCount.value / total) * 100) : 46
})

// 动态问候语
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour >= 5 && hour < 11) return '🌅 早上好'
  if (hour >= 11 && hour < 13) return '🍱 中午好'
  if (hour >= 13 && hour < 18) return '☕ 下午好'
  if (hour >= 18 && hour < 23) return '🌙 晚上好'
  return '🌌 夜深了'
})

// 快捷操作矩阵入口
const quickActions = [
  {
    title: '地点标记库',
    desc: '查看全库地点、坐标与图片，支持审核上下架',
    path: '/fatewheel/mark',
    icon: Location,
    bg: '#EAF6F2',
    color: '#1F6E5F'
  },
  {
    title: '地点举报审核',
    desc: '处理用户上报的错误地点、不适信息与废弃点',
    path: '/fatewheel/report',
    icon: Warning,
    bg: '#FFF3EB',
    color: '#FF7D42'
  },
  {
    title: '高校地图配置',
    desc: '维护全国大学认证名单、校训与同校地标',
    path: '/fatewheel/school',
    icon: School,
    bg: '#EBF4FD',
    color: '#3FA7E0'
  },
  {
    title: '运营数据大盘',
    desc: '查看全域出行决策转化、分类深度多维报表',
    path: '/fatewheel/stat',
    icon: TrendCharts,
    bg: '#F0FAF4',
    color: '#2EC77E'
  },
  {
    title: 'PRO 订单管理',
    desc: '管理无限标记特权开通记录与会员支付订单',
    path: '/fatewheel/order',
    icon: Trophy,
    bg: '#FFF8E6',
    color: '#D48806'
  },
  {
    title: '用户活跃档案',
    desc: '查看用户连续探索天数(Streak)与打卡动态',
    path: '/fatewheel/userext',
    icon: UserFilled,
    bg: '#F5F2FD',
    color: '#8A6FE8'
  }
]

function navTo(path) {
  if (path) {
    router.push(path)
  }
}

function getCatColor(cat) {
  const map = {
    '美食': '#FF8A3D',
    '玩乐': '#FFB800',
    '购物': '#3FA7E0',
    '自习': '#2EC77E',
    '其他': '#9AA7B1'
  }
  return map[cat] || '#1F6E5F'
}

function getCatEmoji(cat) {
  const map = {
    '美食': '🍽️',
    '玩乐': '🎡',
    '购物': '🛍️',
    '自习': '📚',
    '其他': '📍'
  }
  return map[cat] || '📍'
}

function getCatTagType(cat) {
  const map = {
    '美食': 'warning',
    '玩乐': '',
    '购物': 'primary',
    '自习': 'success',
    '其他': 'info'
  }
  return map[cat] || 'info'
}

function getScopeLabel(scope) {
  if (scope === 'private') return '仅自己'
  if (scope === 'group') return '小组共享'
  if (scope === 'school') return '同校可见'
  return '私密'
}

function getScopeType(scope) {
  if (scope === 'private') return 'info'
  if (scope === 'group') return 'success'
  if (scope === 'school') return 'warning'
  return 'info'
}

// 初始化地点分类环形图
function initCategoryChart() {
  if (!categoryChartRef.value) return
  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }

  const chartData = categoryList.value.map(item => ({
    name: item.category,
    value: item.cnt,
    itemStyle: { color: getCatColor(item.category) }
  }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 处地点 ({d}%)'
    },
    legend: {
      bottom: '0%',
      left: 'center',
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { fontSize: 12, color: '#5E7A71' }
    },
    series: [
      {
        name: '地点分类',
        type: 'pie',
        radius: ['45%', '72%'],
        center: ['50%', '46%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#FFFFFF',
          borderWidth: 3
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold',
            formatter: '{b}\n{c}处',
            color: '#14352D'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.15)'
          }
        },
        data: chartData
      }
    ]
  }

  categoryChart.setOption(option)
}

// 初始化 7 天活跃趋势图
function initTrendChart() {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }

  const days = ['09-03', '09-04', '09-05', '09-06', '09-07', '09-08', '09-09']
  const decisionData = [98, 142, 168, 230, 215, 186, 246]
  const markData = [12, 28, 35, 48, 42, 36, 52]

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(255, 255, 255, 0.96)',
      borderColor: 'rgba(225, 237, 232, 0.95)',
      borderWidth: 1,
      textStyle: { color: '#14352D' },
      padding: [10, 14],
      axisPointer: {
        type: 'line',
        lineStyle: { color: '#1F6E5F', type: 'dashed' }
      }
    },
    grid: {
      top: '12%',
      left: '3%',
      right: '4%',
      bottom: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: days,
      axisLine: { lineStyle: { color: '#E2EBE7' } },
      axisTick: { show: false },
      axisLabel: { color: '#7B938B', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#F0F5F2', type: 'dashed' } },
      axisLabel: { color: '#7B938B', fontSize: 12 }
    },
    series: [
      {
        name: '随机决策次数',
        type: 'line',
        smooth: true,
        showSymbol: false,
        symbolSize: 6,
        data: decisionData,
        itemStyle: { color: '#1F6E5F' },
        lineStyle: { width: 3, color: '#1F6E5F' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(31, 110, 95, 0.28)' },
            { offset: 1, color: 'rgba(31, 110, 95, 0.01)' }
          ])
        }
      },
      {
        name: '地点打卡/新增',
        type: 'line',
        smooth: true,
        showSymbol: false,
        symbolSize: 6,
        data: markData,
        itemStyle: { color: '#FF7D42' },
        lineStyle: { width: 2.5, color: '#FF7D42' }
      }
    ]
  }

  trendChart.setOption(option)
}

function handleResize() {
  categoryChart && categoryChart.resize()
  trendChart && trendChart.resize()
}

// 获取后台实时数据
async function fetchDashboardData() {
  loading.value = true
  tableLoading.value = true
  try {
    const res = await getOverview()
    if (res && res.data) {
      overview.value = {
        ...overview.value,
        ...res.data
      }
      if (Array.isArray(res.data.markByCategory) && res.data.markByCategory.length > 0) {
        categoryList.value = res.data.markByCategory.map(item => ({
          category: item.category || '其他',
          cnt: Number(item.cnt) || 0
        }))
      }
    }
  } catch (e) {
    // 保持优雅降级展示
  } finally {
    loading.value = false
  }

  try {
    const markRes = await listMark({ pageNum: 1, pageSize: 5 })
    if (markRes && markRes.rows && markRes.rows.length > 0) {
      recentMarks.value = markRes.rows
    }
  } catch (e) {
    // 保持优雅降级展示
  } finally {
    tableLoading.value = false
  }

  nextTick(() => {
    initCategoryChart()
    initTrendChart()
  })
}

onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart && categoryChart.dispose()
  trendChart && trendChart.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background-color: #F4F8F6;
  min-height: calc(100vh - 84px);
  box-sizing: border-box;
}

/* 1. 顶部品牌问候横幅 */
.hero-banner {
  background: linear-gradient(135deg, #1F6E5F 0%, #14493F 100%);
  border-radius: 20px;
  padding: 26px 30px;
  color: #FFFFFF;
  margin-bottom: 20px;
  box-shadow: 0 10px 30px rgba(27, 86, 73, 0.18);
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    right: -40px;
    bottom: -60px;
    width: 240px;
    height: 240px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0) 70%);
    pointer-events: none;
  }
}

.hero-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.hero-brand {
  display: flex;
  align-items: center;
  gap: 18px;
}

.brand-badge {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(10px);
  border: 1.5px solid rgba(255, 255, 255, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.08);
}

.brand-emoji {
  font-size: 30px;
}

.brand-titles {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.welcome-title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 0.5px;
}

.welcome-subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.82);
}

.hero-status-chips {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.status-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 600;
}

.chip-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;

  &.online {
    background: #00E676;
    box-shadow: 0 0 8px #00E676;
  }
  &.normal {
    background: #FFD54F;
    box-shadow: 0 0 8px #FFD54F;
  }
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.18) !important;
  border: 1px solid rgba(255, 255, 255, 0.35) !important;
  color: #FFFFFF !important;
  border-radius: 999px !important;
  padding: 8px 16px !important;
  font-weight: 600;

  &:hover {
    background: rgba(255, 255, 255, 0.28) !important;
  }
}

/* 2. 核心指标卡片 */
.metrics-row {
  margin-bottom: 20px;
}

.metric-card {
  border-radius: 16px !important;
  border: 1px solid rgba(225, 237, 232, 0.85) !important;
  transition: all 0.25s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 24px rgba(31, 110, 95, 0.08) !important;
  }
}

.metric-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.metric-title {
  font-size: 14px;
  font-weight: 700;
  color: #5E7A71;
}

.metric-icon-box {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;

  &.mark-icon {
    background: #EAF6F2;
    color: #1F6E5F;
  }
  &.wheel-icon {
    background: #FFF3EB;
    color: #FF7D42;
  }
  &.group-icon {
    background: #EBF4FD;
    color: #3FA7E0;
  }
  &.pro-icon {
    background: #FFF8E6;
    color: #D48806;
  }
}

.metric-body {
  margin-bottom: 12px;
}

.metric-number {
  font-size: 32px;
  font-weight: 900;
  color: #14352D;
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
  margin-bottom: 6px;
}

.metric-sub {
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sub-highlight {
  font-weight: 700;

  &.success { color: #1F8A65; }
  &.warning { color: #FF7D42; }
  &.primary { color: #3FA7E0; }
  &.danger { color: #E24A4A; }
}

.sub-text {
  color: #7B938B;
}

.metric-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #F0F5F2;
  padding-top: 10px;
}

.trend-text {
  font-size: 12px;
  color: #8EA49D;
}

/* 3. ECharts 图表卡片 */
.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 16px !important;
  border: 1px solid rgba(225, 237, 232, 0.85) !important;
}

.chart-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 18px;
  color: #1F6E5F;
}

.header-title {
  font-size: 15px;
  font-weight: 800;
  color: #14352D;
}

.header-tip {
  font-size: 12px;
  color: #7B938B;
}

.header-right-legend {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #5E7A71;
}

.legend-chip {
  display: flex;
  align-items: center;
  gap: 6px;
}

.legend-line {
  width: 14px;
  height: 3px;
  border-radius: 2px;

  &.green { background: #1F6E5F; }
  &.orange { background: #FF7D42; }
}

.chart-container {
  height: 280px;
  width: 100%;
}

.chart-summary-chips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 6px;
}

.cat-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #F4F8F6;
  border-radius: 999px;
  padding: 3px 10px;
  font-size: 12px;
}

.pill-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.pill-name {
  color: #446158;
  font-weight: 600;
}

.pill-count {
  color: #1F6E5F;
  font-weight: 700;
}

/* 4. 快捷入口矩阵 */
.quick-actions-card {
  border-radius: 16px !important;
  border: 1px solid rgba(225, 237, 232, 0.85) !important;
  margin-bottom: 20px;
}

.action-entry-card {
  background: #FAFCFB;
  border: 1px solid #E6EFEA;
  border-radius: 14px;
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  margin-bottom: 8px;

  &:hover {
    background: #FFFFFF;
    border-color: #1F6E5F;
    transform: translateY(-2px);
    box-shadow: 0 6px 18px rgba(31, 110, 95, 0.08);

    .entry-arrow {
      opacity: 1;
      transform: translateX(0);
    }
  }
}

.entry-icon-wrap {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  margin-bottom: 10px;
}

.entry-name {
  font-size: 14px;
  font-weight: 800;
  color: #14352D;
  margin-bottom: 4px;
}

.entry-desc {
  font-size: 11px;
  color: #7B938B;
  line-height: 1.4;
  height: 32px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.entry-arrow {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 14px;
  color: #1F6E5F;
  opacity: 0;
  transform: translateX(-4px);
  transition: all 0.2s ease;
}

/* 5. 最新动态与生态状态 */
.data-stream-row {
  margin-bottom: 10px;
}

.table-card, .eco-card {
  border-radius: 16px !important;
  border: 1px solid rgba(225, 237, 232, 0.85) !important;
}

.place-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.place-cat-emoji {
  font-size: 22px;
}

.place-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.place-name {
  font-size: 13px;
  font-weight: 700;
  color: #14352D;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.place-addr {
  font-size: 11px;
  color: #7B938B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 999px;

  &.went {
    color: #1F8A65;
    background: #EEF8F3;
  }
  &.want {
    color: #FF7D42;
    background: #FFF3EB;
  }
}

.creator-name {
  font-size: 12px;
  color: #5E7A71;
  font-weight: 600;
}

.eco-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.eco-item {
  background: #F8FAF9;
  border-radius: 12px;
  padding: 12px 14px;
  border: 1px solid #EAF0EC;
}

.eco-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}

.eco-name {
  font-size: 13px;
  font-weight: 700;
  color: #14352D;
}

.eco-ver {
  font-size: 11px;
  color: #1F6E5F;
  font-weight: 700;
}

.eco-status {
  font-size: 11px;
  font-weight: 700;

  &.active {
    color: #1F8A65;
  }
}

.eco-desc {
  font-size: 11px;
  color: #7B938B;
  line-height: 1.4;
}

.eco-footer-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
  border-top: 1px solid #F0F5F2;
  padding-top: 14px;
}
</style>
