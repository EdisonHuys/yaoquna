<template>
  <view class="fw-tabbar">
    <view
      v-for="t in tabs"
      :key="t.key"
      class="fw-tab"
      :class="{ active: active === t.key }"
      @click="onTab(t)"
    >
      <view class="fw-tab-icon-wrap">
        <image
          class="fw-tab-icon"
          :src="active === t.key ? t.activeIcon : t.icon"
          mode="aspectFit"
        />
        <view v-if="active === t.key" class="fw-tab-active-dot"></view>
      </view>
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
        {
          key: 'index',
          label: '首页',
          icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0IiBoZWlnaHQ9IjI0IiBmaWxsPSJub25lIiBzdHJva2U9IiM4RUE0OUQiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48cGF0aCBkPSJNMyAxMC4yTDEyIDNsOSA3LjJ2OS4zYTEuNSAxLjUgMCAwIDEtMS41IDEuNUg0LjVBMS41IDEuNSAwIDAgMSAzIDE5LjV2LTkuM3oiLz48cGF0aCBkPSJNOSAyMXYtN2g2djciLz48L3N2Zz4=',
          activeIcon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0IiBoZWlnaHQ9IjI0IiBmaWxsPSIjMUY2RTVGIj48cGF0aCBkPSJNMTIgMi41TDIuNSAxMC4xQTEuNSAxLjUgMCAwIDAgMiAxMS4yN3Y4LjIzQTEuNSAxLjUgMCAwIDAgMy41IDIxaDVhMSAxIDAgMCAwIDEtMXYtNS41YS41LjUgMCAwIDEgLjUtLjVoNGEuNS41IDAgMCAxIC41LjVWMjBhMSAxIDAgMCAwIDEgMWg1YTEuNSAxLjUgMCAwIDAgMS41LTEuNXYtOC4yM2ExLjUgMS41IDAgMCAwLS41LTEuMTdMMTIgMi41eiIvPjwvc3ZnPg==',
          url: '/pages/index/index'
        },
        {
          key: 'map',
          label: '地图',
          icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0IiBoZWlnaHQ9IjI0IiBmaWxsPSJub25lIiBzdHJva2U9IiM4RUE0OUQiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48cG9seWdvbiBwb2ludHM9IjEgNiAxIDIyIDggMTggMTYgMjIgMjMgMTggMjMgMiAxNiA2IDggMiAxIDYiLz48bGluZSB4MT0iOCIgeTE9IjIiIHgyPSI4IiB5Mj0iMTgiLz48bGluZSB4MT0iMTYiIHkxPSI2IiB4Mj0iMTYiIHkyPSIyMiIvPjwvc3ZnPg==',
          activeIcon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0IiBoZWlnaHQ9IjI0IiBmaWxsPSIjMUY2RTVGIj48cGF0aCBkPSJNMi41IDUuNWw1LjUtMi4ydjE1LjJsLTUuNSAyLjJhMSAxIDAgMCAxLTEuNS0uOVY2LjRhMSAxIDAgMCAxIDEuNS0uOXoiIGZpbGw9IiMxRjZFNUYiIGZpbGwtb3BhY2l0eT0iMC44NSIvPjxwYXRoIGQ9Ik04IDMuM2w4IDMuMnYxNS4ybC04LTMuMlYzLjN6IiBmaWxsPSIjMUY2RTVGIi8+PHBhdGggZD0iTTE2IDYuNWw1LjUtMi4yYTEgMSAwIDAgMSAxLjUuOXYxNC40YTEgMSAwIDAgMS0xLjUuOUwxNiAxOC4zVjYuNXoiIGZpbGw9IiMxRjZFNUYiIGZpbGwtb3BhY2l0eT0iMC44NSIvPjwvc3ZnPg==',
          url: '/pages/map/map'
        },
        {
          key: 'mine',
          label: '我的',
          icon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0IiBoZWlnaHQ9IjI0IiBmaWxsPSJub25lIiBzdHJva2U9IiM4RUE0OUQiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48Y2lyY2xlIGN4PSIxMiIgY3k9IjciIHI9IjQiLz48cGF0aCBkPSJNNCAyMXYtMS41QTUuNSA1LjUgMCAwIDEgOS41IDE0aDVhNS41IDUuNSAwIDAgMSA1LjUgNS41VjIxIi8+PC9zdmc+',
          activeIcon: 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCIgd2lkdGg9IjI0IiBoZWlnaHQ9IjI0IiBmaWxsPSIjMUY2RTVGIj48Y2lyY2xlIGN4PSIxMiIgY3k9IjciIHI9IjQuNSIvPjxwYXRoIGQ9Ik0zLjUgMjAuNXYtMWE2IDYgMCAwIDEgNi02aDVhNiA2IDAgMCAxIDYgNnYxYS41LjUgMCAwIDEtLjUuNWgtMTZhLjUuNSAwIDAgMS0uNS0uNXoiLz48L3N2Zz4=',
          url: '/pages/mine/mine'
        }
      ]
    }
  },
  methods: {
    onTab(t) {
      if (t.key === this.active) return
      uni.switchTab({ url: t.url })
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
  box-sizing: content-box;
  height: 116rpx;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
  background: rgba(255, 255, 255, 0.96);
  border-top: 1.5rpx solid rgba(225, 237, 232, 0.9);
  box-shadow: 0 -8rpx 32rpx rgba(27, 86, 73, 0.05);
  display: flex;
  align-items: center;
  backdrop-filter: blur(24rpx);
}
.fw-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  position: relative;
  height: 100%;
  transition: opacity 0.15s;
}
.fw-tab:active {
  opacity: 0.7;
}
.fw-tab-icon-wrap {
  position: relative;
  width: 52rpx;
  height: 52rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}
.fw-tab-icon {
  width: 44rpx;
  height: 44rpx;
  transition: transform 0.25s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
.fw-tab.active .fw-tab-icon {
  transform: translateY(-2rpx) scale(1.08);
}
.fw-tab-active-dot {
  position: absolute;
  bottom: -4rpx;
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #1F6E5F;
  box-shadow: 0 2rpx 6rpx rgba(31, 110, 95, 0.4);
}
.fw-tab-text {
  font-size: 20rpx;
  font-weight: 500;
  color: #8EA49D;
  transition: color 0.2s, font-weight 0.2s;
  letter-spacing: 0.5rpx;
  line-height: 28rpx;
  height: 28rpx;
}
.fw-tab.active .fw-tab-text {
  color: #1F6E5F;
  font-weight: 800;
}
</style>
