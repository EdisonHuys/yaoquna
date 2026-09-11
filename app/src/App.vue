<script>
import { store } from './store/index'
import { api } from './api/index'

export default {
  onLaunch() {
    store.bindApi(api)
    store.init()
    // 启动时检查并静默预热 Token，杜绝鉴权失效
    const token = uni.getStorageSync('fw_token')
    if (!token) {
      api.login().catch(() => {})
    }
  }
}
</script>

<style>
/* 全局公共设计基准 */
page {
  background-color: #F4F8F6;
  color: #14352D;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  font-size: 28rpx;
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}

/* 通用高级卡片 */
.fw-card {
  background: #FFFFFF;
  border-radius: 28rpx;
  border: 1.5rpx solid rgba(226, 237, 232, 0.85);
  box-shadow: 0 8rpx 28rpx rgba(27, 86, 73, 0.05), 0 2rpx 6rpx rgba(0, 0, 0, 0.02);
  padding: 28rpx;
  box-sizing: border-box;
}

/* 通用主行动按钮 */
.fw-btn {
  background: linear-gradient(135deg, #248875 0%, #1B6557 100%);
  color: #FFFFFF;
  border-radius: 999rpx;
  font-size: 32rpx;
  font-weight: 700;
  letter-spacing: 1rpx;
  text-align: center;
  padding: 26rpx 0;
  border: none;
  line-height: 1.4;
  box-shadow: 0 8rpx 24rpx rgba(27, 86, 73, 0.28);
  transition: transform 0.15s, opacity 0.15s;
}
.fw-btn::after { border: none; }
.fw-btn:active {
  transform: scale(0.98);
  opacity: 0.92;
}

/* 通用幽灵辅助按钮 */
.fw-btn-ghost {
  background: #FFFFFF;
  color: #1F6E5F;
  border: 2rpx solid #CFE2DA;
  border-radius: 999rpx;
  font-size: 30rpx;
  font-weight: 600;
  text-align: center;
  padding: 22rpx 0;
  line-height: 1.4;
  box-shadow: 0 4rpx 14rpx rgba(31, 110, 95, 0.04);
  transition: transform 0.15s, opacity 0.15s;
}
.fw-btn-ghost::after { border: none; }
.fw-btn-ghost:active {
  transform: scale(0.98);
  opacity: 0.92;
}

/* 胶囊微标签 */
.fw-chip {
  display: inline-flex;
  align-items: center;
  padding: 6rpx 22rpx;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 600;
  background: #EAF5F0;
  color: #1F6E5F;
}

/* 分区标头 */
.fw-sec-title {
  font-size: 32rpx;
  font-weight: 800;
  color: #14352D;
  margin: 32rpx 8rpx 18rpx;
  display: flex;
  align-items: center;
  letter-spacing: 0.5rpx;
}
.fw-sec-title::before {
  content: '';
  width: 8rpx;
  height: 28rpx;
  border-radius: 999rpx;
  background: linear-gradient(180deg, #248875, #38B6E8);
  margin-right: 14rpx;
}

/* 底部安全区占位（配合自定义 TabBar 或底部操作条） */
.fw-safe-bottom {
  height: calc(140rpx + constant(safe-area-inset-bottom));
  height: calc(140rpx + env(safe-area-inset-bottom));
}
</style>
