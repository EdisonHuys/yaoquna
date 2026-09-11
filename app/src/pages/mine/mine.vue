<template>
  <view class="mine-page" :style="{ paddingTop: (nav.statusBarHeight || 44) + 'px' }">
    <!-- 顶部状态栏与胶囊避让导航 -->
    <view
      class="mine-nav-bar"
      :style="{
        height: (nav.navBarHeight || 44) + 'px',
        paddingRight: (nav.menuRight || 96) + 'px'
      }"
    >
      <text class="mine-nav-title">个人中心</text>
    </view>

    <!-- 用户区 -->
    <view class="mine-head">
      <view class="user-row">
        <view class="user-avatar">
          <image v-if="store.user && store.user.avatar" :src="store.user.avatar" class="user-avatar-img" mode="aspectFill" />
          <text v-else>{{ (store.user && store.user.nickname) ? store.user.nickname.slice(0, 1) : '?' }}</text>
        </view>
        <view class="user-info">
          <text class="user-name">{{ store.user ? store.user.nickname : '未登录' }}</text>
          <text class="user-meta">{{ (store.user && store.user.schoolName) ? store.user.schoolName : '未选择学校' }} · 探索中</text>
        </view>
        <view class="pro-tag" v-if="store.user && store.user.isPro">PRO</view>
      </view>

      <!-- 3 大核心业务数据指标（清爽对称） -->
      <view class="mine-stats">
        <view class="m-stat" @click="go('/pages/map/map')">
          <text class="m-num">{{ stats.totalMark }}</text>
          <text class="m-label">我的标记</text>
        </view>
        <view class="m-stat" @click="go('/pages/archive/archive')">
          <text class="m-num">{{ stats.monthDecisions }}</text>
          <text class="m-label">决策记录</text>
        </view>
        <view class="m-stat" @click="go('/pages/group/group')">
          <text class="m-num">{{ store.groups.length }}</text>
          <text class="m-label">我的小组</text>
        </view>
      </view>

      <!-- 独立「每日打卡」互动条（支持一键签到打卡与连签动效） -->
      <view class="checkin-strip" @click="handleCheckIn">
        <view class="checkin-left">
          <view class="checkin-flame-box" :class="{ 'flame-active': isCheckedToday }">
            <text class="checkin-flame">🔥</text>
          </view>
          <view class="checkin-texts">
            <view class="checkin-title-row">
              <text class="checkin-title">连续探索</text>
              <text class="checkin-days">{{ stats.streak || 0 }}</text>
              <text class="checkin-unit">天</text>
            </view>
            <text class="checkin-hint">
              {{ isCheckedToday ? '今日探索打卡已完成，继续保持！' : '今日尚未打卡，点击打卡 +1天' }}
            </text>
          </view>
        </view>

        <view class="checkin-btn" :class="{ 'btn-checked': isCheckedToday }">
          <text class="checkin-btn-icon">{{ isCheckedToday ? '✓' : '✨' }}</text>
          <text class="checkin-btn-label">{{ isCheckedToday ? '已打卡' : '签到' }}</text>
        </view>
      </view>
    </view>

    <!-- Pro 会员 -->
    <view class="pro-card" @click="openPro">
      <view class="pro-top-row">
        <view class="pro-badge-group">
          <text class="pro-title">摇去哪 PRO</text>
          <text class="pro-tag-discount">年度特惠</text>
        </view>
        <view class="pro-price-group">
          <text class="pro-currency">¥</text>
          <text class="pro-amount">25</text>
          <text class="pro-cycle">/年</text>
        </view>
      </view>

      <view class="pro-bottom-row">
        <view class="pro-features">
          <view class="pf-item">
            <text class="pf-text">无限标记</text>
          </view>
          <view class="pf-item">
            <text class="pf-text">去广告</text>
          </view>
        </view>

        <view class="pro-action-btn">
          <text class="pro-action-text">立即开通</text>
          <text class="pro-action-arrow">→</text>
        </view>
      </view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-card">
      <view class="menu-item" @click="go('/pages/map/map')">
        <text class="menu-ico">📍</text>
        <text class="menu-name">我的标记</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/group/group')">
        <text class="menu-ico">🏠</text>
        <text class="menu-name">我的小组</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/archive/archive')">
        <text class="menu-ico">📖</text>
        <text class="menu-name">决策档案</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/alumni/alumni')">
        <text class="menu-ico">🎓</text>
        <text class="menu-name">本校地图</text>
        <text class="menu-tag" v-if="store.user && store.user.schoolName">{{ store.user.schoolName }}</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="editSchool">
        <text class="menu-ico">🏫</text>
        <text class="menu-name">我的学校</text>
        <text class="menu-tag">{{ store.user && store.user.schoolName ? store.user.schoolName : '未选择' }}</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="openPro">
        <text class="menu-ico">👑</text>
        <text class="menu-name">开通 PRO</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 退出 -->
    <view v-if="store.isLogin()" class="logout" @click="logout">退出登录</view>
    <view v-else class="logout" @click="goLogin">去登录</view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { api } from '../../api/index'
import { getNavMetrics } from '../../utils/helper'

export default {
  data() {
    return {
      store,
      stats: { totalMark: 0, monthDecisions: 0, streak: 0 },
      isCheckedToday: false,
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
    this.nav = getNavMetrics()
    this.stats = store.getStats()
    this.checkTodayStatus()
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
    goLogin() {
      uni.navigateTo({ url: '/pages/login/login' })
    },
    getTodayStr() {
      const now = new Date()
      const y = now.getFullYear()
      const m = String(now.getMonth() + 1).padStart(2, '0')
      const d = String(now.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    },
    checkTodayStatus() {
      const last = uni.getStorageSync('fw_last_checkin_date')
      this.isCheckedToday = (last === this.getTodayStr())
    },
    handleCheckIn() {
      if (this.isCheckedToday) {
        uni.showToast({
          title: '今天已经打过卡啦，明天再来探索吧～',
          icon: 'none'
        })
        return
      }
      const today = this.getTodayStr()
      uni.setStorageSync('fw_last_checkin_date', today)
      this.isCheckedToday = true

      // 更新连续签到天数
      const currentStreak = (this.stats && this.stats.streak) ? Number(this.stats.streak) : 0
      const newStreak = currentStreak + 1
      if (!store.user) {
        store.user = { id: 1, nickname: '探索者', streak: 0 }
      }
      store.user.streak = newStreak
      uni.setStorageSync('fw_user', store.user)
      this.stats.streak = newStreak

      try {
        uni.vibrateShort({ type: 'medium' })
      } catch (e) { /* ignore */ }

      uni.showToast({
        title: `打卡成功！已连续探索 ${newStreak} 天 🎉`,
        icon: 'none',
        duration: 2200
      })
    },
    openPro() {
      uni.showModal({
        title: '开通 PRO',
        content: '¥25/年：无限标记 · 去广告\n（App 端接入支付后开通）',
        confirmText: '知道了',
        showCancel: false
      })
    },
    async editSchool() {
      let list = []
      try {
        const res = await api.getSchools()
        list = (res.data || []).map(s => ({ schoolId: s.schoolId, schoolName: s.schoolName }))
      } catch (e) { /* ignore */ }

      if (!list || list.length === 0) {
        list = [
          { schoolId: 1, schoolName: '苏州大学' },
          { schoolId: 2, schoolName: '南京大学' },
          { schoolId: 3, schoolName: '东南大学' },
          { schoolId: 4, schoolName: '西交利物浦大学' },
          { schoolId: 5, schoolName: '中国人民大学(苏州)' }
        ]
      }

      uni.showActionSheet({
        itemList: list.map(s => s.schoolName),
        success: async res => {
          const item = list[res.tapIndex]
          try {
            await api.setSchool(item.schoolId, item.schoolName)
            uni.showToast({ title: '已选择 ' + item.schoolName, icon: 'none' })
          } catch (e) {
            uni.showToast({ title: '选择失败，请重试', icon: 'none' })
          }
        }
      })
    },
    logout() {
      store.logout()
      uni.showToast({ title: '已退出', icon: 'none' })
    }
  }
}
</script>

<style scoped>
.mine-page {
  min-height: 100vh;
  background: #F5F9F7;
  padding-top: calc(env(safe-area-inset-top, 44px) + 8px);
  padding-left: 32rpx;
  padding-right: 32rpx;
  padding-bottom: 48rpx;
  box-sizing: border-box;
}
.mine-nav-bar {
  min-height: 88rpx;
  display: flex;
  align-items: center;
  padding-left: 8rpx;
  box-sizing: border-box;
  margin-bottom: 24rpx;
}
.mine-nav-title {
  font-size: 42rpx;
  font-weight: 900;
  color: #11362E;
  letter-spacing: 1rpx;
}
.mine-head {
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  border-radius: 32rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 36rpx;
  box-shadow: 0 10rpx 32rpx rgba(27, 86, 73, 0.06);
}
.user-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
}
.user-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #34D0A8, #38B6E8);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 52rpx;
  font-weight: 800;
  border: 4rpx solid #FFFFFF;
  box-shadow: 0 8rpx 22rpx rgba(27, 86, 73, 0.2);
  overflow: hidden;
}
.user-avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}
.user-info { flex: 1; display: flex; flex-direction: column; }
.user-name { font-size: 40rpx; font-weight: 900; color: #14352D; }
.user-meta { font-size: 24rpx; color: #4A7A6E; font-weight: 500; margin-top: 6rpx; }
.pro-tag {
  background: linear-gradient(135deg, #FFC94D, #FF8A3D);
  color: #FFFFFF;
  font-size: 22rpx;
  font-weight: 800;
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  letter-spacing: 2rpx;
  box-shadow: 0 4rpx 14rpx rgba(255, 138, 61, 0.3);
}
.mine-stats {
  display: flex;
  background: rgba(255, 255, 255, 0.92);
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  border-radius: 24rpx;
  padding: 24rpx 0;
  margin-top: 32rpx;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.04);
}
.m-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  position: relative;
  transition: transform 0.15s;
}
.m-stat:active {
  transform: scale(0.96);
}
.m-stat:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 15%;
  height: 70%;
  width: 2rpx;
  background: #E8F0EC;
}
.m-num {
  font-size: 42rpx;
  font-weight: 900;
  color: #1F6E5F;
  font-variant-numeric: tabular-nums;
}
/* 独立每日打卡互动条 */
.checkin-strip {
  margin-top: 20rpx;
  background: rgba(255, 255, 255, 0.92);
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  border-radius: 24rpx;
  padding: 18rpx 24rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.04);
  transition: transform 0.15s, background 0.2s;
  cursor: pointer;
}
.checkin-strip:active {
  transform: scale(0.985);
}
.checkin-left {
  display: flex;
  align-items: center;
  gap: 16rpx;
  flex: 1;
  min-width: 0;
}
.checkin-flame-box {
  width: 66rpx;
  height: 66rpx;
  border-radius: 20rpx;
  background: #FFF4EC;
  border: 1.5rpx solid rgba(255, 125, 66, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all 0.2s;
}
.checkin-flame-box.flame-active {
  background: linear-gradient(135deg, #FFE8D6, #FFD1B3);
  box-shadow: 0 4rpx 14rpx rgba(255, 125, 66, 0.25);
  border-color: rgba(255, 125, 66, 0.4);
}
.checkin-flame {
  font-size: 34rpx;
  line-height: 1;
}
.checkin-texts {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
  min-width: 0;
}
.checkin-title-row {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
}
.checkin-title {
  font-size: 26rpx;
  font-weight: 800;
  color: #14352D;
}
.checkin-days {
  font-size: 32rpx;
  font-weight: 900;
  color: #FF7D42;
  font-variant-numeric: tabular-nums;
  margin-left: 2rpx;
}
.checkin-unit {
  font-size: 20rpx;
  font-weight: 600;
  color: #7B938B;
}
.checkin-hint {
  font-size: 20rpx;
  color: #7B938B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.checkin-btn {
  padding: 10rpx 26rpx;
  border-radius: 999rpx;
  background: linear-gradient(135deg, #FF9454, #FF7033);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  gap: 6rpx;
  box-shadow: 0 6rpx 16rpx rgba(255, 112, 51, 0.28);
  flex-shrink: 0;
  transition: all 0.2s;
}
.checkin-btn.btn-checked {
  background: #EEF8F3;
  color: #1F6E5F;
  border: 1.5rpx solid rgba(31, 110, 95, 0.2);
  box-shadow: none;
}
.checkin-btn-icon {
  font-size: 20rpx;
  font-weight: 800;
}
.checkin-btn-label {
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 0.5rpx;
}
.m-label { font-size: 22rpx; font-weight: 600; color: #7B938B; }

.pro-card {
  margin-top: 24rpx;
  background: linear-gradient(135deg, #144E43 0%, #1D6B5C 100%);
  border: 1.5rpx solid rgba(255, 225, 150, 0.3);
  border-radius: 30rpx;
  padding: 28rpx 28rpx 24rpx;
  box-shadow: 0 16rpx 42rpx rgba(20, 78, 67, 0.22);
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  transition: transform 0.15s;
}
.pro-card:active {
  transform: scale(0.98);
}
.pro-top-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.pro-badge-group {
  display: flex;
  align-items: center;
  gap: 12rpx;
}
.pro-crown {
  font-size: 32rpx;
}
.pro-title {
  font-size: 32rpx;
  font-weight: 900;
  color: #FFFFFF;
  letter-spacing: 1rpx;
}
.pro-tag-discount {
  font-size: 18rpx;
  font-weight: 700;
  color: #FFE6A3;
  background: rgba(255, 230, 163, 0.16);
  border: 1rpx solid rgba(255, 230, 163, 0.35);
  padding: 2rpx 14rpx;
  border-radius: 999rpx;
}
.pro-price-group {
  display: flex;
  align-items: baseline;
  gap: 4rpx;
}
.pro-currency {
  font-size: 24rpx;
  font-weight: 800;
  color: #FFD066;
}
.pro-amount {
  font-size: 42rpx;
  font-weight: 900;
  color: #FFD066;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.pro-cycle {
  font-size: 20rpx;
  font-weight: 600;
  color: #B3EBDD;
  margin-left: 2rpx;
}
.pro-bottom-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14rpx;
}
.pro-features {
  display: flex;
  align-items: center;
  gap: 10rpx;
  flex-wrap: wrap;
  flex: 1;
}
.pf-item {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  background: rgba(255, 255, 255, 0.12);
  border: 1rpx solid rgba(255, 255, 255, 0.18);
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
}
.pf-dot {
  font-size: 16rpx;
  color: #FFE6A3;
}
.pf-text {
  font-size: 20rpx;
  font-weight: 600;
  color: #D6F6EC;
}
.pro-action-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: linear-gradient(135deg, #FFE6A3 0%, #F5C765 100%);
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  box-shadow: 0 4rpx 16rpx rgba(245, 199, 101, 0.35);
  flex-shrink: 0;
}
.pro-action-text {
  font-size: 24rpx;
  font-weight: 800;
  color: #14483E;
}
.pro-action-arrow {
  font-size: 24rpx;
  font-weight: 900;
  color: #14483E;
}

.menu-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  margin-top: 24rpx;
  padding: 6rpx 0;
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.04);
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 32rpx;
  border-bottom: 1.5rpx solid #F2F7F5;
  transition: background 0.15s;
}
.menu-item:active {
  background: #F6FAF8;
}
.menu-ico {
  font-size: 38rpx;
  width: 44rpx;
  height: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.menu-name { flex: 1; font-size: 28rpx; font-weight: 600; color: #2D4A41; }
.menu-tag {
  font-size: 22rpx;
  font-weight: 600;
  color: #1F6E5F;
  background: #EAF5F0;
  padding: 4rpx 18rpx;
  border-radius: 999rpx;
}
.menu-arrow { font-size: 36rpx; color: #B5C7C0; }

.logout {
  text-align: center;
  color: #D84D4F;
  background: #FFFFFF;
  border: 1.5rpx solid #F6D6D6;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 700;
  margin-top: 40rpx;
  padding: 24rpx 0;
  box-shadow: 0 4rpx 16rpx rgba(216, 77, 79, 0.05);
  transition: transform 0.15s, background 0.15s;
}
.logout:active {
  transform: scale(0.98);
  background: #FFF5F5;
}
</style>
