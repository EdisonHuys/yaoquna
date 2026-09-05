<template>
  <view class="home">
    <!-- 顶部渐变区 -->
    <view class="hero">
      <view class="hero-nav">
        <view class="hero-brand">
          <view class="mini-wheel" :style="{ background: logoConic }"></view>
          <text class="hero-brand-name">摇去哪</text>
        </view>
        <view class="hero-streak" @click="goArchive">
          <text class="streak-num">{{ stats.streak }}</text>
          <text class="streak-label">连签</text>
        </view>
      </view>

      <view class="hero-title">今天让命运决定什么？</view>
      <view class="hero-sub">选什么 · 去不去 · 买不买，交给转盘</view>
    </view>

    <!-- 三入口 -->
    <view class="entries">
      <view class="entry-card" @click="go('/pages/wheel/wheel')">
        <view class="entry-ico ico-wheel">
          <view class="ico-sector s1"></view>
          <view class="ico-sector s2"></view>
          <view class="ico-sector s3"></view>
          <view class="ico-sector s4"></view>
        </view>
        <view class="entry-title">选什么</view>
        <view class="entry-desc">吃的玩的买啥，转一下</view>
      </view>
      <view class="entry-card" @click="go('/pages/scale/scale')">
        <view class="entry-ico ico-scale">
          <view class="scale-beam"></view>
          <view class="scale-l l"></view>
          <view class="scale-l r"></view>
        </view>
        <view class="entry-title">去不去</view>
        <view class="entry-desc">利弊摆一摆，别纠结</view>
      </view>
      <view class="entry-card" @click="go('/pages/buy/buy')">
        <view class="entry-ico ico-buy">¥</view>
        <view class="entry-title">买不买</view>
        <view class="entry-desc">四维打分，理性剁手</view>
      </view>
    </view>

    <!-- 今日命运卡 -->
    <view class="fate-card" @click="go('/pages/wheel/wheel')">
      <view class="fate-left">
        <text class="fate-tag">今日命运</text>
        <text class="fate-text">今天中午吃什么？</text>
        <text class="fate-hint">让转盘替你决定 →</text>
      </view>
      <view class="fate-wheel" :style="{ background: logoConic }"></view>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-row">
      <view class="quick-item" @click="go('/pages/map/map')">
        <text class="quick-ico">📍</text>
        <text>我的地图</text>
      </view>
      <view class="quick-item" @click="go('/pages/group/group')">
        <text class="quick-ico">🏠</text>
        <text>小组地图</text>
      </view>
      <view class="quick-item" @click="go('/pages/alumni/alumni')">
        <text class="quick-ico">🎓</text>
        <text>本校地图</text>
      </view>
      <view class="quick-item" @click="go('/pages/archive/archive')">
        <text class="quick-ico">📖</text>
        <text>决策档案</text>
      </view>
    </view>

    <view class="fw-safe-bottom"></view>
    <fw-tabbar active="index"></fw-tabbar>
  </view>
</template>

<script>
import FwTabbar from '../../components/fw-tabbar.vue'
import { store } from '../../store/index'

export default {
  components: { FwTabbar },
  data() {
    return {
      logoConic: 'conic-gradient(#FF8A3D 0 90deg, #FFC94D 90deg 180deg, #2EC77E 180deg 270deg, #3FA7E0 270deg 360deg)',
      stats: { streak: 0 }
    }
  },
  onShow() {
    store.init()
    this.stats = store.getStats()
  },
  methods: {
    go(url) {
      uni.navigateTo({ url })
    },
    goArchive() {
      uni.navigateTo({ url: '/pages/archive/archive' })
    }
  }
}
</script>

<style scoped>
.home {
  min-height: 100vh;
  background: #F5F9F7;
  padding-bottom: 160rpx;
  box-sizing: border-box;
}
.hero {
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  padding: calc(88rpx + env(safe-area-inset-top)) 40rpx 70rpx;
  border-bottom-left-radius: 48rpx;
  border-bottom-right-radius: 48rpx;
  position: relative;
  overflow: hidden;
}
.hero::after {
  content: '';
  position: absolute;
  width: 300rpx;
  height: 300rpx;
  border-radius: 50%;
  background: rgba(255,255,255,0.35);
  top: -90rpx;
  right: -60rpx;
}
.hero-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 2;
}
.hero-brand {
  display: flex;
  align-items: center;
  gap: 14rpx;
}
.mini-wheel {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  border: 4rpx solid #FFFFFF;
  box-shadow: 0 6rpx 16rpx rgba(31,110,95,0.2);
  box-sizing: border-box;
}
.hero-brand-name {
  font-size: 36rpx;
  font-weight: 800;
  color: #1F6E5F;
  letter-spacing: 2rpx;
}
.hero-streak {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  background: rgba(255,255,255,0.85);
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
}
.streak-num {
  font-size: 32rpx;
  font-weight: 800;
  color: #FF8A3D;
}
.streak-label {
  font-size: 22rpx;
  color: #7A8A84;
}
.hero-title {
  font-size: 52rpx;
  font-weight: 800;
  color: #1A3B34;
  margin-top: 40rpx;
  position: relative;
  z-index: 2;
}
.hero-sub {
  font-size: 26rpx;
  color: #5E8F81;
  margin-top: 10rpx;
  position: relative;
  z-index: 2;
}
.entries {
  display: flex;
  gap: 20rpx;
  padding: 30rpx 32rpx 0;
  position: relative;
  z-index: 3;
  margin-top: -10rpx;
}
.entry-card {
  flex: 1;
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 28rpx 20rpx 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(31,110,95,0.06);
  display: flex;
  flex-direction: column;
  align-items: center;
}
.entry-ico {
  width: 88rpx;
  height: 88rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}
.ico-wheel {
  background: #FFF3E8;
  position: relative;
  border-radius: 50%;
  width: 76rpx;
  height: 76rpx;
  overflow: hidden;
}
.ico-sector {
  position: absolute;
  width: 38rpx;
  height: 38rpx;
}
.ico-sector.s1 { background: #FF8A3D; top: 0; left: 0; border-radius: 100% 0 0 0; }
.ico-sector.s2 { background: #FFC94D; top: 0; right: 0; border-radius: 0 100% 0 0; }
.ico-sector.s3 { background: #2EC77E; bottom: 0; right: 0; border-radius: 0 0 100% 0; }
.ico-sector.s4 { background: #3FA7E0; bottom: 0; left: 0; border-radius: 0 0 0 100%; }
.ico-scale {
  background: #EAF4FF;
  position: relative;
}
.scale-beam {
  position: absolute;
  width: 64rpx;
  height: 6rpx;
  background: #3FA7E0;
  border-radius: 6rpx;
}
.scale-l {
  position: absolute;
  width: 6rpx;
  height: 40rpx;
  background: #3FA7E0;
  border-radius: 6rpx;
  top: 34rpx;
}
.scale-l.l { left: 14rpx; }
.scale-l.r { right: 14rpx; }
.ico-buy {
  background: #E8F9F0;
  color: #2EC77E;
  font-size: 44rpx;
  font-weight: 800;
}
.entry-title {
  font-size: 30rpx;
  font-weight: 700;
  color: #1A3B34;
}
.entry-desc {
  font-size: 22rpx;
  color: #A9B8B2;
  margin-top: 6rpx;
  text-align: center;
}
.fate-card {
  margin: 30rpx 32rpx 0;
  background: linear-gradient(135deg, #1F6E5F, #2B8A76);
  border-radius: 32rpx;
  padding: 36rpx 36rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 16rpx 40rpx rgba(31,110,95,0.25);
}
.fate-left {
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.fate-tag {
  font-size: 22rpx;
  color: #B8E8D9;
  background: rgba(255,255,255,0.15);
  align-self: flex-start;
  padding: 4rpx 18rpx;
  border-radius: 999rpx;
}
.fate-text {
  font-size: 34rpx;
  font-weight: 700;
  color: #FFFFFF;
}
.fate-hint {
  font-size: 24rpx;
  color: #B8E8D9;
}
.fate-wheel {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 8rpx solid rgba(255,255,255,0.85);
  box-shadow: 0 10rpx 24rpx rgba(0,0,0,0.15);
  box-sizing: border-box;
}
.quick-row {
  display: flex;
  gap: 16rpx;
  padding: 30rpx 32rpx 0;
}
.quick-item {
  flex: 1;
  background: #FFFFFF;
  border-radius: 22rpx;
  padding: 24rpx 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  font-size: 24rpx;
  color: #4A5D57;
  box-shadow: 0 8rpx 24rpx rgba(31,110,95,0.05);
}
.quick-ico {
  font-size: 40rpx;
}
</style>
