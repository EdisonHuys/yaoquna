/**
 * 全局状态 Store（Vue3 reactive 单例）
 * 提供用户、标记、小组、决策等核心数据
 */
import { reactive } from 'vue'

export const store = reactive({
  // 登录态
  token: '',
  user: null,
  loading: false,
  _api: null,

  bindApi(api) {
    this._api = api
  },

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
    // 恢复本地标记缓存
    if (!this.marks || this.marks.length === 0) {
      const cached = uni.getStorageSync('fw_marks')
      if (Array.isArray(cached) && cached.length > 0) {
        this.marks = cached
      }
    }
    // 已登录：从后端装载数据（若绑定了 api 则触发）
    if (this._api && this.token && this.user && !this.loading) {
      this.loading = true
      this._api.loadAll().catch(() => {}).finally(() => {
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
    this.marks = []
    this.groups = []
    this.decisions = []
    this.alumniFeed = []
    uni.removeStorageSync('fw_token')
    uni.removeStorageSync('fw_user')
    uni.removeStorageSync('fw_marks')
  },

  // ===== 标记 =====
  /**
   * 获取当前用户创建的地点标记（我的地点）
   * 铁律：
   * 1. 严格按用户 UID 过滤，他人标记（尤其是私密标记）绝对剔除
   * 2. 当用户未创建任何标记时，返回空数组 []，严禁回退返回全部缓存
   */
  getMyMarks() {
    if (!this.marks || this.marks.length === 0) {
      const cached = uni.getStorageSync('fw_marks')
      if (Array.isArray(cached) && cached.length > 0) {
        this.marks = cached
      }
    }
    if (!this.user || !this.user.id) {
      // 未登录或离线模式：仅返回本地临时创建标记
      return (this.marks || []).filter(m => m && (m.creator ? m.creator.id === 'local_user' : false))
    }
    const uid = String(this.user.id)
    const my = (this.marks || []).filter(m => {
      if (!m) return false
      const cid = m.creator ? String(m.creator.id) : (m.userId ? String(m.userId) : '')
      const isMine = (cid && cid === uid) || (cid === 'local_user')
      // 私密标记绝对仅自己可见
      if (m.shareScope === 'private') {
        return isMine
      }
      return isMine
    })
    return my
  },
  /**
   * 获取当前用户全量可见标记（本人 + 小组 + 同校）
   * 铁律：他人创建的私密标记绝对剔除
   */
  getVisibleMarks() {
    if (!this.marks || this.marks.length === 0) {
      const cached = uni.getStorageSync('fw_marks')
      if (Array.isArray(cached) && cached.length > 0) {
        this.marks = cached
      }
    }
    if (!this.user || !this.user.id) {
      return (this.marks || []).filter(m => m && (m.creator ? m.creator.id === 'local_user' : false))
    }
    const uid = String(this.user.id)
    const myGroupIds = (this.groups || []).map(g => String(g.id || g.groupId))
    return (this.marks || []).filter(m => {
      if (!m) return false
      const cid = m.creator ? String(m.creator.id) : (m.userId ? String(m.userId) : '')
      const isMine = (cid && cid === uid) || (cid === 'local_user')
      if (isMine) return true
      // 他人私密标记绝对隔离
      if (m.shareScope === 'private') return false
      // 小组共享
      if (m.shareScope === 'group') {
        const gids = Array.isArray(m.groupIds) ? m.groupIds.map(String) : (typeof m.groupIds === 'string' ? m.groupIds.split(',').map(s => s.trim()) : [])
        return gids.some(gid => myGroupIds.includes(gid))
      }
      // 同校共享
      if (m.shareScope === 'school' && m.schoolId && this.user.schoolId) {
        return String(m.schoolId) === String(this.user.schoolId)
      }
      return false
    })
  },
  getMarkById(id) {
    const sid = String(id)
    return (this.marks || []).find(m => m && (String(m.id) === sid || String(m.markId) === sid))
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
    uni.setStorageSync('fw_marks', this.marks)
    return item
  },
  updateMark(id, patch) {
    const sid = String(id)
    const idx = this.marks.findIndex(m => m && (String(m.id) === sid || String(m.markId) === sid))
    if (idx > -1) {
      this.marks[idx] = { ...this.marks[idx], ...patch }
      uni.setStorageSync('fw_marks', this.marks)
    }
  },
  removeMark(id) {
    const sid = String(id)
    this.marks = this.marks.filter(m => m && String(m.id) !== sid && String(m.markId) !== sid)
    uni.setStorageSync('fw_marks', this.marks)
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
