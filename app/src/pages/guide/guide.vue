<template>
  <view class="guide">
    <!-- 背景装饰 -->
    <view class="bg-orb orb-1"></view>
    <view class="bg-orb orb-2"></view>

    <view class="guide-main">
      <!-- 品牌区 -->
      <view class="brand">
        <view class="brand-logo">
          <view class="logo-wheel" :style="{ background: logoConic }"></view>
          <view class="logo-pin"></view>
        </view>
        <view class="brand-name">命运转盘</view>
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
export default {
  data() {
    return {
      logoConic: 'conic-gradient(#FF8A3D 0 90deg, #FFC94D 90deg 180deg, #2EC77E 180deg 270deg, #3FA7E0 270deg 360deg)'
    }
  },
  onLoad() {
    // 首次标记引导已展示
    uni.setStorageSync('fw_guided', true)
  },
  methods: {
    start() {
      const token = uni.getStorageSync('fw_token')
      if (token) {
        uni.reLaunch({ url: '/pages/index/index' })
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
  background: linear-gradient(160deg, #DFF7F0 0%, #D5EEF7 100%);
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-sizing: border-box;
}
.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(60rpx);
  opacity: 0.5;
}
.orb-1 {
  width: 420rpx;
  height: 420rpx;
  background: #7FE8CD;
  top: -120rpx;
  right: -100rpx;
}
.orb-2 {
  width: 360rpx;
  height: 360rpx;
  background: #8FD2F5;
  bottom: -80rpx;
  left: -120rpx;
}
.guide-main {
  flex: 1;
  padding: 140rpx 56rpx 0;
  position: relative;
  z-index: 2;
}
.brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 70rpx;
}
.brand-logo {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 24rpx;
}
.logo-wheel {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  border: 12rpx solid #FFFFFF;
  box-shadow: 0 20rpx 50rpx rgba(31, 110, 95, 0.22);
  box-sizing: border-box;
}
.logo-pin {
  position: absolute;
  top: -20rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 26rpx;
  height: 26rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.15);
}
.brand-name {
  font-size: 56rpx;
  font-weight: 800;
  color: #1F6E5F;
  letter-spacing: 6rpx;
}
.brand-sub {
  font-size: 24rpx;
  color: #7FB8A8;
  letter-spacing: 8rpx;
  margin-top: 6rpx;
}
.steps {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}
.step {
  display: flex;
  align-items: flex-start;
  gap: 24rpx;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 28rpx;
  padding: 30rpx 28rpx;
  box-shadow: 0 8rpx 30rpx rgba(31, 110, 95, 0.05);
}
.step-num {
  flex: 0 0 56rpx;
  width: 56rpx;
  height: 56rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #34D0A8, #38B6E8);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.step-body {
  display: flex;
  flex-direction: column;
}
.step-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1A3B34;
}
.step-desc {
  font-size: 24rpx;
  color: #7A8A84;
  margin-top: 6rpx;
}
.guide-footer {
  padding: 40rpx 56rpx 80rpx;
  position: relative;
  z-index: 2;
}
.start-btn {
  background: linear-gradient(135deg, #34D0A8, #38B6E8);
  color: #FFFFFF;
  font-size: 34rpx;
}
.dots {
  display: flex;
  justify-content: center;
  gap: 12rpx;
  margin-top: 28rpx;
}
.dot {
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #C5E4DA;
}
.dot.active {
  width: 40rpx;
  border-radius: 999rpx;
  background: #1F6E5F;
}
</style>
