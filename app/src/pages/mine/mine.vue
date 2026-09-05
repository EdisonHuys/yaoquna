<template>
  <view class="mine-page">
    <!-- 用户区 -->
    <view class="mine-head">
      <view class="user-row">
        <view class="user-avatar">
          <text>{{ (store.user && store.user.nickname) ? store.user.nickname.slice(0, 1) : '?' }}</text>
        </view>
        <view class="user-info">
          <text class="user-name">{{ store.user ? store.user.nickname : '未登录' }}</text>
          <text class="user-meta">已连续决策 {{ stats.streak }} 天</text>
        </view>
        <view class="pro-tag" v-if="store.user && store.user.isPro">PRO</view>
      </view>

      <view class="mine-stats">
        <view class="m-stat" @click="go('/pages/map/map')">
          <text class="m-num">{{ stats.totalMark }}</text>
          <text class="m-label">我的标记</text>
        </view>
        <view class="m-stat" @click="go('/pages/archive/archive')">
          <text class="m-num">{{ stats.monthDecisions }}</text>
          <text class="m-label">决策次数</text>
        </view>
        <view class="m-stat" @click="go('/pages/group/group')">
          <text class="m-num">{{ store.groups.length }}</text>
          <text class="m-label">我的小组</text>
        </view>
      </view>
    </view>

    <!-- Pro 会员 -->
    <view class="pro-card" @click="openPro">
      <view class="pro-left">
        <text class="pro-title">命运转盘 PRO</text>
        <view class="pro-features">
          <text class="pf">无限标记</text>
          <text class="pf">全部皮肤</text>
          <text class="pf">去广告</text>
          <text class="pf">高级分析</text>
        </view>
      </view>
      <view class="pro-right">
        <text class="pro-price">¥25</text>
        <text class="pro-unit">/年</text>
        <view class="pro-buy">立即开通</view>
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

    <view class="fw-safe-bottom"></view>
    <fw-tabbar active="mine"></fw-tabbar>
  </view>
</template>

<script>
import FwTabbar from '../../components/fw-tabbar.vue'
import { store } from '../../store/index'
import { api } from '../../api/index'

export default {
  components: { FwTabbar },
  data() {
    return {
      store,
      stats: { totalMark: 0, monthDecisions: 0, streak: 0 }
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
    goLogin() {
      uni.navigateTo({ url: '/pages/login/login' })
    },
    openPro() {
      uni.showModal({
        title: '开通 PRO',
        content: '¥25/年：无限标记 · 全部皮肤 · 去广告 · 高级分析\n（App 端接入支付后开通）',
        confirmText: '知道了',
        showCancel: false
      })
    },
    async editSchool() {
      const schools = ['苏州大学', '南京大学', '浙江大学', '复旦大学', '上海交通大学']
      uni.showActionSheet({
        itemList: schools,
        success: async res => {
          const name = schools[res.tapIndex]
          await api.setSchool('school_' + res.tapIndex, name)
          uni.showToast({ title: '已选择 ' + name, icon: 'none' })
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
  padding: 24rpx 32rpx 160rpx;
  box-sizing: border-box;
}
.mine-head {
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  border-radius: 32rpx;
  padding: calc(40rpx + env(safe-area-inset-top)) 36rpx 36rpx;
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
  box-shadow: 0 10rpx 26rpx rgba(31,110,95,0.25);
}
.user-info { flex: 1; display: flex; flex-direction: column; }
.user-name { font-size: 38rpx; font-weight: 800; color: #1A3B34; }
.user-meta { font-size: 24rpx; color: #5E8F81; margin-top: 6rpx; }
.pro-tag {
  background: linear-gradient(135deg, #FFC94D, #FF8A3D);
  color: #FFFFFF;
  font-size: 22rpx;
  font-weight: 800;
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  letter-spacing: 2rpx;
}
.mine-stats {
  display: flex;
  background: rgba(255,255,255,0.85);
  border-radius: 24rpx;
  padding: 26rpx 0;
  margin-top: 34rpx;
}
.m-stat { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4rpx; }
.m-num { font-size: 40rpx; font-weight: 800; color: #1F6E5F; }
.m-label { font-size: 22rpx; color: #7A8A84; }

.pro-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #1F6E5F, #2B8A76);
  border-radius: 28rpx;
  padding: 30rpx 32rpx;
  margin-top: 28rpx;
  box-shadow: 0 16rpx 40rpx rgba(31,110,95,0.25);
}
.pro-left { display: flex; flex-direction: column; gap: 14rpx; }
.pro-title { font-size: 32rpx; font-weight: 800; color: #FFFFFF; }
.pro-features { display: flex; gap: 10rpx; flex-wrap: wrap; }
.pf {
  font-size: 20rpx;
  color: #B8E8D9;
  background: rgba(255,255,255,0.15);
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
}
.pro-right { display: flex; flex-direction: column; align-items: center; }
.pro-price { font-size: 44rpx; font-weight: 800; color: #FFC94D; line-height: 1; }
.pro-unit { font-size: 20rpx; color: #B8E8D9; }
.pro-buy {
  margin-top: 12rpx;
  background: #FFFFFF;
  color: #1F6E5F;
  font-size: 24rpx;
  font-weight: 700;
  padding: 10rpx 30rpx;
  border-radius: 999rpx;
}

.menu-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  margin-top: 28rpx;
  padding: 8rpx 0;
  box-shadow: 0 8rpx 30rpx rgba(31,110,95,0.05);
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 28rpx 30rpx;
  border-bottom: 2rpx solid #F4F8F6;
}
.menu-item:last-child { border-bottom: none; }
.menu-ico { font-size: 38rpx; }
.menu-name { flex: 1; font-size: 28rpx; color: #4A5D57; }
.menu-tag {
  font-size: 22rpx;
  color: #A9B8B2;
  background: #F2F6F4;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
}
.menu-arrow { font-size: 36rpx; color: #C9D4D0; }

.logout {
  text-align: center;
  color: #EA6668;
  font-size: 28rpx;
  margin-top: 40rpx;
  padding: 24rpx;
}
</style>
