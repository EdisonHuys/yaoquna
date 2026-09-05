<template>
  <view class="scale-page">
    <!-- 模式切换 -->
    <view class="mode-bar">
      <view
        v-for="m in modes"
        :key="m.key"
        class="mode-chip"
        :class="{ on: mode === m.key }"
        @click="switchMode(m.key)"
      >{{ m.label }}</view>
    </view>

    <!-- 事项输入 -->
    <view class="topic-input">
      <input
        v-model="topic"
        class="topic-field"
        placeholder="比如：要不要去音乐节？"
        placeholder-class="ph"
      />
    </view>

    <!-- ===== 模式一：利弊天平 ===== -->
    <view v-if="mode === 'scale'">
      <view class="scale-visual">
        <view class="scale-pole" :style="{ transform: 'rotate(' + tilt + 'deg)' }">
          <view class="pan left" :style="{ height: pros.length * 26 + 30 + 'rpx' }">
            <text class="pan-count">{{ pros.length }} 利</text>
          </view>
          <view class="pan right" :style="{ height: cons.length * 26 + 30 + 'rpx' }">
            <text class="pan-count">{{ cons.length }} 弊</text>
          </view>
          <view class="pivot-dot"></view>
        </view>
        <view class="scale-base"></view>
      </view>

      <view class="reason-box">
        <view class="reason-col">
          <view class="reason-head good">
            <text>👍 想去 / 利</text>
            <text class="add" @click="openInput('pro')">+</text>
          </view>
          <view v-for="(p, i) in pros" :key="'p' + i" class="reason-item good">
            <text>{{ p }}</text>
            <text class="del" @click="pros.splice(i, 1)">✕</text>
          </view>
          <view v-if="pros.length === 0" class="reason-empty">添加一个理由</view>
        </view>
        <view class="reason-col">
          <view class="reason-head bad">
            <text>👎 顾虑 / 弊</text>
            <text class="add" @click="openInput('con')">+</text>
          </view>
          <view v-for="(c, i) in cons" :key="'c' + i" class="reason-item bad">
            <text>{{ c }}</text>
            <text class="del" @click="cons.splice(i, 1)">✕</text>
          </view>
          <view v-if="cons.length === 0" class="reason-empty">添加一个顾虑</view>
        </view>
      </view>

      <view class="fw-btn conclude" @click="concludeScale">得出结论</view>
      <view v-if="scaleResult" class="scale-result">
        <text class="result-emoji">{{ scaleResult.emoji }}</text>
        <text class="result-text">{{ scaleResult.text }}</text>
      </view>
    </view>

    <!-- ===== 模式二：命运硬币 ===== -->
    <view v-if="mode === 'coin'" class="coin-mode">
      <view class="coin" :class="{ flipping: coinFlipping, face: coinFace }" @click="flipCoin">
        <view class="coin-front">{{ coinFront }}</view>
        <view class="coin-back">命</view>
      </view>
      <view class="coin-hint">{{ coinFlipping ? '命运翻转中…' : '点击硬币，让命运决定' }}</view>
      <view v-if="coinResult" class="scale-result">
        <text class="result-emoji">🪙</text>
        <text class="result-text">{{ coinResult }}</text>
      </view>
      <view v-if="coinResult" class="coin-question">刚才抛出时，你心里希望是哪一面？</view>
    </view>

    <!-- ===== 模式三：冷静计时 ===== -->
    <view v-if="mode === 'cool'" class="cool-mode">
      <view class="cool-ring" @click="startCool">
        <text class="cool-num">{{ coolLeft }}</text>
        <text class="cool-unit">秒</text>
      </view>
      <view class="cool-hint">
        {{ coolRunning ? '冷静一下，深呼吸…' : '给冲动的自己 30 秒冷静时间' }}
      </view>
      <view v-if="coolResult" class="scale-result">
        <text class="result-emoji">🧊</text>
        <text class="result-text">{{ coolResult }}</text>
      </view>
      <view class="fw-btn-ghost cool-btn" @click="coolResult = ''; coolLeft = 30">
        <text v-if="coolRunning">重置</text>
        <text v-else>重新计时</text>
      </view>
    </view>
  </view>
</template>

<script>
import { store } from '../../store/index'

export default {
  data() {
    return {
      modes: [
        { key: 'scale', label: '利弊天平' },
        { key: 'coin', label: '命运硬币' },
        { key: 'cool', label: '冷静计时' }
      ],
      mode: 'scale',
      topic: '',
      pros: ['音乐节阵容不错', '想去放松一下'],
      cons: ['门票有点贵', '周末人多'],
      scaleResult: null,
      coinFlipping: false,
      coinFront: '命',
      coinResult: '',
      coolLeft: 30,
      coolRunning: false,
      coolResult: '',
      coolTimer: null
    }
  },
  computed: {
    tilt() {
      const d = this.pros.length - this.cons.length
      const deg = Math.max(-12, Math.min(12, d * 4))
      return deg
    }
  },
  onUnload() {
    if (this.coolTimer) clearInterval(this.coolTimer)
  },
  methods: {
    switchMode(m) {
      this.mode = m
      this.scaleResult = null
    },
    openInput(type) {
      uni.showModal({
        title: type === 'pro' ? '添加一个理由' : '添加一个顾虑',
        editable: true,
        placeholderText: '写点什么…',
        success: res => {
          if (res.confirm && res.content && res.content.trim()) {
            if (type === 'pro') this.pros.push(res.content.trim())
            else this.cons.push(res.content.trim())
          }
        }
      })
    },
    concludeScale() {
      const d = this.pros.length - this.cons.length
      let result
      if (d > 0) result = { emoji: '✅', text: '天平倾向「去」——去吧，别留遗憾' }
      else if (d < 0) result = { emoji: '🛑', text: '天平倾向「不去」——这次先按住冲动' }
      else result = { emoji: '⚖️', text: '势均力敌——试试命运硬币？' }
      this.scaleResult = result
      store.addDecision({
        type: 'go',
        result: d >= 0 ? '去' : '不去',
        title: (this.topic || '要不要去') + '：利弊天平',
        detail: this.pros.length + ' 利 vs ' + this.cons.length + ' 弊',
        executed: false
      })
    },
    flipCoin() {
      if (this.coinFlipping) return
      this.coinFlipping = true
      this.coinResult = ''
      setTimeout(() => {
        const head = Math.random() > 0.5
        this.coinFront = head ? '去' : '不去'
        this.coinResult = head ? '正面朝上 —— 去！' : '反面朝上 —— 不去！'
        this.coinFlipping = false
        store.addDecision({
          type: 'go',
          result: head ? '去' : '不去',
          title: (this.topic || '要不要去') + '：命运硬币',
          detail: '硬币为你做出了决定',
          executed: false
        })
      }, 1400)
    },
    startCool() {
      if (this.coolRunning) return
      this.coolRunning = true
      this.coolLeft = 30
      this.coolTimer = setInterval(() => {
        this.coolLeft--
        if (this.coolLeft <= 0) {
          clearInterval(this.coolTimer)
          this.coolRunning = false
          this.coolResult = '30 秒冷静结束，你现在还想做吗？想做就做，冲动退了就退。'
        }
      }, 1000)
    }
  }
}
</script>

<style scoped>
.scale-page {
  min-height: 100vh;
  padding: 24rpx 32rpx 60rpx;
  box-sizing: border-box;
}
.mode-bar {
  display: flex;
  gap: 16rpx;
}
.mode-chip {
  flex: 1;
  text-align: center;
  padding: 18rpx 0;
  border-radius: 999rpx;
  font-size: 26rpx;
  background: #FFFFFF;
  color: #7A8A84;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.mode-chip.on {
  background: linear-gradient(135deg, #34D0A8, #38B6E8);
  color: #FFFFFF;
  font-weight: 600;
}
.topic-input {
  margin-top: 28rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 20rpx 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.topic-field {
  font-size: 30rpx;
  color: #1A3B34;
}
.ph { color: #A9B8B2; }

/* 天平 */
.scale-visual {
  position: relative;
  height: 260rpx;
  margin-top: 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}
.scale-pole {
  position: relative;
  width: 480rpx;
  height: 20rpx;
  background: linear-gradient(90deg, #3FA7E0, #2EC77E);
  border-radius: 10rpx;
  transform-origin: center bottom;
  transition: transform 0.6s ease;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.pan {
  width: 140rpx;
  min-height: 30rpx;
  background: linear-gradient(180deg, #DFF0FF, #E8F9F0);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: height 0.4s ease;
}
.pan.left { transform: translateY(-6rpx); }
.pan.right { transform: translateY(-6rpx); }
.pan-count {
  font-size: 24rpx;
  font-weight: 700;
  color: #3FA7E0;
}
.pivot-dot {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 34rpx;
  height: 34rpx;
  border-radius: 50%;
  background: #1F6E5F;
  border: 6rpx solid #FFFFFF;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.15);
}
.scale-base {
  width: 80rpx;
  height: 20rpx;
  background: #1F6E5F;
  border-radius: 6rpx 6rpx 0 0;
}

/* 理由列表 */
.reason-box {
  display: flex;
  gap: 20rpx;
  margin-top: 30rpx;
}
.reason-col {
  flex: 1;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 20rpx;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.reason-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
  font-weight: 700;
  margin-bottom: 14rpx;
}
.reason-head.good { color: #2EC77E; }
.reason-head.bad { color: #EA6668; }
.add {
  font-size: 34rpx;
  font-weight: 600;
  padding: 0 10rpx;
}
.reason-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  padding: 12rpx 16rpx;
  border-radius: 14rpx;
  margin-bottom: 10rpx;
  color: #4A5D57;
}
.reason-item.good { background: #EFF9F4; }
.reason-item.bad { background: #FDF0F0; }
.del { color: #C9D4D0; font-size: 24rpx; padding: 0 6rpx; }
.reason-empty { font-size: 22rpx; color: #C9D4D0; text-align: center; padding: 20rpx 0; }
.conclude {
  margin-top: 30rpx;
}
.scale-result {
  margin-top: 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 26rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.result-emoji { font-size: 44rpx; }
.result-text { font-size: 28rpx; font-weight: 600; color: #1A3B34; }

/* 硬币 */
.coin-mode {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40rpx;
}
.coin {
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFC94D, #FF8A3D);
  box-shadow: 0 20rpx 50rpx rgba(255,138,61,0.35), inset 0 0 0 12rpx rgba(255,255,255,0.25);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transform-style: preserve-3d;
}
.coin.flipping {
  animation: coinflip 1.4s ease;
}
@keyframes coinflip {
  0% { transform: rotateY(0) scale(1); }
  50% { transform: rotateY(180deg) scale(0.9); }
  100% { transform: rotateY(360deg) scale(1); }
}
.coin-front, .coin-back {
  position: absolute;
  font-size: 64rpx;
  font-weight: 800;
  color: #FFFFFF;
  text-shadow: 0 4rpx 8rpx rgba(0,0,0,0.15);
}
.coin-back {
  backface-visibility: hidden;
  transform: rotateY(180deg);
}
.coin-hint {
  margin-top: 40rpx;
  font-size: 26rpx;
  color: #7A8A84;
}
.coin-question {
  margin-top: 16rpx;
  font-size: 24rpx;
  color: #FF8A3D;
  text-align: center;
}

/* 冷静 */
.cool-mode {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 50rpx;
}
.cool-ring {
  width: 300rpx;
  height: 300rpx;
  border-radius: 50%;
  border: 16rpx solid #BFE3D6;
  border-top-color: #1F6E5F;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
  box-shadow: 0 20rpx 50rpx rgba(31,110,95,0.1);
  animation: coolspin 1s linear infinite;
}
.cool-ring.paused { animation: none; }
@keyframes coolspin {
  to { transform: rotate(360deg); }
}
.cool-num {
  font-size: 90rpx;
  font-weight: 800;
  color: #1F6E5F;
  line-height: 1;
}
.cool-unit {
  font-size: 28rpx;
  color: #7A8A84;
}
.cool-hint {
  margin-top: 36rpx;
  font-size: 26rpx;
  color: #7A8A84;
}
.cool-btn {
  margin-top: 40rpx;
  width: 300rpx;
}
</style>
