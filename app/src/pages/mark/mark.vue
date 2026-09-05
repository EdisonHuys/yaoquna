<template>
  <view class="mark-page">
    <!-- 位置 -->
    <view class="loc-box" @click="pickLocation">
      <view class="loc-icon">📍</view>
      <view class="loc-body">
        <text class="loc-title">{{ form.address || '选择地点位置' }}</text>
        <text class="loc-hint">{{ picking ? '定位中…（接入腾讯位置服务后自动定位）' : '点击选择地图位置' }}</text>
      </view>
    </view>

    <!-- 表单 -->
    <view class="form-card">
      <view class="field">
        <text class="label">名称</text>
        <input v-model="form.name" class="input" placeholder="比如：老王烧烤" placeholder-class="ph" />
      </view>

      <view class="field">
        <text class="label">分类</text>
        <view class="cat-row">
          <view
            v-for="c in categories"
            :key="c"
            class="cat-chip"
            :class="{ on: form.category === c }"
            @click="form.category = c"
          >{{ c }}</view>
        </view>
      </view>

      <view class="field">
        <text class="label">标签</text>
        <view class="tags-edit">
          <view v-for="(t, i) in form.tags" :key="i" class="tag-edit">
            <text>{{ t }}</text>
            <text class="tag-del" @click="form.tags.splice(i, 1)">✕</text>
          </view>
          <input
            v-model="tagInput"
            class="tag-input"
            placeholder="添加标签"
            placeholder-class="ph"
            @confirm="addTag"
            @blur="flushTag"
          />
        </view>
      </view>

      <view class="field">
        <text class="label">评分 & 人均</text>
        <view class="rate-row">
          <view class="star-row" @click="tapScore">
            <text v-for="i in 5" :key="i" class="star" :class="{ on: i <= form.rating }">★</text>
          </view>
          <input v-model="form.price" class="price-input" type="digit" placeholder="¥" placeholder-class="ph" />
        </view>
      </view>

      <view class="field">
        <text class="label">备注</text>
        <textarea
          v-model="form.note"
          class="note-input"
          placeholder="写点小贴士：营业时间、招牌菜…"
          placeholder-class="ph"
          :maxlength="100"
        ></textarea>
      </view>
    </view>

    <!-- ===== 共享范围（核心） ===== -->
    <view class="share-card">
      <view class="share-title">共享范围</view>
      <view class="share-opt" :class="{ on: form.shareScope === 'private' }" @click="form.shareScope = 'private'">
        <view class="radio" :class="{ on: form.shareScope === 'private' }"></view>
        <view class="opt-body">
          <text class="opt-name">仅自己可见</text>
          <text class="opt-desc">私密标记，只有你能看到</text>
        </view>
      </view>

      <view class="share-opt" :class="{ on: form.shareScope === 'group' }" @click="toggleGroupScope">
        <view class="radio" :class="{ on: form.shareScope === 'group' }"></view>
        <view class="opt-body">
          <text class="opt-name">共享到小组</text>
          <text class="opt-desc">宿舍 / 小分队成员可见（最多 5 人）</text>
        </view>
      </view>
      <view v-if="form.shareScope === 'group'" class="group-select">
        <view
          v-for="g in myGroups"
          :key="g.id"
          class="group-item"
          :class="{ on: form.groupIds.includes(g.id) }"
          @click="toggleGroup(g.id)"
        >
          <text>{{ g.name }}</text>
          <text class="g-count">{{ g.memberCount }}人</text>
        </view>
      </view>

      <view class="share-opt" :class="{ on: form.shareScope === 'school' }" @click="form.shareScope = 'school'">
        <view class="radio" :class="{ on: form.shareScope === 'school' }"></view>
        <view class="opt-body">
          <text class="opt-name">共享到本校</text>
          <text class="opt-desc" v-if="store.user && store.user.schoolName">同校用户可见（{{ store.user.schoolName }}）· App 端开放</text>
          <text class="opt-desc" v-else>需先在「我的」页选择学校</text>
        </view>
      </view>

      <view class="share-tip">💡 共享范围可随时修改或撤回，撤回后不影响他人已收藏的副本</view>
    </view>

    <view class="fw-btn save-btn" @click="save">保存标记</view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { api } from '../../api/index'

export default {
  data() {
    return {
      categories: ['美食', '玩乐', '购物', '自习', '其他'],
      tagInput: '',
      picking: false,
      editId: '',
      form: {
        name: '',
        category: '美食',
        tags: [],
        rating: 4,
        price: '',
        note: '',
        address: '',
        lat: 0,
        lng: 0,
        shareScope: 'private',
        groupIds: [],
        store
      }
    }
  },
  computed: {
    myGroups() {
      if (!store.user) return []
      return store.groups.filter(g => g.members.some(m => m.id === store.user.id))
    }
  },
  onLoad(options) {
    store.init()
    if (options && options.id) {
      this.editId = options.id
      const m = store.getMarkById(options.id)
      if (m) {
        this.form = {
          name: m.name,
          category: m.category,
          tags: [...m.tags],
          rating: m.rating,
          price: String(m.price || ''),
          note: m.note || '',
          address: m.address,
          lat: m.lat,
          lng: m.lng,
          shareScope: m.shareScope || 'private',
          groupIds: m.groupIds ? [...m.groupIds] : []
        }
        uni.setNavigationBarTitle({ title: '编辑标记' })
      }
    }
  },
  methods: {
    pickLocation() {
      this.picking = true
      // TODO: 接入腾讯位置服务 / 微信 map 选点
      uni.showModal({
        title: '选择位置',
        content: '正式版将调用地图选点组件（腾讯位置服务）\n当前演示：使用示例位置',
        confirmText: '用示例位置',
        success: res => {
          this.picking = false
          if (res.confirm) {
            this.form.address = '苏州大学东校区东门步行街'
            this.form.lat = 31.2989
            this.form.lng = 120.6372
          }
        }
      })
    },
    addTag() {
      this.flushTag()
    },
    flushTag() {
      const t = this.tagInput.trim()
      if (t && !this.form.tags.includes(t)) this.form.tags.push(t)
      this.tagInput = ''
    },
    tapScore() {
      this.form.rating = this.form.rating >= 5 ? 1 : this.form.rating + 1
    },
    toggleGroupScope() {
      if (this.form.shareScope === 'group') this.form.shareScope = 'private'
      else this.form.shareScope = 'group'
    },
    toggleGroup(id) {
      const idx = this.form.groupIds.indexOf(id)
      if (idx > -1) this.form.groupIds.splice(idx, 1)
      else this.form.groupIds.push(id)
      if (this.form.groupIds.length === 0) {
        uni.showToast({ title: '至少选择一个小组', icon: 'none' })
        this.form.groupIds.push(id)
      }
    },
    async save() {
      if (!this.form.name.trim()) {
        uni.showToast({ title: '请填写地点名称', icon: 'none' })
        return
      }
      if (this.form.shareScope === 'group' && this.form.groupIds.length === 0) {
        uni.showToast({ title: '请选择要共享的小组', icon: 'none' })
        return
      }
      if (this.form.shareScope === 'school' && !(store.user && store.user.schoolName)) {
        uni.showToast({ title: '请先在「我的」页选择学校', icon: 'none' })
        return
      }
      uni.showLoading({ title: '保存中' })
      const payload = {
        name: this.form.name,
        category: this.form.category,
        tags: this.form.tags,
        rating: this.form.rating,
        price: Number(this.form.price) || 0,
        note: this.form.note,
        address: this.form.address,
        lat: this.form.lat,
        lng: this.form.lng,
        shareScope: this.form.shareScope,
        groupIds: this.form.groupIds,
        schoolId: this.form.shareScope === 'school' && store.user ? store.user.schoolId : null
      }
      try {
        if (this.editId) {
          await api.updateMark(this.editId, payload)
        } else {
          await api.createMark(payload)
        }
        uni.hideLoading()
        uni.showToast({ title: '已保存', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 500)
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '保存失败', icon: 'none' })
      }
    }
  }
}
</script>

<style scoped>
.mark-page {
  min-height: 100vh;
  padding: 24rpx 32rpx 60rpx;
  box-sizing: border-box;
}
.loc-box {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 26rpx;
  box-shadow: 0 6rpx 24rpx rgba(31,110,95,0.05);
}
.loc-icon { font-size: 44rpx; }
.loc-body { display: flex; flex-direction: column; }
.loc-title { font-size: 30rpx; font-weight: 600; color: #1A3B34; }
.loc-hint { font-size: 22rpx; color: #A9B8B2; margin-top: 4rpx; }
.form-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 10rpx 30rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(31,110,95,0.05);
}
.field {
  padding: 24rpx 0;
  border-bottom: 2rpx solid #F2F6F4;
}
.field:last-child { border-bottom: none; }
.label { font-size: 26rpx; font-weight: 700; color: #4A5D57; display: block; margin-bottom: 14rpx; }
.input {
  font-size: 30rpx;
  color: #1A3B34;
}
.ph { color: #A9B8B2; }
.cat-row { display: flex; flex-wrap: wrap; gap: 14rpx; }
.cat-chip {
  padding: 10rpx 28rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  background: #F2F6F4;
  color: #7A8A84;
}
.cat-chip.on { background: #1F6E5F; color: #FFFFFF; font-weight: 600; }
.tags-edit { display: flex; flex-wrap: wrap; gap: 12rpx; align-items: center; }
.tag-edit {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  background: #EFF7F3;
  color: #2C7A66;
  font-size: 24rpx;
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
}
.tag-del { color: #9AC4B6; }
.tag-input { font-size: 26rpx; color: #1A3B34; min-width: 180rpx; }
.rate-row { display: flex; justify-content: space-between; align-items: center; }
.star-row { display: flex; gap: 4rpx; }
.star { font-size: 44rpx; color: #E3EBE7; }
.star.on { color: #FFC94D; }
.price-input {
  width: 160rpx;
  background: #F2F6F4;
  border-radius: 16rpx;
  padding: 12rpx 20rpx;
  text-align: center;
  font-size: 28rpx;
}
.note-input {
  width: 100%;
  height: 120rpx;
  font-size: 26rpx;
  background: #F8FAF9;
  border-radius: 16rpx;
  padding: 18rpx;
  box-sizing: border-box;
}

/* 共享范围 */
.share-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(31,110,95,0.05);
}
.share-title { font-size: 30rpx; font-weight: 700; color: #1A3B34; margin-bottom: 20rpx; }
.share-opt {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 22rpx 0;
  border-bottom: 2rpx solid #F2F6F4;
}
.share-opt.on .opt-name { color: #1F6E5F; }
.radio {
  flex: 0 0 36rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 4rpx solid #D3DEDA;
  margin-top: 4rpx;
  box-sizing: border-box;
}
.radio.on {
  border: 10rpx solid #1F6E5F;
}
.opt-body { display: flex; flex-direction: column; }
.opt-name { font-size: 30rpx; font-weight: 600; color: #4A5D57; }
.opt-desc { font-size: 22rpx; color: #A9B8B2; margin-top: 6rpx; }
.group-select {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
  padding: 16rpx 0 16rpx 56rpx;
}
.group-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F2F6F4;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  color: #4A5D57;
}
.group-item.on { background: #EFF7F3; color: #1F6E5F; font-weight: 600; }
.g-count { font-size: 22rpx; color: #A9B8B2; }
.share-tip {
  margin-top: 22rpx;
  font-size: 22rpx;
  color: #7A8A84;
  background: #F8FAF9;
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
  line-height: 1.6;
}
.save-btn { margin-top: 36rpx; }
</style>
