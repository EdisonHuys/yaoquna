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
      return this.scores.need + this.scores.freq + this.scores.value + this.scores.alt
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
    conclude() {
      const v = this.verdict
      this.buyResult = { emoji: v.emoji, text: `${v.label}（${this.total}/20）` }
      store.addDecision({
        type: 'buy',
        result: v.label,
        title: '要不要买' + (this.item || '这件东西'),
        detail: `四维打分 ${this.total}/20`,
        executed: false
      })
    }
  }
}
</script>

<style scoped>
.buy-page {
  min-height: 100vh;
  padding: 24rpx 32rpx 60rpx;
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
  padding: 22rpx 26rpx;
  font-size: 28rpx;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.price-field {
  width: 200rpx;
  background: #FFFFFF;
  border-radius: 24rpx;
  padding: 22rpx 26rpx;
  font-size: 28rpx;
  text-align: center;
  box-shadow: 0 4rpx 16rpx rgba(31,110,95,0.05);
}
.ph { color: #A9B8B2; }
.score-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-top: 28rpx;
  box-shadow: 0 8rpx 30rpx rgba(31,110,95,0.05);
}
.score-head, .cost-head {
  font-size: 30rpx;
  font-weight: 700;
  color: #1A3B34;
  margin-bottom: 20rpx;
}
.dim-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 2rpx solid #F2F6F4;
}
.dim-info { display: flex; flex-direction: column; }
.dim-name { font-size: 28rpx; font-weight: 600; color: #4A5D57; }
.dim-desc { font-size: 22rpx; color: #A9B8B2; }
.star-row { display: flex; gap: 4rpx; }
.star { font-size: 44rpx; color: #E3EBE7; }
.star.on { color: #FFC94D; text-shadow: 0 2rpx 6rpx rgba(255,201,77,0.4); }
.score-total {
  margin-top: 30rpx;
  border-radius: 24rpx;
  padding: 28rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
}
.score-total.good { background: #E8F9F0; }
.score-total.mid { background: #FFF6E5; }
.score-total.bad { background: #FDF0F0; }
.total-row { display: flex; align-items: baseline; }
.total-num { font-size: 80rpx; font-weight: 800; color: #1F6E5F; }
.total-den { font-size: 28rpx; color: #7A8A84; }
.total-label { font-size: 32rpx; font-weight: 700; color: #1A3B34; }
.total-tip { font-size: 24rpx; color: #7A8A84; }
.cost-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  padding: 30rpx;
  margin-top: 24rpx;
  box-shadow: 0 8rpx 30rpx rgba(31,110,95,0.05);
}
.cost-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.cost-label { font-size: 28rpx; color: #4A5D57; }
.stepper {
  display: flex;
  align-items: center;
  gap: 24rpx;
  background: #F2F6F4;
  border-radius: 999rpx;
  padding: 8rpx 24rpx;
}
.step-btn { font-size: 36rpx; color: #1F6E5F; padding: 0 6rpx; }
.step-num { font-size: 30rpx; font-weight: 700; color: #1A3B34; min-width: 50rpx; text-align: center; }
.cost-result {
  margin-top: 24rpx;
  text-align: center;
  font-size: 26rpx;
  color: #7A8A84;
}
.cost-num {
  font-size: 48rpx;
  font-weight: 800;
  color: #FF8A3D;
  margin: 0 8rpx;
}
.cost-compare {
  display: block;
  margin-top: 8rpx;
  font-size: 24rpx;
  color: #2EC77E;
}
.conclude { margin-top: 30rpx; }
.buy-result {
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
</style>
