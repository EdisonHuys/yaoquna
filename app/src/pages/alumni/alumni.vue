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
      const sid = store.user && store.user.schoolId ? store.user.schoolId : null
      try {
        const res = await api.getAlumniMarks(sid)
        this.alumniMarks = res.data || []
      } catch (e) {
        this.alumniMarks = []
      }
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
            this.loadAlumni()
          } catch (e) {
            uni.showToast({ title: '选择失败，请重试', icon: 'none' })
          }
        }
      })
    },
    async collect(m) {
      try {
        uni.showLoading({ title: '处理中…' })
        await api.toggleFavorite(m.id)
        uni.hideLoading()
        uni.showToast({ title: m.favorited ? '已取消收藏' : '收藏成功！', icon: 'success' })
      } catch (e) {
        uni.hideLoading()
        // 复制一份到个人标记库
        store.addMark({
          name: m.name,
          category: m.category,
          tags: [...(m.tags || [])],
          rating: m.rating,
          price: m.price,
          note: m.note || m.remark,
          address: m.address,
          lat: m.lat,
          lng: m.lng,
          shareScope: 'private',
          groupIds: []
        })
        uni.showToast({ title: '已转存至我的标记', icon: 'success' })
      }
    },
    useWheel(m) {
      uni.navigateTo({ url: '/pages/wheel/wheel' })
    },
    report(m) {
      uni.showModal({
        title: '举报地点',
        content: '确定举报「' + m.name + '」？\n（违规/虚假信息/侵权）',
        confirmText: '确认举报',
        success: async res => {
          if (res.confirm) {
            try {
              await api.submitReport(m.id, '违规/虚假信息')
              uni.showToast({ title: '举报已提交，核实中', icon: 'success' })
            } catch (e) {
              uni.showToast({ title: '已提交，感谢反馈', icon: 'none' })
            }
          }
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
  background: #F4F8F6;
  padding-bottom: calc(60rpx + constant(safe-area-inset-bottom));
  padding-bottom: calc(60rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

/* 头部 Hero 区域 */
.alumni-hero {
  background: linear-gradient(150deg, #E6F5F0 0%, #D8EFE8 60%, #E2F4EE 100%);
  padding: 24rpx 32rpx 36rpx;
  border-bottom: 1.5rpx solid rgba(225, 237, 232, 0.8);
}
.school-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
}
.school-badge {
  font-size: 52rpx;
  filter: drop-shadow(0 4rpx 10rpx rgba(31, 110, 95, 0.15));
}
.school-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.school-name {
  font-size: 38rpx;
  font-weight: 900;
  color: #14352D;
  letter-spacing: 1rpx;
}
.school-sub {
  font-size: 24rpx;
  font-weight: 600;
  color: #2D6A5D;
  margin-top: 6rpx;
}
.school-edit {
  font-size: 24rpx;
  font-weight: 800;
  color: #1F6E5F;
  background: #FFFFFF;
  padding: 12rpx 28rpx;
  border-radius: 999rpx;
  border: 1.5rpx solid rgba(31, 110, 95, 0.18);
  box-shadow: 0 4rpx 14rpx rgba(31, 110, 95, 0.08);
  transition: transform 0.15s ease;
}
.school-edit:active {
  transform: scale(0.94);
}

/* 宣传横幅卡片 */
.alumni-banner {
  margin-top: 28rpx;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 28rpx;
  padding: 24rpx 28rpx;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  box-shadow: 0 8rpx 24rpx rgba(31, 110, 95, 0.06);
}
.banner-t1 {
  font-size: 30rpx;
  font-weight: 800;
  color: #14352D;
}
.banner-t2 {
  font-size: 24rpx;
  color: #5C7C73;
  font-weight: 500;
}

/* 空状态：未选学校 */
.no-school {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin: 60rpx 32rpx;
  padding: 80rpx 48rpx;
  text-align: center;
  background: #FFFFFF;
  border-radius: 36rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  box-shadow: 0 10rpx 32rpx rgba(31, 110, 95, 0.06);
}
.ns-emoji {
  font-size: 100rpx;
  margin-bottom: 16rpx;
  filter: drop-shadow(0 6rpx 16rpx rgba(31, 110, 95, 0.12));
}
.ns-title {
  font-size: 38rpx;
  font-weight: 900;
  color: #14352D;
  margin-top: 16rpx;
}
.ns-desc {
  font-size: 26rpx;
  color: #5C7C73;
  margin-top: 16rpx;
  line-height: 1.7;
}
.ns-btn {
  margin-top: 44rpx;
  width: 360rpx;
  height: 90rpx;
  line-height: 90rpx;
  font-size: 30rpx;
  font-weight: 800;
  border-radius: 999rpx;
}

/* 分类筛选横滑条 */
.filter-bar {
  white-space: nowrap;
  padding: 28rpx 32rpx 0;
  width: 100%;
  box-sizing: border-box;
}
.filter-chip {
  display: inline-flex;
  padding: 14rpx 34rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 700;
  background: #FFFFFF;
  color: #5C7C73;
  margin-right: 18rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  box-shadow: 0 4rpx 14rpx rgba(31, 110, 95, 0.04);
  transition: all 0.2s ease;
}
.filter-chip.on {
  background: linear-gradient(135deg, #248875 0%, #1B6557 100%);
  color: #FFFFFF;
  border-color: #1F6E5F;
  box-shadow: 0 6rpx 18rpx rgba(31, 110, 95, 0.25);
}
.filter-chip:active {
  transform: scale(0.95);
}

/* 地点列表 */
.a-list {
  padding: 24rpx 32rpx 0;
}
.a-item {
  background: #FFFFFF;
  border-radius: 32rpx;
  padding: 30rpx 28rpx;
  margin-bottom: 22rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  box-shadow: 0 8rpx 28rpx rgba(31, 110, 95, 0.06);
  transition: transform 0.15s ease;
}
.a-item:active {
  transform: scale(0.99);
}
.a-top {
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
}
.a-dot {
  flex: 0 0 14rpx;
  width: 14rpx;
  height: 60rpx;
  border-radius: 8rpx;
  margin-top: 6rpx;
}
.a-name-box {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.a-name {
  font-size: 32rpx;
  font-weight: 800;
  color: #14352D;
  line-height: 1.4;
}
.a-meta {
  font-size: 24rpx;
  color: #4A7A6E;
  font-weight: 600;
  margin-top: 8rpx;
}
.a-fav {
  font-size: 22rpx;
  font-weight: 700;
  color: #E24A4A;
  background: rgba(226, 74, 74, 0.08);
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
  border: 1.5rpx solid rgba(226, 74, 74, 0.18);
}
.a-tags {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
  margin-top: 18rpx;
}
.tag {
  font-size: 22rpx;
  font-weight: 600;
  color: #2D6A5D;
  background: #EAF4F0;
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
  border: 1rpx solid rgba(31, 110, 95, 0.12);
}
.a-note {
  font-size: 25rpx;
  color: #3D5950;
  margin-top: 16rpx;
  line-height: 1.65;
  background: #F8FAF9;
  border-radius: 18rpx;
  padding: 16rpx 20rpx;
  border-left: 6rpx solid #1F6E5F;
}
.a-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20rpx;
  padding-top: 18rpx;
  border-top: 1.5rpx solid #F0F4F2;
}
.a-creator {
  font-size: 22rpx;
  color: #7B938B;
  font-weight: 500;
}
.a-actions {
  display: flex;
  gap: 16rpx;
}
.a-act {
  font-size: 24rpx;
  font-weight: 800;
  color: #1F6E5F;
  background: rgba(31, 110, 95, 0.08);
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  transition: transform 0.15s ease;
}
.a-act:active {
  transform: scale(0.92);
}
.empty {
  text-align: center;
  color: #7B938B;
  font-size: 26rpx;
  font-weight: 600;
  padding: 80rpx 0;
}

/* 底部分享 CTA 浮动卡片 */
.share-cta {
  margin: 24rpx 32rpx 40rpx;
  display: flex;
  align-items: center;
  gap: 18rpx;
  background: linear-gradient(135deg, #248875 0%, #17584C 100%);
  border-radius: 32rpx;
  padding: 28rpx 32rpx;
  box-shadow: 0 12rpx 36rpx rgba(31, 110, 95, 0.28);
  border: 1.5rpx solid rgba(255, 255, 255, 0.2);
  transition: transform 0.15s ease;
}
.share-cta:active {
  transform: scale(0.98);
}
.cta-emoji {
  font-size: 48rpx;
}
.cta-text {
  flex: 1;
  font-size: 26rpx;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.4;
}
.cta-btn {
  font-size: 26rpx;
  color: #1F6E5F;
  background: #FFFFFF;
  padding: 14rpx 32rpx;
  border-radius: 999rpx;
  font-weight: 800;
  box-shadow: 0 4rpx 14rpx rgba(0, 0, 0, 0.12);
}
</style>
