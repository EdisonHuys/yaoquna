<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="78px">
      <el-form-item label="地点名称" prop="keyword">
        <el-input v-model="queryParams.keyword" placeholder="请输入地点名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="全部分类" clearable style="width: 160px">
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
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['fatewheel:mark:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['fatewheel:mark:remove']">删除</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="markList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="markId" width="70" />
      <el-table-column label="地点名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="分类" align="center" prop="category" width="80" />
      <el-table-column label="创建者" align="center" prop="creatorName" width="100" />
      <el-table-column label="共享范围" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.shareScope === 'private'">仅自己</el-tag>
          <el-tag v-else-if="scope.row.shareScope === 'group'" type="success">小组</el-tag>
          <el-tag v-else type="warning">同校</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'normal'" type="success">未去</el-tag>
          <el-tag v-else-if="scope.row.status === 'visited'" type="info">已去</el-tag>
          <el-tag v-else type="danger">已下架</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="收藏数" align="center" prop="favoriteCount" width="80" />
      <el-table-column label="学校" align="center" prop="schoolName" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['fatewheel:mark:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['fatewheel:mark:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="title" v-model="open" width="520px" append-to-body>
      <el-form ref="markRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="地点名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入地点名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="共享范围" prop="shareScope">
          <el-radio-group v-model="form.shareScope">
            <el-radio value="private">仅自己</el-radio>
            <el-radio value="group">小组</el-radio>
            <el-radio value="school">同校</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="normal">未去</el-radio>
            <el-radio value="visited">已去</el-radio>
            <el-radio value="off">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评分" prop="rating">
          <el-rate v-model="form.rating" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="小贴士" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="FwMark">
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

const data = reactive({
  form: {},
  queryParams: { pageNum: 1, pageSize: 10, keyword: undefined, category: undefined, shareScope: undefined },
  rules: {
    name: [{ required: true, message: '地点名称不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function getList() {
  loading.value = true
  listMark(queryParams.value).then(response => {
    markList.value = response.rows
    total.value = response.total
    loading.value = false
  })
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
    rating: 0,
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
  title.value = '新增标记'
}

function handleUpdate(row) {
  reset()
  const id = row.markId
  getMark(id).then(response => {
    form.value = response.data
    open.value = true
    title.value = '修改标记'
  })
}

function submitForm() {
  proxy.$refs['markRef'].validate(valid => {
    if (valid) {
      if (form.value.markId != null) {
        updateMark(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        addMark(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

function handleDelete(row) {
  const markIds = row.markId || ids.value
  proxy.$modal.confirm('是否确认删除标记编号为"' + markIds + '"的数据项？').then(function () {
    return delMark(markIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

getList()
</script>
