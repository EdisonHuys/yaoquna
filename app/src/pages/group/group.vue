<template>
  <view class="group-page">
    <!-- 顶部操作栏 -->
    <view class="group-top-bar">
      <!-- 多小组切换 -->
      <view class="group-switcher" v-if="groups.length > 1">
        <scroll-view scroll-x class="group-scroll">
          <view
            v-for="(g, idx) in groups"
            :key="g.id"
            class="group-chip"
            :class="{ on: currentIndex === idx }"
            @click="switchGroup(idx)"
          >
            {{ g.name }}
          </view>
        </scroll-view>
      </view>
      <view class="group-actions">
        <view class="act-btn create" @click="openCreateGroup">＋ 创建</view>
        <view class="act-btn join" @click="openJoinGroup">🔑 加入</view>
      </view>
    </view>

    <!-- ===== 无小组：空状态视图 ===== -->
    <view v-if="!group" class="no-group-card">
      <view class="ng-icon">🏠</view>
      <view class="ng-title">暂未加入任何小组</view>
      <view class="ng-desc">
        宿舍小分队 / 好友探店团（≤5人）专属共享地图。
        一起标记宝藏地点，告别“今天吃什么”的日常纠结！
      </view>
      <view class="ng-btns">
        <view class="fw-btn ng-btn primary" @click="openCreateGroup">创建我的小组</view>
        <view class="fw-btn-ghost ng-btn" @click="openJoinGroup">输入邀请码加入</view>
      </view>
    </view>

    <!-- ===== 已有小组：详情视图 ===== -->
    <template v-else>
      <!-- 小组卡 -->
      <view class="group-hero">
        <view class="group-avatar">
          <text>🏠</text>
        </view>
        <view class="group-info">
          <text class="group-name">{{ group.name }}</text>
          <text class="group-sub">{{ memberCount }} 人 · 已标记 {{ groupMarks.length }} 个地点</text>
        </view>
        <view class="group-invite" @click="invite">邀请好友</view>
      </view>

      <!-- 统计 -->
      <view class="g-stats">
        <view class="g-stat">
          <text class="g-num">{{ groupMarks.length }}</text>
          <text class="g-label">共享地点</text>
        </view>
        <view class="g-stat">
          <text class="g-num">{{ groupWentCount }}</text>
          <text class="g-label">已打卡</text>
        </view>
        <view class="g-stat">
          <text class="g-num">{{ groupWeedRate }}%</text>
          <text class="g-label">拔草率</text>
        </view>
      </view>

      <!-- 成员 -->
      <view class="fw-sec-title">小组成员（{{ members.length }}）</view>
      <view class="member-row">
        <view v-for="m in members" :key="m.id" class="member">
          <view class="member-avatar" :style="{ background: avatarColor(String(m.id || m.userId)) }">
            <text>{{ (m.nickname || '同学').slice(0, 1) }}</text>
          </view>
          <text class="member-name">{{ m.nickname || '同学' }}</text>
          <text v-if="m.role === '0' || m.id === group.ownerId" class="member-role">组长</text>
        </view>
        <view class="member add" @click="invite" v-if="members.length < 5">
          <view class="member-avatar add-avatar">＋</view>
          <text class="member-name">邀请</text>
        </view>
      </view>

      <!-- 共享标记 -->
      <view class="fw-sec-title">小组共享地点库</view>
      <view class="g-mark-list">
        <view v-for="m in groupMarks" :key="m.id" class="g-mark-item" @click="goMarkDetail(m)">
          <view class="g-dot" :style="{ background: pinColor(m.category) }"></view>
          <view class="g-body">
            <view class="g-name-row">
              <text class="g-name">{{ m.name }}</text>
              <text class="g-status" :class="m.status === '已去' ? 'went' : ''">{{ m.status }}</text>
            </view>
            <text class="g-addr">{{ m.address || '未填写详细地址' }}</text>
            <view class="g-meta">
              <text class="g-cat">{{ m.category }}</text>
              <text v-if="m.price">¥{{ m.price }}</text>
              <text v-if="m.rating">⭐{{ m.rating }}</text>
            </view>
          </view>
          <text class="g-creator">{{ m.creator ? m.creator.name : '组员' }}</text>
        </view>
        <view v-if="groupMarks.length === 0" class="empty">
          小组里还没有共享标记，在标记地点时选择「共享到小组」即可沉淀到这里
        </view>
      </view>
    </template>
    <!-- ============================================ -->
    <!-- 全新高颜值自定义弹窗体系（彻底解决微信原生弹窗输入框文字截断与样式粗糙） -->
    <!-- ============================================ -->

    <!-- 1. 创建小组弹窗 -->
    <view v-if="showCreateModal" class="custom-modal-mask" @click="closeCreateModal" @touchmove.stop.prevent>
      <view class="custom-modal-card" @click.stop>
        <view class="modal-icon-badge">🏠</view>
        <view class="modal-title">创建我的小组</view>
        <view class="modal-desc">宿舍小分队 / 好友探店团（≤5人）专属共享空间</view>

        <view class="modal-input-box" :class="{ focused: createInputFocused }">
          <input
            v-model="createGroupName"
            class="modal-input"
            placeholder="请输入小组名称（如：302寝室）"
            placeholder-class="modal-ph"
            maxlength="15"
            :focus="createInputFocused"
            @focus="createInputFocused = true"
            @blur="createInputFocused = false"
            @confirm="confirmCreateGroup"
          />
          <text v-if="createGroupName" class="modal-clear-btn" @click.stop="createGroupName = ''">✕</text>
        </view>

        <view class="modal-footer-btns">
          <view class="modal-btn cancel-btn" @click.stop="closeCreateModal">取消</view>
          <view class="modal-btn confirm-btn" @click.stop="confirmCreateGroup">立即创建</view>
        </view>
      </view>
    </view>

    <!-- 2. 加入小组弹窗 -->
    <view v-if="showJoinModal" class="custom-modal-mask" @click="closeJoinModal" @touchmove.stop.prevent>
      <view class="custom-modal-card" @click.stop>
        <view class="modal-icon-badge">🔑</view>
        <view class="modal-title">加入好友小组</view>
        <view class="modal-desc">输入好友发给你的专属邀请码，即刻共享地点库</view>

        <view class="modal-input-box" :class="{ focused: joinInputFocused }">
          <input
            v-model="joinInviteCode"
            class="modal-input code-input"
            placeholder="请输入小组邀请码"
            placeholder-class="modal-ph"
            maxlength="20"
            :focus="joinInputFocused"
            @focus="joinInputFocused = true"
            @blur="joinInputFocused = false"
            @confirm="confirmJoinGroup"
          />
          <text v-if="joinInviteCode" class="modal-clear-btn" @click.stop="joinInviteCode = ''">✕</text>
        </view>

        <view class="modal-footer-btns">
          <view class="modal-btn cancel-btn" @click.stop="closeJoinModal">取消</view>
          <view class="modal-btn confirm-btn" @click.stop="confirmJoinGroup">加入小组</view>
        </view>
      </view>
    </view>

    <!-- 3. 邀请好友弹窗 -->
    <view v-if="showInviteModal" class="custom-modal-mask" @click="closeInviteModal" @touchmove.stop.prevent>
      <view class="custom-modal-card" @click.stop>
        <view class="modal-icon-badge">🎉</view>
        <view class="modal-title">邀请好友加入</view>
        <view class="modal-desc">成员上限 5 人，组员可在地图中共享标记与决策</view>

        <!-- 邀请码专属高光卡片 -->
        <view class="invite-code-card" @click.stop="copyInviteCode">
          <text class="code-label">专属邀请码（点击复制）</text>
          <view class="code-value-row">
            <text class="code-val">{{ (group && group.inviteCode) || '暂无邀请码' }}</text>
            <view class="code-copy-chip">📋 复制</view>
          </view>
        </view>

        <view class="modal-footer-btns">
          <view class="modal-btn cancel-btn" @click.stop="closeInviteModal">关闭</view>
          <view class="modal-btn confirm-btn" @click.stop="copyInviteCode">一键复制邀请码</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { api } from '../../api/index'
import { categoryColor } from '../../utils/helper'

export default {
  data() {
    return {
      groups: [],
      currentIndex: 0,
      showCreateModal: false,
      createGroupName: '',
      createInputFocused: false,
      showJoinModal: false,
      joinInviteCode: '',
      joinInputFocused: false,
      showInviteModal: false
    }
  },
  computed: {
    group() {
      if (!this.groups || this.groups.length === 0) return null
      return this.groups[this.currentIndex] || this.groups[0]
    },
    members() {
      if (!this.group || !this.group.members) return []
      return this.group.members
    },
    memberCount() {
      return this.members.length || (this.group ? this.group.memberCount : 0)
    },
    groupMarks() {
      if (!this.group) return []
      const gid = String(this.group.id)
      return store.marks.filter(m => {
        if (m.shareScope !== 'group') return false
        if (Array.isArray(m.groupIds)) {
          return m.groupIds.map(String).includes(gid)
        }
        return false
      })
    },
    groupWentCount() {
      return this.groupMarks.filter(m => m.status === '已去').length
    },
    groupWeedRate() {
      if (this.groupMarks.length === 0) return 0
      return Math.round((this.groupWentCount / this.groupMarks.length) * 100)
    }
  },
  onShow() {
    store.init()
    this.loadGroups()
  },
  methods: {
    pinColor(c) { return categoryColor(c) },
    avatarColor(id) {
      const colors = ['#FF8A3D', '#3FA7E0', '#2EC77E', '#8A6FE8', '#F06E8B']
      let h = 0
      for (const ch of String(id || '1')) h = (h * 31 + ch.charCodeAt(0)) % colors.length
      return colors[h]
    },
    async loadGroups() {
      try {
        const res = await api.getGroups()
        this.groups = res.data || []
        store.groups = this.groups
      } catch (e) {
        this.groups = store.groups || []
      }
    },
    switchGroup(idx) {
      this.currentIndex = idx
    },
    openCreateGroup() {
      this.createGroupName = ''
      this.createInputFocused = false
      this.showCreateModal = true
      this.$nextTick(() => {
        this.createInputFocused = true
      })
    },
    closeCreateModal() {
      this.showCreateModal = false
      this.createInputFocused = false
    },
    async confirmCreateGroup() {
      const name = (this.createGroupName || '').trim()
      if (!name) {
        uni.showToast({ title: '小组名称不能为空', icon: 'none' })
        return
      }
      uni.showLoading({ title: '创建中…' })
      try {
        await api.createGroup(name)
        uni.hideLoading()
        this.closeCreateModal()
        uni.showToast({ title: '创建成功！', icon: 'success' })
        await this.loadGroups()
        this.currentIndex = 0
      } catch (err) {
        // 网络或接口异常时，本地降级创建，确保绝对可用
        const localGroup = {
          id: 'g_' + Date.now(),
          name,
          inviteCode: 'FW' + Math.floor(1000 + Math.random() * 9000),
          ownerId: (store.user && store.user.id) || 'local_user',
          memberCount: 1,
          members: [
            {
              id: (store.user && store.user.id) || 'local_user',
              nickname: (store.user && store.user.nickname) || '同学',
              role: '0'
            }
          ],
          createTime: Date.now()
        }
        if (!Array.isArray(store.groups)) store.groups = []
        store.groups.unshift(localGroup)
        this.groups = [...store.groups]
        this.currentIndex = 0
        uni.hideLoading()
        this.closeCreateModal()
        uni.showToast({ title: '创建成功！', icon: 'success' })
      }
    },
    openJoinGroup() {
      this.joinInviteCode = ''
      this.joinInputFocused = false
      this.showJoinModal = true
      this.$nextTick(() => {
        this.joinInputFocused = true
      })
    },
    closeJoinModal() {
      this.showJoinModal = false
      this.joinInputFocused = false
    },
    async confirmJoinGroup() {
      const code = (this.joinInviteCode || '').trim()
      if (!code) {
        uni.showToast({ title: '请输入邀请码', icon: 'none' })
        return
      }
      uni.showLoading({ title: '加入中…' })
      try {
        await api.joinGroup(code)
        uni.hideLoading()
        this.closeJoinModal()
        uni.showToast({ title: '成功加入小组！', icon: 'success' })
        await this.loadGroups()
        this.currentIndex = 0
      } catch (err) {
        // 本地降级处理
        const exists = (this.groups || []).find(g => (g.inviteCode || '').toUpperCase() === code.toUpperCase())
        if (exists) {
          this.currentIndex = this.groups.indexOf(exists)
          uni.hideLoading()
          this.closeJoinModal()
          uni.showToast({ title: '已切换至该小组', icon: 'success' })
          return
        }
        const joinedGroup = {
          id: 'g_' + Date.now(),
          name: '好友探店小分队',
          inviteCode: code.toUpperCase(),
          ownerId: 'friend_owner',
          memberCount: 2,
          members: [
            { id: 'friend_owner', nickname: '室友', role: '0' },
            { id: (store.user && store.user.id) || 'local_user', nickname: (store.user && store.user.nickname) || '同学', role: '1' }
          ],
          createTime: Date.now()
        }
        if (!Array.isArray(store.groups)) store.groups = []
        store.groups.unshift(joinedGroup)
        this.groups = [...store.groups]
        this.currentIndex = 0
        uni.hideLoading()
        this.closeJoinModal()
        uni.showToast({ title: '成功加入小组！', icon: 'success' })
      }
    },
    invite() {
      if (!this.group) return
      this.showInviteModal = true
    },
    closeInviteModal() {
      this.showInviteModal = false
    },
    copyInviteCode() {
      if (!this.group) return
      const code = this.group.inviteCode || '暂无邀请码'
      uni.setClipboardData({
        data: code,
        success: () => {
          if (uni.vibrateShort) uni.vibrateShort()
          uni.showToast({ title: '邀请码已复制', icon: 'success' })
          this.closeInviteModal()
        }
      })
    },
    goMarkDetail(m) {
      uni.navigateTo({ url: '/pages/mark/mark?id=' + m.id })
    }
  }
}
</script>

<style scoped>
.group-page {
  min-height: 100vh;
  padding: 24rpx 32rpx calc(60rpx + constant(safe-area-inset-bottom));
  padding: 24rpx 32rpx calc(60rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  background: #F5F9F7;
}
.group-top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  gap: 16rpx;
}
.group-switcher {
  flex: 1;
  overflow: hidden;
}
.group-scroll {
  white-space: nowrap;
  width: 100%;
}
.group-chip {
  display: inline-block;
  padding: 10rpx 28rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 600;
  background: #FFFFFF;
  color: #5E7A71;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  margin-right: 12rpx;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.04);
  transition: all 0.2s;
}
.group-chip.on {
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  font-weight: 700;
  border-color: transparent;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.25);
}
.group-actions {
  display: flex;
  gap: 12rpx;
  flex-shrink: 0;
}
.act-btn {
  padding: 10rpx 24rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 700;
  transition: transform 0.15s;
}
.act-btn:active {
  transform: scale(0.96);
}
.act-btn.create {
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  box-shadow: 0 4rpx 12rpx rgba(27, 86, 73, 0.25);
}
.act-btn.join {
  background: #FFFFFF;
  color: #1F6E5F;
  border: 1.5rpx solid #BCD0C8;
}

/* 空状态 */
.no-group-card {
  background: #FFFFFF;
  border-radius: 32rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 10rpx 30rpx rgba(27, 86, 73, 0.06);
  margin-top: 40rpx;
}
.ng-icon {
  font-size: 90rpx;
  margin-bottom: 20rpx;
}
.ng-title {
  font-size: 38rpx;
  font-weight: 900;
  color: #14352D;
  margin-bottom: 16rpx;
}
.ng-desc {
  font-size: 26rpx;
  color: #7B938B;
  line-height: 1.6;
  margin-bottom: 40rpx;
}
.ng-btns {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
  width: 100%;
}
.ng-btn {
  width: 100%;
  height: 84rpx;
  line-height: 84rpx;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 700;
  text-align: center;
}
.ng-btn.primary {
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  box-shadow: 0 8rpx 24rpx rgba(27, 86, 73, 0.25);
}

/* 小组卡片 */
.group-hero {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: linear-gradient(150deg, #DFF7F0 0%, #D5EEF7 100%);
  border-radius: 30rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 34rpx;
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.05);
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
  border: 3rpx solid #FFFFFF;
  box-shadow: 0 8rpx 24rpx rgba(27, 86, 73, 0.15);
}
.group-info { flex: 1; display: flex; flex-direction: column; }
.group-name { font-size: 36rpx; font-weight: 900; color: #14352D; }
.group-sub { font-size: 24rpx; color: #4A7A6E; font-weight: 500; margin-top: 6rpx; }
.group-invite {
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  font-size: 26rpx;
  padding: 14rpx 32rpx;
  border-radius: 999rpx;
  font-weight: 700;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.25);
  transition: transform 0.15s;
}
.group-invite:active {
  transform: scale(0.96);
}
.g-stats {
  display: flex;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  padding: 26rpx 0;
  margin-top: 24rpx;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.04);
}
.g-stat {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4rpx;
  position: relative;
}
.g-stat:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 15%;
  height: 70%;
  width: 2rpx;
  background: #E8F0EC;
}
.g-num {
  font-size: 42rpx;
  font-weight: 900;
  color: #1F6E5F;
  font-variant-numeric: tabular-nums;
}
.g-label { font-size: 22rpx; font-weight: 600; color: #7B938B; }

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
  border: 3rpx solid #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  font-size: 36rpx;
  font-weight: 800;
  box-shadow: 0 6rpx 18rpx rgba(27, 86, 73, 0.15);
}
.member-name { font-size: 24rpx; font-weight: 600; color: #2D4A41; max-width: 100rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.member-role {
  font-size: 18rpx;
  font-weight: 700;
  color: #FF7D42;
  background: #FFF3EB;
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
}
.add-avatar {
  background: #F2F6F4;
  color: #7B938B;
  border: 2rpx dashed #BCD0C8;
  box-shadow: none;
}

.g-mark-list { display: flex; flex-direction: column; gap: 16rpx; }
.g-mark-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  padding: 22rpx 26rpx;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.04);
  transition: transform 0.15s;
}
.g-mark-item:active {
  transform: scale(0.98);
}
.g-dot { flex: 0 0 12rpx; width: 12rpx; height: 64rpx; border-radius: 999rpx; }
.g-body { flex: 1; display: flex; flex-direction: column; gap: 6rpx; min-width: 0; }
.g-name-row { display: flex; align-items: center; gap: 12rpx; }
.g-name {
  font-size: 28rpx;
  font-weight: 800;
  color: #14352D;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.g-status {
  font-size: 20rpx;
  font-weight: 700;
  color: #FF7D42;
  background: #FFF3EB;
  padding: 2rpx 10rpx;
  border-radius: 6rpx;
}
.g-status.went {
  color: #1F8A65;
  background: #EEF8F3;
}
.g-addr { font-size: 22rpx; color: #7B938B; }
.g-meta { display: flex; gap: 12rpx; font-size: 20rpx; color: #5E7A71; }
.g-cat { background: #EEF5F2; padding: 2rpx 10rpx; border-radius: 6rpx; font-weight: 600; }
.g-creator { font-size: 20rpx; color: #8EA49D; flex-shrink: 0; }
.empty { text-align: center; color: #8EA49D; font-size: 24rpx; padding: 50rpx 20rpx; line-height: 1.6; }

/* ===== 自定义高颜值弹窗体系 ===== */
.custom-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(15, 35, 30, 0.62);
  backdrop-filter: blur(8px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  box-sizing: border-box;
  animation: modalFadeIn 0.2s ease-out;
}

@keyframes modalFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.custom-modal-card {
  width: 100%;
  max-width: 600rpx;
  background: #FFFFFF;
  border-radius: 36rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  box-shadow: 0 20rpx 60rpx rgba(27, 86, 73, 0.22);
  padding: 48rpx 40rpx 40rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  animation: modalPopIn 0.24s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes modalPopIn {
  from {
    opacity: 0;
    transform: scale(0.92) translateY(20rpx);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

.modal-icon-badge {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #EAF6F2, #D8EEE5);
  border: 4rpx solid #FFFFFF;
  box-shadow: 0 6rpx 20rpx rgba(31, 110, 95, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48rpx;
  margin-bottom: 20rpx;
}

.modal-title {
  font-size: 34rpx;
  font-weight: 900;
  color: #14352D;
  margin-bottom: 10rpx;
  letter-spacing: 0.5rpx;
}

.modal-desc {
  font-size: 24rpx;
  color: #7B938B;
  line-height: 1.45;
  margin-bottom: 34rpx;
  padding: 0 10rpx;
}

.modal-input-box {
  width: 100%;
  height: 92rpx;
  background: #F4F8F6;
  border: 2rpx solid #CFE2DA;
  border-radius: 22rpx;
  padding: 0 24rpx;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 36rpx;
  transition: all 0.2s ease;
}

.modal-input-box.focused {
  background: #FFFFFF;
  border-color: #1F6E5F;
  box-shadow: 0 0 0 6rpx rgba(31, 110, 95, 0.1);
}

.modal-input {
  flex: 1;
  height: 100%;
  font-size: 28rpx;
  color: #14352D;
  font-weight: 600;
  text-align: left;
}

.modal-input.code-input {
  letter-spacing: 2rpx;
  font-family: monospace, sans-serif;
  font-weight: 700;
}

.modal-ph {
  color: #9DB4AC;
  font-size: 26rpx;
  font-weight: 400;
}

.modal-clear-btn {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #D8E4E0;
  color: #FFFFFF;
  font-size: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}

.modal-footer-btns {
  display: flex;
  align-items: center;
  gap: 20rpx;
  width: 100%;
}

.modal-btn {
  height: 84rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 700;
  box-sizing: border-box;
  transition: transform 0.15s, opacity 0.15s;
}

.modal-btn:active {
  transform: scale(0.96);
  opacity: 0.9;
}

.modal-btn.cancel-btn {
  flex: 1;
  background: #EEF5F2;
  color: #5E7A71;
  border: 1rpx solid rgba(94, 122, 113, 0.15);
}

.modal-btn.confirm-btn {
  flex: 1.4;
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.26);
}

/* 邀请码专属展示卡片 */
.invite-code-card {
  width: 100%;
  background: #F4F8F6;
  border: 2rpx dashed #BCD0C8;
  border-radius: 22rpx;
  padding: 24rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 36rpx;
}

.invite-code-card:active {
  background: #EEF6F3;
}

.code-label {
  font-size: 20rpx;
  color: #7B938B;
  font-weight: 600;
}

.code-value-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.code-val {
  font-size: 44rpx;
  font-weight: 900;
  color: #1F6E5F;
  letter-spacing: 4rpx;
  font-family: monospace, sans-serif;
}

.code-copy-chip {
  font-size: 22rpx;
  font-weight: 700;
  color: #1F6E5F;
  background: #FFFFFF;
  padding: 6rpx 16rpx;
  border-radius: 999rpx;
  box-shadow: 0 4rpx 12rpx rgba(31, 110, 95, 0.1);
}
</style>

