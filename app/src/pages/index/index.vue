<template>
  <view class="home">
    <!-- 顶部极简品牌与问候区 -->
    <view class="hero" :style="{ paddingTop: (nav.statusBarHeight || 44) + 'px' }">
      <view
        class="hero-nav"
        :style="{
          height: (nav.navBarHeight || 44) + 'px',
          paddingRight: (nav.menuRight || 96) + 'px'
        }"
      >
        <view class="hero-brand">
          <view class="mini-wheel" :style="{ background: logoConic }"></view>
          <text class="hero-brand-name">摇去哪</text>
        </view>
      </view>

      <view class="hero-title">今天让命运决定什么？</view>
      <view class="hero-sub">选什么 · 去不去 · 买不买，交给转盘</view>
    </view>

    <!-- 三大核心决策入口（文字、图标、底座、外框像素级绝对等高对齐） -->
    <view class="entries">
      <!-- 1. 选什么 -->
      <view class="entry-card" @click="go('/pages/wheel/wheel')">
        <view class="entry-ico-base base-wheel">
          <view class="ico-wheel-disc">
            <view class="sector s1"></view>
            <view class="sector s2"></view>
            <view class="sector s3"></view>
            <view class="sector s4"></view>
            <view class="disc-pin"></view>
          </view>
        </view>
        <text class="entry-title">选什么</text>
        <text class="entry-desc">吃喝玩乐</text>
      </view>

      <!-- 2. 去不去 -->
      <view class="entry-card" @click="go('/pages/scale/scale')">
        <view class="entry-ico-base base-scale">
          <view class="ico-scale-disc">
            <view class="scale-pillar"></view>
            <view class="scale-beam"></view>
            <view class="scale-pan-l"></view>
            <view class="scale-pan-r"></view>
            <view class="scale-plate-l"></view>
            <view class="scale-plate-r"></view>
          </view>
        </view>
        <text class="entry-title">去不去</text>
        <text class="entry-desc">利弊权衡</text>
      </view>

      <!-- 3. 买不买 -->
      <view class="entry-card" @click="go('/pages/buy/buy')">
        <view class="entry-ico-base base-buy">
          <view class="ico-buy-disc">
            <text class="buy-symbol">¥</text>
          </view>
        </view>
        <text class="entry-title">买不买</text>
        <text class="entry-desc">理性剁手</text>
      </view>
    </view>

    <!-- 今日命运主卡（翡翠深翠微立体质感） -->
    <view class="fate-card" @click="go('/pages/wheel/wheel')">
      <view class="fate-left">
        <view class="fate-tag-box">
          <text class="fate-tag">今日命运</text>
        </view>
        <text class="fate-text">今天中午吃什么？</text>
        <text class="fate-hint">让转盘替你决定 →</text>
      </view>
      <view class="fate-wheel-wrap">
        <view class="fate-wheel-glow"></view>
        <view class="fate-wheel" :style="{ background: logoConic }">
          <view class="wheel-center-core"></view>
        </view>
      </view>
    </view>

    <!-- 快捷入口（图标大小、容器高度、文字基线 100% 严格对齐） -->
    <view class="quick-row">
      <view class="quick-item" @click="go('/pages/map/map')">
        <view class="quick-ico-box">
          <text class="quick-ico">📍</text>
        </view>
        <text class="quick-label">我的地图</text>
      </view>
      <view class="quick-item" @click="go('/pages/group/group')">
        <view class="quick-ico-box">
          <text class="quick-ico">🏠</text>
        </view>
        <text class="quick-label">小组地图</text>
      </view>
      <view class="quick-item" @click="go('/pages/alumni/alumni')">
        <view class="quick-ico-box">
          <text class="quick-ico">🎓</text>
        </view>
        <text class="quick-label">本校地图</text>
      </view>
      <view class="quick-item" @click="go('/pages/archive/archive')">
        <view class="quick-ico-box">
          <text class="quick-ico">📖</text>
        </view>
        <text class="quick-label">决策档案</text>
      </view>
    </view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { getNavMetrics } from '../../utils/helper'

export default {
  data() {
    return {
      logoConic: 'conic-gradient(#FF8A3D 0 90deg, #FFC94D 90deg 180deg, #2EC77E 180deg 270deg, #3FA7E0 270deg 360deg)',
      stats: { streak: 0 },
      nav: {
        statusBarHeight: 44,
        navBarHeight: 44,
        menuRight: 96
      }
    }
  },
  onLoad() {
    this.nav = getNavMetrics()
  },
  onShow() {
    store.init()
    this.stats = store.getStats()
    this.nav = getNavMetrics()
  },
  methods: {
    go(url) {
      const tabPages = ['/pages/index/index', '/pages/map/map', '/pages/mine/mine']
      if (tabPages.includes(url)) {
        uni.switchTab({ url })
      } else {
        uni.navigateTo({ url })
      }
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
  padding-bottom: 40rpx;
  box-sizing: border-box;
}

/* 顶部轻透区域 */
.hero {
  background: linear-gradient(150deg, #E2F5EE 0%, #D8EEF5 100%);
  padding-left: 36rpx;
  padding-right: 36rpx;
  padding-bottom: 56rpx;
  border-bottom-left-radius: 44rpx;
  border-bottom-right-radius: 44rpx;
  position: relative;
  overflow: hidden;
}
.hero::after {
  content: '';
  position: absolute;
  width: 280rpx;
  height: 280rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.45);
  top: -80rpx;
  right: -50rpx;
  pointer-events: none;
}
.hero-nav {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  position: relative;
  z-index: 2;
}
.hero-brand {
  display: flex;
  align-items: center;
  gap: 14rpx;
}
.mini-wheel {
  width: 50rpx;
  height: 50rpx;
  border-radius: 50%;
  border: 3rpx solid #FFFFFF;
  box-shadow: 0 4rpx 14rpx rgba(31, 110, 95, 0.22);
}
.hero-brand-name {
  font-size: 36rpx;
  font-weight: 900;
  color: #14483E;
  letter-spacing: 1.5rpx;
}
.hero-title {
  font-size: 42rpx;
  font-weight: 900;
  color: #11362E;
  margin-top: 28rpx;
  line-height: 1.25;
  letter-spacing: 0.5rpx;
  position: relative;
  z-index: 2;
}
.hero-sub {
  font-size: 24rpx;
  color: #4A7A6E;
  font-weight: 500;
  margin-top: 8rpx;
  position: relative;
  z-index: 2;
  white-space: nowrap;
}

/* ============================================================
   三大核心决策入口（严格工业级等高、等距、等宽、绝对像素对齐）
   ============================================================ */
.entries {
  display: flex;
  gap: 18rpx;
  padding: 0 32rpx;
  margin-top: -30rpx;
  position: relative;
  z-index: 3;
}
.entry-card {
  flex: 1;
  height: 246rpx;
  box-sizing: border-box;
  background: #FFFFFF;
  border-radius: 30rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  padding: 26rpx 12rpx 24rpx;
  box-shadow: 0 10rpx 26rpx rgba(27, 86, 73, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  transition: transform 0.15s, box-shadow 0.15s;
}
.entry-card:active {
  transform: translateY(2rpx) scale(0.97);
  box-shadow: 0 4rpx 12rpx rgba(27, 86, 73, 0.04);
}

/* 1. 统一图标底座（严格 96rpx × 96rpx，视觉几何光学中心分毫不差） */
.entry-ico-base {
  width: 96rpx;
  height: 96rpx;
  border-radius: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.base-wheel { background: #FFF5EB; box-shadow: 0 4rpx 12rpx rgba(255, 138, 61, 0.12); }
.base-scale { background: #EFF6FD; box-shadow: 0 4rpx 12rpx rgba(58, 150, 245, 0.12); }
.base-buy   { background: #EDF8F4; box-shadow: 0 4rpx 12rpx rgba(36, 136, 117, 0.12); }

/* 2. 选什么内部图形（56rpx × 56rpx 几何居中） */
.ico-wheel-disc {
  position: relative;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
  border: 2rpx solid #FFFFFF;
}
.sector {
  position: absolute;
  width: 28rpx;
  height: 28rpx;
}
.sector.s1 { background: #FF8A3D; top: 0; left: 0; border-radius: 100% 0 0 0; }
.sector.s2 { background: #FFC94D; top: 0; right: 0; border-radius: 0 100% 0 0; }
.sector.s3 { background: #28C78B; bottom: 0; right: 0; border-radius: 0 0 100% 0; }
.sector.s4 { background: #3FA7E0; bottom: 0; left: 0; border-radius: 0 0 0 100%; }
.disc-pin {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.2);
}

/* 3. 去不去内部图形（56rpx × 56rpx 绝对中心对称天平） */
.ico-scale-disc {
  position: relative;
  width: 56rpx;
  height: 56rpx;
}
.scale-pillar {
  position: absolute;
  left: 26rpx;
  top: 6rpx;
  width: 4rpx;
  height: 44rpx;
  background: #3A96F5;
  border-radius: 4rpx;
}
.scale-beam {
  position: absolute;
  left: 6rpx;
  top: 10rpx;
  width: 44rpx;
  height: 5rpx;
  background: #3A96F5;
  border-radius: 4rpx;
}
.scale-pan-l {
  position: absolute;
  left: 10rpx;
  top: 14rpx;
  width: 2rpx;
  height: 22rpx;
  background: #3A96F5;
}
.scale-plate-l {
  position: absolute;
  left: 4rpx;
  top: 36rpx;
  width: 14rpx;
  height: 4rpx;
  background: #3A96F5;
  border-radius: 2rpx;
}
.scale-pan-r {
  position: absolute;
  right: 10rpx;
  top: 14rpx;
  width: 2rpx;
  height: 22rpx;
  background: #3A96F5;
}
.scale-plate-r {
  position: absolute;
  right: 4rpx;
  top: 36rpx;
  width: 14rpx;
  height: 4rpx;
  background: #3A96F5;
  border-radius: 2rpx;
}

/* 4. 买不买内部图形（56rpx × 56rpx 绝对中心对称钱币徽章） */
.ico-buy-disc {
  width: 54rpx;
  height: 54rpx;
  border-radius: 50%;
  border: 3.5rpx solid #248875;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
}
.buy-symbol {
  font-size: 32rpx;
  font-weight: 900;
  color: #248875;
  line-height: 1;
}

/* 5. 统一标题栏（高度 42rpx，行高 42rpx，字体 32rpx，绝对水平对齐） */
.entry-title {
  height: 42rpx;
  line-height: 42rpx;
  font-size: 32rpx;
  font-weight: 800;
  color: #14352D;
  margin-top: 16rpx;
  text-align: center;
  letter-spacing: 0.5rpx;
}

/* 6. 统一副标栏（高度 32rpx，行高 32rpx，字体 22rpx，绝对水平对齐） */
.entry-desc {
  height: 32rpx;
  line-height: 32rpx;
  font-size: 22rpx;
  font-weight: 600;
  color: #5A7E74;
  margin-top: 6rpx;
  text-align: center;
  white-space: nowrap;
  letter-spacing: 0.5rpx;
}

/* ============================================================
   今日命运卡（翡翠深翠高光卡片）
   ============================================================ */
.fate-card {
  margin: 28rpx 32rpx 0;
  background: linear-gradient(135deg, #185B4E 0%, #227665 100%);
  border-radius: 34rpx;
  padding: 34rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 16rpx 40rpx rgba(24, 91, 78, 0.22);
  transition: transform 0.15s;
}
.fate-card:active {
  transform: scale(0.98);
}
.fate-left {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}
.fate-tag-box {
  display: flex;
}
.fate-tag {
  font-size: 20rpx;
  font-weight: 700;
  color: #B3EBDD;
  background: rgba(255, 255, 255, 0.18);
  border: 1rpx solid rgba(255, 255, 255, 0.24);
  padding: 4rpx 18rpx;
  border-radius: 999rpx;
  letter-spacing: 0.5rpx;
}
.fate-text {
  font-size: 34rpx;
  font-weight: 800;
  color: #FFFFFF;
  letter-spacing: 0.5rpx;
}
.fate-hint {
  font-size: 24rpx;
  font-weight: 500;
  color: #A3DFD1;
}
.fate-wheel-wrap {
  position: relative;
  width: 120rpx;
  height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.fate-wheel-glow {
  position: absolute;
  inset: -6rpx;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,0.22) 0%, transparent 70%);
}
.fate-wheel {
  width: 116rpx;
  height: 116rpx;
  border-radius: 50%;
  border: 6rpx solid rgba(255, 255, 255, 0.95);
  box-shadow: 0 10rpx 26rpx rgba(0, 0, 0, 0.22);
  box-sizing: border-box;
  position: relative;
}
.wheel-center-core {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 24rpx;
  height: 24rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.25);
}

/* ============================================================
   快捷入口（4项严格统一高度 148rpx，居中对齐）
   ============================================================ */
.quick-row {
  display: flex;
  gap: 16rpx;
  padding: 28rpx 32rpx 0;
}
.quick-item {
  flex: 1;
  height: 148rpx;
  box-sizing: border-box;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  padding: 22rpx 0 20rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  box-shadow: 0 8rpx 24rpx rgba(27, 86, 73, 0.04);
  transition: transform 0.15s;
}
.quick-item:active {
  transform: scale(0.96);
}
.quick-ico-box {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.quick-ico {
  font-size: 38rpx;
  line-height: 1;
}
.quick-label {
  height: 32rpx;
  line-height: 32rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #2B4E44;
  margin-top: 8rpx;
  text-align: center;
  white-space: nowrap;
}
</style>
