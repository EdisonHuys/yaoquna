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
import { api } from '../../api/index'
import { timeAgo } from '../../utils/helper'

export default {
  data() {
    return {
      typeFilters: [
        { key: 'all', label: '全部' },
        { key: 'select', label: '选什么' },
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
      if (this.typeFilter === 'select') {
        list = list.filter(d => d.type === 'select' || d.type === 'choose' || d.type === '0')
      } else if (this.typeFilter === 'go') {
        list = list.filter(d => d.type === 'go' || d.type === '1')
      } else if (this.typeFilter === 'buy') {
        list = list.filter(d => d.type === 'buy' || d.type === '2')
      }
      return list
    }
  },
  async onShow() {
    store.init()
    try {
      const res = await api.getDecisions()
      if (res && res.data) {
        store.decisions = res.data
      }
    } catch (e) { /* ignore */ }
    this.stats = store.getStats()
  },
  methods: {
    timeAgo,
    typeIcon(type) {
      if (type === 'choose' || type === 'select' || type === '0') return '🎡'
      if (type === 'go' || type === '1') return '⚖️'
      return '🛒'
    }
  }
}
</script>

<style scoped>
.archive-page {
  min-height: 100vh;
  padding: 24rpx 32rpx calc(60rpx + constant(safe-area-inset-bottom));
  padding: 24rpx 32rpx calc(60rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.stat-hero {
  display: flex;
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  border-radius: 30rpx;
  padding: 34rpx 0;
  box-shadow: 0 10rpx 32rpx rgba(27, 86, 73, 0.06);
}
.stat-big {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  position: relative;
}
.stat-big:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 15%;
  height: 70%;
  width: 2rpx;
  background: rgba(31, 110, 95, 0.12);
}
.sb-num {
  font-size: 56rpx;
  font-weight: 900;
  color: #1F6E5F;
  font-variant-numeric: tabular-nums;
  line-height: 1.1;
}
.sb-label { font-size: 24rpx; font-weight: 600; color: #4A7A6E; }
.fun-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 26rpx 30rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 24rpx rgba(27, 86, 73, 0.04);
}
.fun-emoji { font-size: 52rpx; }
.fun-text { font-size: 26rpx; font-weight: 600; color: #2D4A41; flex: 1; line-height: 1.4; }
.fun-num { color: #FF7D42; font-weight: 900; font-size: 32rpx; }
.filter-bar {
  white-space: nowrap;
  padding: 24rpx 0 0;
  width: 100%;
  box-sizing: border-box;
}
.filter-chip {
  display: inline-flex;
  padding: 12rpx 32rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 600;
  background: #FFFFFF;
  color: #5E7A71;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  margin-right: 16rpx;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.04);
  transition: all 0.2s;
}
.filter-chip.on {
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  font-weight: 700;
  border-color: transparent;
  box-shadow: 0 6rpx 18rpx rgba(27, 86, 73, 0.25);
}
.decision-list { margin-top: 24rpx; }
.decision-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  padding: 24rpx 28rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.04);
  transition: transform 0.15s;
}
.decision-item:active {
  transform: scale(0.98);
}
.d-type {
  flex: 0 0 80rpx;
  width: 80rpx;
  height: 80rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 42rpx;
}
.d-type.type-choose { background: #FFF4EB; }
.d-type.type-go { background: #EBF4FD; }
.d-type.type-buy { background: #EEF8F3; }
.d-body { flex: 1; display: flex; flex-direction: column; gap: 6rpx; min-width: 0; }
.d-title {
  font-size: 30rpx;
  font-weight: 800;
  color: #14352D;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.d-detail {
  font-size: 24rpx;
  font-weight: 500;
  color: #5E7A71;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.d-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8rpx; flex-shrink: 0; }
.d-time { font-size: 22rpx; color: #8EA49D; }
.d-done {
  font-size: 20rpx;
  font-weight: 600;
  color: #8EA49D;
  background: #F0F5F2;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
}
.d-done.ok { color: #1F8A65; background: #EEF8F3; font-weight: 700; }
.empty { text-align: center; color: #8EA49D; font-size: 26rpx; padding: 80rpx 0; }
</style>
