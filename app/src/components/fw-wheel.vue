<template>
  <view class="fw-wheel-box">
    <!-- 顶部高亮指针（带物理指针微颤动画） -->
    <view class="fw-pointer" :class="{ 'is-spinning': isRotating }">
      <view class="pointer-cap"></view>
      <view class="pointer-arrow"></view>
    </view>

    <!-- 外圈发光轮框与跑马灯灯珠 -->
    <view class="fw-wheel-ring" :class="{ 'is-spinning': isRotating }">
      <!-- 装饰灯珠（交替闪烁跑马灯） -->
      <view
        v-for="d in 16"
        :key="d"
        class="wheel-light-dot"
        :class="d % 2 === 0 ? 'dot-even' : 'dot-odd'"
        :style="{ transform: 'rotate(' + (d * 22.5) + 'deg) translateY(-276rpx)' }"
      ></view>

      <!-- 旋转盘体（采用自然物理惯性平滑加减速曲线，拒绝急刹与慢爬） -->
      <view
        class="fw-wheel-plate"
        :style="{
          background: conicStyle,
          transform: 'rotate(' + rotateDeg + 'deg)',
          transition: allowTransition ? 'transform 3.2s cubic-bezier(0.25, 0.1, 0.15, 1)' : 'none'
        }"
      >
        <!-- 扇区图文 -->
        <view
          v-for="(seg, i) in segments"
          :key="i"
          class="wheel-slot"
          :style="{ transform: 'rotate(' + seg.deg + 'deg)' }"
        >
          <view class="wheel-slot-content">
            <text class="slot-icon">{{ seg.icon }}</text>
            <text class="slot-text">{{ seg.label }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 独立中心 GO 按钮（不随转盘旋转，始终端正平稳） -->
    <view
      class="fw-center-btn"
      :class="{ 'btn-active': isRotating }"
      @click="onCenterTap"
    >
      <view class="center-btn-inner">
        <text class="center-btn-title">{{ isRotating ? '…' : 'GO' }}</text>
        <text class="center-btn-sub">{{ isRotating ? '抽选中' : '转动' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
const THEME_COLORS = [
  '#FF6E42', // 珊瑚暖橙
  '#3AA7F8', // 晴空海蓝
  '#28C78B', // 清新薄荷
  '#FFAE1A', // 琥珀晶黄
  '#8F65F8', // 幻彩星紫
  '#F75276', // 樱花甜粉
  '#0EA5E9', // 蔚蓝天际
  '#10B981'  // 翠玉浓绿
]

const CAT_ICONS = {
  '美食': '🍜',
  '玩乐': '🎡',
  '购物': '🛍️',
  '自习': '📚',
  '其他': '📍'
}

export default {
  name: 'FwWheel',
  props: {
    pool: { type: Array, default: () => [] },
    spinning: { type: Boolean, default: false }
  },
  data() {
    return {
      rotateDeg: 0,
      isRotating: false,
      allowTransition: true,
      spinCount: 0,
      timer: null
    }
  },
  computed: {
    segments() {
      const n = this.pool.length || 1
      const seg = 360 / n
      return this.pool.map((item, i) => {
        const cat = (item.raw && item.raw.category) || item.category || '美食'
        const icon = CAT_ICONS[cat] || '📍'
        let label = item.name || '地点'
        if (label.length > 5) label = label.slice(0, 5)
        return {
          label,
          icon,
          deg: i * seg + seg / 2
        }
      })
    },
    conicStyle() {
      const n = this.pool.length
      if (n === 0) return 'conic-gradient(#D5E0DC 0deg 360deg)'
      const seg = 360 / n
      const parts = []
      for (let i = 0; i < n; i++) {
        const from = i * seg
        const to = (i + 1) * seg
        const color = this.pool[i].color || THEME_COLORS[i % THEME_COLORS.length]
        // 扇区间保留 1.2deg 的纯白分割线，更显精致立体
        parts.push(`${color} ${from}deg ${to - 1.2}deg, #FFFFFF ${to - 1.2}deg ${to}deg`)
      }
      return `conic-gradient(${parts.join(', ')})`
    }
  },
  watch: {
    spinning(v) {
      if (v && !this.isRotating) {
        this.spin()
      }
    }
  },
  beforeUnmount() {
    if (this.timer) clearTimeout(this.timer)
  },
  methods: {
    onCenterTap() {
      if (this.isRotating || this.spinning) return
      if (this.pool.length === 0) {
        uni.showToast({ title: '当前还没有候选地点', icon: 'none' })
        return
      }
      this.$emit('spin')
    },

    spin() {
      const n = this.pool.length
      if (n === 0 || this.isRotating) return
      this.spinCount++
      const target = Math.floor(Math.random() * n)
      const seg = 360 / n
      // 目标扇区中心角度（顺时针从 12 点钟方向起算）
      const angleTarget = target * seg + seg / 2
      // 当前累计角度对 360 取模
      const currentMod = this.rotateDeg % 360
      // 计算达到指针位置（顶部 12 点钟，0度）所需的顺时针旋转增量
      let addDeg = (360 - angleTarget) - currentMod
      if (addDeg <= 0) {
        addDeg += 360
      }

      // 旋转 5 圈，转速充沛有冲击力，观感平滑自然
      const rounds = 5
      this.allowTransition = true
      this.isRotating = true
      this.rotateDeg += rounds * 360 + addDeg

      // 严格在 3.2s 物理加减速完全平稳落定后，触发结果反馈与触感震动
      const DURATION = 3200
      if (this.timer) clearTimeout(this.timer)
      this.timer = setTimeout(() => {
        this.isRotating = false
        try {
          uni.vibrateShort({ type: 'medium' })
        } catch (e) { /* ignore */ }
        this.$emit('result', { index: target, item: this.pool[target] })
      }, DURATION + 80)
    },

    reset() {
      if (this.timer) clearTimeout(this.timer)
      this.isRotating = false
      this.allowTransition = false
      this.rotateDeg = 0
      this.$nextTick(() => {
        setTimeout(() => {
          this.allowTransition = true
        }, 50)
      })
    }
  }
}
</script>

<style scoped>
.fw-wheel-box {
  position: relative;
  width: 600rpx;
  height: 600rpx;
  margin: 10rpx auto 16rpx;
}

/* 顶部定位指针（带物理指针微颤动画） */
.fw-pointer {
  position: absolute;
  top: -16rpx;
  left: 50%;
  transform: translateX(-50%);
  z-index: 25;
  display: flex;
  flex-direction: column;
  align-items: center;
  transform-origin: 50% 16rpx;
  transition: transform 0.2s ease-out;
}
.fw-pointer.is-spinning {
  animation: pointer-flapper 0.12s infinite ease-in-out;
}
@keyframes pointer-flapper {
  0% { transform: translateX(-50%) rotate(0deg); }
  25% { transform: translateX(-50%) rotate(-5deg); }
  75% { transform: translateX(-50%) rotate(4deg); }
  100% { transform: translateX(-50%) rotate(0deg); }
}
.pointer-cap {
  width: 32rpx;
  height: 32rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFE896, #F5A623);
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.25);
  border: 4rpx solid #FFFFFF;
}
.pointer-arrow {
  width: 0;
  height: 0;
  margin-top: -6rpx;
  border-left: 20rpx solid transparent;
  border-right: 20rpx solid transparent;
  border-top: 46rpx solid #F5A623;
  filter: drop-shadow(0 6rpx 8rpx rgba(0,0,0,0.2));
}

/* 外圈轮环与灯珠 */
.fw-wheel-ring {
  width: 580rpx;
  height: 580rpx;
  border-radius: 50%;
  margin: 10rpx auto;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1C5A4D, #113B32);
  box-shadow: 0 20rpx 60rpx rgba(27, 86, 73, 0.26), inset 0 2rpx 8rpx rgba(255,255,255,0.25);
}
.wheel-light-dot {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 14rpx;
  height: 14rpx;
  border-radius: 50%;
  background: #FFEAA7;
  box-shadow: 0 0 8rpx #FFEAA7;
  transition: opacity 0.2s;
}
.fw-wheel-ring.is-spinning .wheel-light-dot.dot-odd {
  animation: marquee-blink-odd 0.3s infinite ease-in-out;
}
.fw-wheel-ring.is-spinning .wheel-light-dot.dot-even {
  animation: marquee-blink-even 0.3s infinite ease-in-out;
}
@keyframes marquee-blink-odd {
  0%, 100% { opacity: 1; filter: brightness(1.3); box-shadow: 0 0 14rpx #FFFFFF, 0 0 20rpx #FFEAA7; }
  50% { opacity: 0.35; filter: brightness(0.7); box-shadow: 0 0 4rpx #FFEAA7; }
}
@keyframes marquee-blink-even {
  0%, 100% { opacity: 0.35; filter: brightness(0.7); box-shadow: 0 0 4rpx #FFEAA7; }
  50% { opacity: 1; filter: brightness(1.3); box-shadow: 0 0 14rpx #FFFFFF, 0 0 20rpx #FFEAA7; }
}

/* 旋转主盘体 */
.fw-wheel-plate {
  width: 530rpx;
  height: 530rpx;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  box-shadow: inset 0 0 16rpx rgba(0,0,0,0.18);
  transform-origin: 50% 50%;
  will-change: transform;
}

/* 扇区文字排版（沿半径外侧居中放置，绝对不遮挡中心按钮） */
.wheel-slot {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  pointer-events: none;
}
.wheel-slot-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 32rpx;
  max-width: 140rpx;
}
.slot-icon {
  font-size: 32rpx;
  margin-bottom: 4rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(0,0,0,0.3));
}
.slot-text {
  font-size: 26rpx;
  font-weight: 800;
  color: #FFFFFF;
  text-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.5);
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 130rpx;
  line-height: 1.2;
}

/* 独立中心 GO 按钮 */
.fw-center-btn {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: #FFFFFF;
  box-shadow: 0 12rpx 36rpx rgba(0,0,0,0.18), 0 0 0 10rpx rgba(255,255,255,0.9);
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s;
}
.fw-center-btn:active {
  transform: translate(-50%, -50%) scale(0.92);
}
.center-btn-inner {
  width: 140rpx;
  height: 140rpx;
  border-radius: 50%;
  background: linear-gradient(145deg, #248875, #14594C);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: inset 0 2rpx 6rpx rgba(255,255,255,0.35);
}
.center-btn-title {
  font-size: 40rpx;
  font-weight: 900;
  color: #FFE793;
  letter-spacing: 2rpx;
  text-shadow: 0 2rpx 6rpx rgba(0,0,0,0.3);
}
.center-btn-sub {
  font-size: 20rpx;
  font-weight: 600;
  color: #FFFFFF;
  opacity: 0.9;
  margin-top: 2rpx;
}
</style>

