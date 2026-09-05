<template>
  <view class="group-page">
    <!-- 小组卡 -->
    <view class="group-hero">
      <view class="group-avatar">
        <text>🏠</text>
      </view>
      <view class="group-info">
        <text class="group-name">{{ group.name }}</text>
        <text class="group-sub">{{ group.memberCount }} 人 · 已标记 {{ group.markCount }} 个地点</text>
      </view>
      <view class="group-invite" @click="invite">邀请</view>
    </view>

    <!-- 统计 -->
    <view class="g-stats">
      <view class="g-stat">
        <text class="g-num">{{ group.decisionCount }}</text>
        <text class="g-label">共同决策</text>
      </view>
      <view class="g-stat">
        <text class="g-num">{{ group.weekNew }}</text>
        <text class="g-label">本周新增</text>
      </view>
      <view class="g-stat">
        <text class="g-num">{{ group.weedOutRate }}%</text>
        <text class="g-label">拔草率</text>
      </view>
    </view>

    <!-- 成员 -->
    <view class="fw-sec-title">成员（{{ group.members.length }}）</view>
    <view class="member-row">
      <view v-for="m in group.members" :key="m.id" class="member">
        <view class="member-avatar" :style="{ background: avatarColor(m.id) }">
          <text>{{ m.nickname.slice(0, 1) }}</text>
        </view>
        <text class="member-name">{{ m.nickname }}</text>
        <text v-if="m.role === 'owner'" class="member-role">组长</text>
      </view>
      <view class="member add" @click="invite">
        <view class="member-avatar add-avatar">＋</view>
        <text class="member-name">邀请</text>
      </view>
    </view>

    <!-- 组内排行 -->
    <view class="fw-sec-title">本周组内活跃</view>
    <view class="rank-card">
      <view v-for="(r, i) in rank" :key="r.name" class="rank-row">
        <text class="rank-no" :class="'n' + (i + 1)">{{ i + 1 }}</text>
        <text class="rank-name">{{ r.name }}</text>
        <view class="rank-bar-wrap">
          <view class="rank-bar" :style="{ width: r.ratio + '%' }"></view>
        </view>
        <text class="rank-count">{{ r.count }} 标记</text>
      </view>
    </view>

    <!-- 共享标记 -->
    <view class="fw-sec-title">小组共享地图</view>
    <view class="g-mark-list">
      <view v-for="m in groupMarks" :key="m.id" class="g-mark-item">
        <view class="g-dot" :style="{ background: pinColor(m.category) }"></view>
        <view class="g-body">
          <text class="g-name">{{ m.name }}</text>
          <text class="g-addr">{{ m.address }}</text>
        </view>
        <text class="g-creator">{{ m.creator.nickname }} 标记</text>
      </view>
      <view v-if="groupMarks.length === 0" class="empty">小组里还没有共享标记</view>
    </view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { categoryColor } from '../../utils/helper'

export default {
  data() {
    return {
      group: null,
      rank: [],
      groupMarks: []
    }
  },
  onShow() {
    store.init()
    this.group = store.groups[0] || null
    if (this.group) {
      this.rank = [
        { name: '阿伟', count: 14 },
        { name: '小命运', count: 11 },
        { name: '小美', count: 9 },
        { name: '小胖', count: 7 },
        { name: '老猫', count: 7 }
      ].map((r, i, arr) => ({ ...r, ratio: Math.round((r.count / arr[0].count) * 100) }))
      const ids = this.group.members.map(m => m.id)
      this.groupMarks = store.marks.filter(m => m.shareScope === 'group' && m.groupIds.includes(this.group.id))
    }
  },
  methods: {
    pinColor(c) { return categoryColor(c) },
    avatarColor(id) {
      const colors = ['#FF8A3D', '#3FA7E0', '#2EC77E', '#8A6FE8', '#F06E8B']
      let h = 0
      for (const ch of id) h = (h * 31 + ch.charCodeAt(0)) % colors.length
      return colors[h]
    },
    invite() {
      uni.showModal({
        title: '邀请成员',
        content: '生成邀请码，通过微信分享给室友（≤5人）\n演示版仅展示界面',
        confirmText: '生成邀请',
        success: res => {
          if (res.confirm) uni.showToast({ title: '邀请码已生成（演示）', icon: 'none' })
        }
      })
    }
  }
}
</script>

<style scoped>
.group-page {
  min-height: 100vh;
  padding: 24rpx 32rpx 60rpx;
  box-sizing: border-box;
}
.group-hero {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  border-radius: 28rpx;
  padding: 34rpx;
}
.group-avatar {
  width: 96rpx;
  height: 96rpx;
  border-radius: 26rpx;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 52rpx;
  box-shadow: 0 8rpx 24rpx rgba(31,110,95,0.15);
}
.group-info { flex: 1; display: flex; flex-direction: column; }
.group-name { font-size: 34rpx; font-weight: 800; color: #1A3B34; }
.group-sub { font-size: 24rpx; color: #5E8F81; margin-top: 6rpx; }
.group-invite {
  background: #1F6E5F;
  color: #FFFFFF;
  font-size: 26rpx;
  padding: 14rpx 32rpx;
  border-radius: 999rpx;
  font-weight: 600;
}
.g-stats {
  display: flex;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 28rpx 0;
  margin-top: 24rpx;
  box-shadow: 0 6rpx 24rpx rgba(31,110,95,0.05);
}
.g-stat { flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4rpx; }
.g-num { font-size: 40rpx; font-weight: 800; color: #1F6E5F; }
.g-label { font-size: 22rpx; color: #A9B8B2; }
.member-row {
  display: flex;
  gap: 20rpx;
  overflow-x: auto;
  padding: 4rpx;
}
.member {
  flex: 0 0 110rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
}
.member-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 36rpx;
  font-weight: 700;
}
.member-name { font-size: 24rpx; color: #4A5D57; }
.member-role {
  font-size: 18rpx;
  color: #FF8A3D;
  background: #FFF3E8;
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
}
.add-avatar {
  background: #F2F6F4;
  color: #7A8A84;
  border: 2rpx dashed #C9D4D0;
}
.rank-card {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  box-shadow: 0 6rpx 24rpx rgba(31,110,95,0.05);
}
.rank-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 12rpx 0;
}
.rank-no {
  width: 40rpx;
  height: 40rpx;
  border-radius: 12rpx;
  background: #F2F6F4;
  color: #7A8A84;
  font-size: 24rpx;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rank-no.n1 { background: #FFF3E8; color: #FF8A3D; }
.rank-no.n2 { background: #EAF4FF; color: #3FA7E0; }
.rank-no.n3 { background: #FFF6E5; color: #E0A93D; }
.rank-name { font-size: 26rpx; color: #4A5D57; width: 120rpx; }
.rank-bar-wrap { flex: 1; height: 18rpx; background: #F2F6F4; border-radius: 999rpx; overflow: hidden; }
.rank-bar { height: 100%; background: linear-gradient(90deg, #34D0A8, #38B6E8); border-radius: 999rpx; }
.rank-count { font-size: 22rpx; color: #A9B8B2; }
.g-mark-list { display: flex; flex-direction: column; gap: 16rpx; }
.g-mark-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 22rpx;
  padding: 22rpx 26rpx;
  box-shadow: 0 6rpx 24rpx rgba(31,110,95,0.05);
}
.g-dot { flex: 0 0 14rpx; width: 14rpx; height: 60rpx; border-radius: 8rpx; }
.g-body { flex: 1; display: flex; flex-direction: column; gap: 4rpx; }
.g-name { font-size: 28rpx; font-weight: 600; color: #1A3B34; }
.g-addr { font-size: 22rpx; color: #A9B8B2; }
.g-creator { font-size: 20rpx; color: #C9D4D0; }
.empty { text-align: center; color: #A9B8B2; font-size: 26rpx; padding: 60rpx 0; }
</style>
