/**
 * 通用工具函数
 */
import { store } from '../store/index'

// 分类 → 颜色（地图图钉、标签）
export const CATEGORY_COLOR = {
  '美食': '#FF8A3D',
  '玩乐': '#FFC94D',
  '购物': '#3FA7E0',
  '自习': '#2EC77E',
  '其他': '#9AA7B1'
}

export function categoryColor(cat) {
  return CATEGORY_COLOR[cat] || CATEGORY_COLOR['其他']
}

// 相对时间格式化
export function timeAgo(ts) {
  if (!ts) return ''
  let t = ts
  if (typeof ts === 'string') {
    t = new Date(ts.trim().replace(/-/g, '/')).getTime()
  } else if (ts instanceof Date) {
    t = ts.getTime()
  }
  if (isNaN(t)) return ''
  const diff = Date.now() - t
  const m = Math.floor(diff / 60000)
  if (m < 1) return '刚刚'
  if (m < 60) return m + '分钟前'
  const h = Math.floor(m / 60)
  if (h < 24) return h + '小时前'
  const d = Math.floor(h / 24)
  if (d < 30) return d + '天前'
  const mo = Math.floor(d / 30)
  if (mo < 12) return mo + '个月前'
  return Math.floor(mo / 12) + '年前'
}

// 年月日格式化（安全兼容 iOS 格式）
export function formatDate(ts) {
  if (!ts) return ''
  let d
  if (typeof ts === 'string') {
    d = new Date(ts.trim().replace(/-/g, '/'))
  } else {
    d = new Date(ts)
  }
  if (isNaN(d.getTime())) return ''
  const p = n => (n < 10 ? '0' + n : '' + n)
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}

// 星级渲染（返回满星/半星/空星）
export function stars(rating) {
  const full = Math.floor(rating)
  const half = rating - full >= 0.5
  let s = '★'.repeat(full)
  if (half) s += '☆'
  s += '☆'.repeat(5 - full - (half ? 1 : 0))
  return s
}

// 获取当前可用的转盘素材（私密+已加入小组+同校）
export function getWheelPool(category, maxDistance) {
  const user = store.user
  if (!user) return store.marks
  const uid = String(user.id)
  const myGroupIds = (store.groups || [])
    .filter(g => g && g.members && g.members.some(m => String(m.id || m.userId) === uid))
    .map(g => String(g.id))

  let pool = (store.marks || []).filter(m => {
    if (!m) return false
    // 自己的全部可见
    if (m.creator && String(m.creator.id) === uid) return true
    // 小组共享
    if (m.shareScope === 'group' && Array.isArray(m.groupIds)) {
      if (m.groupIds.some(id => myGroupIds.includes(String(id)))) return true
    }
    // 同校共享
    if (m.shareScope === 'school' && m.schoolId && user.schoolId && String(m.schoolId) === String(user.schoolId)) {
      return true
    }
    return false
  })

  if (category && category !== '全部') {
    pool = pool.filter(m => m.category === category)
  }
  return pool
}

let cachedNavMetrics = null

/**
 * 获取窗口尺寸与系统基础参数（优先使用微信官方现代拆分 API，彻底消除 wx.getSystemInfoSync 弃用警告）
 */
export function getSystemWindowInfo() {
  // #ifdef MP-WEIXIN
  if (typeof wx !== 'undefined' && typeof wx.getWindowInfo === 'function') {
    try {
      const win = wx.getWindowInfo()
      if (win && (win.windowWidth || win.statusBarHeight)) return win
    } catch (e) {}
  }
  // #endif
  if (typeof uni !== 'undefined' && typeof uni.getWindowInfo === 'function') {
    try {
      const win = uni.getWindowInfo()
      if (win && (win.windowWidth || win.statusBarHeight)) return win
    } catch (e) {}
  }
  if (typeof uni !== 'undefined' && typeof uni.getSystemInfoSync === 'function') {
    try {
      return uni.getSystemInfoSync()
    } catch (e) {}
  }
  return { statusBarHeight: 44, windowWidth: 375, windowHeight: 667 }
}

/**
 * 获取小程序自定义导航栏与胶囊安全区指标
 * 用于精确避让刘海屏、状态栏与右上角微信胶囊按钮（带内存级缓存，避免高频触发）
 */
export function getNavMetrics() {
  if (cachedNavMetrics) {
    return cachedNavMetrics
  }
  let statusBarHeight = 44
  let navBarHeight = 44
  let menuRight = 96
  let menuBottom = 88

  try {
    const sys = getSystemWindowInfo()
    if (sys && sys.statusBarHeight) {
      statusBarHeight = sys.statusBarHeight
    }
    // #ifdef MP-WEIXIN
    if (typeof wx !== 'undefined' && typeof wx.getMenuButtonBoundingClientRect === 'function') {
      const menu = wx.getMenuButtonBoundingClientRect()
      if (menu && menu.top && menu.height) {
        const gap = menu.top - statusBarHeight
        navBarHeight = Math.max(gap * 2 + menu.height, 40)
        menuBottom = menu.bottom
        menuRight = (sys.windowWidth - menu.left) + 10
      } else {
        navBarHeight = 44
        menuBottom = statusBarHeight + navBarHeight
        menuRight = 96
      }
    }
    // #endif
  } catch (e) {
    statusBarHeight = 44
    navBarHeight = 44
  }

  cachedNavMetrics = {
    statusBarHeight,
    navBarHeight,
    totalHeaderHeight: statusBarHeight + navBarHeight,
    menuRight,
    menuBottom
  }
  return cachedNavMetrics
}

/**
 * 微信小程序本地文件永久转存
 * 将拍照或从相册选取的临时文件 (http://tmp/ 或 wxfile://tmp) 转存到用户沙箱目录 (wxfile://usr/)
 * 彻底防止微信开发者工具或真机重新编译、重启后临时文件被清理导致的图片破图无法显示
 */
export function persistLocalImage(tempPath) {
  if (!tempPath || typeof tempPath !== 'string') return tempPath
  // 如果已经是网络图片或非临时路径，直接返回
  if (tempPath.startsWith('http://') || tempPath.startsWith('https://')) {
    if (!tempPath.includes('/tmp/')) {
      return tempPath
    }
  }
  // #ifdef MP-WEIXIN
  try {
    if (typeof wx !== 'undefined' && wx.getFileSystemManager && wx.env && wx.env.USER_DATA_PATH) {
      const fs = wx.getFileSystemManager()
      const extMatch = tempPath.match(/\.([a-zA-Z0-9]+)$/)
      const ext = extMatch ? extMatch[1] : 'jpg'
      const targetName = 'mark_img_' + Date.now() + '_' + Math.random().toString(36).slice(2, 7) + '.' + ext
      const targetPath = `${wx.env.USER_DATA_PATH}/${targetName}`
      fs.saveFileSync(tempPath, targetPath)
      return targetPath
    }
  } catch (e) {
    console.warn('persistLocalImage fail', e)
  }
  // #endif
  return tempPath
}

/**
 * 标记列表图片数据安全清洗
 * 探测已物理清理失效的临时文件，将其剔除，防止在界面上显示空白方块破图
 */
export function sanitizeMarksImages(marks = []) {
  if (!Array.isArray(marks) || marks.length === 0) return marks
  let changed = false
  // #ifdef MP-WEIXIN
  let fs = null
  try {
    if (typeof wx !== 'undefined' && wx.getFileSystemManager) {
      fs = wx.getFileSystemManager()
    }
  } catch (e) {}
  // #endif

  const cleaned = marks.map(m => {
    if (!m || !Array.isArray(m.images) || m.images.length === 0) return m
    const validImages = m.images.filter(img => {
      if (!img || typeof img !== 'string') return false
      if (img.includes('/tmp/') || img.includes('tmp_')) {
        // #ifdef MP-WEIXIN
        if (fs) {
          try {
            fs.accessSync(img)
            return true
          } catch (err) {
            changed = true
            return false // 文件已被清理，丢弃失效路径
          }
        }
        // #endif
      }
      return true
    })
    if (validImages.length !== m.images.length) {
      changed = true
      return { ...m, images: validImages }
    }
    return m
  })

  if (changed) {
    uni.setStorageSync('fw_marks', cleaned)
    store.marks = cleaned
  }
  return cleaned
}
