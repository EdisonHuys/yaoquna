<template>
  <view class="alumni-page">
    <!-- 头部 -->
    <view class="alumni-hero">
      <view class="school-row">
        <text class="school-badge">🎓</text>
        <view class="school-info">
          <text class="school-name">{{ schoolName }}</text>
          <text class="school-sub">本校宝藏地图 · {{ alumniMarks.length }} 个共享地点</text>
        </view>
        <view class="school-edit" @click="editSchool">{{ schoolName ? '切换' : '选择' }}</view>
      </view>
      <view class="alumni-banner">
        <text class="banner-t1">学长学姐私藏的校园地图</text>
        <text class="banner-t2">吃 · 玩 · 买 · 自习，同校人帮你踩好点了</text>
      </view>
    </view>

    <view v-if="!schoolName" class="no-school">
      <text class="ns-emoji">🗺️</text>
      <text class="ns-title">还没选择学校</text>
      <text class="ns-desc">选择学校后，就能看到同校同学共享的宝藏地点，也能把标记共享给本校同学</text>
      <view class="fw-btn ns-btn" @click="editSchool">选择我的学校</view>
    </view>

    <template v-else>
      <!-- 筛选 -->
      <scroll-view scroll-x class="filter-bar">
        <view
          v-for="c in filters"
          :key="c"
          class="filter-chip"
          :class="{ on: filter === c }"
          @click="filter = c"
        >{{ c }}</view>
      </scroll-view>

      <!-- 地点列表 -->
      <view class="a-list">
        <view v-for="m in filtered" :key="m.id" class="a-item">
          <view class="a-top">
            <view class="a-dot" :style="{ background: pinColor(m.category) }"></view>
            <view class="a-name-box">
              <text class="a-name">{{ m.name }}</text>
              <text class="a-meta">⭐{{ m.rating }} · ¥{{ m.price }} · {{ m.address }}</text>
            </view>
            <text class="a-fav">❤️ {{ m.favoriteCount }}</text>
          </view>
          <view class="a-tags">
            <text class="tag" v-for="t in m.tags" :key="t">{{ t }}</text>
          </view>
          <text class="a-note" v-if="m.note">{{ m.note }}</text>
          <view class="a-footer">
            <text class="a-creator">由 {{ m.creator.nickname }} 共享</text>
            <view class="a-actions">
              <text class="a-act" @click="collect(m)">收藏</text>
              <text class="a-act" @click="useWheel(m)">转盘</text>
              <text class="a-act" @click="report(m)">举报</text>
            </view>
          </view>
        </view>
        <view v-if="filtered.length === 0" class="empty">本校还没有这个分类的地点，做第一个分享的人吧</view>
      </view>

      <view class="share-cta" @click="shareToSchool">
        <text class="cta-emoji">📍</text>
        <text class="cta-text">把标记共享到本校，帮学弟学妹少踩坑</text>
        <text class="cta-btn">去共享</text>
      </view>
    </template>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { api } from '../../api/index'
import { categoryColor, timeAgo } from '../../utils/helper'

export default {
  data() {
    return {
      filters: ['全部', '美食', '玩乐', '购物', '自习'],
      filter: '全部',
      alumniMarks: []
    }
  },
  computed: {
    schoolName() {
      return store.user ? store.user.schoolName : ''
    },
    filtered() {
      let list = this.alumniMarks
      if (this.filter !== '全部') list = list.filter(m => m.category === this.filter)
      return list
    }
  },
  onShow() {
    store.init()
    this.loadAlumni()
  },
  methods: {
    pinColor(c) { return categoryColor(c) },
    timeAgo,
    async loadAlumni() {
      const res = await api.getAlumniMarks(store.user ? store.user.schoolId : 'school_suda')
      this.alumniMarks = res.data
    },
    editSchool() {
      const schools = ['苏州大学', '南京大学', '浙江大学', '复旦大学', '上海交通大学']
      uni.showActionSheet({
        itemList: schools,
        success: async res => {
          const name = schools[res.tapIndex]
          await api.setSchool('school_' + res.tapIndex, name)
          uni.showToast({ title: '已选择 ' + name, icon: 'none' })
          this.loadAlumni()
        }
      })
    },
    collect(m) {
      // 收藏 = 复制一份到个人库
      store.addMark({
        name: m.name,
        category: m.category,
        tags: [...m.tags],
        rating: m.rating,
        price: m.price,
        note: m.note,
        address: m.address,
        lat: m.lat,
        lng: m.lng,
        shareScope: 'private',
        groupIds: []
      })
      uni.showToast({ title: '已收藏到我的标记', icon: 'success' })
    },
    useWheel(m) {
      uni.navigateTo({ url: '/pages/wheel/wheel' })
    },
    report(m) {
      uni.showModal({
        title: '举报地点',
        content: '举报「' + m.name + '」？\n（违规/虚假信息/侵权）',
        confirmText: '举报',
        success: res => {
          if (res.confirm) uni.showToast({ title: '已提交，感谢反馈', icon: 'none' })
        }
      })
    },
    shareToSchool() {
      uni.navigateTo({ url: '/pages/mark/mark' })
    }
  }
}
</script>

<style scoped>
.alumni-page {
  min-height: 100vh;
  padding-bottom: 60rpx;
  box-sizing: border-box;
}
.alumni-hero {
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  padding: 24rpx 32rpx 40rpx;
}
.school-row {
  display: flex;
  align-items: center;
  gap: 18rpx;
}
.school-badge { font-size: 44rpx; }
.school-info { flex: 1; display: flex; flex-direction: column; }
.school-name { font-size: 32rpx; font-weight: 800; color: #1A3B34; }
.school-sub { font-size: 22rpx; color: #5E8F81; margin-top: 4rpx; }
.school-edit {
  font-size: 24rpx;
  color: #1F6E5F;
  background: rgba(255,255,255,0.85);
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
}
.alumni-banner {
  margin-top: 26rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.banner-t1 { font-size: 30rpx; font-weight: 700; color: #1F6E5F; }
.banner-t2 { font-size: 22rpx; color: #7A8A84; }
.no-school {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 60rpx;
  text-align: center;
}
.ns-emoji { font-size: 90rpx; }
.ns-title { font-size: 36rpx; font-weight: 800; color: #1A3B34; margin-top: 24rpx; }
.ns-desc { font-size: 26rpx; color: #7A8A84; margin-top: 16rpx; line-height: 1.7; }
.ns-btn { margin-top: 40rpx; width: 320rpx; }
.filter-bar {
  white-space: nowrap;
  padding: 24rpx 32rpx 0;
  width: 100%;
  box-sizing: border-box;
}
.filter-chip {
  display: inline-flex;
  padding: 12rpx 30rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  background: #FFFFFF;
  color: #7A8A84;
  margin-right: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.filter-chip.on { background: #1F6E5F; color: #FFFFFF; font-weight: 600; }
.a-list { padding: 24rpx 32rpx 0; }
.a-item {
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 26rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 6rpx 24rpx rgba(31,110,95,0.05);
}
.a-top { display: flex; align-items: flex-start; gap: 18rpx; }
.a-dot {
  flex: 0 0 16rpx;
  width: 16rpx;
  height: 64rpx;
  border-radius: 8rpx;
  margin-top: 4rpx;
}
.a-name-box { flex: 1; display: flex; flex-direction: column; }
.a-name { font-size: 30rpx; font-weight: 700; color: #1A3B34; }
.a-meta { font-size: 22rpx; color: #A9B8B2; margin-top: 6rpx; }
.a-fav { font-size: 22rpx; color: #EA6668; }
.a-tags { display: flex; gap: 10rpx; flex-wrap: wrap; margin-top: 16rpx; }
.tag {
  font-size: 20rpx;
  color: #4A5D57;
  background: #F2F6F4;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
}
.a-note { font-size: 24rpx; color: #7A8A84; margin-top: 14rpx; line-height: 1.6; }
.a-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 18rpx;
  padding-top: 16rpx;
  border-top: 2rpx solid #F2F6F4;
}
.a-creator { font-size: 22rpx; color: #C9D4D0; }
.a-actions { display: flex; gap: 24rpx; }
.a-act { font-size: 24rpx; color: #1F6E5F; font-weight: 600; }
.empty { text-align: center; color: #A9B8B2; font-size: 26rpx; padding: 60rpx 0; }
.share-cta {
  margin: 10rpx 32rpx 40rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: linear-gradient(135deg, #34D0A8, #38B6E8);
  border-radius: 24rpx;
  padding: 24rpx 28rpx;
}
.cta-emoji { font-size: 44rpx; }
.cta-text { flex: 1; font-size: 24rpx; color: #FFFFFF; }
.cta-btn {
  font-size: 24rpx;
  color: #1F6E5F;
  background: #FFFFFF;
  padding: 12rpx 26rpx;
  border-radius: 999rpx;
  font-weight: 700;
}
</style>
