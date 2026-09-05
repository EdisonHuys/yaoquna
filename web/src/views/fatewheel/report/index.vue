<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="78px">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
          <el-option label="待处理" value="pending" />
          <el-option label="已处理" value="handled" />
          <el-option label="驳回" value="rejected" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="reportList">
      <el-table-column label="ID" align="center" prop="reportId" width="70" />
      <el-table-column label="被举报地点" align="center" prop="markName" :show-overflow-tooltip="true" />
      <el-table-column label="举报人" align="center" prop="reporterName" width="110" />
      <el-table-column label="举报原因" align="center" prop="reason" :show-overflow-tooltip="true" />
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'pending'" type="danger">待处理</el-tag>
          <el-tag v-else-if="scope.row.status === 'handled'" type="success">已处理</el-tag>
          <el-tag v-else type="info">驳回</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="处理备注" align="center" prop="handleRemark" :show-overflow-tooltip="true" />
      <el-table-column label="举报时间" align="center" prop="createTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="Check" @click="handleHandle(scope.row, 'handled')" v-hasPermi="['fatewheel:report:edit']">通过</el-button>
          <el-button link type="warning" icon="Close" @click="handleHandle(scope.row, 'rejected')" v-hasPermi="['fatewheel:report:edit']">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="FwReport">
import { listReport, handleReport } from '@/api/fatewheel/report'

const { proxy } = getCurrentInstance()
const reportList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, status: undefined }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listReport(queryParams.value).then(response => {
    reportList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleHandle(row, status) {
  const text = status === 'handled' ? '确认举报成立，处理该举报？' : '确认举报不成立，驳回该举报？'
  proxy.$modal.confirm(text).then(function () {
    return handleReport({ reportId: row.reportId, status })
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('处理成功')
  }).catch(() => {})
}

getList()
</script>
