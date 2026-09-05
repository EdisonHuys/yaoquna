/**
 * API 接口层（若依后端对接版）
 *
 * 后端契约（fate-wheel-ruoyi 工程）：
 *   POST /api/login        { openid, nickname, avatar } -> { token, user }
 *   GET  /api/profile      -> AppUserVO
 *   PUT  /api/profile/school { schoolId }
 *   GET  /api/marks?scope&category
 *   POST /api/marks / PUT /api/marks/{id} / DELETE /api/marks/{id}
 *   GET  /api/wheel/pool?category
 *   POST /api/marks/{id}/favorite   (收藏/取消)
 *   POST /api/marks/{id}/visited
 *   GET  /api/decisions?type / POST /api/decisions
 *   GET  /api/groups / POST /api/groups / POST /api/groups/join / GET /api/groups/{id}/members
 *   GET  /api/schools
 *   GET  /api/alumni/marks?schoolId&category
 *   POST /api/reports
 *   GET  /api/stats
 *   POST /api/orders { itemType } / GET /api/orders
 *
 * 所有方法保持旧签名，页面层改动极小；字段在此层完成“后端 -> 前端 store”映射。
 */
import { store } from '../store/index'
import { get, post, put, del } from '../utils/request'

// ===== 字段映射 =====

// 后端用户 -> 前端 store.user
function mapUser(u) {
  if (!u) return null
  return {
    id: u.userId,
    openid: u.openid,
    nickname: u.nickname || '微信用户',
    avatar: u.avatar || '',
    schoolId: u.schoolId || null,
    schoolName: u.schoolName || '',
    isPro: u.isPro === '1' || u.isPro === 1 ? '1' : '0',
    streak: u.streak || 0,
    sharedCount: u.sharedCount || 0,
    totalFavorites: u.totalFavorites || 0
  }
}

// 后端标记 -> 前端 mark
function mapMark(m) {
  let status = m.status
  if (status === 'normal') status = '想去'
  else if (status === 'visited') status = '已去'
  else if (status === 'off') status = '已下架'
  return {
    id: m.markId,
    name: m.name,
    category: m.category || '其他',
    lat: m.lat,
    lng: m.lng,
    address: m.address || '',
    remark: m.remark || '',
    rating: m.rating || 0,
    price: m.price,
    tags: m.tags ? String(m.tags).split(',').filter(Boolean) : [],
    images: m.images ? (Array.isArray(m.images) ? m.images : []) : [],
    shareScope: m.shareScope || 'private',
    groupIds: m.groupIds ? String(m.groupIds).split(',').filter(Boolean) : [],
    schoolId: m.schoolId || null,
    status,
    favoriteCount: m.favoriteCount || 0,
    reportCount: m.reportCount || 0,
    favorited: !!m.favorited,
    creator: { id: m.userId, name: m.creatorName || '同学', avatar: m.creatorAvatar || '' },
    createTime: m.createTime ? new Date(m.createTime).getTime() : Date.now()
  }
}

function mapDecision(d) {
  return {
    id: d.decisionId,
    type: d.decisionType || 'select',
    title: d.title || '',
    result: d.result || '',
    detail: d.detail || '',
    executed: d.executed === '1' || d.executed === 1 || d.executed === true,
    time: d.createTime ? new Date(d.createTime).getTime() : Date.now()
  }
}

function mapGroup(g) {
  return {
    id: g.groupId,
    name: g.groupName,
    inviteCode: g.inviteCode || '',
    ownerId: g.ownerId,
    memberCount: g.memberCount || 0,
    members: g.members || [],
    createTime: g.createTime || ''
  }
}

// 生成/读取本地设备 openid（未配置微信登录时使用，保证可跑通）
function localOpenId() {
  let oid = uni.getStorageSync('fw_openid')
  if (!oid) {
    oid = 'local_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
    uni.setStorageSync('fw_openid', oid)
  }
  return oid
}

export const api = {
  // ===== 用户 =====
  async login(profile = {}) {
    // 微信环境优先取 code；否则用本地设备标识，保证演示可跑通
    let openid = profile.openid || ''
    if (!openid) {
      try {
        const r = await new Promise((resolve, reject) => {
          uni.login({
            provider: 'weixin',
            success: (res) => resolve(res.code || ''),
            fail: () => resolve('')
          })
        })
        openid = r || ''
      } catch (e) { openid = '' }
    }
    if (!openid) openid = localOpenId()

    const body = await post('/api/login', {
      openid,
      nickname: profile.nickname || '',
      avatar: profile.avatar || ''
    })
    const token = body.data.token
    const user = mapUser(body.data.user)
    store.login(token, user)
    // 拉取数据填充 store
    await api.loadAll()
    return { code: 0, user: store.user }
  },

  async getProfile() {
    const body = await get('/api/profile')
    return { code: 0, user: mapUser(body.data) }
  },

  async setSchool(schoolId, schoolName) {
    await put('/api/profile/school', { schoolId })
    if (store.user) {
      store.user.schoolId = schoolId
      store.user.schoolName = schoolName || store.user.schoolName
    }
    return { code: 0 }
  },

  // ===== 标记 =====
  async getMarks(params = {}) {
    const body = await get('/api/marks', {
      scope: params.scope || '',
      category: params.category && params.category !== '全部' ? params.category : ''
    })
    return { code: 0, data: (body.data || []).map(mapMark) }
  },

  async getMark(id) {
    const body = await get('/api/marks/' + id)
    return { code: 0, data: mapMark(body.data) }
  },

  async createMark(data) {
    const payload = {
      name: data.name,
      category: data.category,
      lat: data.lat,
      lng: data.lng,
      address: data.address,
      remark: data.remark,
      rating: data.rating,
      price: data.price,
      tags: Array.isArray(data.tags) ? data.tags.join(',') : data.tags,
      shareScope: data.shareScope || 'private',
      groupIds: Array.isArray(data.groupIds) ? data.groupIds.join(',') : data.groupIds,
      schoolId: data.schoolId,
      status: 'normal'
    }
    const body = await post('/api/marks', payload)
    const mark = mapMark(body.data)
    store.marks.unshift(mark)
    return { code: 0, data: mark }
  },

  async updateMark(id, data) {
    const payload = { ...data }
    delete payload.id
    await put('/api/marks/' + id, payload)
    store.updateMark(id, data)
    return { code: 0 }
  },

  async deleteMark(id) {
    await del('/api/marks/' + id)
    store.removeMark(id)
    return { code: 0 }
  },

  async toggleFavorite(id) {
    const body = await post('/api/marks/' + id + '/favorite')
    const mark = store.getMarkById(id)
    if (mark) {
      mark.favorited = body.data === 1
      mark.favoriteCount = Math.max(0, (mark.favoriteCount || 0) + (body.data === 1 ? 1 : -1))
    }
    return { code: 0, data: body.data }
  },

  async markVisited(id) {
    await post('/api/marks/' + id + '/visited')
    const mark = store.getMarkById(id)
    if (mark) mark.status = '已去'
    return { code: 0 }
  },

  // ===== 决策 =====
  async getDecisions() {
    const body = await get('/api/decisions')
    return { code: 0, data: (body.data || []).map(mapDecision) }
  },

  async createDecision(data) {
    const body = await post('/api/decisions', {
      decisionType: data.type || data.decisionType || 'select',
      title: data.title || '',
      result: data.result || '',
      detail: data.detail || '',
      executed: data.executed ? '1' : '0'
    })
    const d = mapDecision(body.data)
    store.decisions.unshift(d)
    return { code: 0, data: d }
  },

  // ===== 小组 =====
  async getGroups() {
    const body = await get('/api/groups')
    const groups = (body.data || []).map(mapGroup)
    // 并行补齐成员
    const withMembers = await Promise.all(groups.map(async (g) => {
      try {
        const mb = await get('/api/groups/' + g.id + '/members')
        g.members = (mb.data || []).map(u => ({ id: u.userId, nickname: u.nickname || '同学' }))
        g.memberCount = g.members.length
      } catch (e) { /* 忽略 */ }
      return g
    }))
    return { code: 0, data: withMembers }
  },

  async createGroup(groupName) {
    const body = await post('/api/groups', { groupName })
    return { code: 0, data: mapGroup(body.data) }
  },

  async joinGroup(inviteCode) {
    const body = await post('/api/groups/join', { inviteCode })
    return { code: 0, data: mapGroup(body.data) }
  },

  // ===== 学校 / 校友 =====
  async getSchools() {
    const body = await get('/api/schools')
    return { code: 0, data: body.data || [] }
  },

  async getAlumniMarks(schoolId) {
    const body = await get('/api/alumni/marks', { schoolId: schoolId || '' })
    return { code: 0, data: (body.data || []).map(mapMark) }
  },

  async submitReport(markId, reason) {
    await post('/api/reports', { markId, reason })
    return { code: 0 }
  },

  // ===== 统计 =====
  async getStats() {
    const body = await get('/api/stats')
    const s = body.data || {}
    const execTotal = store.decisions.length
    const execDone = store.decisions.filter(d => d.executed).length
    return {
      code: 0,
      data: {
        totalMark: s.totalMark || 0,
        went: s.visitedCount || 0,
        weedOutRate: s.weedRate || 0,
        monthDecisions: s.monthDecisions || 0,
        execRate: execTotal ? Math.round(execDone / execTotal * 100) : 0,
        streak: s.streak || 0
      }
    }
  },

  // ===== 一次性装载（登录后填充 store） =====
  async loadAll() {
    const tasks = []
    tasks.push(api.getMarks().then(r => { store.marks = r.data || [] }).catch(() => {}))
    tasks.push(api.getGroups().then(r => { store.groups = r.data || [] }).catch(() => {}))
    tasks.push(api.getDecisions().then(r => { store.decisions = r.data || [] }).catch(() => {}))
    await Promise.all(tasks)
  }
}
