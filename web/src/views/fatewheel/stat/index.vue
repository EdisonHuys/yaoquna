<template>
  <div class="app-container">
    <el-row :gutter="20" class="mb8">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-num">{{ overview.markCount ?? 0 }}</div>
          <div class="stat-label">地点标记总数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-num">{{ overview.paidOrderCount ?? 0 }}</div>
          <div class="stat-label">已支付订单数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-num">{{ totalCategory }}</div>
          <div class="stat-label">分类数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <template #header>
        <span>地点标记分类分布</span>
      </template>
      <el-table :data="categoryList" style="width: 100%">
        <el-table-column label="分类" align="center" prop="category" />
        <el-table-column label="数量" align="center" prop="cnt" />
        <el-table-column label="占比" align="center" width="260">
          <template #default="scope">
            <el-progress :percentage="percent(scope.row.cnt)" :stroke-width="14" />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="categoryList.length === 0" description="暂无数据" />
    </el-card>
  </div>
</template>

<script setup name="FwStat">
import { getOverview } from '@/api/fatewheel/stat'

const overview = ref({ markCount: 0, markByCategory: [], paidOrderCount: 0 })
const categoryList = ref([])

const totalCategory = computed(() => {
  return categoryList.value.length
})

function percent(cnt) {
  const total = overview.value.markCount || 0
  if (total === 0) return 0
  return Math.round(cnt * 100 / total)
}

function getData() {
  getOverview().then(response => {
    overview.value = response.data
    categoryList.value = (response.data.markByCategory || []).map(row => ({
      category: row.category,
      cnt: Number(row.cnt)
    }))
  })
}

getData()
</script>

<style scoped>
.stat-num {
  font-size: 36px;
  font-weight: 700;
  color: #2ec77e;
  text-align: center;
}
.stat-label {
  text-align: center;
  color: #909399;
  margin-top: 8px;
}
</style>
