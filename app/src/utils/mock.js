/**
 * Mock 数据源
 * 用途：本地开发/预览阶段的数据，后续切换 uniCloud 时替换 api/index.js 的实现即可
 * 场景设定：苏州大学（示例学校）
 */

const NOW = Date.now()
const day = 86400000

export const mockUser = {
  id: 'u_1001',
  nickname: '小命运',
  avatar: '',
  schoolId: 'school_suda',
  schoolName: '苏州大学',
  isPro: false,
  streak: 23,
  sharedCount: 12,
  totalFavorites: 128,
  createdAt: NOW - 200 * day
}

export const mockGroups = [
  {
    id: 'g_1',
    name: '310宿舍小分队',
    avatar: '',
    memberCount: 5,
    members: [
      { id: 'u_1001', nickname: '小命运', role: 'owner' },
      { id: 'u_1002', nickname: '阿伟', role: 'member' },
      { id: 'u_1003', nickname: '小美', role: 'member' },
      { id: 'u_1004', nickname: '小胖', role: 'member' },
      { id: 'u_1005', nickname: '老猫', role: 'member' }
    ],
    markCount: 48,
    decisionCount: 12,
    weekNew: 8,
    weedOutRate: 32,
    createdAt: NOW - 90 * day
  }
]

export const mockMarks = [
  {
    id: 'm_1',
    name: '老王烧烤',
    category: '美食',
    address: '苏州大学东校区东门步行街',
    lat: 31.2989,
    lng: 120.6372,
    tags: ['烧烤', '夜宵', '宿舍最爱'],
    rating: 4,
    price: 45,
    note: '羊肉串yyds，晚上人巨多，建议9点后去',
    images: [],
    shareScope: 'group',
    groupIds: ['g_1'],
    schoolId: null,
    status: '想去',
    createTime: NOW - 5 * day,
    favoriteCount: 32,
    reportCount: 0,
    creator: { id: 'u_1001', nickname: '小命运' },
    contributions: []
  },
  {
    id: 'm_2',
    name: '独墅湖图书馆',
    category: '自习',
    address: '苏州大学独墅湖校区',
    lat: 31.2862,
    lng: 120.7336,
    tags: ['自习', '安静', '期末必去'],
    rating: 5,
    price: 0,
    note: '3楼靠窗位置光线最好，要早起占座',
    images: [],
    shareScope: 'school',
    groupIds: [],
    schoolId: 'school_suda',
    status: '已去',
    createTime: NOW - 20 * day,
    favoriteCount: 98,
    reportCount: 0,
    creator: { id: 'u_1006', nickname: '学姐阿岚' },
    contributions: []
  },
  {
    id: 'm_3',
    name: '金鸡湖音乐节',
    category: '玩乐',
    address: '苏州工业园区金鸡湖',
    lat: 31.3161,
    lng: 120.7219,
    tags: ['音乐节', '周末', '人多'],
    rating: 5,
    price: 180,
    note: '每年6月，阵容不错，早点去占前排',
    images: [],
    shareScope: 'school',
    groupIds: [],
    schoolId: 'school_suda',
    status: '想去',
    createTime: NOW - 3 * day,
    favoriteCount: 66,
    reportCount: 0,
    creator: { id: 'u_1007', nickname: '学长阿凯' },
    contributions: []
  },
  {
    id: 'm_4',
    name: '山塘街糖粥铺',
    category: '美食',
    address: '苏州山塘街',
    lat: 31.3136,
    lng: 120.6108,
    tags: ['甜品', '老字号', '打卡'],
    rating: 4,
    price: 20,
    note: '桂花糖粥很绝，排队20分钟起',
    images: [],
    shareScope: 'school',
    groupIds: [],
    schoolId: 'school_suda',
    status: '想去',
    createTime: NOW - 12 * day,
    favoriteCount: 45,
    reportCount: 0,
    creator: { id: 'u_1008', nickname: '同学小何' },
    contributions: []
  },
  {
    id: 'm_5',
    name: '平江路奶茶店',
    category: '美食',
    address: '苏州平江路',
    lat: 31.3146,
    lng: 120.6281,
    tags: ['奶茶', '网红店'],
    rating: 3,
    price: 25,
    note: '装修好看，味道一般，胜在出片',
    images: [],
    shareScope: 'private',
    groupIds: [],
    schoolId: null,
    status: '已去',
    createTime: NOW - 30 * day,
    favoriteCount: 0,
    reportCount: 0,
    creator: { id: 'u_1001', nickname: '小命运' },
    contributions: []
  },
  {
    id: 'm_6',
    name: '宜家家居',
    category: '购物',
    address: '苏州工业园区葑亭大道',
    lat: 31.3489,
    lng: 120.7164,
    tags: ['家具', '便宜', '肉丸'],
    rating: 4,
    price: 80,
    note: '一块钱冰淇淋和肉丸，逛完顺手买点收纳',
    images: [],
    shareScope: 'group',
    groupIds: ['g_1'],
    schoolId: null,
    status: '想去',
    createTime: NOW - 2 * day,
    favoriteCount: 0,
    reportCount: 0,
    creator: { id: 'u_1002', nickname: '阿伟' },
    contributions: []
  },
  {
    id: 'm_7',
    name: '书城二手书店',
    category: '购物',
    address: '苏州观前街',
    lat: 31.3115,
    lng: 120.6279,
    tags: ['二手书', '便宜', '教材'],
    rating: 4,
    price: 15,
    note: '考研教材半价收，学长学姐的旧书都在这里',
    images: [],
    shareScope: 'school',
    groupIds: [],
    schoolId: 'school_suda',
    status: '想去',
    createTime: NOW - 7 * day,
    favoriteCount: 51,
    reportCount: 0,
    creator: { id: 'u_1009', nickname: '学姐Kiki' },
    contributions: []
  }
]

export const mockDecisions = [
  {
    id: 'd_1',
    type: 'choose', // choose=选什么 go=去不去 buy=买不买
    result: '老王烧烤',
    title: '转盘决定：去吃老王烧烤',
    markId: 'm_1',
    time: NOW - 4 * day,
    detail: '从 6 个美食候选里，命运帮你选中了它',
    executed: true
  },
  {
    id: 'd_2',
    type: 'go',
    result: '去',
    title: '要不要去金鸡湖音乐节',
    markId: 'm_3',
    time: NOW - 2 * day,
    detail: '利弊天平：4 利 1 弊，命运倾向「去」',
    executed: false
  },
  {
    id: 'd_3',
    type: 'buy',
    result: '先别买',
    title: '要不要买新款 AirPods',
    markId: null,
    time: NOW - 1 * day,
    detail: '价值打分 11/20，建议「再想想」',
    executed: false
  },
  {
    id: 'd_4',
    type: 'choose',
    result: '山塘街糖粥铺',
    title: '转盘决定：周末去山塘街',
    markId: 'm_4',
    time: NOW - 6 * day,
    detail: '从 3 个玩乐候选里，命运帮你选中了它',
    executed: true
  }
]

export const mockAlumniFeed = [
  { id: 'a_1', title: '新学期打卡：独墅湖自习地图', author: '学姐阿岚', count: 98, time: '2天前' },
  { id: 'a_2', title: '学长私藏：苏州10家高性价比小吃', author: '学长阿凯', count: 66, time: '5天前' },
  { id: 'a_3', title: '新生必看：上课教室周边攻略', author: '同学小何', count: 45, time: '1周前' }
]
