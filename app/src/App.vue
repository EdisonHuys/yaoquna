<script>
export default {
  onLaunch() {
    // 启动时读取本地登录态
    const user = uni.getStorageSync('fw_user')
    const token = uni.getStorageSync('fw_token')
    if (user && token) {
      // 已登录
    }
  }
}
</script>

<style>
/* 全局公共样式 */
page {
  background-color: #F5F9F7;
  color: #1A3B34;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  font-size: 28rpx;
  line-height: 1.5;
}

/* 通用卡片 */
.fw-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  box-shadow: 0 8rpx 30rpx rgba(31, 110, 95, 0.06);
  padding: 28rpx;
  box-sizing: border-box;
}

/* 通用按钮 */
.fw-btn {
  background: linear-gradient(135deg, #34D0A8 0%, #38B6E8 100%);
  color: #FFFFFF;
  border-radius: 999rpx;
  font-size: 32rpx;
  font-weight: 600;
  text-align: center;
  padding: 26rpx 0;
  border: none;
  line-height: 1.4;
}
.fw-btn::after { border: none; }
.fw-btn:active { opacity: 0.88; }

.fw-btn-ghost {
  background: #FFFFFF;
  color: #1F6E5F;
  border: 2rpx solid #BFE3D6;
  border-radius: 999rpx;
  font-size: 30rpx;
  font-weight: 500;
  text-align: center;
  padding: 22rpx 0;
  line-height: 1.4;
}
.fw-btn-ghost::after { border: none; }

/* 胶囊标签 */
.fw-chip {
  display: inline-flex;
  align-items: center;
  padding: 8rpx 22rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  background: #EFF7F3;
  color: #2C7A66;
}

/* 分区标题 */
.fw-sec-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1A3B34;
  margin: 30rpx 8rpx 18rpx;
  display: flex;
  align-items: center;
}
.fw-sec-title::before {
  content: '';
  width: 10rpx;
  height: 30rpx;
  border-radius: 6rpx;
  background: linear-gradient(180deg, #34D0A8, #38B6E8);
  margin-right: 14rpx;
}

/* 底部安全区占位 */
.fw-safe-bottom { height: 40rpx; }
</style>
