<template>
  <view class="mark-page">
    <!-- 位置选择 -->
    <view class="loc-box" @click="pickLocation">
      <view class="loc-icon">📍</view>
      <view class="loc-body">
        <text class="loc-title">{{ form.address || form.name || '点击在地图上选点' }}</text>
        <text class="loc-hint">{{ picking ? '地图选点中…' : (form.lat ? '已定位：点击可重新打开地图重选' : '点击打开腾讯原生地图精确定位') }}</text>
      </view>
      <text class="loc-arrow">›</text>
    </view>

    <!-- 微缩地图预览卡片 -->
    <view v-if="form.lat && form.lng" class="mini-map-wrap">
      <map
        class="mini-map"
        :latitude="form.lat"
        :longitude="form.lng"
        :markers="miniMapMarkers"
        :scale="15"
        :show-location="true"
        @click="pickLocation"
      ></map>
      <view class="mini-map-bar">
        <text class="mini-coord">坐标：{{ Number(form.lat).toFixed(4) }}, {{ Number(form.lng).toFixed(4) }}</text>
        <text class="mini-repick-btn" @click="pickLocation">重新选点 ›</text>
      </view>
    </view>

    <!-- 表单卡片 -->
    <view class="form-card">
      <view class="field">
        <text class="label">名称</text>
        <input v-model="form.name" class="input" placeholder="比如：独墅湖图书馆、老王烧烤" placeholder-class="ph" />
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
          <view class="tag-input-box">
            <input
              v-model="tagInput"
              class="tag-input"
              placeholder="输入标签"
              placeholder-class="ph"
              @confirm="addTag"
              @blur="flushTag"
            />
            <text class="tag-add-btn" @click="addTag">+添加</text>
          </view>
        </view>
      </view>

      <view class="field">
        <text class="label">评分 & 人均</text>
        <view class="rate-row">
          <view class="star-row">
            <text
              v-for="i in 5"
              :key="i"
              class="star"
              :class="{ on: i <= form.rating }"
              @click="form.rating = i"
            >★</text>
          </view>
          <view class="price-box">
            <text class="price-prefix">¥</text>
            <input v-model="form.price" class="price-input" type="digit" placeholder="人均" placeholder-class="ph" />
          </view>
        </view>
      </view>

      <!-- 拍照与图片上传 -->
      <view class="field photo-field">
        <view class="photo-header">
          <text class="label">现场照片</text>
          <text class="photo-hint">{{ (form.images || []).length }}/4 张（支持拍照与相册）</text>
        </view>
        <view class="photo-grid">
          <view v-for="(img, idx) in form.images" :key="idx" class="photo-item">
            <image class="photo-thumb" :src="img" mode="aspectFill" @click="previewPhoto(idx)" />
            <view class="photo-del" @click.stop="removePhoto(idx)">✕</view>
          </view>
          <view v-if="(form.images || []).length < 4" class="photo-upload-btn" @click="choosePhoto">
            <text class="cam-icon">📷</text>
            <text class="cam-text">拍照 / 选图</text>
          </view>
        </view>
      </view>

      <view class="field">
        <text class="label">备注 / 小贴士</text>
        <textarea
          v-model="form.note"
          class="note-input"
          placeholder="写点小贴士：招牌菜、必打卡机位、营业时间…"
          placeholder-class="ph"
          maxlength="150"
        />
      </view>
    </view>

    <!-- ===== 共享范围 ===== -->
    <view class="share-card">
      <view class="share-title">共享范围</view>
      <view class="share-opt" :class="{ on: form.shareScope === 'private' }" @click="form.shareScope = 'private'">
        <view class="radio" :class="{ on: form.shareScope === 'private' }"></view>
        <view class="opt-body">
          <text class="opt-name">仅自己可见</text>
          <text class="opt-desc">私密标记，只有你能看到并加入转盘</text>
        </view>
      </view>

      <view class="share-opt" :class="{ on: form.shareScope === 'group' }" @click="toggleGroupScope">
        <view class="radio" :class="{ on: form.shareScope === 'group' }"></view>
        <view class="opt-body">
          <text class="opt-name">共享到小组</text>
          <text class="opt-desc">宿舍 / 小分队成员可见（共同打卡）</text>
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
          <text class="opt-desc" v-if="store.user && store.user.schoolName">同校用户可见（{{ store.user.schoolName }}）</text>
          <text class="opt-desc" v-else>需先在「我的」页选择学校</text>
        </view>
      </view>

      <view class="share-tip">💡 共享范围可随时修改或撤回，撤回后不影响他人已收藏的记录</view>
    </view>

    <view class="fw-btn save-btn" @click="save">保存地点标记</view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { api } from '../../api/index'
import { persistLocalImage } from '../../utils/helper'

export default {
  data() {
    return {
      store,
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
        images: [],
        shareScope: 'private',
        groupIds: []
      }
    }
  },
  computed: {
    myGroups() {
      if (!store.user) return []
      return (store.groups || []).filter(g => g && g.members && g.members.some(m => String(m.id || m.userId) === String(store.user.id)))
    },
    miniMapMarkers() {
      if (!this.form.lat || !this.form.lng) return []
      return [{
        id: 1,
        latitude: Number(this.form.lat),
        longitude: Number(this.form.lng),
        title: this.form.name || this.form.address || '标记位置',
        width: 28,
        height: 36
      }]
    }
  },
  onLoad(options) {
    store.init()
    if (options && options.id) {
      this.editId = options.id
      const m = store.getMarkById(options.id)
      if (m) {
        this.form = {
          name: m.name || '',
          category: m.category || '美食',
          tags: Array.isArray(m.tags) ? [...m.tags] : [],
          rating: Number(m.rating) || 4,
          price: m.price ? String(m.price) : '',
          note: m.note || m.remark || '',
          address: m.address || '',
          lat: Number(m.lat) || 0,
          lng: Number(m.lng) || 0,
          images: Array.isArray(m.images) ? [...m.images] : [],
          shareScope: m.shareScope || 'private',
          groupIds: Array.isArray(m.groupIds) ? [...m.groupIds] : []
        }
        uni.setNavigationBarTitle({ title: '编辑标记' })
      }
    }
  },
  methods: {
    // 调用地图选点（优先调用微信/原生腾讯地图 chooseLocation）
    pickLocation() {
      this.picking = true
      uni.chooseLocation({
        success: (res) => {
          this.picking = false
          if (res.name || res.address) {
            this.form.address = res.address || res.name
            this.form.lat = Number(res.latitude)
            this.form.lng = Number(res.longitude)
            if (!this.form.name) {
              this.form.name = res.name
            }
          }
        },
        fail: (err) => {
          this.picking = false
          console.warn('chooseLocation fail', err)
          // 检查是否拒绝权限
          if (err && (String(err.errMsg).includes('auth') || String(err.errMsg).includes('deny'))) {
            uni.showModal({
              title: '定位授权提示',
              content: '需要您的位置权限来唤起地图选点，是否前往设置允许？',
              confirmText: '去设置',
              success: (m) => {
                if (m.confirm) uni.openSetting()
              }
            })
            return
          }

          // 降级候选列表（开发工具环境或取消选点时）
          const samples = [
            { name: '独墅湖高教区文星广场', address: '苏州市苏州工业园区仁爱路文星广场', lat: 31.2821, lng: 120.7389 },
            { name: '苏州大学东校区商业街', address: '苏州市姑苏区干将东路333号', lat: 31.2989, lng: 120.6372 },
            { name: '文荟广场美食城', address: '苏州市苏州工业园区仁爱路文荟广场', lat: 31.2755, lng: 120.7412 },
            { name: '苏州中心商场', address: '苏州市苏州工业园区苏绣路', lat: 31.3198, lng: 120.6721 },
            { name: '观前街步行街', address: '苏州市姑苏区观前街', lat: 31.3128, lng: 120.6241 }
          ]
          uni.showActionSheet({
            itemList: samples.map(s => s.name),
            success: (r) => {
              const s = samples[r.tapIndex]
              this.form.name = this.form.name || s.name
              this.form.address = s.address
              this.form.lat = s.lat
              this.form.lng = s.lng
            }
          })
        }
      })
    },

    // 拍照或从相册选择图片
    choosePhoto() {
      const remain = 4 - (this.form.images || []).length
      if (remain <= 0) {
        uni.showToast({ title: '最多上传4张照片', icon: 'none' })
        return
      }

      uni.showActionSheet({
        itemList: ['📷 现场拍照', '🖼️ 从相册选择'],
        success: (act) => {
          const sourceType = act.tapIndex === 0 ? ['camera'] : ['album']
          uni.chooseImage({
            count: remain,
            sizeType: ['compressed'],
            sourceType,
            success: async (res) => {
              const paths = res.tempFilePaths || []
              uni.showLoading({ title: '处理照片中…' })
              for (const p of paths) {
                let finalUrl = ''
                try {
                  finalUrl = await api.uploadImage(p)
                } catch (e) { /* ignore */ }
                // 若上传未返回有效远程链接，立即转存至微信永久沙箱目录 wxfile://usr/，永不失效
                if (!finalUrl || finalUrl === p || finalUrl.includes('/tmp/') || finalUrl.includes('tmp_')) {
                  finalUrl = persistLocalImage(p)
                }
                this.form.images.push(finalUrl)
              }
              uni.hideLoading()
            }
          })
        }
      })
    },

    // 移除图片
    removePhoto(idx) {
      this.form.images.splice(idx, 1)
    },

    // 预览大图
    previewPhoto(idx) {
      uni.previewImage({
        urls: this.form.images,
        current: this.form.images[idx]
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

    // 保存标记
    async save() {
      if (!this.form.name.trim()) {
        uni.showToast({ title: '请填写地点名称', icon: 'none' })
        return
      }
      if (this.form.shareScope === 'group' && (!this.form.groupIds || this.form.groupIds.length === 0)) {
        uni.showToast({ title: '请选择要共享的小组', icon: 'none' })
        return
      }
      if (this.form.shareScope === 'school' && !(store.user && store.user.schoolName)) {
        uni.showToast({ title: '请先在「我的」页选择学校', icon: 'none' })
        return
      }

      uni.showLoading({ title: '保存中' })
      const payload = {
        name: this.form.name.trim(),
        category: this.form.category,
        tags: this.form.tags,
        rating: Number(this.form.rating) || 4,
        price: Number(this.form.price) || 0,
        note: this.form.note,
        remark: this.form.note,
        address: this.form.address || this.form.name.trim(),
        lat: Number(this.form.lat) || 31.2989,
        lng: Number(this.form.lng) || 120.6372,
        images: this.form.images || [],
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
        uni.showToast({ title: '地点标记已保存', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 500)
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '保存成功（本地缓存）', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 500)
      }
    }
  }
}
</script>

<style scoped>
.mark-page {
  min-height: 100vh;
  padding: 24rpx 32rpx calc(60rpx + constant(safe-area-inset-bottom));
  padding: 24rpx 32rpx calc(60rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
  background: #F5F9F7;
}

/* 位置选点条 */
.loc-box {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 26rpx 28rpx;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.05);
  transition: transform 0.15s;
}
.loc-box:active {
  transform: scale(0.98);
}
.loc-icon { font-size: 46rpx; }
.loc-body { flex: 1; display: flex; flex-direction: column; }
.loc-title { font-size: 32rpx; font-weight: 800; color: #14352D; }
.loc-hint { font-size: 22rpx; color: #1F6E5F; margin-top: 6rpx; font-weight: 600; }
.loc-arrow { font-size: 36rpx; color: #8EA49D; }

/* 微缩地图预览卡片 */
.mini-map-wrap {
  margin-top: 16rpx;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  overflow: hidden;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.04);
}
.mini-map {
  width: 100%;
  height: 240rpx;
}
.mini-map-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #FFFFFF;
  border-top: 1.5rpx solid #F0F4F2;
}
.mini-coord { font-size: 22rpx; color: #7B938B; font-variant-numeric: tabular-nums; }
.mini-repick-btn { font-size: 24rpx; font-weight: 700; color: #1F6E5F; }

/* 表单卡片 */
.form-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 12rpx 32rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.04);
}
.field {
  padding: 24rpx 0;
  border-bottom: 1.5rpx solid #F0F5F2;
}
.field:last-child { border-bottom: none; }
.label {
  font-size: 26rpx;
  font-weight: 800;
  color: #2D4A41;
  display: block;
  margin-bottom: 14rpx;
}
.input {
  font-size: 30rpx;
  font-weight: 600;
  color: #14352D;
  height: 64rpx;
  line-height: 64rpx;
  width: 100%;
}
.ph { color: #8EA49D; font-weight: normal; }
.cat-row { display: flex; flex-wrap: wrap; gap: 14rpx; }
.cat-chip {
  padding: 12rpx 32rpx;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 600;
  background: #EEF5F2;
  color: #5E7A71;
  transition: all 0.2s;
}
.cat-chip.on {
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  font-weight: 700;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.25);
}
.tags-edit { display: flex; flex-wrap: wrap; gap: 12rpx; align-items: center; }
.tag-edit {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  background: #EAF5F0;
  color: #1F6E5F;
  font-size: 24rpx;
  font-weight: 600;
  padding: 8rpx 20rpx;
  border-radius: 999rpx;
}
.tag-del { color: #8EA49D; padding: 0 4rpx; }
.tag-input-box {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  background: #F0F5F2;
  border-radius: 999rpx;
  padding: 6rpx 18rpx;
}
.tag-input { font-size: 24rpx; color: #14352D; width: 150rpx; }
.tag-add-btn { font-size: 22rpx; color: #1F6E5F; font-weight: 800; padding: 4rpx 8rpx; }
.rate-row { display: flex; justify-content: space-between; align-items: center; }
.star-row { display: flex; gap: 8rpx; }
.star {
  font-size: 48rpx;
  color: #E2ECE8;
  padding: 0 4rpx;
  transition: transform 0.15s;
}
.star:active {
  transform: scale(1.2);
}
.star.on {
  color: #FFB319;
  text-shadow: 0 2rpx 8rpx rgba(255, 179, 25, 0.4);
}
.price-box {
  display: flex;
  align-items: center;
  background: #EEF5F2;
  border-radius: 16rpx;
  padding: 10rpx 22rpx;
}
.price-prefix {
  font-size: 28rpx;
  color: #5E7A71;
  font-weight: 800;
  margin-right: 6rpx;
}
.price-input {
  width: 120rpx;
  text-align: center;
  font-size: 28rpx;
  font-weight: 700;
  color: #FF7D42;
}

/* 照片网格 */
.photo-field { padding: 24rpx 0; }
.photo-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16rpx; }
.photo-hint { font-size: 22rpx; color: #8EA49D; }
.photo-grid { display: flex; flex-wrap: wrap; gap: 18rpx; }
.photo-item {
  position: relative;
  width: 150rpx;
  height: 150rpx;
  border-radius: 18rpx;
  overflow: hidden;
  box-shadow: 0 4rpx 14rpx rgba(0,0,0,0.06);
}
.photo-thumb { width: 100%; height: 100%; }
.photo-del {
  position: absolute;
  top: 6rpx;
  right: 6rpx;
  width: 38rpx;
  height: 38rpx;
  background: rgba(0,0,0,0.65);
  color: #FFFFFF;
  font-size: 22rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.photo-upload-btn {
  width: 150rpx;
  height: 150rpx;
  border: 2rpx dashed #BCD0C8;
  border-radius: 18rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #F6FAF8;
  transition: transform 0.15s;
}
.photo-upload-btn:active {
  transform: scale(0.95);
}
.cam-icon { font-size: 42rpx; margin-bottom: 6rpx; }
.cam-text { font-size: 20rpx; color: #7B938B; font-weight: 600; }

.note-input {
  width: 100%;
  height: 140rpx;
  font-size: 26rpx;
  background: #F8FAF9;
  border: 1.5rpx solid #E8EFEA;
  border-radius: 18rpx;
  padding: 18rpx 20rpx;
  box-sizing: border-box;
  color: #14352D;
}

/* 共享范围 */
.share-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 30rpx 32rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.04);
}
.share-title { font-size: 32rpx; font-weight: 800; color: #14352D; margin-bottom: 20rpx; }
.share-opt {
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
  padding: 22rpx 0;
  border-bottom: 1.5rpx solid #F0F5F2;
}
.share-opt.on .opt-name { color: #1F6E5F; }
.radio {
  flex: 0 0 36rpx;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 4rpx solid #CFE2DA;
  margin-top: 4rpx;
  box-sizing: border-box;
  transition: all 0.2s;
}
.radio.on {
  border: 10rpx solid #1F6E5F;
  background: #FFFFFF;
}
.opt-body { display: flex; flex-direction: column; }
.opt-name { font-size: 30rpx; font-weight: 700; color: #2D4A41; }
.opt-desc { font-size: 22rpx; color: #8EA49D; margin-top: 6rpx; }
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
  background: #EEF5F2;
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  font-size: 26rpx;
  color: #3D5A51;
}
.group-item.on {
  background: #EAF5F0;
  color: #1F6E5F;
  font-weight: 700;
  border: 1.5rpx solid #C4EBD8;
}
.g-count { font-size: 22rpx; color: #8EA49D; }
.share-tip {
  margin-top: 22rpx;
  font-size: 22rpx;
  color: #7B938B;
  background: #F6FAF8;
  border-radius: 16rpx;
  padding: 16rpx 20rpx;
  line-height: 1.6;
}
.save-btn { margin-top: 36rpx; }
</style>
