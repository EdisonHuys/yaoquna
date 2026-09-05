<template>
  <view class="archive-page">
    <!-- 统计 -->
    <view class="stat-hero">
      <view class="stat-big">
        <text class="sb-num">{{ stats.monthDecisions }}</text>
        <text class="sb-label">本月决策</text>
      </view>
      <view class="stat-big">
        <text class="sb-num">{{ stats.execRate }}%</text>
        <text class="sb-label">执行率</text>
      </view>
      <view class="stat-big">
        <text class="sb-num">{{ stats.streak }}</text>
        <text class="sb-label">连续天数</text>
      </view>
    </view>

    <!-- 趣味统计 -->
    <view class="fun-card">
      <text class="fun-emoji">🍜</text>
      <text class="fun-text">你今年已经让命运帮你决定了 <text class="fun-num">24</text> 次吃什么</text>
    </view>

    <!-- 筛选 -->
    <scroll-view scroll-x class="filter-bar">
      <view
        v-for="c in typeFilters"
        :key="c.key"
        class="filter-chip"
        :class="{ on: typeFilter === c.key }"
        @click="typeFilter = c.key"
      >{{ c.label }}</view>
    </scroll-view>

    <!-- 决策列表 -->
    <view class="decision-list">
      <view v-for="d in filtered" :key="d.id" class="decision-item">
        <view class="d-type" :class="'type-' + d.type">
          {{ typeIcon(d.type) }}
        </view>
        <view class="d-body">
          <text class="d-title">{{ d.title }}</text>
          <text class="d-detail">{{ d.detail }}</text>
        </view>
        <view class="d-right">
          <text class="d-time">{{ timeAgo(d.time) }}</text>
          <text class="d-done" :class="d.executed ? 'ok' : ''">{{ d.executed ? '✓ 已执行' : '待执行' }}</text>
        </view>
      </view>
      <view v-if="filtered.length === 0" class="empty">还没有决策记录，去转一下转盘吧</view>
    </view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { timeAgo } from '../../utils/helper'

export default {
  data() {
    return {
      typeFilters: [
        { key: 'all', label: '全部' },
        { key: 'choose', label: '选什么' },
        { key: 'go', label: '去不去' },
        { key: 'buy', label: '买不买' }
      ],
      typeFilter: 'all',
      stats: { monthDecisions: 0, execRate: 0, streak: 0 }
    }
  },
  computed: {
    filtered() {
      let list = store.decisions
      if (this.typeFilter !== 'all') list = list.filter(d => d.type === this.typeFilter)
      return list
    }
  },
  onShow() {
    store.init()
    this.stats = store.getStats()
  },
  methods: {
    timeAgo,
    typeIcon(type) {
      if (type === 'choose') return '🎡'
      if (type === 'go') return '⚖️'
      return '🛒'
    }
  }
}
</script>

<style scoped>
.archive-page {
  min-height: 100vh;
  padding: 24rpx 32rpx 60rpx;
  box-sizing: border-box;
}
.stat-hero {
  display: flex;
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  border-radius: 28rpx;
  padding: 34rpx 0;
}
.stat-big {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}
.sb-num { font-size: 52rpx; font-weight: 800; color: #1F6E5F; }
.sb-label { font-size: 24rpx; color: #5E8F81; }
.fun-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 26rpx 30rpx;
  margin-top: 24rpx;
  box-shadow: 0 6rpx 24rpx rgba(31,110,95,0.05);
}
.fun-emoji { font-size: 48rpx; }
.fun-text { font-size: 26rpx; color: #4A5D57; flex: 1; }
.fun-num { color: #FF8A3D; font-weight: 800; font-size: 30rpx; }
.filter-bar {
  white-space: nowrap;
  padding: 24rpx 0 0;
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
.decision-list { margin-top: 24rpx; }
.decision-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 26rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 6rpx 24rpx rgba(31,110,95,0.05);
}
.d-type {
  flex: 0 0 76rpx;
  width: 76rpx;
  height: 76rpx;
  border-radius: 22rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40rpx;
}
.d-type.type-choose { background: #FFF3E8; }
.d-type.type-go { background: #EAF4FF; }
.d-type.type-buy { background: #E8F9F0; }
.d-body { flex: 1; display: flex; flex-direction: column; gap: 6rpx; }
.d-title { font-size: 28rpx; font-weight: 600; color: #1A3B34; }
.d-detail { font-size: 22rpx; color: #A9B8B2; }
.d-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8rpx; }
.d-time { font-size: 22rpx; color: #C9D4D0; }
.d-done {
  font-size: 20rpx;
  color: #A9B8B2;
  background: #F2F6F4;
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
}
.d-done.ok { color: #2EC77E; background: #E8F9F0; }
.empty { text-align: center; color: #A9B8B2; font-size: 26rpx; padding: 80rpx 0; }
</style>
