<template>
  <view class="fw-wheel-box">
    <!-- 定位针 -->
    <view class="fw-pin">
      <view class="fw-pin-dot"></view>
    </view>

    <!-- 转盘 -->
    <view
      class="fw-wheel"
      :style="{
        background: conicStyle,
        transform: 'rotate(' + rotateDeg + 'deg)'
      }"
    >
      <!-- 分段文字 -->
      <view
        v-for="(seg, i) in segments"
        :key="i"
        class="fw-seg"
        :style="{ transform: 'rotate(' + seg.deg + 'deg)' }"
      >
        <text class="fw-seg-text" :style="{ color: segTextColor(i) }">{{ seg.label }}</text>
      </view>

      <!-- 中心按钮 -->
      <view class="fw-center" @click="onCenterTap">
        <text class="fw-center-t1">{{ centerText }}</text>
        <text class="fw-center-t2">{{ centerSub }}</text>
      </view>
    </view>
  </view>
</template>

<script>
const COLORS = ['#FF8A3D', '#FFC94D', '#2EC77E', '#3FA7E0', '#F06E8B', '#8A6FE8']

export default {
  name: 'FwWheel',
  props: {
    pool: { type: Array, default: () => [] }, // [{name, color?}]
    spinning: { type: Boolean, default: false }
  },
  data() {
    return {
      rotateDeg: 0,
      spinCount: 0
    }
  },
  computed: {
    segments() {
      const n = this.pool.length || 1
      const seg = 360 / n
      return this.pool.map((item, i) => ({
        label: item.name,
        deg: i * seg + seg / 2 - 90 // 文字沿半径朝外
      }))
    },
    conicStyle() {
      const n = this.pool.length
      if (n === 0) return 'conic-gradient(#E5E7EB 0deg 360deg)' // 空转盘：灰色占位
      const seg = 360 / n
      const parts = []
      for (let i = 0; i < n; i++) {
        const from = i * seg
        const to = (i + 1) * seg
        const color = this.pool[i].color || COLORS[i % COLORS.length]
        parts.push(`${color} ${from}deg ${to}deg`)
      }
      return `conic-gradient(${parts.join(',')})`
    },
    centerText() {
      if (this.pool.length === 0) return '空空'
      return this.spinning ? '…' : '转动'
    },
    centerSub() {
      return this.spinning ? '命运ing' : 'GO'
    }
  },
  watch: {
    spinning(v) {
      if (v) this.spin()
    }
  },
  methods: {
    segTextColor(i) {
      return '#FFFFFF'
    },
    onCenterTap() {
      if (this.spinning) return
      if (this.pool.length === 0) {
        uni.showToast({ title: '先标记一些地点吧', icon: 'none' })
        return
      }
      this.$emit('spin')
    },
    // 由父组件在数据就绪后调用：旋转到随机结果
    spin() {
      const n = this.pool.length
      if (n === 0) return
      this.spinCount++
      const target = Math.floor(Math.random() * n)
      // 让指针（顶部0度）停在目标扇区中心
      const seg = 360 / n
      const targetDeg = -((target * seg) + seg / 2)
      // 多圈 + 随机偏移
      const rounds = 5 + Math.floor(Math.random() * 3)
      this.rotateDeg = this.rotateDeg + rounds * 360 + (targetDeg - (this.rotateDeg % 360))
      this.$emit('result', { index: target, item: this.pool[target] })
    },
    reset() {
      this.rotateDeg = 0
      this.spinCount = 0
    }
  }
}
</script>

<style scoped>
.fw-wheel-box {
  position: relative;
  width: 560rpx;
  height: 560rpx;
  margin: 0 auto;
}
.fw-pin {
  position: absolute;
  top: -24rpx;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  width: 0;
  height: 0;
  border-left: 24rpx solid transparent;
  border-right: 24rpx solid transparent;
  border-top: 44rpx solid #FFFFFF;
  filter: drop-shadow(0 4rpx 8rpx rgba(0,0,0,0.15));
}
.fw-pin-dot {
  position: absolute;
  top: -52rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 22rpx;
  height: 22rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 2rpx 6rpx rgba(0,0,0,0.15);
}
.fw-wheel {
  position: relative;
  width: 560rpx;
  height: 560rpx;
  border-radius: 50%;
  border: 14rpx solid #FFFFFF;
  box-shadow: 0 20rpx 60rpx rgba(31, 110, 95, 0.18);
  transition: transform 3.6s cubic-bezier(0.12, 0.8, 0.16, 1);
  overflow: hidden;
  box-sizing: border-box;
}
.fw-seg {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100%;
  height: 100%;
  transform-origin: 0 0;
  display: flex;
  justify-content: center;
}
.fw-seg-text {
  position: absolute;
  left: 28rpx;
  top: 12rpx;
  font-size: 30rpx;
  font-weight: 700;
  transform: rotate(90deg);
  white-space: nowrap;
  text-shadow: 0 2rpx 4rpx rgba(0,0,0,0.12);
}
.fw-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 170rpx;
  height: 170rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 8rpx 24rpx rgba(31, 110, 95, 0.18);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 5;
}
.fw-center-t1 {
  font-size: 34rpx;
  font-weight: 700;
  color: #1F6E5F;
}
.fw-center-t2 {
  font-size: 22rpx;
  color: #A9B8B2;
  margin-top: 2rpx;
}
</style>
