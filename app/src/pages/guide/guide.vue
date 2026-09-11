<template>
  <view class="guide">
    <!-- 背景装饰 -->
    <view class="bg-orb orb-1"></view>
    <view class="bg-orb orb-2"></view>

    <view class="guide-main" :style="{ paddingTop: ((nav.totalHeaderHeight || 88) + 16) + 'px' }">
      <!-- 品牌区 -->
      <view class="brand">
        <view class="brand-logo">
          <view class="logo-wheel" :style="{ background: logoConic }"></view>
          <view class="logo-pin"></view>
        </view>
        <view class="brand-name">摇去哪</view>
        <view class="brand-sub">FATE WHEEL</view>
      </view>

      <!-- 三步说明 -->
      <view class="steps">
        <view class="step">
          <view class="step-num">1</view>
          <view class="step-body">
            <text class="step-title">标记想去的地方</text>
            <text class="step-desc">吃的、玩的、买的，把心愿钉在地图上</text>
          </view>
        </view>
        <view class="step">
          <view class="step-num">2</view>
          <view class="step-body">
            <text class="step-title">让转盘替你决定</text>
            <text class="step-desc">选择困难？命运帮你拍板</text>
          </view>
        </view>
        <view class="step">
          <view class="step-num">3</view>
          <view class="step-body">
            <text class="step-title">出发打卡记录</text>
            <text class="step-desc">每一次决定，都成为你的成长档案</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部 -->
    <view class="guide-footer">
      <view class="fw-btn start-btn" @click="start">开始体验</view>
      <view class="dots">
        <view class="dot active"></view>
        <view class="dot"></view>
        <view class="dot"></view>
      </view>
    </view>
  </view>
</template>

<script>
import { getNavMetrics } from '../../utils/helper'

export default {
  data() {
    return {
      logoConic: 'conic-gradient(#FF8A3D 0 90deg, #FFC94D 90deg 180deg, #2EC77E 180deg 270deg, #3FA7E0 270deg 360deg)',
      nav: {
        statusBarHeight: 44,
        navBarHeight: 44,
        totalHeaderHeight: 88,
        menuRight: 96
      }
    }
  },
  onLoad() {
    this.nav = getNavMetrics()
    const token = uni.getStorageSync('fw_token')
    const guided = uni.getStorageSync('fw_guided')
    if (token) {
      uni.switchTab({ url: '/pages/index/index' })
      return
    } else if (guided) {
      uni.reLaunch({ url: '/pages/login/login' })
      return
    }
    // 标记首次引导
    uni.setStorageSync('fw_guided', true)
  },
  methods: {
    start() {
      const token = uni.getStorageSync('fw_token')
      if (token) {
        uni.switchTab({ url: '/pages/index/index' })
      } else {
        uni.reLaunch({ url: '/pages/login/login' })
      }
    }
  }
}
</script>

<style scoped>
.guide {
  min-height: 100vh;
  background: linear-gradient(165deg, #E6F5F1 0%, #F5FBF8 40%, #E8F6F1 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80rpx);
  opacity: 0.6;
}
.orb-1 {
  width: 500rpx;
  height: 500rpx;
  background: #A3E8D5;
  top: -140rpx;
  right: -120rpx;
}
.orb-2 {
  width: 440rpx;
  height: 440rpx;
  background: #B2E5F6;
  bottom: -100rpx;
  left: -140rpx;
}
.guide-main {
  flex: 1;
  padding: calc(env(safe-area-inset-top, 44px) + 80rpx) 48rpx 0;
  position: relative;
  z-index: 2;
  box-sizing: border-box;
}

/* 品牌区域 */
.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 60rpx;
}
.brand-logo {
  position: relative;
  width: 210rpx;
  height: 210rpx;
  margin-bottom: 28rpx;
}
.logo-wheel {
  width: 210rpx;
  height: 210rpx;
  border-radius: 50%;
  border: 14rpx solid #FFFFFF;
  box-shadow: 0 20rpx 56rpx rgba(31, 110, 95, 0.2), inset 0 2rpx 8rpx rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
}
.logo-pin {
  position: absolute;
  top: -22rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 28rpx;
  height: 28rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 4rpx 14rpx rgba(0, 0, 0, 0.2);
}
.brand-name {
  font-size: 60rpx;
  font-weight: 900;
  color: #14352D;
  letter-spacing: 8rpx;
  line-height: 1.2;
}
.brand-sub {
  font-size: 20rpx;
  font-weight: 800;
  color: #1F6E5F;
  letter-spacing: 6rpx;
  margin-top: 10rpx;
  background: rgba(31, 110, 95, 0.09);
  padding: 4rpx 20rpx;
  border-radius: 999rpx;
  border: 1rpx solid rgba(31, 110, 95, 0.15);
}

/* 步骤说明 */
.steps {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.step {
  display: flex;
  align-items: center;
  gap: 26rpx;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20rpx);
  border-radius: 32rpx;
  padding: 32rpx 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  box-shadow: 0 10rpx 32rpx rgba(31, 110, 95, 0.06);
  transition: transform 0.15s ease;
}
.step:active {
  transform: scale(0.98);
}
.step-num {
  flex: 0 0 64rpx;
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #248875 0%, #1B6557 100%);
  color: #FFFFFF;
  font-size: 32rpx;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 18rpx rgba(31, 110, 95, 0.28);
}
.step-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.step-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #14352D;
}
.step-desc {
  font-size: 25rpx;
  font-weight: 500;
  color: #4A7A6E;
  margin-top: 8rpx;
  line-height: 1.4;
}

/* 底部操作区 */
.guide-footer {
  padding: 30rpx 56rpx calc(60rpx + constant(safe-area-inset-bottom));
  padding: 30rpx 56rpx calc(60rpx + env(safe-area-inset-bottom));
  position: relative;
  z-index: 2;
}
.start-btn {
  background: linear-gradient(135deg, #248875 0%, #1B6557 100%);
  color: #FFFFFF;
  font-size: 34rpx;
  font-weight: 800;
  height: 96rpx;
  line-height: 96rpx;
  border-radius: 999rpx;
  text-align: center;
  box-shadow: 0 12rpx 36rpx rgba(31, 110, 95, 0.3);
  border: 1.5rpx solid rgba(255, 255, 255, 0.3);
  transition: transform 0.15s ease;
}
.start-btn:active {
  transform: scale(0.97);
}
.dots {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 14rpx;
  margin-top: 32rpx;
}
.dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #C2DFD6;
  transition: all 0.3s ease;
}
.dot.active {
  width: 44rpx;
  border-radius: 999rpx;
  background: #1F6E5F;
  box-shadow: 0 2rpx 8rpx rgba(31, 110, 95, 0.3);
}
</style>
