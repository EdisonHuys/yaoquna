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
  const diff = Date.now() - ts
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

// 年月日格式化
export function formatDate(ts) {
  if (!ts) return ''
  const d = new Date(ts)
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
  const myGroupIds = store.groups
    .filter(g => g.members.some(m => m.id === user.id))
    .map(g => g.id)
  let pool = store.marks.filter(m => {
    if (m.creator.id === user.id) return true // 自己的全部可见
    if (m.shareScope === 'group' && m.groupIds.some(id => myGroupIds.includes(id))) return true
    if (m.shareScope === 'school' && m.schoolId && m.schoolId === user.schoolId) return true
    return false
  })
  if (category && category !== '全部') {
    pool = pool.filter(m => m.category === category)
  }
  return pool
}
