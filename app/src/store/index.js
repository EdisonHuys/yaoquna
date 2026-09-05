/**
 * 全局状态 Store（Vue3 reactive 单例）
 * 提供用户、标记、小组、决策等核心数据
 */
import { reactive } from 'vue'

export const store = reactive({
  // 登录态
  token: '',
  user: null,

  // 数据
  marks: [],
  groups: [],
  decisions: [],
  alumniFeed: [],

  // 当前页签（自定义 tabBar 用）
  currentTab: 'index',

  // ===== 初始化 =====
  init() {
    // 恢复本地登录态（页面刷新后重新拉起）
    if (!this.token) {
      const token = uni.getStorageSync('fw_token')
      let user = uni.getStorageSync('fw_user')
      // 兼容：H5 端裸 JSON 字符串 / uni 标准 {type,data} 两种存储格式
      if (typeof user === 'string' && user) {
        try { user = JSON.parse(user) } catch (e) { user = null }
      }
      if (token && user) {
        this.token = token
        this.user = user
      }
    }
    // 已登录：从后端装载数据（异步，reactive 自动更新视图）
    if (this.token && this.user && !this.loading) {
      this.loading = true
      // 动态 import 避免循环依赖（api -> store -> api）
      import('../api/index.js').then(({ api }) => {
        return api.loadAll().catch(() => {})
      }).finally(() => {
        this.loading = false
      })
    }
  },

  // ===== 登录态 =====
  isLogin() {
    return !!this.token && !!this.user
  },
  login(token = '', user = null) {
    this.token = token || ('mock_token_' + Date.now())
    this.user = user ? { ...user } : null
    uni.setStorageSync('fw_token', this.token)
    uni.setStorageSync('fw_user', this.user)
  },
  logout() {
    this.token = ''
    this.user = null
    uni.removeStorageSync('fw_token')
    uni.removeStorageSync('fw_user')
  },

  // ===== 标记 =====
  getMyMarks() {
    if (!this.user) return []
    return this.marks.filter(m => m.creator.id === this.user.id)
  },
  getMarkById(id) {
    return this.marks.find(m => m.id === id)
  },
  addMark(mark) {
    const item = {
      id: 'm_' + Date.now(),
      createTime: Date.now(),
      favoriteCount: 0,
      reportCount: 0,
      status: '想去',
      images: [],
      contributions: [],
      creator: this.user ? { id: this.user.id, nickname: this.user.nickname } : { id: 'u_x', nickname: '游客' },
      ...mark
    }
    this.marks.unshift(item)
    return item
  },
  updateMark(id, patch) {
    const idx = this.marks.findIndex(m => m.id === id)
    if (idx > -1) {
      this.marks[idx] = { ...this.marks[idx], ...patch }
    }
  },
  removeMark(id) {
    this.marks = this.marks.filter(m => m.id !== id)
  },

  // ===== 决策 =====
  addDecision(d) {
    const item = {
      id: 'd_' + Date.now(),
      time: Date.now(),
      executed: false,
      ...d
    }
    this.decisions.unshift(item)
    return item
  },

  // ===== 小组 =====
  getGroupById(id) {
    return this.groups.find(g => g.id === id)
  },
  isInGroup(groupId) {
    if (!this.user) return false
    const g = this.getGroupById(groupId)
    return g && g.members.some(m => m.id === this.user.id)
  },

  // ===== 统计 =====
  getStats() {
    const mine = this.getMyMarks()
    const went = mine.filter(m => m.status === '已去').length
    const total = mine.length
    return {
      totalMark: total,
      went: went,
      weedOutRate: total ? Math.round((went / total) * 100) : 0,
      monthDecisions: this.decisions.length,
      execRate: this.decisions.length
        ? Math.round((this.decisions.filter(d => d.executed).length / this.decisions.length) * 100)
        : 0,
      streak: this.user ? this.user.streak : 0
    }
  }
})
