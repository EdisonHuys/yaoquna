/**
 * API 接口层（摇去哪后端对接版）
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

// 跨平台安全日期毫秒解析（针对 iOS WebKit 兼容，将 yyyy-MM-dd 转换为 yyyy/MM/dd，彻底消除微信小程序警告与解析失效）
function parseSafeTime(val) {
  if (!val) return Date.now()
  if (typeof val === 'number') return val
  if (val instanceof Date) return val.getTime()
  if (typeof val === 'string') {
    const safeStr = val.trim().replace(/-/g, '/')
    const t = new Date(safeStr).getTime()
    return isNaN(t) ? Date.now() : t
  }
  return Date.now()
}

// 后端标记 -> 前端 mark
function mapMark(m) {
  if (!m) return null
  let status = m.status
  if (status === 'normal') status = '想去'
  else if (status === 'visited') status = '已去'
  else if (status === 'off') status = '已下架'

  // 解析图片列表（后端可能为逗号分隔字符串或数组）
  let imgs = []
  if (Array.isArray(m.images)) {
    imgs = m.images
  } else if (typeof m.images === 'string' && m.images.trim()) {
    imgs = m.images.split(',').map(s => s.trim()).filter(Boolean)
  }

  return {
    id: m.markId || m.id,
    name: m.name || '',
    category: m.category || '其他',
    lat: Number(m.lat) || 31.2989,
    lng: Number(m.lng) || 120.6372,
    address: m.address || '',
    remark: m.remark || m.note || '',
    note: m.remark || m.note || '',
    rating: Number(m.rating) || 0,
    price: Number(m.price) || 0,
    tags: m.tags ? (Array.isArray(m.tags) ? m.tags : String(m.tags).split(',').filter(Boolean)) : [],
    images: imgs,
    shareScope: m.shareScope || 'private',
    groupIds: m.groupIds ? (Array.isArray(m.groupIds) ? m.groupIds : String(m.groupIds).split(',').filter(Boolean)) : [],
    schoolId: m.schoolId || null,
    status,
    favoriteCount: m.favoriteCount || 0,
    reportCount: m.reportCount || 0,
    favorited: !!m.favorited,
    creator: {
      id: m.userId || (m.creator && m.creator.id) || (store.user && store.user.id) || 'local_user',
      name: m.creatorName || (m.creator && m.creator.name) || (store.user && store.user.nickname) || '同学',
      avatar: m.creatorAvatar || (m.creator && m.creator.avatar) || (store.user && store.user.avatar) || ''
    },
    createTime: parseSafeTime(m.createTime)
  }
}

function mapDecision(d) {
  let dt = d.decisionType || 'select'
  if (dt === '0' || dt === 0) dt = 'select'
  else if (dt === '1' || dt === 1) dt = 'go'
  else if (dt === '2' || dt === 2) dt = 'buy'
  return {
    id: d.decisionId,
    type: dt,
    title: d.title || '',
    result: d.result || '',
    detail: d.detail || '',
    executed: d.executed === '1' || d.executed === 1 || d.executed === true,
    time: parseSafeTime(d.createTime)
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
    let openid = profile.openid || uni.getStorageSync('fw_openid')
    if (!openid) {
      try {
        const r = await new Promise((resolve) => {
          uni.login({
            provider: 'weixin',
            success: (res) => resolve(res.code || ''),
            fail: () => resolve('')
          })
        })
        if (r) openid = 'wx_' + r
      } catch (e) { /* ignore */ }
    }
    if (!openid) openid = localOpenId()
    uni.setStorageSync('fw_openid', openid)

    const nickname = profile.nickname || '微信用户'
    const avatar = profile.avatar || ''
    const body = await post('/api/login', {
      openid,
      nickname,
      avatar
    })
    const token = body.data.token
    const user = mapUser(body.data.user)
    if (nickname) user.nickname = nickname
    if (avatar) user.avatar = avatar
    store.login(token, user)
    // 拉取数据填充 store
    await api.loadAll().catch(() => {})
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
    try {
      const body = await get('/api/marks', {
        scope: params.scope || '',
        category: params.category && params.category !== '全部' ? params.category : ''
      })
      const list = (body.data || []).map(mapMark).filter(Boolean)
      store.marks = list
      uni.setStorageSync('fw_marks', list)
      return { code: 0, data: list }
    } catch (e) {
      // 离线兜底读取本地缓存
      const cached = uni.getStorageSync('fw_marks')
      if (Array.isArray(cached) && cached.length > 0) {
        store.marks = cached
        return { code: 0, data: cached }
      }
      return { code: 0, data: store.marks || [] }
    }
  },

  async getMark(id) {
    try {
      const body = await get('/api/marks/' + id)
      return { code: 0, data: mapMark(body.data) }
    } catch (e) {
      const m = store.getMarkById(id)
      return { code: 0, data: m }
    }
  },

  async createMark(data) {
    const rawImages = Array.isArray(data.images)
      ? data.images.join(',')
      : (typeof data.images === 'string' ? data.images : '')

    const payload = {
      name: data.name,
      category: data.category || '美食',
      lat: Number(data.lat) || 31.2989,
      lng: Number(data.lng) || 120.6372,
      address: data.address || '',
      remark: data.note || data.remark || '',
      rating: Number(data.rating) || 4,
      price: Number(data.price) || 0,
      tags: Array.isArray(data.tags) ? data.tags.join(',') : (data.tags || ''),
      images: rawImages,
      shareScope: data.shareScope || 'private',
      groupIds: Array.isArray(data.groupIds) ? data.groupIds.join(',') : (data.groupIds || ''),
      schoolId: data.schoolId || (store.user ? store.user.schoolId : null),
      status: 'normal'
    }

    try {
      const body = await post('/api/marks', payload)
      const mark = mapMark(body.data)
      if (!mark.creator || !mark.creator.id) {
        mark.creator = store.user
          ? { id: store.user.id, name: store.user.nickname, avatar: store.user.avatar }
          : { id: 'local_user', name: '我', avatar: '' }
      }
      // 补充前端上传的图片与备注（防止后端只存入部分字段）
      if ((!mark.images || mark.images.length === 0) && data.images && data.images.length > 0) {
        mark.images = Array.isArray(data.images) ? data.images : [data.images]
      }
      if (!mark.remark && (data.note || data.remark)) {
        mark.remark = data.note || data.remark
        mark.note = mark.remark
      }
      store.marks.unshift(mark)
      uni.setStorageSync('fw_marks', store.marks)
      return { code: 0, data: mark }
    } catch (err) {
      console.warn('createMark post failed, fallback to local storage', err)
      // 离线/服务未响应时，生成本地稳定记录
      const localMark = {
        id: 'loc_' + Date.now(),
        name: data.name,
        category: data.category || '美食',
        lat: Number(data.lat) || 31.2989,
        lng: Number(data.lng) || 120.6372,
        address: data.address || '',
        remark: data.note || data.remark || '',
        note: data.note || data.remark || '',
        rating: Number(data.rating) || 4,
        price: Number(data.price) || 0,
        tags: Array.isArray(data.tags) ? data.tags : [],
        images: Array.isArray(data.images) ? data.images : (data.images ? [data.images] : []),
        shareScope: data.shareScope || 'private',
        groupIds: Array.isArray(data.groupIds) ? data.groupIds : [],
        schoolId: data.schoolId || (store.user ? store.user.schoolId : null),
        status: '想去',
        favoriteCount: 0,
        reportCount: 0,
        favorited: false,
        creator: store.user
          ? { id: store.user.id, name: store.user.nickname, avatar: store.user.avatar }
          : { id: 'local_user', name: '我', avatar: '' },
        createTime: Date.now()
      }
      store.marks.unshift(localMark)
      uni.setStorageSync('fw_marks', store.marks)
      return { code: 0, data: localMark }
    }
  },

  async updateMark(id, data) {
    const payload = { ...data }
    delete payload.id
    if (payload.note && !payload.remark) payload.remark = payload.note
    if (Array.isArray(payload.images)) payload.images = payload.images.join(',')
    if (Array.isArray(payload.tags)) payload.tags = payload.tags.join(',')
    if (Array.isArray(payload.groupIds)) payload.groupIds = payload.groupIds.join(',')
    try {
      await put('/api/marks/' + id, payload)
    } catch (e) {
      // 远程同步异常时已平滑应用本地存储，保持控制台整洁
    }
    store.updateMark(id, data)
    uni.setStorageSync('fw_marks', store.marks)
    return { code: 0 }
  },

  async deleteMark(id) {
    try {
      await del('/api/marks/' + id)
    } catch (e) {
      // 远程同步异常时已平滑应用本地存储，保持控制台整洁
    }
    store.removeMark(id)
    uni.setStorageSync('fw_marks', store.marks)
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

  // ===== 上传图片（支持小程序与跨端拍照/相册图片上传） =====
  async uploadImage(filePath) {
    const token = uni.getStorageSync('fw_token')
    const headers = {}
    if (token) headers['Authorization'] = 'Bearer ' + token
    return new Promise((resolve) => {
      uni.uploadFile({
        url: 'http://180.76.105.198:8090/common/upload',
        filePath,
        name: 'file',
        header: headers,
        timeout: 15000,
        success: (res) => {
          try {
            const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
            if (body && (body.code === 200 || body.url)) {
              resolve(body.url || body.fileName)
            } else {
              resolve(filePath)
            }
          } catch (e) {
            resolve(filePath)
          }
        },
        fail: (err) => {
          console.warn('uploadFile fail, fallback to local path', err)
          resolve(filePath)
        }
      })
    })
  },

  // ===== 一次性装载（登录后填充 store） =====
  async loadAll() {
    const tasks = []
    tasks.push(api.getMarks().then(r => {
      if (r.data && r.data.length > 0) {
        store.marks = r.data
        uni.setStorageSync('fw_marks', r.data)
      }
    }).catch(() => {}))
    tasks.push(api.getGroups().then(r => { store.groups = r.data || [] }).catch(() => {}))
    tasks.push(api.getDecisions().then(r => { store.decisions = r.data || [] }).catch(() => {}))
    await Promise.all(tasks)
  }
}

// 绑定 API 单例到 store，避免在小程序环境中使用 dynamic import
store.bindApi(api)
