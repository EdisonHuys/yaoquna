<template>
  <view class="map-page">
    <!-- 顶部极简沉浸导航 -->
    <view class="map-head" :style="{ paddingTop: (nav.statusBarHeight || 44) + 'px' }">
      <view
        class="map-title-row"
        :style="{
          height: (nav.navBarHeight || 44) + 'px',
          paddingRight: (nav.menuRight || 96) + 'px'
        }"
      >
        <view class="title-brand">
          <text class="map-title">我的标记地图</text>
        </view>
      </view>
    </view>

    <!-- 搜索栏 + 视图切换二合一 -->
    <view class="search-wrap">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input
          v-model="keyword"
          class="search-input"
          placeholder="搜索地点、地址或标签…"
          placeholder-class="ph"
        />
        <text v-if="keyword" class="search-clear" @click="keyword = ''">✕</text>
      </view>
      <!-- 切换视图微胶囊按钮 -->
      <view class="mode-btn" @click="view = (view === 'map' ? 'list' : 'map')">
        <text class="mode-ico">{{ view === 'map' ? '📋' : '🗺️' }}</text>
        <text class="mode-label">{{ view === 'map' ? '列表' : '地图' }}</text>
      </view>
    </view>

    <!-- 分类筛选横滑条 -->
    <scroll-view scroll-x class="filter-bar">
      <view
        v-for="c in filters"
        :key="c"
        class="filter-chip"
        :class="{ on: filter === c }"
        @click="filter = c"
      >{{ c }}</view>
    </scroll-view>

    <!-- ===== 原生地图沉浸视图 ===== -->
    <view v-if="view === 'map'" class="map-section">
      <view class="real-map-container">
        <map
          id="fateMap"
          class="real-map"
          :latitude="centerLat"
          :longitude="centerLng"
          :scale="mapScale"
          :markers="mapMarkers"
          :show-location="true"
          @markertap="onMarkerTap"
          @callouttap="onMarkerTap"
        ></map>

        <!-- 地图定位回到当前点按钮（右上角，绝不被底部遮挡） -->
        <view class="map-locate-btn" @click="moveToMyLocation">🎯</view>

        <!-- 腾讯地图左下角 Logo 物理遮罩 -->
        <view class="map-logo-mask"></view>

        <!-- 核心地图抽屉面板（常驻底部不可关闭，支持精简卡片与长高可滚动列表平滑动画切换） -->
        <view
          v-if="filteredMarks.length > 0"
          class="map-bottom-sheet"
          :class="{ expanded: isSheetExpanded }"
          @touchmove.stop
        >
          <!-- 1. 顶部手柄条（支持点击展开/收起，并监听平滑上下轻扫） -->
          <view
            class="sheet-drag-handle-bar"
            @click="toggleSheet"
            @touchstart="onDragStart"
            @touchend="onDragEnd"
          >
            <view class="drag-handle-line"></view>
            <view class="drag-hint-row">
              <text class="drag-hint-text">
                {{ isSheetExpanded ? '▼ 点击收起为精简卡片' : '▲ 点击或向上拖拽长高列表 (' + filteredMarks.length + ')' }}
              </text>
            </view>
          </view>

          <!-- 抽屉舞台容器（双状态平滑淡入淡出动画） -->
          <view class="sheet-content-stage">
            <!-- 2. 精简单卡态（图文双栏黄金分割排版，彻底解决压扁与空白） -->
            <view
              v-if="selected"
              class="sheet-compact-view"
              :class="{ 'view-active': !isSheetExpanded, 'view-hidden': isSheetExpanded }"
            >
              <!-- 上部：图文英雄双栏区（左图右文） -->
              <view class="card-hero-row">
                <!-- 左栏：封面缩略大图（有图时展示，带张数角标与高清大图预览） -->
                <view
                  v-if="selected.images && selected.images.length > 0 && !selected._imgErr"
                  class="card-hero-thumb-box"
                  @click.stop="previewPhoto(selected.images[0], selected.images)"
                >
                  <image
                    class="card-hero-thumb"
                    :src="selected.images[0]"
                    mode="aspectFill"
                    @error="onCardPhotoError(selected, 0)"
                  />
                  <view v-if="selected.images.length > 1" class="thumb-count-pill">
                    📷 {{ selected.images.length }}
                  </view>
                </view>

                <!-- 无照片时的专属高颜值占位图（尺寸 154rpx 严格保持一致，色彩随分类动态映射） -->
                <view
                  v-else
                  class="card-hero-thumb-box card-hero-placeholder"
                  :style="{ background: placeholderBg(selected.category), borderColor: placeholderBorder(selected.category) }"
                  @click.stop="onPlaceholderClick(selected)"
                >
                  <view class="placeholder-icon-circle">
                    <text class="placeholder-emoji">{{ categoryIcon(selected.category) }}</text>
                  </view>
                  <text class="placeholder-label" :style="{ color: pinColor(selected.category) }">暂无图片</text>
                </view>

                <!-- 右栏：文本与属性信息流 -->
                <view class="card-hero-info">
                  <view class="card-title-row">
                    <text class="card-name">{{ selected.name }}</text>
                  </view>

                  <view class="card-meta-row">
                    <text class="card-cat" :style="{ color: pinColor(selected.category) }">{{ selected.category }}</text>
                    <text class="card-rate">⭐ {{ selected.rating }}分</text>
                    <text v-if="selected.price" class="card-price">¥{{ selected.price }}/人</text>
                    <text class="card-scope-badge" :class="'scope-' + selected.shareScope">
                      {{ shareLabel(selected.shareScope) }}
                    </text>
                  </view>

                  <text v-if="selected.address" class="card-addr">📍 {{ selected.address }}</text>
                  <text v-if="selected.remark || selected.note" class="card-note">💡 {{ selected.remark || selected.note }}</text>
                </view>
              </view>

              <!-- 下部：主次分明的操作按钮栏（导航主行动 + 编辑与删除辅助行动） -->
              <view class="card-actions">
                <view class="act-btn nav-btn" @click.stop="navigatePlace(selected)">
                  <text class="btn-ico">🧭</text>
                  <text class="btn-txt">导航</text>
                </view>
                <view class="act-btn edit-btn" @click.stop="goMark(selected)">
                  <text class="btn-ico">✏️</text>
                  <text class="btn-txt">编辑</text>
                </view>
                <view class="act-btn del-btn" @click.stop="removeMark(selected)">
                  <text class="btn-ico">🗑️</text>
                  <text class="btn-txt">删除</text>
                </view>
              </view>
            </view>

            <!-- 3. 长高展开列表态（高度 54vh，可滚动查看所有卡片，地图上半部保留清晰视野） -->
            <view
              class="sheet-expanded-view"
              :class="{ 'view-active': isSheetExpanded, 'view-hidden': !isSheetExpanded }"
            >
              <view class="expanded-header">
                <view class="expanded-title-box">
                  <text class="exp-title">地点卡片列表</text>
                  <text class="exp-count">({{ filteredMarks.length }} 处地点)</text>
                </view>
              </view>

              <!-- 垂直滚动卡片列表 -->
              <scroll-view scroll-y class="expanded-scroll-area" :enhanced="true" :show-scrollbar="false">
                <view
                  v-for="m in filteredMarks"
                  :key="m.id"
                  class="exp-mark-card"
                  :class="{ 'exp-active-card': selected && String(selected.id) === String(m.id) }"
                  @click="selectMarkInSheet(m)"
                >
                  <!-- 上部：图文英雄双栏区（左图右文） -->
                  <view class="card-hero-row">
                    <!-- 左栏：封面大图（有图时展示，带张数角标与高清大图预览） -->
                    <view
                      v-if="m.images && m.images.length > 0 && !m._imgErr"
                      class="card-hero-thumb-box"
                      @click.stop="previewPhoto(m.images[0], m.images)"
                    >
                      <image
                        class="card-hero-thumb"
                        :src="m.images[0]"
                        mode="aspectFill"
                        @error="onImageError(m)"
                      />
                      <view v-if="m.images.length > 1" class="thumb-count-pill">
                        📷 {{ m.images.length }}
                      </view>
                    </view>

                    <!-- 无照片时的专属高颜值占位图（完全对齐图一） -->
                    <view
                      v-else
                      class="card-hero-thumb-box card-hero-placeholder"
                      :style="{ background: placeholderBg(m.category), borderColor: placeholderBorder(m.category) }"
                      @click.stop="onPlaceholderClick(m)"
                    >
                      <view class="placeholder-icon-circle">
                        <text class="placeholder-emoji">{{ categoryIcon(m.category) }}</text>
                      </view>
                      <text class="placeholder-label" :style="{ color: pinColor(m.category) }">暂无图片</text>
                    </view>

                    <!-- 右栏：文本与属性信息流 -->
                    <view class="card-hero-info">
                      <view class="card-title-row">
                        <text class="card-name">{{ m.name }}</text>
                        <text v-if="m.status === '已去'" class="exp-card-status went">已去</text>
                      </view>

                      <view class="card-meta-row">
                        <text class="card-cat" :style="{ color: pinColor(m.category) }">{{ m.category }}</text>
                        <text class="card-rate">⭐ {{ m.rating }}分</text>
                        <text v-if="m.price" class="card-price">¥{{ m.price }}/人</text>
                        <text class="card-scope-badge" :class="'scope-' + m.shareScope">
                          {{ shareLabel(m.shareScope) }}
                        </text>
                      </view>

                      <text v-if="m.address" class="card-addr">📍 {{ m.address }}</text>
                      <text v-if="m.remark || m.note" class="card-note">💡 {{ m.remark || m.note }}</text>
                    </view>
                  </view>

                  <!-- 下部：主次分明的操作按钮栏（导航 + 编辑 + 删除） -->
                  <view class="card-actions">
                    <view class="act-btn nav-btn" @click.stop="navigatePlace(m)">
                      <text class="btn-ico">🧭</text>
                      <text class="btn-txt">导航</text>
                    </view>
                    <view class="act-btn edit-btn" @click.stop="goMark(m)">
                      <text class="btn-ico">✏️</text>
                      <text class="btn-txt">编辑</text>
                    </view>
                    <view class="act-btn del-btn" @click.stop="removeMark(m)">
                      <text class="btn-ico">🗑️</text>
                      <text class="btn-txt">删除</text>
                    </view>
                  </view>
                </view>

                <view class="expanded-scroll-bottom-padding"></view>
              </scroll-view>
            </view>
          </view>
        </view>

        <view v-if="filteredMarks.length === 0" class="map-empty-tip">
          当前分类下暂无标记，点击右下角 ＋ 添加新地点
        </view>
      </view>
    </view>

    <!-- ===== 列表视图（全面对齐图一统一卡片布局） ===== -->
    <view v-else class="mark-list">
      <view
        v-for="m in filteredMarks"
        :key="m.id"
        class="exp-mark-card"
        @click="selectMark(m)"
      >
        <!-- 上部：图文英雄双栏区（左图右文） -->
        <view class="card-hero-row">
          <!-- 左栏：封面大图（有图时展示，带张数角标与高清大图预览） -->
          <view
            v-if="m.images && m.images.length > 0 && !m._imgErr"
            class="card-hero-thumb-box"
            @click.stop="previewPhoto(m.images[0], m.images)"
          >
            <image
              class="card-hero-thumb"
              :src="m.images[0]"
              mode="aspectFill"
              @error="onImageError(m)"
            />
            <view v-if="m.images.length > 1" class="thumb-count-pill">
              📷 {{ m.images.length }}
            </view>
          </view>

          <!-- 无照片时的专属高颜值占位图（完全对齐图一） -->
          <view
            v-else
            class="card-hero-thumb-box card-hero-placeholder"
            :style="{ background: placeholderBg(m.category), borderColor: placeholderBorder(m.category) }"
            @click.stop="onPlaceholderClick(m)"
          >
            <view class="placeholder-icon-circle">
              <text class="placeholder-emoji">{{ categoryIcon(m.category) }}</text>
            </view>
            <text class="placeholder-label" :style="{ color: pinColor(m.category) }">暂无图片</text>
          </view>

          <!-- 右栏：文本与属性信息流 -->
          <view class="card-hero-info">
            <view class="card-title-row">
              <text class="card-name">{{ m.name }}</text>
              <text v-if="m.status === '已去'" class="exp-card-status went">已去</text>
            </view>

            <view class="card-meta-row">
              <text class="card-cat" :style="{ color: pinColor(m.category) }">{{ m.category }}</text>
              <text class="card-rate">⭐ {{ m.rating }}分</text>
              <text v-if="m.price" class="card-price">¥{{ m.price }}/人</text>
              <text class="card-scope-badge" :class="'scope-' + m.shareScope">
                {{ shareLabel(m.shareScope) }}
              </text>
            </view>

            <text v-if="m.address" class="card-addr">📍 {{ m.address }}</text>
            <text v-if="m.remark || m.note" class="card-note">💡 {{ m.remark || m.note }}</text>
          </view>
        </view>

        <!-- 下部：主次分明的操作按钮栏（导航 + 编辑 + 删除） -->
        <view class="card-actions">
          <view class="act-btn nav-btn" @click.stop="navigatePlace(m)">
            <text class="btn-ico">🧭</text>
            <text class="btn-txt">导航</text>
          </view>
          <view class="act-btn edit-btn" @click.stop="goMark(m)">
            <text class="btn-ico">✏️</text>
            <text class="btn-txt">编辑</text>
          </view>
          <view class="act-btn del-btn" @click.stop="removeMark(m)">
            <text class="btn-ico">🗑️</text>
            <text class="btn-txt">删除</text>
          </view>
        </view>
      </view>

      <view v-if="filteredMarks.length === 0" class="list-empty">
        <text>暂无匹配标记，点击右下角 ＋ 立即添加</text>
      </view>
    </view>

    <!-- 自由拖拽悬浮添加按钮（默认在卡片上方偏右，支持全屏自由拖拽） -->
    <view
      class="add-fab"
      :class="{ dragging: isFabDragging }"
      :style="{
        transform: 'translate3d(' + fabX + 'px, ' + fabY + 'px, 0)'
      }"
      @touchstart="onFabTouchStart"
      @touchmove.stop.prevent="onFabTouchMove"
      @touchend="onFabTouchEnd"
      @click="onFabClick"
    >
      <text class="add-fab-plus">＋</text>
    </view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { api } from '../../api/index'
import { categoryColor, getNavMetrics, getSystemWindowInfo, sanitizeMarksImages } from '../../utils/helper'

export default {
  data() {
    return {
      nav: {
        statusBarHeight: 44,
        navBarHeight: 44,
        menuRight: 96
      },
      filters: ['全部', '美食', '玩乐', '购物', '自习', '其他'],
      filter: '全部',
      keyword: '',
      view: 'map',
      selected: null,
      stats: { totalMark: 0, went: 0, weedOutRate: 0 },
      centerLat: 31.2989,
      centerLng: 120.6372,
      mapScale: 14,
      isSheetExpanded: false,
      dragStartY: 0,
      fabX: 0,
      fabY: 0,
      isFabDragging: false,
      fabMoved: false,
      fabStart: { touchX: 0, touchY: 0, initX: 0, initY: 0 },
      winW: 375,
      winH: 667
    }
  },
  onLoad() {
    this.nav = getNavMetrics()
    this.initFabPosition()
  },
  mounted() {
    this.initFabPosition()
  },
  computed: {
    filteredMarks() {
      let list = store.getMyMarks()
      if (this.filter !== '全部') {
        list = list.filter(m => m && m.category === this.filter)
      }
      if (this.keyword && this.keyword.trim()) {
        const kw = this.keyword.trim().toLowerCase()
        list = list.filter(m => {
          if (!m) return false
          const n = (m.name || '').toLowerCase()
          const a = (m.address || '').toLowerCase()
          const r = (m.remark || m.note || '').toLowerCase()
          const tags = (m.tags || []).join(' ').toLowerCase()
          return n.includes(kw) || a.includes(kw) || r.includes(kw) || tags.includes(kw)
        })
      }
      return list
    },
    mapMarkers() {
      return this.filteredMarks.map((m, idx) => {
        const lat = Number(m.lat) || 31.2989
        const lng = Number(m.lng) || 120.6372
        const isSelected = this.selected && (String(this.selected.id) === String(m.id))
        return {
          id: idx,
          latitude: lat,
          longitude: lng,
          title: m.name || '标记点',
          width: isSelected ? 32 : 24,
          height: isSelected ? 40 : 30,
          callout: {
            content: (m.name || '地点') + '\n' + (m.category || '') + (m.status === '已去' ? ' · 已去' : ''),
            color: '#1A3B34',
            fontSize: 12,
            borderRadius: 8,
            bgColor: '#FFFFFF',
            padding: 6,
            display: isSelected ? 'ALWAYS' : 'BYCLICK'
          }
        }
      })
    }
  },
  onShow() {
    store.init()
    this.nav = getNavMetrics()
    if (store.marks && store.marks.length > 0) {
      sanitizeMarksImages(store.marks)
    }
    this.refreshMarks()
  },
  methods: {
    pinColor(cat) { return categoryColor(cat) },
    categoryIcon(cat) {
      const map = {
        '美食': '🍽️',
        '玩乐': '🎡',
        '购物': '🛍️',
        '自习': '📚',
        '其他': '📍'
      }
      return map[cat] || '📍'
    },
    placeholderBg(cat) {
      const map = {
        '美食': 'linear-gradient(145deg, #FFF7F0 0%, #FFEBD9 100%)',
        '玩乐': 'linear-gradient(145deg, #FFFDF0 0%, #FEF4CC 100%)',
        '购物': 'linear-gradient(145deg, #F0F8FF 0%, #E0F2FE 100%)',
        '自习': 'linear-gradient(145deg, #F0FAF4 0%, #DDF4E8 100%)',
        '其他': 'linear-gradient(145deg, #F5F7F6 0%, #E7ECE9 100%)'
      }
      return map[cat] || 'linear-gradient(145deg, #F5F7F6 0%, #E7ECE9 100%)'
    },
    placeholderBorder(cat) {
      const map = {
        '美食': 'rgba(255, 138, 61, 0.28)',
        '玩乐': 'rgba(255, 201, 77, 0.32)',
        '购物': 'rgba(63, 167, 224, 0.28)',
        '自习': 'rgba(46, 199, 126, 0.28)',
        '其他': 'rgba(154, 167, 177, 0.28)'
      }
      return map[cat] || 'rgba(154, 167, 177, 0.28)'
    },
    onPlaceholderClick(item) {
      // 静默无操作，不弹出提示
    },
    shareLabel(scope) {
      if (scope === 'private') return '私密'
      if (scope === 'group') return '小组'
      if (scope === 'school') return '本校'
      return '私密'
    },

    // 初始化可拖拽 + 按钮的默认位置（位于底部卡片抽屉上方偏右，绝不遮挡卡片）
    initFabPosition() {
      try {
        const sys = getSystemWindowInfo()
        this.winW = sys.windowWidth || 375
        this.winH = sys.windowHeight || 667
        const fabSize = 54
        // 靠右 16px
        this.fabX = this.winW - fabSize - 16
        // 默认悬浮在抽屉卡片上方约 20px 处（避开抽屉与底部 TabBar）
        this.fabY = this.winH - fabSize - 265
      } catch (e) {
        this.fabX = 300
        this.fabY = 380
      }
    },

    // 拖拽手势监听
    onFabTouchStart(e) {
      if (!e.touches || !e.touches[0]) return
      const t = e.touches[0]
      this.isFabDragging = true
      this.fabMoved = false
      this.fabStart = {
        touchX: t.clientX,
        touchY: t.clientY,
        initX: this.fabX,
        initY: this.fabY
      }
    },
    onFabTouchMove(e) {
      if (!this.isFabDragging || !e.touches || !e.touches[0]) return
      const t = e.touches[0]
      const dx = t.clientX - this.fabStart.touchX
      const dy = t.clientY - this.fabStart.touchY

      if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
        this.fabMoved = true
      }

      const fabSize = 54
      const minX = 8
      const maxX = this.winW - fabSize - 8
      const minY = (this.nav.statusBarHeight || 44) + 44 + 8
      const maxY = this.winH - fabSize - 65

      let nx = this.fabStart.initX + dx
      let ny = this.fabStart.initY + dy

      nx = Math.max(minX, Math.min(maxX, nx))
      ny = Math.max(minY, Math.min(maxY, ny))

      this.fabX = nx
      this.fabY = ny
    },
    onFabTouchEnd() {
      this.isFabDragging = false
    },
    onFabClick() {
      if (this.fabMoved) return
      this.goMark(null)
    },

    // 抽屉面板交互控制
    toggleSheet() {
      this.isSheetExpanded = !this.isSheetExpanded
    },
    expandSheet() {
      this.isSheetExpanded = true
    },
    collapseSheet() {
      this.isSheetExpanded = false
    },
    onDragStart(e) {
      if (e.touches && e.touches[0]) {
        this.dragStartY = e.touches[0].clientY
      }
    },
    onDragEnd(e) {
      if (e.changedTouches && e.changedTouches[0] && this.dragStartY) {
        const diff = this.dragStartY - e.changedTouches[0].clientY
        // 上滑超过 35px -> 展开长高
        if (diff > 35) {
          this.isSheetExpanded = true
          } else if (diff < -35) {
          // 下滑超过 35px -> 收起
          this.isSheetExpanded = false
        }
      }
      this.dragStartY = 0
    },

    // 在长高列表中点击某一个地点卡片
    selectMarkInSheet(m) {
      this.selected = m
      if (m.lat && m.lng) {
        this.centerLat = Number(m.lat)
        this.centerLng = Number(m.lng)
      }
    },

    // 图片加载异常智能容错降级
    onImageError(m) {
      if (!m) return
      this.$set ? this.$set(m, '_imgErr', true) : (m._imgErr = true)
      if (Array.isArray(m.images)) {
        m.images = m.images.filter(img => img && !img.includes('/tmp/') && !img.includes('tmp_'))
        store.marks = (store.marks || []).map(item => String(item.id) === String(m.id) ? { ...item, images: m.images } : item)
        uni.setStorageSync('fw_marks', store.marks)
      }
    },
    onCardPhotoError(selected, idx) {
      if (!selected || !Array.isArray(selected.images)) return
      selected.images.splice(idx, 1)
      if (selected.images.length === 0) {
        this.$set ? this.$set(selected, '_imgErr', true) : (selected._imgErr = true)
      }
    },

    // 动态刷新标记列表
    async refreshMarks() {
      try {
        const res = await api.getMarks()
        if (res && Array.isArray(res.data)) {
          store.marks = res.data
          uni.setStorageSync('fw_marks', res.data)
        }
      } catch (e) {
        console.warn('refreshMarks fail', e)
      }
      if (store.marks && store.marks.length > 0) {
        sanitizeMarksImages(store.marks)
      }
      this.stats = store.getStats()
      this.autoFocus()
    },

    // 自动聚焦中心
    autoFocus() {
      const list = this.filteredMarks
      if (list.length > 0) {
        const target = this.selected || list[0]
        if (target && target.lat && target.lng) {
          this.centerLat = Number(target.lat)
          this.centerLng = Number(target.lng)
          this.selected = target
        }
      }
    },

    // 移动到我的当前位置
    moveToMyLocation() {
      uni.getLocation({
        type: 'gcj02',
        success: (res) => {
          this.centerLat = res.latitude
          this.centerLng = res.longitude
          uni.showToast({ title: '已定位到当前位置', icon: 'none' })
        },
        fail: () => {
          this.autoFocus()
        }
      })
    },

    // 地图 Marker 点击
    onMarkerTap(e) {
      const idx = e.detail ? (e.detail.markerId !== undefined ? e.detail.markerId : e.markerId) : 0
      const m = this.filteredMarks[idx]
      if (m) {
        this.selected = m
        this.centerLat = Number(m.lat) || this.centerLat
        this.centerLng = Number(m.lng) || this.centerLng
      }
    },

    selectMark(m) {
      this.selected = m
      if (m.lat && m.lng) {
        this.centerLat = Number(m.lat)
        this.centerLng = Number(m.lng)
      }
      this.view = 'map'
    },

    // 调起腾讯地图官方原路导航
    navigatePlace(m) {
      if (!m || !m.lat || !m.lng) {
        uni.showToast({ title: '无有效经纬度', icon: 'none' })
        return
      }
      uni.openLocation({
        latitude: Number(m.lat),
        longitude: Number(m.lng),
        name: m.name,
        address: m.address || m.name,
        scale: 16
      })
    },

    // 预览照片
    previewPhoto(current, all = []) {
      const urls = all && all.length > 0 ? all : [current]
      uni.previewImage({ urls, current })
    },

    goMark(m) {
      uni.navigateTo({ url: '/pages/mark/mark' + (m ? '?id=' + m.id : '') })
    },

    useInWheel(m) {
      uni.navigateTo({ url: '/pages/wheel/wheel' })
    },

    removeMark(m) {
      uni.showModal({
        title: '删除标记',
        content: '确定删除地点「' + m.name + '」吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({ title: '删除中…' })
              await api.deleteMark(m.id)
              uni.hideLoading()
              this.selected = null
              this.stats = store.getStats()
              uni.showToast({ title: '已删除', icon: 'none' })
            } catch (e) {
              uni.hideLoading()
              store.removeMark(m.id)
              this.selected = null
              this.stats = store.getStats()
              uni.showToast({ title: '已删除', icon: 'none' })
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.map-page {
  height: 100vh;
  max-height: 100vh;
  overflow: hidden;
  background: #F5F9F7;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 顶部极简导航 */
.map-head {
  background: linear-gradient(150deg, #E6F5F0 0%, #D8EFE8 100%);
  padding-top: calc(env(safe-area-inset-top, 44px) + 8px);
  padding-left: 32rpx;
  padding-right: 32rpx;
  padding-bottom: 20rpx;
  border-bottom: 1.5rpx solid rgba(225, 237, 232, 0.8);
}
.map-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-sizing: border-box;
}
.title-brand {
  display: flex;
  align-items: center;
  gap: 16rpx;
}
.map-title {
  font-size: 38rpx;
  font-weight: 900;
  color: #14352D;
  letter-spacing: 1rpx;
}
.map-badge {
  font-size: 22rpx;
  font-weight: 700;
  color: #1F6E5F;
  background: #FFFFFF;
  padding: 6rpx 18rpx;
  border-radius: 999rpx;
  box-shadow: 0 4rpx 10rpx rgba(31, 110, 95, 0.08);
  border: 1rpx solid rgba(31, 110, 95, 0.15);
}

/* 搜索栏 + 模式切换二合一 */
.search-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 32rpx 0;
}
.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 14rpx;
  background: #FFFFFF;
  border-radius: 999rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  padding: 16rpx 26rpx;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.04);
}
.search-icon { font-size: 28rpx; opacity: 0.8; }
.search-input { flex: 1; font-size: 26rpx; font-weight: 500; color: #14352D; }
.search-clear { font-size: 26rpx; color: #8EA49D; padding: 4rpx 8rpx; }
.ph { color: #8EA49D; font-weight: normal; }

.mode-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  background: #FFFFFF;
  border-radius: 999rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  padding: 16rpx 26rpx;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.06);
  transition: transform 0.15s;
}
.mode-btn:active {
  transform: scale(0.95);
}
.mode-ico { font-size: 26rpx; }
.mode-label { font-size: 24rpx; font-weight: 800; color: #1F6E5F; }

/* 分类微胶囊横滑条 */
.filter-bar {
  white-space: nowrap;
  padding: 16rpx 32rpx 0;
  width: 100%;
  box-sizing: border-box;
}
.filter-chip {
  display: inline-flex;
  padding: 10rpx 28rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 600;
  background: #FFFFFF;
  color: #5E7A71;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  margin-right: 14rpx;
  box-shadow: 0 4rpx 12rpx rgba(27, 86, 73, 0.03);
  transition: all 0.2s;
}
.filter-chip.on {
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  font-weight: 700;
  border-color: transparent;
  box-shadow: 0 6rpx 18rpx rgba(27, 86, 73, 0.25);
}

/* 沉浸式大地图区域（自适应撑满剩余高度，刚好一屏） */
.map-section {
  flex: 1;
  min-height: 0;
  padding: 12rpx 20rpx 14rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}
.real-map-container {
  position: relative;
  width: 100%;
  height: 100%;
  flex: 1;
  min-height: 0;
  border-radius: 32rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  overflow: hidden;
  box-shadow: 0 10rpx 36rpx rgba(27, 86, 73, 0.08);
}
.real-map {
  width: 100%;
  height: calc(100% + 80rpx);
  margin-bottom: -80rpx;
}
.map-logo-mask {
  position: absolute;
  left: 0;
  bottom: 0;
  width: 220rpx;
  height: 70rpx;
  background: #FFFFFF;
  z-index: 15;
  pointer-events: none;
}
.map-locate-btn {
  position: absolute;
  right: 24rpx;
  top: 24rpx;
  width: 76rpx;
  height: 76rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 6rpx 20rpx rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  z-index: 10;
  transition: transform 0.15s;
}
.map-locate-btn:active {
  transform: scale(0.92);
}

/* 还原小药丸按钮 */
.sheet-restore-pill {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  bottom: 24rpx;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(16rpx);
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  padding: 14rpx 32rpx;
  border-radius: 999rpx;
  box-shadow: 0 8rpx 28rpx rgba(20, 53, 45, 0.16);
  display: flex;
  align-items: center;
  gap: 12rpx;
  z-index: 25;
  transition: transform 0.15s;
}
.sheet-restore-pill:active {
  transform: translateX(-50%) scale(0.95);
}
.restore-icon { font-size: 28rpx; }
.restore-text { font-size: 24rpx; font-weight: 700; color: #1F6E5F; }

/* ============================================================
   核心抽屉面板（常驻底部不可关闭，支持精简卡片与长高可滚动列表丝滑拉伸动画）
   ============================================================ */
.map-bottom-sheet {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 388rpx;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(24rpx);
  border-top-left-radius: 36rpx;
  border-top-right-radius: 36rpx;
  border-bottom-left-radius: 0;
  border-bottom-right-radius: 0;
  border-top: 1.5rpx solid rgba(225, 237, 232, 0.95);
  box-shadow: 0 -10rpx 40rpx rgba(20, 53, 45, 0.12);
  z-index: 30;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: height 0.36s cubic-bezier(0.25, 1, 0.5, 1), box-shadow 0.36s ease;
}
.map-bottom-sheet.expanded {
  height: 54vh;
  box-shadow: 0 -16rpx 50rpx rgba(20, 53, 45, 0.22);
}

.sheet-content-stage {
  position: relative;
  flex: 1;
  width: 100%;
  overflow: hidden;
}

/* 顶部拖拽手柄条 */
.sheet-drag-handle-bar {
  padding: 14rpx 20rpx 8rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  background: #F9FCFA;
  border-bottom: 1rpx solid rgba(225, 237, 232, 0.7);
  cursor: pointer;
}
.drag-handle-line {
  width: 72rpx;
  height: 8rpx;
  border-radius: 999rpx;
  background: #C4D7D0;
}
.drag-hint-row {
  display: flex;
  align-items: center;
}
.drag-hint-text {
  font-size: 18rpx;
  font-weight: 600;
  color: #7B938B;
  letter-spacing: 0.5rpx;
}

/* 1. 精简单卡态样式（图文双栏黄金分割排版） */
.sheet-compact-view {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16rpx 28rpx 20rpx;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: opacity 0.28s ease, transform 0.32s cubic-bezier(0.25, 1, 0.5, 1);
}
.sheet-compact-view.view-active {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}
.sheet-compact-view.view-hidden {
  opacity: 0;
  transform: translateY(-24rpx);
  pointer-events: none;
}

/* 图文英雄区（左图右文） */
.card-hero-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  width: 100%;
}
.card-hero-info,
.card-hero-left {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}
.card-title-row {
  display: flex;
  align-items: center;
}
.card-name {
  font-size: 34rpx;
  font-weight: 900;
  color: #14352D;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: 0.5rpx;
}
.card-meta-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
  font-size: 22rpx;
}
.card-cat {
  font-weight: 800;
}
.card-rate {
  color: #D48806;
  font-weight: 700;
}
.card-price {
  color: #1F6E5F;
  font-weight: 700;
}
.card-scope-badge {
  font-size: 18rpx;
  font-weight: 600;
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
}
.card-scope-badge.scope-private { color: #8EA49D; background: #F0F5F2; }
.card-scope-badge.scope-group { color: #3FA7E0; background: #EBF4FD; }
.card-scope-badge.scope-school { color: #1F8A65; background: #EEF8F3; }

.card-addr {
  font-size: 22rpx;
  color: #7B938B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-note {
  font-size: 22rpx;
  color: #446158;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 左侧封面照片与占位图 */
.card-hero-thumb-box {
  position: relative;
  width: 154rpx;
  height: 154rpx;
  border-radius: 20rpx;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow: 0 6rpx 18rpx rgba(27, 86, 73, 0.12);
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  background: #EAF3EF;
}
.card-hero-thumb {
  width: 100%;
  height: 100%;
  border-radius: 20rpx;
  display: block;
}
.thumb-count-pill {
  position: absolute;
  right: 8rpx;
  bottom: 8rpx;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(8rpx);
  color: #FFFFFF;
  font-size: 18rpx;
  font-weight: 600;
  padding: 2rpx 10rpx;
  border-radius: 999rpx;
}

/* 精简卡片专属高颜值占位图样式 */
.card-hero-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  cursor: pointer;
  transition: transform 0.15s ease;
}
.card-hero-placeholder:active {
  transform: scale(0.96);
}
.placeholder-icon-circle {
  width: 78rpx;
  height: 78rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}
.placeholder-emoji {
  font-size: 42rpx;
  line-height: 1;
}
.placeholder-label {
  font-size: 18rpx;
  font-weight: 700;
  letter-spacing: 0.5rpx;
}

/* 展开抽屉列表项占位图样式 */
.exp-card-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
}
.exp-ph-emoji {
  font-size: 40rpx;
  line-height: 1;
}

/* 列表模式项目占位图样式 */
.item-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  border-radius: 18rpx;
}
.item-ph-emoji {
  font-size: 42rpx;
  line-height: 1;
}
.item-ph-label {
  font-size: 16rpx;
  font-weight: 700;
  line-height: 1;
}

/* 底部操作按钮栏 */
.card-actions {
  display: flex;
  align-items: center;
  gap: 16rpx;
  width: 100%;
  margin-top: 10rpx;
}
.act-btn {
  height: 68rpx;
  border-radius: 999rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  font-size: 24rpx;
  font-weight: 700;
  transition: transform 0.15s, opacity 0.15s;
  box-sizing: border-box;
}
.act-btn:active {
  transform: scale(0.96);
  opacity: 0.9;
}
.btn-ico { font-size: 24rpx; }
.btn-txt { letter-spacing: 0.5rpx; }

.nav-btn {
  flex: 1.4;
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  box-shadow: 0 6rpx 18rpx rgba(27, 86, 73, 0.28);
}
.edit-btn {
  flex: 1;
  background: #EEF8F3;
  color: #1F6E5F;
  border: 1rpx solid rgba(31, 110, 95, 0.15);
}
.wheel-btn {
  flex: 1;
  background: #FFF8EC;
  color: #D48806;
  border: 1rpx solid rgba(212, 136, 6, 0.15);
}
.del-btn {
  flex: 1;
  background: #FDF2F2;
  color: #D04547;
  border: 1rpx solid rgba(208, 69, 71, 0.15);
}

/* 2. 长高展开列表态样式（支持平滑从下淡入） */
.sheet-expanded-view {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 16rpx 20rpx 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  transition: opacity 0.28s ease, transform 0.32s cubic-bezier(0.25, 1, 0.5, 1);
}
.sheet-expanded-view.view-active {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}
.sheet-expanded-view.view-hidden {
  opacity: 0;
  transform: translateY(30rpx);
  pointer-events: none;
}
.expanded-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8rpx 14rpx;
  border-bottom: 1.5rpx solid #F0F5F2;
}
.expanded-title-box {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
}
.exp-title {
  font-size: 30rpx;
  font-weight: 900;
  color: #14352D;
}
.exp-count {
  font-size: 22rpx;
  color: #7B938B;
  font-weight: 600;
}
.exp-collapse-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  background: #F0F6F3;
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  font-size: 22rpx;
  font-weight: 700;
  color: #1F6E5F;
}
.exp-collapse-btn:active {
  transform: scale(0.95);
}
.exp-collapse-ico { font-size: 18rpx; }

/* 垂直滚动区域 */
.expanded-scroll-area {
  flex: 1;
  height: 0;
  padding-top: 14rpx;
  box-sizing: border-box;
}
.exp-mark-card {
  display: flex;
  flex-direction: column;
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  padding: 22rpx 24rpx 20rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 6rpx 20rpx rgba(20, 53, 45, 0.05);
  transition: all 0.2s;
  box-sizing: border-box;
}
.exp-mark-card:active {
  transform: scale(0.985);
}
.exp-mark-card.exp-active-card {
  border: 2rpx solid #1F6E5F;
  background: #F4FAF7;
  box-shadow: 0 8rpx 28rpx rgba(31, 110, 95, 0.14);
}
.exp-card-thumb {
  width: 104rpx;
  height: 104rpx;
  border-radius: 16rpx;
  flex-shrink: 0;
  background: #EAF3EF;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
}
.exp-card-dot {
  width: 10rpx;
  height: 80rpx;
  border-radius: 999rpx;
  flex-shrink: 0;
}
.exp-card-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}
.exp-card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}
.exp-card-name {
  font-size: 28rpx;
  font-weight: 800;
  color: #14352D;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}
.exp-card-status {
  font-size: 18rpx;
  font-weight: 700;
  color: #FF7D42;
  background: #FFF3EB;
  padding: 4rpx 14rpx;
  border-radius: 999rpx;
  white-space: nowrap;
  flex-shrink: 0;
  line-height: 1.3;
}
.exp-card-status.went { color: #1F8A65; background: #EEF8F3; }
.exp-card-addr {
  font-size: 20rpx;
  color: #7B938B;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.exp-card-meta {
  display: flex;
  align-items: center;
  gap: 14rpx;
  font-size: 20rpx;
}
.exp-cat-badge { font-weight: 700; }
.exp-rate { color: #D48806; font-weight: 600; }
.exp-price { color: #1F6E5F; font-weight: 700; }

.exp-card-actions {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
  flex-shrink: 0;
}
.exp-act-btn {
  padding: 8rpx 18rpx;
  border-radius: 999rpx;
  font-size: 20rpx;
  font-weight: 700;
  text-align: center;
}
.exp-nav {
  background: #1F6E5F;
  color: #FFFFFF;
}
.exp-edit {
  background: #EEF8F3;
  color: #1F6E5F;
}
.expanded-scroll-bottom-padding {
  height: 30rpx;
}

/* 原生列表视图样式（弹性滚动，保持整页刚好一屏） */
.mark-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 16rpx 28rpx 40rpx;
  box-sizing: border-box;
}
.mark-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.85);
  padding: 22rpx;
  margin-bottom: 18rpx;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.04);
  transition: transform 0.15s;
}
.mark-item:active {
  transform: scale(0.98);
}
.item-thumb {
  width: 124rpx;
  height: 124rpx;
  border-radius: 18rpx;
  flex-shrink: 0;
}
.mark-dot {
  flex: 0 0 12rpx;
  width: 12rpx;
  height: 80rpx;
  border-radius: 999rpx;
}
.mark-body { flex: 1; display: flex; flex-direction: column; gap: 8rpx; min-width: 0; }
.mark-name-row { display: flex; align-items: center; gap: 12rpx; }
.mark-name {
  font-size: 30rpx;
  font-weight: 800;
  color: #14352D;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.mark-status {
  font-size: 20rpx;
  font-weight: 700;
  color: #FF7D42;
  background: #FFF3EB;
  padding: 2rpx 12rpx;
  border-radius: 999rpx;
}
.mark-status.went { color: #1F8A65; background: #EEF8F3; }
.mark-addr { font-size: 22rpx; color: #7B938B; }
.mark-tip { font-size: 22rpx; color: #446158; line-height: 1.4; }
.mark-tags { display: flex; gap: 10rpx; flex-wrap: wrap; margin-top: 4rpx; }
.tag {
  font-size: 20rpx;
  font-weight: 600;
  color: #5E7A71;
  background: #EEF5F2;
  padding: 2rpx 14rpx;
  border-radius: 999rpx;
}
.cat-tag { background: #FFF3EB; color: #FF7D42; font-weight: 700; }
.item-right { display: flex; flex-direction: column; align-items: flex-end; gap: 16rpx; }
.mark-share {
  font-size: 20rpx;
  font-weight: 600;
  padding: 4rpx 16rpx;
  border-radius: 999rpx;
}
.mark-share.scope-private { color: #8EA49D; background: #F0F5F2; }
.mark-share.scope-group { color: #3FA7E0; background: #EBF4FD; }
.mark-share.scope-school { color: #1F8A65; background: #EEF8F3; }
.item-nav-icon { font-size: 36rpx; padding: 4rpx; }

.list-empty { text-align: center; color: #8EA49D; font-size: 26rpx; padding: 80rpx 0; }

.add-fab {
  position: fixed;
  left: 0;
  top: 0;
  width: 108rpx;
  height: 108rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #248875, #1B6557);
  color: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 14rpx 38rpx rgba(27, 86, 73, 0.38), 0 2rpx 6rpx rgba(255, 255, 255, 0.35) inset;
  z-index: 60;
  border: 2rpx solid rgba(255, 255, 255, 0.25);
  transition: box-shadow 0.2s, transform 0.04s linear;
  touch-action: none;
}
.add-fab.dragging {
  transition: none;
  box-shadow: 0 22rpx 54rpx rgba(20, 53, 45, 0.48);
  opacity: 0.95;
  transform: scale(1.06);
}
.add-fab:active {
  transform: scale(0.94);
}
.add-fab-plus {
  font-size: 60rpx;
  font-weight: 300;
  line-height: 1;
  margin-top: -4rpx;
}
</style>
