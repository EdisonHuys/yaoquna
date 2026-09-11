<template>
  <view class="buy-page">
    <!-- 物品输入 -->
    <view class="item-input">
      <input v-model="item" class="item-field" placeholder="想买什么？比如：新款 AirPods" placeholder-class="ph" />
      <input v-model="price" class="price-field" type="digit" placeholder="价格(¥)" placeholder-class="ph" />
    </view>

    <!-- 四维打分 -->
    <view class="score-card">
      <view class="score-head">四维价值打分</view>
      <view v-for="dim in dims" :key="dim.key" class="dim-row">
        <view class="dim-info">
          <text class="dim-name">{{ dim.name }}</text>
          <text class="dim-desc">{{ dim.desc }}</text>
        </view>
        <view class="star-row" @click="setScore(dim.key)">
          <text
            v-for="i in 5"
            :key="i"
            class="star"
            :class="{ on: i <= scores[dim.key] }"
          >★</text>
        </view>
      </view>

      <!-- 得分 -->
      <view class="score-total" :class="verdict.class">
        <view class="total-row">
          <text class="total-num">{{ total }}</text>
          <text class="total-den">/20</text>
        </view>
        <text class="total-label">{{ verdict.label }}</text>
        <text class="total-tip">{{ verdict.tip }}</text>
      </view>
    </view>

    <!-- 拥有成本计算器 -->
    <view class="cost-card" v-if="Number(price) > 0">
      <view class="cost-head">拥有成本计算器</view>
      <view class="cost-row">
        <text class="cost-label">预计使用次数</text>
        <view class="stepper">
          <text class="step-btn" @click="times = Math.max(1, times - 1)">−</text>
          <text class="step-num">{{ times }}</text>
          <text class="step-btn" @click="times++">+</text>
        </view>
      </view>
      <view class="cost-result">
        每次使用成本
        <text class="cost-num">¥{{ (Number(price) / times).toFixed(2) }}</text>
        <text class="cost-compare">{{ compareText }}</text>
      </view>
    </view>

    <view class="fw-btn conclude" @click="conclude">得出建议</view>

    <view v-if="buyResult" class="buy-result">
      <text class="result-emoji">{{ buyResult.emoji }}</text>
      <text class="result-text">{{ buyResult.text }}</text>
    </view>
  </view>
</template>

<script>
import { store } from '../../store/index'
import { api } from '../../api/index'

export default {
  data() {
    return {
      item: '',
      price: '',
      times: 10,
      scores: { need: 3, freq: 4, value: 2, alt: 3 },
      dims: [
        { key: 'need', name: '必要性', desc: '真的需要吗' },
        { key: 'freq', name: '使用频率', desc: '会用几次' },
        { key: 'value', name: '性价比', desc: '值这个价吗' },
        { key: 'alt', name: '替代性', desc: '有平替吗' }
      ],
      buyResult: null
    }
  },
  computed: {
    total() {
      return Object.values(this.scores).reduce((a, b) => a + b, 0)
    },
    verdict() {
      const t = this.total
      if (t >= 16) return { label: '建议入手', tip: '高价值消费，买它！', class: 'good', emoji: '🛒' }
      if (t >= 11) return { label: '建议再想想', tip: '有亮点但别冲动', class: 'mid', emoji: '🤔' }
      return { label: '建议别买', tip: '省下这笔钱更香', class: 'bad', emoji: '💸' }
    },
    compareText() {
      if (!Number(this.price)) return ''
      const per = Number(this.price) / this.times
      if (per < 3) return '比一杯奶茶还便宜！'
      if (per < 15) return '差不多一杯奶茶钱'
      return '有点小贵，再掂量掂量'
    }
  },
  methods: {
    setScore(key) {
      // 简单交互：本轮点击的星 = 5
      this.scores[key] = this.scores[key] >= 5 ? 1 : this.scores[key] + 1
    },
    async conclude() {
      const v = this.verdict
      this.buyResult = { emoji: v.emoji, text: `${v.label}（${this.total}/20）` }
      const decisionData = {
        type: 'buy',
        result: v.label,
        title: '要不要买' + (this.item ? '「' + this.item + '」' : '这件东西'),
        detail: `四维打分 ${this.total}/20` + (this.price ? ` · 总价¥${this.price}` : ''),
        executed: false
      }
      try {
        await api.createDecision(decisionData)
      } catch (e) {
        store.addDecision(decisionData)
      }
      uni.showToast({ title: '决策已归档', icon: 'none' })
    }
  }
}
</script>

<style scoped>
.buy-page {
  min-height: 100vh;
  padding: 24rpx 32rpx calc(60rpx + constant(safe-area-inset-bottom));
  padding: 24rpx 32rpx calc(60rpx + env(safe-area-inset-bottom));
  box-sizing: border-box;
}
.item-input {
  display: flex;
  gap: 16rpx;
}
.item-field {
  flex: 1;
  background: #FFFFFF;
  border-radius: 24rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 22rpx 28rpx;
  font-size: 28rpx;
  font-weight: 600;
  color: #14352D;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.04);
}
.price-field {
  width: 210rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 22rpx 24rpx;
  font-size: 28rpx;
  font-weight: 700;
  color: #FF7D42;
  text-align: center;
  box-shadow: 0 4rpx 14rpx rgba(27, 86, 73, 0.04);
}
.ph { color: #8EA49D; font-weight: normal; }
.score-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 32rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.04);
}
.score-head, .cost-head {
  font-size: 32rpx;
  font-weight: 800;
  color: #14352D;
  margin-bottom: 20rpx;
  display: flex;
  align-items: center;
}
.score-head::before, .cost-head::before {
  content: '';
  width: 6rpx;
  height: 26rpx;
  background: #248875;
  border-radius: 6rpx;
  margin-right: 12rpx;
}
.dim-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22rpx 0;
  border-bottom: 1.5rpx solid #F0F5F2;
}
.dim-info { display: flex; flex-direction: column; gap: 4rpx; }
.dim-name { font-size: 28rpx; font-weight: 700; color: #2D4A41; }
.dim-desc { font-size: 22rpx; color: #8EA49D; }
.star-row { display: flex; gap: 6rpx; padding: 6rpx 0; }
.star {
  font-size: 46rpx;
  color: #E2ECE8;
  transition: transform 0.15s, color 0.15s;
}
.star:active {
  transform: scale(1.2);
}
.star.on {
  color: #FFB319;
  text-shadow: 0 2rpx 10rpx rgba(255, 179, 25, 0.4);
}
.score-total {
  margin-top: 30rpx;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}
.score-total.good {
  background: linear-gradient(135deg, #EEF9F4, #E3F6ED);
  border: 1.5rpx solid #C4EBD8;
}
.score-total.mid {
  background: linear-gradient(135deg, #FFF8EC, #FFF3DD);
  border: 1.5rpx solid #FFE6B3;
}
.score-total.bad {
  background: linear-gradient(135deg, #FDF3F3, #FCE8E8);
  border: 1.5rpx solid #F8C8C8;
}
.total-row { display: flex; align-items: baseline; }
.total-num {
  font-size: 88rpx;
  font-weight: 900;
  color: #1F6E5F;
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.total-den { font-size: 28rpx; font-weight: 600; color: #7B938B; margin-left: 4rpx; }
.total-label { font-size: 32rpx; font-weight: 800; color: #14352D; margin-top: 6rpx; }
.total-tip { font-size: 24rpx; font-weight: 500; color: #5E7A71; }
.cost-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.9);
  padding: 32rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.04);
}
.cost-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cost-label { font-size: 28rpx; font-weight: 600; color: #3D5A51; }
.stepper {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: #EEF5F2;
  border-radius: 999rpx;
  padding: 8rpx 24rpx;
}
.step-btn {
  font-size: 38rpx;
  font-weight: 700;
  color: #1F6E5F;
  padding: 0 10rpx;
  transition: transform 0.15s;
}
.step-btn:active {
  transform: scale(1.2);
}
.step-num {
  font-size: 32rpx;
  font-weight: 800;
  color: #14352D;
  min-width: 50rpx;
  text-align: center;
  font-variant-numeric: tabular-nums;
}
.cost-result {
  margin-top: 24rpx;
  text-align: center;
  font-size: 26rpx;
  font-weight: 500;
  color: #7B938B;
  background: #F8FAF9;
  border-radius: 20rpx;
  padding: 20rpx;
}
.cost-num {
  font-size: 48rpx;
  font-weight: 900;
  color: #FF7D42;
  margin: 0 10rpx;
  font-variant-numeric: tabular-nums;
}
.cost-compare {
  display: inline-block;
  margin-top: 10rpx;
  font-size: 24rpx;
  font-weight: 600;
  color: #1F8A65;
  background: #EAF7F1;
  padding: 6rpx 20rpx;
  border-radius: 999rpx;
}
.conclude { margin-top: 32rpx; }
.buy-result {
  margin-top: 24rpx;
  background: #FFFFFF;
  border-radius: 26rpx;
  border: 1.5rpx solid rgba(225, 237, 232, 0.95);
  padding: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  box-shadow: 0 10rpx 30rpx rgba(27, 86, 73, 0.08);
  animation: fadeIn 0.3s ease;
}
.result-emoji { font-size: 48rpx; }
.result-text { font-size: 30rpx; font-weight: 800; color: #14352D; }
</style>
