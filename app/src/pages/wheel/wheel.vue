<template>
  <view class="wheel-page">
    <!-- 筛选 -->
    <scroll-view scroll-x class="filter-bar">
      <view
        v-for="c in categories"
        :key="c"
        class="filter-chip"
        :class="{ on: category === c }"
        @click="switchCat(c)"
      >{{ c }}</view>
    </scroll-view>

    <!-- 转盘 -->
    <view class="wheel-area">
      <fw-wheel
        ref="wheel"
        :pool="pool"
        :spinning="spinning"
        @spin="doSpin"
        @result="onResult"
      ></fw-wheel>
    </view>

    <view class="wheel-hint" v-if="pool.length === 0">
      还没有符合条件的标记，先去标记一些地点吧
    </view>

    <!-- 结果卡片 -->
    <view v-if="result" class="result-card">
      <view class="result-top">
        <text class="result-tag">命运的选择</text>
        <text class="result-name">{{ result.name }}</text>
        <view class="result-meta">
          <text class="chip cat-chip">{{ result.category }}</text>
          <text class="chip">⭐ {{ result.rating }}</text>
          <text class="chip">¥{{ result.price }}</text>
        </view>
        <text class="result-note" v-if="result.note">{{ result.note }}</text>
      </view>
      <view class="result-actions">
        <view class="fw-btn-ghost act-btn" @click="markDone">标记已去</view>
        <view class="fw-btn act-btn" @click="goMap">出发打卡</view>
      </view>
    </view>

    <!-- 底部统计 -->
    <view class="stats-bar">
      <view class="stat">
        <text class="stat-num">{{ stats.totalMark }}</text>
        <text class="stat-label">已标记</text>
      </view>
      <view class="stat">
        <text class="stat-num">{{ stats.went }}</text>
        <text class="stat-label">已去过</text>
      </view>
      <view class="stat">
        <text class="stat-num">{{ pool.length }}</text>
        <text class="stat-label">候选</text>
      </view>
    </view>
  </view>
</template>

<script>
import FwWheel from '../../components/fw-wheel.vue'
import { store } from '../../store/index'
import { getWheelPool } from '../../utils/helper'

export default {
  components: { FwWheel },
  data() {
    return {
      categories: ['全部', '美食', '玩乐', '购物', '自习'],
      category: '全部',
      pool: [],
      spinning: false,
      result: null,
      stats: { totalMark: 0, went: 0 }
    }
  },
  onShow() {
    store.init()
    this.reloadPool()
    this.stats = store.getStats()
  },
  methods: {
    reloadPool() {
      this.pool = getWheelPool(this.category).map(m => ({
        id: m.id,
        name: m.name.length > 4 ? m.name.slice(0, 4) : m.name,
        color: '',
        raw: m
      }))
      this.result = null
      if (this.$refs.wheel) this.$refs.wheel.reset()
    },
    switchCat(c) {
      if (this.spinning) return
      this.category = c
      this.reloadPool()
    },
    doSpin() {
      this.spinning = true
      this.result = null
    },
    onResult({ item }) {
      this.spinning = false
      this.result = item.raw
    },
    markDone() {
      if (!this.result) return
      store.updateMark(this.result.id, { status: '已去' })
      store.addDecision({
        type: 'choose',
        result: this.result.name,
        title: '转盘决定：' + this.result.name,
        markId: this.result.id,
        detail: '命运帮你选中了它',
        executed: true
      })
      uni.showToast({ title: '已打卡，干得漂亮！', icon: 'success' })
      this.stats = store.getStats()
      this.result = null
    },
    goMap() {
      uni.navigateTo({ url: '/pages/map/map' })
    }
  }
}
</script>

<style scoped>
.wheel-page {
  min-height: 100vh;
  padding: 24rpx 32rpx 60rpx;
  box-sizing: border-box;
}
.filter-bar {
  white-space: nowrap;
  width: 100%;
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
.filter-chip.on {
  background: #1F6E5F;
  color: #FFFFFF;
  font-weight: 600;
}
.wheel-area {
  padding: 70rpx 0 40rpx;
}
.wheel-hint {
  text-align: center;
  color: #A9B8B2;
  font-size: 26rpx;
  margin: -10rpx 0 30rpx;
}
.result-card {
  background: #FFFFFF;
  border-radius: 32rpx;
  padding: 32rpx;
  box-shadow: 0 16rpx 40rpx rgba(31,110,95,0.1);
  animation: pop 0.35s ease;
}
@keyframes pop {
  from { transform: scale(0.92); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.result-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
}
.result-tag {
  font-size: 22rpx;
  color: #2EC77E;
  background: #E8F9F0;
  padding: 4rpx 20rpx;
  border-radius: 999rpx;
}
.result-name {
  font-size: 44rpx;
  font-weight: 800;
  color: #1A3B34;
}
.result-meta {
  display: flex;
  gap: 12rpx;
  margin-top: 6rpx;
}
.chip {
  font-size: 24rpx;
  color: #4A5D57;
  background: #F2F6F4;
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
}
.cat-chip {
  color: #FF8A3D;
  background: #FFF3E8;
}
.result-note {
  font-size: 24rpx;
  color: #7A8A84;
  text-align: center;
  margin-top: 8rpx;
}
.result-actions {
  display: flex;
  gap: 20rpx;
  margin-top: 28rpx;
}
.act-btn {
  flex: 1;
  font-size: 28rpx;
}
.stats-bar {
  display: flex;
  justify-content: space-around;
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 28rpx 0;
  margin-top: 36rpx;
  box-shadow: 0 8rpx 30rpx rgba(31,110,95,0.05);
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
}
.stat-num {
  font-size: 40rpx;
  font-weight: 800;
  color: #1F6E5F;
}
.stat-label {
  font-size: 22rpx;
  color: #A9B8B2;
}
</style>
