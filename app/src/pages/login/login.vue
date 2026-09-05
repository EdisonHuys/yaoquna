<template>
  <view class="login">
    <view class="bg-orb orb-1"></view>
    <view class="bg-orb orb-2"></view>

    <view class="login-main">
      <view class="brand-logo">
        <view class="logo-wheel" :style="{ background: logoConic }"></view>
        <view class="logo-pin"></view>
      </view>
      <view class="login-title">登录开始使用</view>
      <view class="login-sub">仅需微信授权，无需手机号</view>
      <view class="login-tip">个人开发者版本 · 登录即默认同意用户协议</view>
    </view>

    <view class="login-footer">
      <view class="wechat-btn" @click="wechatLogin">
        <text class="wx-logo">💬</text>
        <text>微信一键授权登录</text>
      </view>
      <view class="guest-btn" @click="guestLogin">暂不登录，先逛逛</view>
      <view class="agreement">
        <text>登录即代表同意</text>
        <text class="link">《用户协议》</text>
        <text>和</text>
        <text class="link">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '../../api/index'

export default {
  data() {
    return {
      logoConic: 'conic-gradient(#FF8A3D 0 90deg, #FFC94D 90deg 180deg, #2EC77E 180deg 270deg, #3FA7E0 270deg 360deg)'
    }
  },
  methods: {
    async wechatLogin() {
      uni.showLoading({ title: '登录中' })
      try {
        // uni-app 跨端登录：App 端 uni.login 返回 code（可换 openid），小程序端同理
        // 本阶段 mock：直接生成昵称登录；后续接入 uniCloud 后调用云函数换取 openid
        const profile = {
          nickname: '小命运',
          avatar: '',
          schoolId: '',
          schoolName: '',
          isPro: false,
          streak: 1
        }
        await api.login(profile)
        uni.hideLoading()
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          uni.reLaunch({ url: '/pages/index/index' })
        }, 500)
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '登录失败，请重试', icon: 'none' })
      }
    },
    guestLogin() {
      uni.showToast({ title: '游客模式：功能受限', icon: 'none' })
      setTimeout(() => {
        uni.reLaunch({ url: '/pages/index/index' })
      }, 600)
    }
  }
}
</script>

<style scoped>
.login {
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
.login-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 56rpx;
  position: relative;
  z-index: 2;
}
.brand-logo {
  position: relative;
  width: 220rpx;
  height: 220rpx;
  margin-bottom: 48rpx;
}
.logo-wheel {
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  border: 14rpx solid #FFFFFF;
  box-shadow: 0 24rpx 60rpx rgba(31, 110, 95, 0.22);
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
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.15);
}
.login-title {
  font-size: 48rpx;
  font-weight: 800;
  color: #1F6E5F;
}
.login-sub {
  font-size: 28rpx;
  color: #4A8A78;
  margin-top: 14rpx;
}
.login-tip {
  font-size: 22rpx;
  color: #A9C7BC;
  margin-top: 20rpx;
}
.login-footer {
  padding: 40rpx 56rpx 80rpx;
  position: relative;
  z-index: 2;
}
.wechat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14rpx;
  background: #FFFFFF;
  color: #1A3B34;
  border-radius: 999rpx;
  font-size: 32rpx;
  font-weight: 600;
  padding: 28rpx 0;
  box-shadow: 0 12rpx 36rpx rgba(31, 110, 95, 0.1);
}
.wx-logo {
  font-size: 36rpx;
}
.guest-btn {
  text-align: center;
  color: #4A8A78;
  font-size: 28rpx;
  margin-top: 30rpx;
  padding: 12rpx;
}
.agreement {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  font-size: 22rpx;
  color: #9AB8AE;
  margin-top: 26rpx;
  line-height: 1.6;
}
.agreement .link {
  color: #4A8A78;
}
</style>
