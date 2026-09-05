<template>
  <view class="fw-tabbar">
    <view
      v-for="t in tabs"
      :key="t.key"
      class="fw-tab"
      :class="{ active: active === t.key }"
      @click="onTab(t)"
    >
      <text class="fw-tab-icon">{{ t.icon }}</text>
      <text class="fw-tab-text">{{ t.label }}</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'FwTabbar',
  props: {
    active: { type: String, default: 'index' } // index | map | mine
  },
  data() {
    return {
      tabs: [
        { key: 'index', label: '首页', icon: '🏠', url: '/pages/index/index' },
        { key: 'map', label: '地图', icon: '🗺️', url: '/pages/map/map' },
        { key: 'mine', label: '我的', icon: '👤', url: '/pages/mine/mine' }
      ]
    }
  },
  methods: {
    onTab(t) {
      if (t.key === this.active) return
      uni.reLaunch({ url: t.url })
    }
  }
}
</script>

<style scoped>
.fw-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  height: 120rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -8rpx 30rpx rgba(31, 110, 95, 0.06);
  display: flex;
  backdrop-filter: blur(12rpx);
}
.fw-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
}
.fw-tab-icon {
  font-size: 40rpx;
  line-height: 1;
  filter: grayscale(0.4);
  opacity: 0.75;
}
.fw-tab-text {
  font-size: 22rpx;
  color: #A9B8B2;
}
.fw-tab.active .fw-tab-icon {
  filter: none;
  opacity: 1;
  transform: translateY(-4rpx);
  transition: transform 0.2s;
}
.fw-tab.active .fw-tab-text {
  color: #1F6E5F;
  font-weight: 600;
}
</style>
