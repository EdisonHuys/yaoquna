<template>
  <view class="wheel-page">
    <!-- 分类筛选 -->
    <scroll-view scroll-x class="filter-bar">
      <view
        v-for="c in categories"
        :key="c"
        class="filter-chip"
        :class="{ on: category === c }"
        @click="switchCat(c)"
      >{{ c }}</view>
    </scroll-view>

    <!-- 转盘展示区 -->
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
      当前分类暂无地点，先去「地图」或点击右下角标记一些地点吧
    </view>

    <!-- 1. 抽选中悬念卡片（旋转过程中展示） -->
    <view v-if="spinning" class="spinning-card">
      <view class="spinning-dice">🎲</view>
      <text class="spinning-title">命运之轮飞速运转中…</text>
      <text class="spinning-sub">正在为您精挑细选最佳去处，请稍候</text>
    </view>

    <!-- 2. 结果卡片（仅在转盘完全停止后展现！） -->
    <view v-else-if="result" class="result-card">
      <view class="result-top">
        <view class="result-badge-row">
          <text class="result-tag">🎉 命运的选择</text>
        </view>
        <text class="result-name">{{ result.name }}</text>
        <view class="result-meta">
          <text class="chip cat-chip">{{ result.category }}</text>
          <text class="chip rate-chip">⭐ {{ result.rating }}分</text>
          <text class="chip price-chip" v-if="result.price">¥{{ result.price }}/人</text>
        </view>
        <text class="result-addr" v-if="result.address">📍 {{ result.address }}</text>
        <text class="result-note" v-if="result.remark || result.note">💡 {{ result.remark || result.note }}</text>
      </view>
      <view class="result-actions">
        <view class="act-btn ghost-btn" @click="markDone">标记已去</view>
        <view class="act-btn primary-btn" @click="goNavigate">🧭 出发导航</view>
      </view>
    </view>

    <!-- 3. 未开始抽选时的就绪引导卡片 -->
    <view v-else-if="pool.length > 0" class="ready-hint-card">
      <text class="ready-icon">👆</text>
      <text class="ready-title">点击转盘中心「GO」开启命运抉择</text>
      <text class="ready-sub">从「{{ category }}」分类中随机抽选 1 个好去处</text>
    </view>

    <!-- 底部统计面板 -->
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
        <text class="stat-label">当前候选</text>
      </view>
    </view>
  </view>
</template>

<script>
import FwWheel from '../../components/fw-wheel.vue'
import { store } from '../../store/index'
import { api } from '../../api/index'
import { getWheelPool } from '../../utils/helper'

export default {
  components: { FwWheel },
  data() {
    return {
      categories: ['全部', '美食', '玩乐', '购物', '自习', '其他'],
      category: '全部',
      pool: [],
      spinning: false,
      result: null,
      stats: { totalMark: 0, went: 0 }
    }
  },
  onLoad(options) {
    if (options && options.category && this.categories.includes(options.category)) {
      this.category = options.category
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
        name: m.name.length > 5 ? m.name.slice(0, 5) : m.name,
        color: '',
        category: m.category,
        raw: m
      }))
      this.result = null
      this.spinning = false
      if (this.$refs.wheel) this.$refs.wheel.reset()
    },

    switchCat(c) {
      if (this.spinning) return
      this.category = c
      this.reloadPool()
    },

    // 点击中心按钮触发旋转
    doSpin() {
      this.spinning = true
      this.result = null
    },

    // 接收动画完全结束后的结果事件（转盘完全停止后才触发！）
    onResult({ item }) {
      this.spinning = false
      this.result = item.raw
    },

    // 标记已去打卡
    async markDone() {
      if (!this.result) return
      const target = this.result
      try {
        uni.showLoading({ title: '记录中…' })
        // 1. 标记地点为已去
        await api.markVisited(target.id)
        // 2. 写入决策记录
        await api.createDecision({
          type: 'select',
          result: target.name,
          title: '转盘决定：' + target.name,
          detail: '命运为你抽中了：' + target.name + (target.address ? '（' + target.address + '）' : ''),
          executed: true
        })
        store.updateMark(target.id, { status: '已去' })
        uni.hideLoading()
        uni.showToast({ title: '打卡成功！干得漂亮', icon: 'success' })
        this.stats = store.getStats()
        this.result = null
      } catch (e) {
        uni.hideLoading()
        store.updateMark(target.id, { status: '已去' })
        store.addDecision({
          type: 'select',
          result: target.name,
          title: '转盘决定：' + target.name,
          detail: '命运帮您选中了它',
          executed: true
        })
        uni.showToast({ title: '已打卡记录', icon: 'none' })
        this.stats = store.getStats()
        this.result = null
      }
    },

    // 出发打卡与原生地图导航
    goNavigate() {
      if (!this.result) return
      const lat = Number(this.result.lat)
      const lng = Number(this.result.lng)
      if (lat && lng) {
        uni.openLocation({
          latitude: lat,
          longitude: lng,
          name: this.result.name,
          address: this.result.address || this.result.name,
          scale: 16
        })
      } else {
        uni.switchTab({ url: '/pages/map/map' })
      }
    }
  }
}
</script>

<style scoped>
.wheel-page {
  min-height: 100vh;
  padding: 24rpx 32rpx calc(60rpx + constant(safe-area-inset-bottom));
  padding: 24rpx 32rpx calc(60rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  background: #F5F9F7;
}

/* 分类胶囊栏 */
.filter-bar {
  white-space: nowrap;
  width: 100%;
}
.filter-chip {
  display: inline-flex;
  padding: 12rpx 34rpx;
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

/* 转盘区域 */
.wheel-area {
  padding: 30rpx 0 24rpx;
}
.wheel-hint {
  text-align: center;
  color: #8EA49D;
  font-size: 26rpx;
  margin: -10rpx 0 30rpx;
}

/* 1. 抽选中悬念卡片 */
.spinning-card {
  background: #FFFFFF;
  border-radius: 32rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 38rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 16rpx 40rpx rgba(27, 86, 73, 0.08);
  animation: fadeIn 0.3s ease;
}
.spinning-dice {
  font-size: 56rpx;
  animation: rotateDice 1.2s infinite linear;
  margin-bottom: 12rpx;
}
@keyframes rotateDice {
  from { transform: rotate(0deg) scale(1); }
  50% { transform: rotate(180deg) scale(1.15); }
  to { transform: rotate(360deg) scale(1); }
}
.spinning-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #1F6E5F;
}
.spinning-sub {
  font-size: 24rpx;
  color: #7B938B;
  margin-top: 8rpx;
}

/* 2. 就绪提示卡片 */
.ready-hint-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 28rpx 32rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.04);
}
.ready-icon { font-size: 40rpx; margin-bottom: 6rpx; }
.ready-title { font-size: 28rpx; font-weight: 800; color: #14352D; }
.ready-sub { font-size: 22rpx; color: #8EA49D; margin-top: 6rpx; }

/* 3. 结果卡片（动画弹出） */
.result-card {
  background: #FFFFFF;
  border-radius: 32rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  padding: 36rpx 32rpx;
  box-shadow: 0 18rpx 48rpx rgba(27, 86, 73, 0.12);
  animation: popUp 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
@keyframes popUp {
  from { transform: translateY(30rpx) scale(0.92); opacity: 0; }
  to { transform: translateY(0) scale(1); opacity: 1; }
}
.result-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
}
.result-tag {
  font-size: 24rpx;
  font-weight: 800;
  color: #1F6E5F;
  background: #EAF5F0;
  padding: 6rpx 24rpx;
  border-radius: 999rpx;
  letter-spacing: 0.5rpx;
}
.result-name {
  font-size: 42rpx;
  font-weight: 900;
  color: #14352D;
  text-align: center;
  line-height: 1.3;
}
.result-meta {
  display: flex;
  gap: 14rpx;
  margin-top: 6rpx;
}
.chip {
  font-size: 24rpx;
  color: #4A5D57;
  background: #F2F6F4;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
  font-weight: 600;
}
.cat-chip { color: #FF7D42; background: #FFF3EB; font-weight: 700; }
.rate-chip { color: #D48806; background: #FFF9E6; }
.price-chip { color: #1F6E5F; background: #EAF5F0; font-weight: 700; }
.result-addr { font-size: 24rpx; color: #7B938B; text-align: center; }
.result-note { font-size: 24rpx; color: #446158; text-align: center; line-height: 1.5; }

.result-actions { display: flex; gap: 20rpx; margin-top: 30rpx; width: 100%; }
.act-btn {
  flex: 1;
  height: 88rpx;
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
  transition: transform 0.15s, opacity 0.15s;
}
.act-btn:active {
  transform: scale(0.98);
  opacity: 0.92;
}
.ghost-btn {
  background: #FFFFFF;
  color: #38554D;
  border: 2rpx solid #D2E3DC;
}
.primary-btn {
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  box-shadow: 0 8rpx 24rpx rgba(27, 86, 73, 0.28);
}

/* 底部统计栏 */
.stats-bar {
  display: flex;
  justify-content: space-around;
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  padding: 26rpx 0;
  margin-top: 36rpx;
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.04);
}
.stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  position: relative;
}
.stat:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 15%;
  height: 70%;
  width: 2rpx;
  background: #E8F0EC;
}
.stat-num {
  font-size: 42rpx;
  font-weight: 900;
  color: #1F6E5F;
  font-variant-numeric: tabular-nums;
}
.stat-label {
  font-size: 22rpx;
  font-weight: 600;
  color: #7B938B;
}
</style>

