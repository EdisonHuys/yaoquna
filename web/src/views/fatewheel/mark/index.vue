<template>
  <div class="app-container">
    <!-- 筛选表单 -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="78px">
      <el-form-item label="地点名称" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="请输入地点名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="全部分类" clearable style="width: 150px">
          <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>
      <el-form-item label="共享范围" prop="shareScope">
        <el-select v-model="queryParams.shareScope" placeholder="全部" clearable style="width: 140px">
          <el-option label="仅自己" value="private" />
          <el-option label="小组" value="group" />
          <el-option label="同校" value="school" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="未去" value="normal" />
          <el-option label="已去" value="visited" />
          <el-option label="下架" value="off" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['fatewheel:mark:add']">新增地点</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['fatewheel:mark:remove']">批量删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 地点数据表格 -->
    <el-table v-loading="loading" :data="markList" @selection-change="handleSelectionChange" border stripe>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" align="center" prop="markId" width="70" />
      <el-table-column label="封面图" align="center" width="85">
        <template #default="scope">
          <div class="table-cover-box" v-if="getFirstImage(scope.row.images)">
            <el-image
              :src="getFirstImage(scope.row.images)"
              :preview-src-list="parseImageList(scope.row.images)"
              fit="cover"
              class="table-thumb"
              preview-teleported
            />
          </div>
          <span v-else class="text-muted text-xs">无图</span>
        </template>
      </el-table-column>
      <el-table-column label="地点名称" align="left" prop="name" :show-overflow-tooltip="true" min-width="140">
        <template #default="scope">
          <span class="font-bold cursor-pointer hover-link" @click="handleDetail(scope.row)">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column label="分类" align="center" prop="category" width="85">
        <template #default="scope">
          <el-tag :type="getCategoryTagType(scope.row.category)" effect="light" size="small">{{ scope.row.category }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评分" align="center" prop="rating" width="95">
        <template #default="scope">
          <span class="rate-text" v-if="scope.row.rating">⭐ {{ scope.row.rating }}分</span>
          <span v-else class="text-muted text-xs">-</span>
        </template>
      </el-table-column>
      <el-table-column label="人均" align="center" prop="price" width="85">
        <template #default="scope">
          <span v-if="scope.row.price && scope.row.price > 0" class="price-text">¥{{ scope.row.price }}</span>
          <span v-else class="text-muted text-xs">免费</span>
        </template>
      </el-table-column>
      <el-table-column label="创建者" align="center" width="120">
        <template #default="scope">
          <div class="creator-cell">
            <el-avatar :size="22" :src="scope.row.creatorAvatar" v-if="scope.row.creatorAvatar" />
            <el-avatar :size="22" v-else class="avatar-default">{{ (scope.row.creatorName || '用')[0] }}</el-avatar>
            <span class="creator-name" :title="scope.row.creatorName">{{ scope.row.creatorName || '匿名' }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="共享范围" align="center" width="95">
        <template #default="scope">
          <el-tag v-if="scope.row.shareScope === 'private'" type="info" size="small">仅自己</el-tag>
          <el-tag v-else-if="scope.row.shareScope === 'group'" type="success" size="small">小组</el-tag>
          <el-tag v-else type="warning" size="small">同校</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="85">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'normal'" type="success" size="small">想去</el-tag>
          <el-tag v-else-if="scope.row.status === 'visited'" type="info" size="small">已去</el-tag>
          <el-tag v-else type="danger" size="small">下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收藏数" align="center" prop="favoriteCount" width="75" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="155" />
      <el-table-column label="操作" align="center" width="180" fixed="right" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['fatewheel:mark:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['fatewheel:mark:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- ===== 地点全维度详情抽屉 ===== -->
    <el-drawer
      title="地点标记全量详情"
      v-model="detailOpen"
      size="680px"
      append-to-body
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-container">
        <!-- 头部精美 Hero 卡片 -->
        <div class="detail-hero-card">
          <div class="hero-left">
            <div class="hero-cat-avatar" :style="{ background: getCategoryBg(detailData.category) }">
              {{ getCategoryEmoji(detailData.category) }}
            </div>
            <div class="hero-main">
              <div class="hero-title-row">
                <span class="hero-name">{{ detailData.name || '未命名地点' }}</span>
                <el-tag :type="getScopeTagType(detailData.shareScope)" effect="plain" round size="small">
                  {{ getScopeLabel(detailData.shareScope) }}
                </el-tag>
                <el-tag :type="getStatusTagType(detailData.status)" effect="dark" round size="small">
                  {{ getStatusLabel(detailData.status) }}
                </el-tag>
              </div>
              <div class="hero-sub-row">
                <span class="hero-cat-text">{{ detailData.category || '未分类' }}</span>
                <span class="hero-dot">·</span>
                <span class="hero-price-tag" v-if="detailData.price && detailData.price > 0">¥{{ detailData.price }}/人</span>
                <span class="hero-price-tag" v-else>未设人均消费</span>
                <span class="hero-dot">·</span>
                <span class="hero-fav-tag">❤️ {{ detailData.favoriteCount || 0 }} 次收藏</span>
              </div>
            </div>
          </div>
          <div class="hero-rate-box">
            <el-rate v-model="detailData.rating" disabled show-score text-color="#ff9900" score-template="{value}分" />
          </div>
        </div>

        <!-- 基础属性描述表 -->
        <el-descriptions title="📌 核心属性" :column="2" border class="mt-4">
          <el-descriptions-item label="地点 ID">
            <el-tag size="small" type="info">#{{ detailData.markId }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="地点分类">
            <el-tag size="small" :type="getCategoryTagType(detailData.category)">{{ detailData.category || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="人均消费">
            <span v-if="detailData.price && detailData.price > 0" class="price-highlight">¥{{ detailData.price }} 元/人</span>
            <span v-else class="text-muted">未设消费金额</span>
          </el-descriptions-item>
          <el-descriptions-item label="打卡状态">
            <el-tag size="small" :type="getStatusTagType(detailData.status)">{{ getStatusLabel(detailData.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="详细地址" :span="2">
            <span v-if="detailData.address">📍 {{ detailData.address }}</span>
            <span v-else class="text-muted">未提供详细地址</span>
          </el-descriptions-item>
          <el-descriptions-item label="经纬度坐标" :span="2">
            <span v-if="detailData.lat && detailData.lng" class="geo-coord">
              🌍 经度: <strong>{{ detailData.lng }}</strong> ， 纬度: <strong>{{ detailData.lat }}</strong>
              <el-button link type="primary" size="small" icon="CopyDocument" @click="copyGeo(detailData.lng, detailData.lat)">复制坐标</el-button>
            </span>
            <span v-else class="text-muted">未记录 GPS 坐标</span>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 归属与人员体系 -->
        <el-descriptions title="👤 归属与共享关系" :column="2" border class="mt-4">
          <el-descriptions-item label="创建者">
            <div class="creator-cell">
              <el-avatar :size="24" :src="detailData.creatorAvatar" v-if="detailData.creatorAvatar" />
              <el-avatar :size="24" v-else class="avatar-default">{{ (detailData.creatorName || '用')[0] }}</el-avatar>
              <span class="creator-name font-bold">{{ detailData.creatorName || '匿名同学' }}</span>
              <span class="creator-id text-muted" v-if="detailData.userId">(UID: {{ detailData.userId }})</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="共享范围">
            <el-tag :type="getScopeTagType(detailData.shareScope)" size="small">{{ getScopeLabel(detailData.shareScope) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="所属学校">
            {{ detailData.schoolName || (detailData.schoolId ? ('学校 ID: ' + detailData.schoolId) : '未关联学校') }}
          </el-descriptions-item>
          <el-descriptions-item label="共享小组">
            <span v-if="detailData.groupIds">👥 {{ detailData.groupIds }}</span>
            <span v-else class="text-muted">未向小组共享</span>
          </el-descriptions-item>
          <el-descriptions-item label="入库时间">
            {{ detailData.createTime || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="更新时间">
            {{ detailData.updateTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <!-- 标签胶囊流 -->
        <div class="detail-section-box mt-4">
          <div class="section-header-title">🏷️ 地点标签 (Tags)</div>
          <div class="tags-cloud" v-if="parsedTags.length > 0">
            <el-tag v-for="tag in parsedTags" :key="tag" effect="plain" class="mr-2 mb-2" round size="default">
              # {{ tag }}
            </el-tag>
          </div>
          <div v-else class="empty-tip-text">暂无标签信息</div>
        </div>

        <!-- 备注与小贴士 -->
        <div class="detail-section-box mt-4">
          <div class="section-header-title">💡 备注与心得小贴士</div>
          <div v-if="detailData.remark" class="remark-quote-card">
            {{ detailData.remark }}
          </div>
          <div v-else class="empty-tip-text">暂无心得或避坑指南</div>
        </div>

        <!-- 实景照片多图画廊与大图预览（重点！） -->
        <div class="detail-section-box mt-4 mb-4">
          <div class="section-header-between">
            <div class="section-header-title">📷 实景照片画廊</div>
            <span class="photo-badge-count" v-if="parsedImages.length > 0">共 {{ parsedImages.length }} 张（点击图片支持全屏灯箱轮播与缩放）</span>
          </div>

          <div v-if="parsedImages.length > 0" class="image-grid-container">
            <div v-for="(img, idx) in parsedImages" :key="idx" class="image-card-cell">
              <div v-if="img.isWxSandbox" class="wx-sandbox-tag">小程序本地暂存</div>
              <el-image
                :src="img.previewUrl"
                :preview-src-list="previewList"
                :initial-index="idx"
                preview-teleported
                fit="cover"
                class="gallery-preview-image"
                loading="lazy"
              >
                <template #placeholder>
                  <div class="img-slot-box">
                    <el-icon class="is-loading"><Loading /></el-icon>
                    <span>加载中…</span>
                  </div>
                </template>
                <template #error>
                  <div class="img-slot-box error-slot">
                    <el-icon :size="24"><PictureFilled /></el-icon>
                    <span v-if="img.isWxSandbox" class="sandbox-tip">手机本地沙箱路径<br>({{ img.raw.slice(0, 20) }}…)</span>
                    <span v-else class="sandbox-tip">图片加载失败</span>
                  </div>
                </template>
              </el-image>
            </div>
          </div>
          <div v-else class="no-images-placeholder">
            <el-icon :size="48" class="placeholder-icon"><Picture /></el-icon>
            <div class="placeholder-title">暂无上传实景照片</div>
            <div class="placeholder-desc">用户打卡或创建时未附带实景图片</div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="drawer-footer-actions">
          <el-button @click="detailOpen = false">关 闭</el-button>
          <el-button type="primary" icon="Edit" @click="handleEditFromDetail" v-hasPermi="['fatewheel:mark:edit']">修改此地点</el-button>
        </div>
      </template>
    </el-drawer>

    <!-- ===== 新增/编辑对话框（已补齐全部字段） ===== -->
    <el-dialog :title="title" v-model="open" width="620px" append-to-body destroy-on-close>
      <el-form ref="markRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="地点名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入地点名称" maxlength="60" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="地点分类" prop="category">
              <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
                <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人均消费" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="5" placeholder="元/人" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="address">
              <el-input v-model="form.address" placeholder="请输入详细地址或路名门牌号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="经度 (lng)" prop="lng">
              <el-input-number v-model="form.lng" :precision="6" :step="0.001" placeholder="如 120.637200" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="纬度 (lat)" prop="lat">
              <el-input-number v-model="form.lat" :precision="6" :step="0.001" placeholder="如 31.298900" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="共享范围" prop="shareScope">
              <el-radio-group v-model="form.shareScope">
                <el-radio value="private">仅自己可见（私密）</el-radio>
                <el-radio value="group">小组共享</el-radio>
                <el-radio value="school">同校共享</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="打卡状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio value="normal">想去（待打卡）</el-radio>
                <el-radio value="visited">已去（已打卡）</el-radio>
                <el-radio value="off">下架隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地点评分" prop="rating">
              <el-rate v-model="form.rating" show-score text-color="#ff9900" score-template="{value}分" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="地点标签" prop="tags">
              <el-input v-model="form.tags" placeholder="多个标签以英文逗号分隔，如：夜市,烧烤,网红店" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="照片URL" prop="images">
              <el-input v-model="form.images" type="textarea" :rows="2" placeholder="多张照片URL请用英文逗号分隔，支持相对路径 /profile/... 或网络图片 http(s)://" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="心得备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="填写避坑指南、招牌推荐或打卡心得" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">保 存</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FwMark">
import { ref, reactive, toRefs, computed, getCurrentInstance } from 'vue'
import { listMark, getMark, addMark, updateMark, delMark } from '@/api/fatewheel/mark'

const { proxy } = getCurrentInstance()
const categoryOptions = ['美食', '玩乐', '购物', '自习', '其他']

const markList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const open = ref(false)

// 详情抽屉控制与数据
const detailOpen = ref(false)
const detailLoading = ref(false)
const detailData = ref({})

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    keyword: undefined,
    category: undefined,
    shareScope: undefined,
    status: undefined
  },
  rules: {
    name: [{ required: true, message: '地点名称不能为空', trigger: 'blur' }],
    category: [{ required: true, message: '请选择地点分类', trigger: 'change' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

/** 查询地点标记列表 */
function getList() {
  loading.value = true
  listMark(queryParams.value).then(response => {
    markList.value = response.rows || []
    total.value = response.total || 0
    loading.value = false
  }).catch(() => {
    loading.value = false
  })
}

/** 详情解析：标签数组 */
const parsedTags = computed(() => {
  const raw = detailData.value.tags
  if (!raw) return []
  if (Array.isArray(raw)) return raw.filter(Boolean)
  if (typeof raw === 'string') {
    return raw.split(',').map(s => s.trim()).filter(Boolean)
  }
  return []
})

/** 详情解析：图片列表与格式化前缀 */
const parsedImages = computed(() => {
  const raw = detailData.value.images
  if (!raw) return []
  let arr = []
  if (Array.isArray(raw)) {
    arr = raw
  } else if (typeof raw === 'string') {
    const trimmed = raw.trim()
    if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
      try {
        arr = JSON.parse(trimmed)
      } catch (e) {
        arr = trimmed.split(',')
      }
    } else {
      arr = trimmed.split(',')
    }
  }
  return arr.map(s => String(s || '').trim()).filter(Boolean).map(url => {
    const isWxSandbox = url.startsWith('wxfile://') || url.includes('/tmp/') || url.includes('tmp_')
    let previewUrl = url
    if (url.startsWith('/profile/')) {
      const baseApi = import.meta.env.VITE_APP_BASE_API || ''
      previewUrl = baseApi + url
    }
    return {
      raw: url,
      previewUrl,
      isWxSandbox
    }
  })
})

/** 详情大图轮播列表 */
const previewList = computed(() => {
  return parsedImages.value.map(i => i.previewUrl)
})

/** 获取第一张图片用于表格缩略图 */
function getFirstImage(images) {
  if (!images) return null
  let first = ''
  if (Array.isArray(images)) {
    first = images[0] || ''
  } else if (typeof images === 'string') {
    first = images.split(',')[0] || ''
  }
  if (!first) return null
  first = first.trim()
  if (first.startsWith('/profile/')) {
    return (import.meta.env.VITE_APP_BASE_API || '') + first
  }
  return first
}

/** 表格多图预览列表解析 */
function parseImageList(images) {
  if (!images) return []
  const arr = typeof images === 'string' ? images.split(',') : (Array.isArray(images) ? images : [])
  return arr.map(s => {
    const u = String(s || '').trim()
    if (u.startsWith('/profile/')) {
      return (import.meta.env.VITE_APP_BASE_API || '') + u
    }
    return u
  }).filter(Boolean)
}

/** 辅助标签映射方法 */
function getCategoryTagType(cat) {
  const map = { '美食': 'warning', '玩乐': 'success', '购物': 'primary', '自习': 'info' }
  return map[cat] || ''
}

function getCategoryEmoji(cat) {
  const map = { '美食': '🍽️', '玩乐': '🎡', '购物': '🛍️', '自习': '📚', '其他': '📍' }
  return map[cat] || '📍'
}

function getCategoryBg(cat) {
  const map = {
    '美食': 'linear-gradient(135deg, #FFEDD5 0%, #FED7AA 100%)',
    '玩乐': 'linear-gradient(135deg, #FEF08A 0%, #FDE047 100%)',
    '购物': 'linear-gradient(135deg, #E0F2FE 0%, #BAE6FD 100%)',
    '自习': 'linear-gradient(135deg, #DCFCE7 0%, #BBF7D0 100%)'
  }
  return map[cat] || 'linear-gradient(135deg, #F1F5F9 0%, #E2E8F0 100%)'
}

function getScopeTagType(scope) {
  if (scope === 'group') return 'success'
  if (scope === 'school') return 'warning'
  return 'info'
}

function getScopeLabel(scope) {
  if (scope === 'group') return '小组共享'
  if (scope === 'school') return '同校共享'
  return '仅自己可见'
}

function getStatusTagType(status) {
  if (status === 'visited') return 'info'
  if (status === 'off') return 'danger'
  return 'success'
}

function getStatusLabel(status) {
  if (status === 'visited') return '已去 (打卡)'
  if (status === 'off') return '已下架'
  return '想去 (待去)'
}

/** 复制经纬度 */
function copyGeo(lng, lat) {
  const text = `${lng}, ${lat}`
  navigator.clipboard.writeText(text).then(() => {
    proxy.$modal.msgSuccess('经纬度坐标已复制到剪贴板')
  }).catch(() => {
    proxy.$modal.msg('坐标：' + text)
  })
}

/** 打开地点全量详情抽屉 */
function handleDetail(row) {
  const id = row.markId
  detailLoading.value = true
  detailOpen.value = true
  getMark(id).then(response => {
    detailData.value = response.data || {}
    detailLoading.value = false
  }).catch(() => {
    detailLoading.value = false
  })
}

/** 从详情页直接跳转编辑 */
function handleEditFromDetail() {
  detailOpen.value = false
  handleUpdate(detailData.value)
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    markId: undefined,
    name: undefined,
    category: '美食',
    shareScope: 'private',
    status: 'normal',
    rating: 5,
    price: 0,
    address: undefined,
    lat: 31.2989,
    lng: 120.6372,
    tags: undefined,
    images: undefined,
    remark: undefined
  }
  proxy.resetForm('markRef')
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.markId)
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增地点标记'
}

function handleUpdate(row) {
  reset()
  const id = row.markId
  getMark(id).then(response => {
    form.value = { ...response.data }
    open.value = true
    title.value = '修改地点标记'
  })
}

function submitForm() {
  proxy.$refs['markRef'].validate(valid => {
    if (valid) {
      if (form.value.markId != null) {
        updateMark(form.value).then(() => {
          proxy.$modal.msgSuccess('修改地点成功')
          open.value = false
          getList()
        })
      } else {
        addMark(form.value).then(() => {
          proxy.$modal.msgSuccess('新增地点成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const markIds = row.markId || ids.value
  proxy.$modal.confirm('是否确认删除地点标记编号为 "' + markIds + '" 的数据项？').then(function () {
    return delMark(markIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>

<style scoped lang="scss">
.cursor-pointer { cursor: pointer; }
.hover-link:hover { color: #07c160; text-decoration: underline; }
.font-bold { font-weight: 600; }
.text-muted { color: #94a3b8; }
.text-xs { font-size: 12px; }
.price-text { color: #ea580c; font-weight: 600; font-size: 13px; }
.price-highlight { color: #ea580c; font-weight: 700; font-size: 15px; }
.rate-text { color: #d97706; font-weight: 600; font-size: 13px; }

.table-cover-box {
  width: 50px;
  height: 50px;
  margin: 0 auto;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}

.table-thumb {
  width: 100%;
  height: 100%;
  display: block;
}

.creator-cell {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  .creator-name {
    font-size: 12px;
    color: #334155;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .avatar-default {
    background: #10b981;
    color: #fff;
    font-size: 11px;
  }
}

/* 详情抽屉内部样式 */
.detail-container {
  padding: 0 8px;
}

.detail-hero-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 18px 20px;
  margin-bottom: 20px;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-cat-avatar {
  width: 54px;
  height: 54px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.hero-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.hero-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.hero-name {
  font-size: 19px;
  font-weight: 700;
  color: #0f172a;
}

.hero-sub-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #64748b;
}

.hero-dot { color: #cbd5e1; }
.hero-price-tag { color: #f97316; font-weight: 600; }
.hero-fav-tag { color: #ef4444; }

.detail-section-box {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 16px 18px;
  .section-header-title {
    font-size: 15px;
    font-weight: 600;
    color: #1e293b;
    margin-bottom: 12px;
  }
  .section-header-between {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    .photo-badge-count {
      font-size: 12px;
      color: #64748b;
    }
  }
  .empty-tip-text {
    font-size: 13px;
    color: #94a3b8;
    font-style: italic;
  }
}

.tags-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.remark-quote-card {
  background: #f8fafc;
  border-left: 4px solid #10b981;
  padding: 12px 16px;
  border-radius: 0 8px 8px 0;
  color: #334155;
  font-size: 14px;
  line-height: 1.6;
}

.geo-coord {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #334155;
}

/* 多图画廊网格 */
.image-grid-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.image-card-cell {
  position: relative;
  aspect-ratio: 4 / 3;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
}

.wx-sandbox-tag {
  position: absolute;
  top: 6px;
  left: 6px;
  z-index: 2;
  background: rgba(15, 23, 42, 0.75);
  color: #fbbf24;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  pointer-events: none;
}

.gallery-preview-image {
  width: 100%;
  height: 100%;
  display: block;
  cursor: pointer;
  transition: transform 0.2s ease;
  &:hover {
    transform: scale(1.03);
  }
}

.img-slot-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #94a3b8;
  font-size: 12px;
  gap: 6px;
  padding: 8px;
  text-align: center;
}

.error-slot {
  background: #f1f5f9;
  color: #64748b;
  .sandbox-tip {
    font-size: 10px;
    line-height: 1.3;
    color: #94a3b8;
  }
}

.no-images-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 36px 0;
  background: #f8fafc;
  border-radius: 8px;
  border: 1px dashed #cbd5e1;
  .placeholder-icon {
    color: #cbd5e1;
    margin-bottom: 10px;
  }
  .placeholder-title {
    font-size: 14px;
    font-weight: 600;
    color: #64748b;
  }
  .placeholder-desc {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 4px;
  }
}

.drawer-footer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
