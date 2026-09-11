<template>
  <view class="login">
    <view class="bg-orb orb-1"></view>
    <view class="bg-orb orb-2"></view>

    <view class="login-main" :style="{ paddingTop: ((nav.totalHeaderHeight || 88) + 16) + 'px' }">
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

    <!-- 微信资料授权弹窗（符合微信小程序官方最新头像昵称填写规范） -->
    <view v-if="showAuthModal" class="auth-mask" @click="showAuthModal = false">
      <view class="auth-card" @click.stop>
        <view class="auth-header">
          <text class="auth-title">微信资料授权</text>
          <text class="auth-desc">申请获取你的微信头像和昵称，用于主页展示与标记共享</text>
        </view>

        <view class="auth-form">
          <!-- 头像选择 -->
          <view class="auth-item">
            <text class="auth-item-label">头像</text>
            <button
              class="auth-avatar-btn"
              open-type="chooseAvatar"
              @chooseavatar="onChooseAvatar"
              @click="fallbackChooseAvatar"
            >
              <image v-if="authAvatar" :src="authAvatar" class="auth-avatar-img" mode="aspectFill" />
              <view v-else class="auth-avatar-default">
                <text class="auth-cam">📷</text>
                <text class="auth-cam-tip">点选头像</text>
              </view>
            </button>
          </view>

          <!-- 昵称输入 -->
          <view class="auth-item">
            <text class="auth-item-label">昵称</text>
            <input
              type="nickname"
              class="auth-nickname-input"
              placeholder="请输入或点击微信昵称"
              placeholder-class="ph"
              v-model="authNickname"
              @blur="onNicknameBlur"
            />
          </view>
        </view>

        <view class="auth-actions">
          <view class="auth-submit-btn" @click="confirmAuthLogin">授权并登录</view>
          <view class="auth-cancel-btn" @click="showAuthModal = false">暂不授权</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { api } from '../../api/index'
import { getNavMetrics } from '../../utils/helper'

export default {
  data() {
    return {
      logoConic: 'conic-gradient(#FF8A3D 0 90deg, #FFC94D 90deg 180deg, #2EC77E 180deg 270deg, #3FA7E0 270deg 360deg)',
      showAuthModal: false,
      authAvatar: '',
      authNickname: '',
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
  },
  onShow() {
    this.nav = getNavMetrics()
  },
  methods: {
    wechatLogin() {
      this.showAuthModal = true
    },
    onChooseAvatar(e) {
      if (e && e.detail && e.detail.avatarUrl) {
        this.authAvatar = e.detail.avatarUrl
      }
    },
    fallbackChooseAvatar() {
      // #ifndef MP-WEIXIN
      uni.chooseImage({
        count: 1,
        success: (res) => {
          if (res.tempFilePaths && res.tempFilePaths[0]) {
            this.authAvatar = res.tempFilePaths[0]
          }
        }
      })
      // #endif
    },
    onNicknameBlur(e) {
      if (e && e.detail && e.detail.value) {
        this.authNickname = e.detail.value.trim()
      }
    },
    async confirmAuthLogin() {
      const nickname = (this.authNickname || '').trim()
      if (!nickname) {
        uni.showToast({ title: '请输入或点选微信昵称', icon: 'none' })
        return
      }
      this.showAuthModal = false
      uni.showLoading({ title: '登录中…' })
      try {
        await api.login({
          nickname: nickname,
          avatar: this.authAvatar || ''
        })
        uni.hideLoading()
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => {
          uni.switchTab({ url: '/pages/index/index' })
        }, 500)
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '登录失败，请重试', icon: 'none' })
      }
    },
    async guestLogin() {
      uni.showLoading({ title: '进入中…' })
      try {
        await api.login({
          nickname: '游客同学',
          avatar: ''
        })
        uni.hideLoading()
        uni.switchTab({ url: '/pages/index/index' })
      } catch (e) {
        uni.hideLoading()
        uni.switchTab({ url: '/pages/index/index' })
      }
    }
  }
}
</script>

<style scoped>
.login {
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
.login-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: calc(env(safe-area-inset-top, 44px) + 80rpx) 56rpx 0;
  position: relative;
  z-index: 2;
  box-sizing: border-box;
}
.brand-logo {
  position: relative;
  width: 220rpx;
  height: 220rpx;
  margin-bottom: 44rpx;
}
.logo-wheel {
  width: 220rpx;
  height: 220rpx;
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
.login-title {
  font-size: 52rpx;
  font-weight: 900;
  color: #14352D;
  letter-spacing: 2rpx;
}
.login-sub {
  font-size: 28rpx;
  font-weight: 600;
  color: #2D6A5D;
  margin-top: 14rpx;
}
.login-tip {
  font-size: 22rpx;
  font-weight: 500;
  color: #7B938B;
  margin-top: 20rpx;
}

/* 底部操作区 */
.login-footer {
  padding: 30rpx 56rpx calc(60rpx + constant(safe-area-inset-bottom));
  padding: 30rpx 56rpx calc(60rpx + env(safe-area-inset-bottom));
  position: relative;
  z-index: 2;
}
.wechat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  background: linear-gradient(135deg, #07C160 0%, #05A050 100%);
  color: #FFFFFF;
  border-radius: 999rpx;
  font-size: 32rpx;
  font-weight: 800;
  height: 96rpx;
  box-shadow: 0 12rpx 36rpx rgba(7, 193, 96, 0.32);
  border: 1.5rpx solid rgba(255, 255, 255, 0.25);
  transition: transform 0.15s ease;
}
.wechat-btn:active {
  transform: scale(0.97);
}
.wx-logo {
  font-size: 36rpx;
}
.guest-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 86rpx;
  color: #1F6E5F;
  font-size: 28rpx;
  font-weight: 700;
  margin-top: 24rpx;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 999rpx;
  border: 1.5rpx solid rgba(31, 110, 95, 0.16);
  box-shadow: 0 4rpx 14rpx rgba(31, 110, 95, 0.05);
  transition: transform 0.15s ease;
}
.guest-btn:active {
  transform: scale(0.97);
}
.agreement {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  font-size: 22rpx;
  color: #7B938B;
  margin-top: 28rpx;
  line-height: 1.6;
}
.agreement .link {
  color: #1F6E5F;
  font-weight: 700;
}

/* 微信资料授权弹窗 */
.auth-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8rpx);
  z-index: 999;
  display: flex;
  align-items: flex-end;
}
.auth-card {
  width: 100%;
  background: #FFFFFF;
  border-radius: 40rpx 40rpx 0 0;
  padding: 48rpx 48rpx calc(48rpx + constant(safe-area-inset-bottom));
  padding: 48rpx 48rpx calc(48rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  box-shadow: 0 -10rpx 40rpx rgba(0, 0, 0, 0.12);
  border-top: 1.5rpx solid rgba(225, 237, 232, 0.9);
}
.auth-header {
  text-align: center;
  margin-bottom: 36rpx;
}
.auth-title {
  display: block;
  font-size: 38rpx;
  font-weight: 900;
  color: #14352D;
}
.auth-desc {
  display: block;
  font-size: 24rpx;
  font-weight: 500;
  color: #5C7C73;
  margin-top: 12rpx;
  line-height: 1.5;
}
.auth-form {
  background: #F4F8F6;
  border-radius: 28rpx;
  padding: 10rpx 32rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
}
.auth-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx 0;
  border-bottom: 2rpx solid #E5EFEA;
}
.auth-item:last-child {
  border-bottom: none;
}
.auth-item-label {
  font-size: 28rpx;
  font-weight: 700;
  color: #14352D;
}
.auth-avatar-btn {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  padding: 0;
  margin: 0;
  background: #E5EFEA;
  border: 4rpx solid #FFFFFF;
  line-height: normal;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 6rpx 18rpx rgba(31, 110, 95, 0.12);
}
.auth-avatar-btn::after {
  border: none;
}
.auth-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}
.auth-avatar-default {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.auth-cam {
  font-size: 34rpx;
}
.auth-cam-tip {
  font-size: 16rpx;
  font-weight: 600;
  color: #5C7C73;
  margin-top: 2rpx;
}
.auth-nickname-input {
  flex: 1;
  text-align: right;
  font-size: 28rpx;
  font-weight: 700;
  color: #14352D;
  height: 64rpx;
}
.ph {
  color: #7B938B;
  font-weight: 400;
}
.auth-actions {
  margin-top: 40rpx;
}
.auth-submit-btn {
  background: linear-gradient(135deg, #248875 0%, #1B6557 100%);
  color: #FFFFFF;
  font-size: 30rpx;
  font-weight: 800;
  border-radius: 999rpx;
  text-align: center;
  height: 90rpx;
  line-height: 90rpx;
  box-shadow: 0 10rpx 30rpx rgba(31, 110, 95, 0.28);
  transition: transform 0.15s ease;
}
.auth-submit-btn:active {
  transform: scale(0.97);
}
.auth-cancel-btn {
  text-align: center;
  font-size: 26rpx;
  font-weight: 600;
  color: #7B938B;
  margin-top: 24rpx;
  padding: 10rpx 0;
}
</style>
