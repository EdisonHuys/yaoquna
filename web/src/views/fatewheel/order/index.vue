<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="78px">
      <el-form-item label="商品类型" prop="itemType">
        <el-select v-model="queryParams.itemType" placeholder="全部" clearable style="width: 150px">
          <el-option label="Pro会员" value="pro" />
          <el-option label="无限标记" value="unlimited" />
          <el-option label="转盘皮肤" value="skin" />
          <el-option label="去广告" value="adfree" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 140px">
          <el-option label="待支付" value="pending" />
          <el-option label="已支付" value="paid" />
          <el-option label="已关闭" value="closed" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="orderList">
      <el-table-column label="订单号" align="center" prop="orderNo" width="200" />
      <el-table-column label="用户ID" align="center" prop="userId" width="90" />
      <el-table-column label="商品" align="center" width="120">
        <template #default="scope">
          <span v-if="scope.row.itemType === 'pro'">Pro会员</span>
          <span v-else-if="scope.row.itemType === 'unlimited'">无限标记</span>
          <span v-else-if="scope.row.itemType === 'skin'">转盘皮肤</span>
          <span v-else-if="scope.row.itemType === 'adfree'">去广告</span>
          <span v-else>{{ scope.row.itemType }}</span>
        </template>
      </el-table-column>
      <el-table-column label="金额" align="center" prop="amount" width="100" />
      <el-table-column label="状态" align="center" width="90">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'paid'" type="success">已支付</el-tag>
          <el-tag v-else-if="scope.row.status === 'pending'" type="warning">待支付</el-tag>
          <el-tag v-else type="info">已关闭</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" align="center" prop="payTime" width="160" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="160" />
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script setup name="FwOrder">
import { listOrder } from '@/api/fatewheel/order'

const { proxy } = getCurrentInstance()
const orderList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, itemType: undefined, status: undefined }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows
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
