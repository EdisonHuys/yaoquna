<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="78px">
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="是否Pro" prop="isPro">
        <el-select v-model="queryParams.isPro" placeholder="全部" clearable style="width: 130px">
          <el-option label="普通用户" value="0" />
          <el-option label="Pro会员" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="userList">
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="昵称" align="center" prop="nickName" width="120" />
      <el-table-column label="手机号" align="center" prop="phonenumber" width="130" />
      <el-table-column label="学校" align="center" prop="schoolName" width="150" :show-overflow-tooltip="true" />
      <el-table-column label="连续决策" align="center" prop="streak" width="100" />
      <el-table-column label="共享地点" align="center" prop="sharedCount" width="100" />
      <el-table-column label="被收藏" align="center" prop="totalFavorites" width="100" />
      <el-table-column label="Pro" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.isPro === '1'" type="warning">PRO</el-tag>
          <el-tag v-else type="info">普通</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" align="center" prop="createTime" width="160" />
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="FwUserExtend">
import { listUserExtend } from '@/api/fatewheel/userext'

const { proxy } = getCurrentInstance()
const userList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, userId: undefined, isPro: undefined }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listUserExtend(queryParams.value).then(response => {
    userList.value = response.rows
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

getList()
</script>
