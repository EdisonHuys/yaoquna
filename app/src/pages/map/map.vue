<template>
  <view class="map-page">
    <!-- 顶部 -->
    <view class="map-head">
      <view class="map-title-row">
        <text class="map-title">我的标记地图</text>
        <text class="map-sub">把想去的地方，钉在地图上</text>
      </view>
      <!-- 统计 -->
      <view class="stat-panel">
        <view class="stat-item">
          <text class="stat-num">{{ stats.totalMark }}</text>
          <text class="stat-label">标记总数</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.went }}</text>
          <text class="stat-label">已去过</text>
        </view>
        <view class="stat-item">
          <text class="stat-num">{{ stats.weedOutRate }}%</text>
          <text class="stat-label">拔草率</text>
        </view>
      </view>
    </view>

    <!-- 筛选 -->
    <scroll-view scroll-x class="filter-bar">
      <view
        v-for="c in filters"
        :key="c"
        class="filter-chip"
        :class="{ on: filter === c }"
        @click="filter = c"
      >{{ c }}</view>
    </scroll-view>

    <!-- 视图切换 -->
    <view class="view-switch">
      <view class="vs-item" :class="{ on: view === 'map' }" @click="view = 'map'">🗺️ 地图</view>
      <view class="vs-item" :class="{ on: view === 'list' }" @click="view = 'list'">📋 列表</view>
    </view>

    <!-- 地图视图（示意地图） -->
    <view v-if="view === 'map'" class="mock-map">
      <view class="map-grid"></view>
      <view class="road h1"></view>
      <view class="road h2"></view>
      <view class="road v1"></view>
      <view class="road v2"></view>

      <view
        v-for="(m, i) in filteredMarks"
        :key="m.id"
        class="map-pin"
        :class="m.status === '已去' ? 'went' : ''"
        :style="{ left: pinPos[i].x + '%', top: pinPos[i].y + '%', background: pinColor(m.category) }"
        @click="selectMark(m)"
      >
        <text class="pin-text">{{ m.name.slice(0, 2) }}</text>
      </view>

      <view v-if="selected" class="map-bubble">
        <view class="bubble-name">{{ selected.name }}</view>
        <view class="bubble-meta">
          <text class="b-cat">{{ selected.category }}</text>
          <text>⭐{{ selected.rating }}</text>
          <text>¥{{ selected.price }}</text>
        </view>
        <view class="bubble-actions">
          <text class="b-act" @click="goMark(selected)">编辑</text>
          <text class="b-act" @click="useInWheel(selected)">加入转盘</text>
          <text class="b-act" @click="removeMark(selected)">删除</text>
        </view>
      </view>

      <view v-if="filteredMarks.length === 0" class="map-empty">这个分类还没有标记</view>
    </view>

    <!-- 列表视图 -->
    <view v-else class="mark-list">
      <view v-for="m in filteredMarks" :key="m.id" class="mark-item" @click="selectMark(m)">
        <view class="mark-dot" :style="{ background: pinColor(m.category) }"></view>
        <view class="mark-body">
          <view class="mark-name-row">
            <text class="mark-name">{{ m.name }}</text>
            <text class="mark-status" :class="m.status === '已去' ? 'went' : ''">{{ m.status }}</text>
          </view>
          <text class="mark-addr">{{ m.address }}</text>
          <view class="mark-tags">
            <text class="tag">{{ m.category }}</text>
            <text v-for="t in m.tags" :key="t" class="tag">{{ t }}</text>
          </view>
        </view>
        <view class="mark-share" :class="'scope-' + m.shareScope">
          {{ shareLabel(m.shareScope) }}
        </view>
      </view>
      <view v-if="filteredMarks.length === 0" class="list-empty">
        <text>还没有标记，点右下角 + 添加第一个地点</text>
      </view>
    </view>

    <!-- 添加按钮 -->
    <view class="add-fab" @click="goMark(null)">＋</view>

    <view class="fw-safe-bottom"></view>
    <fw-tabbar active="map"></fw-tabbar>
  </view>
</template>

<script>
import FwTabbar from '../../components/fw-tabbar.vue'
import { store } from '../../store/index'
import { categoryColor, getWheelPool } from '../../utils/helper'

export default {
  components: { FwTabbar },
  data() {
    return {
      filters: ['全部', '美食', '玩乐', '购物', '自习'],
      filter: '全部',
      view: 'map',
      selected: null,
      stats: { totalMark: 0, went: 0, weedOutRate: 0 },
      pinPos: []
    }
  },
  computed: {
    filteredMarks() {
      let list = store.getMyMarks()
      if (this.filter !== '全部') list = list.filter(m => m.category === this.filter)
      // 生成随机分布点
      this.pinPos = list.map(() => ({
        x: 10 + Math.random() * 78,
        y: 12 + Math.random() * 76
      }))
      return list
    }
  },
  onShow() {
    store.init()
    this.stats = store.getStats()
  },
  methods: {
    pinColor(cat) { return categoryColor(cat) },
    shareLabel(scope) {
      if (scope === 'private') return '私密'
      if (scope === 'group') return '小组'
      if (scope === 'school') return '本校'
      return '私密'
    },
    selectMark(m) {
      this.selected = m
    },
    goMark(m) {
      uni.navigateTo({ url: '/pages/mark/mark' + (m ? '?id=' + m.id : '') })
    },
    useInWheel(m) {
      uni.navigateTo({ url: '/pages/wheel/wheel' })
    },
    removeMark(m) {
      uni.showModal({
        title: '删除标记',
        content: '确定删除「' + m.name + '」吗？',
        success: res => {
          if (res.confirm) {
            store.removeMark(m.id)
            this.selected = null
            this.stats = store.getStats()
            uni.showToast({ title: '已删除', icon: 'none' })
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.map-page {
  min-height: 100vh;
  background: #F5F9F7;
  padding-bottom: 160rpx;
  box-sizing: border-box;
}
.map-head {
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  padding: calc(88rpx + env(safe-area-inset-top)) 40rpx 40rpx;
  border-bottom-left-radius: 40rpx;
  border-bottom-right-radius: 40rpx;
}
.map-title-row { display: flex; flex-direction: column; }
.map-title { font-size: 44rpx; font-weight: 800; color: #1A3B34; }
.map-sub { font-size: 24rpx; color: #5E8F81; margin-top: 6rpx; }
.stat-panel {
  display: flex;
  justify-content: space-around;
  background: rgba(255,255,255,0.85);
  border-radius: 24rpx;
  padding: 24rpx 0;
  margin-top: 30rpx;
}
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4rpx; }
.stat-num { font-size: 40rpx; font-weight: 800; color: #1F6E5F; }
.stat-label { font-size: 22rpx; color: #7A8A84; }
.filter-bar {
  white-space: nowrap;
  padding: 24rpx 32rpx 0;
  width: 100%;
  box-sizing: border-box;
}
.filter-chip {
  display: inline-flex;
  padding: 12rpx 30rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  background: #FFFFFF;
  color: #7A8A84;
  margin-right: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.filter-chip.on { background: #1F6E5F; color: #FFFFFF; font-weight: 600; }
.view-switch {
  display: flex;
  gap: 12rpx;
  margin: 24rpx 32rpx 0;
}
.vs-item {
  padding: 12rpx 34rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  background: #FFFFFF;
  color: #7A8A84;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.vs-item.on { background: linear-gradient(135deg, #34D0A8, #38B6E8); color: #FFFFFF; font-weight: 600; }

/* 示意地图 */
.mock-map {
  position: relative;
  height: 560rpx;
  margin: 24rpx 32rpx 0;
  border-radius: 28rpx;
  background: linear-gradient(160deg, #EAF3E8, #E3EEF4);
  overflow: hidden;
  border: 2rpx solid #E0EBE6;
}
.map-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(120,160,150,0.08) 2rpx, transparent 2rpx),
    linear-gradient(90deg, rgba(120,160,150,0.08) 2rpx, transparent 2rpx);
  background-size: 60rpx 60rpx;
}
.road {
  position: absolute;
  background: rgba(255,255,255,0.7);
  border-radius: 4rpx;
}
.road.h1 { top: 30%; left: 0; right: 0; height: 16rpx; }
.road.h2 { top: 66%; left: 0; right: 0; height: 10rpx; }
.road.v1 { left: 40%; top: 0; bottom: 0; width: 14rpx; }
.road.v2 { left: 72%; top: 0; bottom: 0; width: 8rpx; }
.map-pin {
  position: absolute;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50% 50% 50% 4rpx;
  transform: rotate(-45deg);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 20rpx rgba(0,0,0,0.2);
  border: 4rpx solid #FFFFFF;
  box-sizing: border-box;
  transition: transform 0.2s;
}
.map-pin.went { opacity: 0.55; }
.pin-text {
  transform: rotate(45deg);
  color: #FFFFFF;
  font-size: 22rpx;
  font-weight: 700;
}
.map-bubble {
  position: absolute;
  left: 24rpx;
  right: 24rpx;
  bottom: 24rpx;
  background: #FFFFFF;
  border-radius: 22rpx;
  padding: 24rpx;
  box-shadow: 0 16rpx 40rpx rgba(0,0,0,0.12);
  z-index: 20;
}
.bubble-name { font-size: 32rpx; font-weight: 700; color: #1A3B34; }
.bubble-meta { display: flex; gap: 12rpx; margin-top: 8rpx; font-size: 24rpx; color: #7A8A84; }
.b-cat { color: #FF8A3D; }
.bubble-actions { display: flex; gap: 24rpx; margin-top: 16rpx; }
.b-act { font-size: 26rpx; color: #1F6E5F; font-weight: 600; }
.map-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #A9B8B2;
  font-size: 26rpx;
}

/* 列表 */
.mark-list { padding: 24rpx 32rpx 0; }
.mark-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 6rpx 24rpx rgba(31,110,95,0.05);
}
.mark-dot {
  flex: 0 0 16rpx;
  width: 16rpx;
  height: 70rpx;
  border-radius: 8rpx;
}
.mark-body { flex: 1; display: flex; flex-direction: column; gap: 6rpx; }
.mark-name-row { display: flex; align-items: center; gap: 14rpx; }
.mark-name { font-size: 30rpx; font-weight: 700; color: #1A3B34; }
.mark-status {
  font-size: 20rpx;
  color: #FF8A3D;
  background: #FFF3E8;
  padding: 2rpx 14rpx;
  border-radius: 999rpx;
}
.mark-status.went { color: #2EC77E; background: #E8F9F0; }
.mark-addr { font-size: 22rpx; color: #A9B8B2; }
.mark-tags { display: flex; gap: 10rpx; flex-wrap: wrap; }
.tag {
  font-size: 20rpx;
  color: #4A5D57;
  background: #F2F6F4;
  padding: 2rpx 14rpx;
  border-radius: 999rpx;
}
.mark-share {
  font-size: 20rpx;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
}
.mark-share.scope-private { color: #A9B8B2; background: #F2F6F4; }
.mark-share.scope-group { color: #3FA7E0; background: #EAF4FF; }
.mark-share.scope-school { color: #2EC77E; background: #E8F9F0; }
.list-empty { text-align: center; color: #A9B8B2; font-size: 26rpx; padding: 80rpx 0; }

.add-fab {
  position: fixed;
  right: 40rpx;
  bottom: 200rpx;
  width: 110rpx;
  height: 110rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #34D0A8, #38B6E8);
  color: #FFFFFF;
  font-size: 60rpx;
  font-weight: 300;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 16rpx 40rpx rgba(31,110,95,0.35);
  z-index: 50;
}
</style>
