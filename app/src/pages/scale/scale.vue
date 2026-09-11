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
import { api } from '../../api/index'

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
    async concludeScale() {
      const d = this.pros.length - this.cons.length
      let result
      if (d > 0) result = { emoji: '✅', text: '天平倾向「去」——去吧，别留遗憾' }
      else if (d < 0) result = { emoji: '🛑', text: '天平倾向「不去」——这次先按住冲动' }
      else result = { emoji: '⚖️', text: '势均力敌——试试命运硬币？' }
      this.scaleResult = result

      const decisionData = {
        type: 'go',
        result: d >= 0 ? '去' : '不去',
        title: (this.topic || '要不要去') + '：利弊天平',
        detail: this.pros.length + ' 利 vs ' + this.cons.length + ' 弊',
        executed: false
      }
      try {
        await api.createDecision(decisionData)
      } catch (e) {
        store.addDecision(decisionData)
      }
      uni.showToast({ title: '决策已归档', icon: 'none' })
    },
    flipCoin() {
      if (this.coinFlipping) return
      this.coinFlipping = true
      this.coinResult = ''
      setTimeout(async () => {
        const head = Math.random() > 0.5
        this.coinFront = head ? '去' : '不去'
        this.coinResult = head ? '正面朝上 —— 去！' : '反面朝上 —— 不去！'
        this.coinFlipping = false
        const decisionData = {
          type: 'go',
          result: head ? '去' : '不去',
          title: (this.topic || '要不要去') + '：命运硬币',
          detail: '硬币为你做出了决定',
          executed: false
        }
        try {
          await api.createDecision(decisionData)
        } catch (e) {
          store.addDecision(decisionData)
        }
        uni.showToast({ title: '决策已归档', icon: 'none' })
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
  padding: 24rpx 32rpx calc(60rpx + constant(safe-area-inset-bottom));
  padding: 24rpx 32rpx calc(60rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.mode-bar {
  display: flex;
  background: #E5EFEA;
  border-radius: 999rpx;
  padding: 6rpx;
  gap: 6rpx;
}
.mode-chip {
  flex: 1;
  text-align: center;
  padding: 16rpx 0;
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 600;
  color: #5E7A71;
  transition: all 0.2s;
}
.mode-chip.on {
  background: #FFFFFF;
  color: #1F6E5F;
  font-weight: 800;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.12);
}
.topic-input {
  margin-top: 24rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 22rpx 28rpx;
  box-shadow: 0 6rpx 20rpx rgba(27, 86, 73, 0.04);
}
.topic-field {
  font-size: 30rpx;
  font-weight: 600;
  color: #14352D;
}
.ph { color: #8EA49D; font-weight: normal; }

/* 天平视觉区 */
.scale-visual {
  position: relative;
  height: 270rpx;
  margin-top: 30rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}
.scale-pole {
  position: relative;
  width: 500rpx;
  height: 18rpx;
  background: linear-gradient(90deg, #3FA7E0, #2EC77E);
  border-radius: 9rpx;
  transform-origin: center bottom;
  transition: transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
}
.pan {
  width: 144rpx;
  min-height: 40rpx;
  background: linear-gradient(180deg, #FFFFFF, #E8F4F0);
  border: 2rpx solid #D5E7E0;
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: height 0.4s ease;
  box-shadow: 0 6rpx 16rpx rgba(27, 86, 73, 0.08);
}
.pan.left { transform: translateY(-8rpx); }
.pan.right { transform: translateY(-8rpx); }
.pan-count {
  font-size: 24rpx;
  font-weight: 800;
  color: #1F6E5F;
}
.pivot-dot {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  background: #1F6E5F;
  border: 6rpx solid #FFFFFF;
  box-shadow: 0 4rpx 14rpx rgba(0,0,0,0.18);
}
.scale-base {
  width: 90rpx;
  height: 24rpx;
  background: linear-gradient(180deg, #248875, #14594C);
  border-radius: 8rpx 8rpx 0 0;
  box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.1);
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
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 22rpx 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(27, 86, 73, 0.04);
}
.reason-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 26rpx;
  font-weight: 800;
  margin-bottom: 16rpx;
  padding-bottom: 10rpx;
  border-bottom: 1.5rpx solid #F0F5F2;
}
.reason-head.good { color: #1F8A65; }
.reason-head.bad { color: #E05355; }
.add {
  font-size: 36rpx;
  font-weight: 700;
  padding: 0 8rpx;
  color: #1F6E5F;
  transition: transform 0.15s;
}
.add:active {
  transform: scale(1.2);
}
.reason-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 24rpx;
  font-weight: 500;
  padding: 14rpx 18rpx;
  border-radius: 16rpx;
  margin-bottom: 12rpx;
  line-height: 1.3;
}
.reason-item.good { background: #EEF8F3; color: #1F6E5F; }
.reason-item.bad { background: #FDF2F2; color: #D04547; }
.del {
  color: #B5C7C0;
  font-size: 26rpx;
  padding: 4rpx 8rpx;
}
.del:active {
  color: #E05355;
}
.reason-empty {
  font-size: 22rpx;
  color: #A3B5AF;
  text-align: center;
  padding: 30rpx 0;
}
.conclude {
  margin-top: 32rpx;
}
.scale-result {
  margin-top: 24rpx;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  padding: 28rpx 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 10rpx 30rpx rgba(27, 86, 73, 0.08);
  animation: fadeIn 0.3s ease;
}
.result-emoji { font-size: 48rpx; }
.result-text { font-size: 30rpx; font-weight: 800; color: #14352D; }

/* 命运硬币 */
.coin-mode {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 40rpx;
}
.coin {
  width: 230rpx;
  height: 230rpx;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFD066, #FF8A3D);
  box-shadow: 0 20rpx 50rpx rgba(255, 138, 61, 0.35), inset 0 0 0 12rpx rgba(255,255,255,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: transform 0.15s;
}
.coin:active {
  transform: scale(0.94);
}
.coin.flipping {
  animation: coinflip 1.4s cubic-bezier(0.4, 0, 0.2, 1);
}
@keyframes coinflip {
  0% { transform: scale(1) rotateY(0); }
  50% { transform: scale(1.15) rotateY(540deg); }
  100% { transform: scale(1) rotateY(1080deg); }
}
.coin-front, .coin-back {
  position: absolute;
  font-size: 68rpx;
  font-weight: 900;
  color: #FFFFFF;
  text-shadow: 0 4rpx 10rpx rgba(0,0,0,0.2);
}
.coin-back {
  display: none;
}
.coin-hint {
  margin-top: 40rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #5E7A71;
}
.coin-question {
  margin-top: 18rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #FF7D42;
  text-align: center;
}

/* 冷静期 */
.cool-mode {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 50rpx;
}
.cool-ring {
  width: 310rpx;
  height: 310rpx;
  border-radius: 50%;
  border: 16rpx solid #CFE8DF;
  border-top-color: #1F6E5F;
  border-right-color: #2EC77E;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
  box-shadow: 0 20rpx 50rpx rgba(27, 86, 73, 0.12);
  transition: transform 0.15s;
}
.cool-ring:active {
  transform: scale(0.96);
}
.cool-num {
  font-size: 96rpx;
  font-weight: 900;
  color: #1F6E5F;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.cool-unit {
  font-size: 28rpx;
  font-weight: 600;
  color: #7B938B;
  margin-top: 6rpx;
}
.cool-hint {
  margin-top: 36rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #5E7A71;
}
.cool-btn {
  margin-top: 40rpx;
  width: 320rpx;
}
</style>
