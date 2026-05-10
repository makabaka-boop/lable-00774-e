<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">告警记录</h2>
      <div class="filter-bar">
        <el-select v-model="filters.severity" placeholder="告警级别" clearable style="width: 120px" @change="loadData">
          <el-option label="提示" value="INFO" />
          <el-option label="警告" value="WARNING" />
          <el-option label="严重" value="CRITICAL" />
        </el-select>
      </div>
    </div>

    <el-card>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="ruleName" label="规则名称" width="200" />
        <el-table-column prop="severity" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)">{{ severityLabel(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="告警内容" show-overflow-tooltip />
        <el-table-column prop="notifyStatus" label="通知状态" width="100">
          <template #default="{ row }">
            <el-tag :type="notifyStatusType(row.notifyStatus)">
              {{ notifyStatusLabel(row.notifyStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notifyResult" label="通知结果" width="200" show-overflow-tooltip />
        <el-table-column prop="triggeredAt" label="触发时间" width="180" />
      </el-table>
      <el-pagination
        class="pagination"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'

const loading = ref(false)
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filters = reactive({ severity: '' })

const severityType = (s) => ({ CRITICAL: 'danger', WARNING: 'warning', INFO: 'info' }[s] || 'info')
const severityLabel = (s) => ({ CRITICAL: '严重', WARNING: '警告', INFO: '提示' }[s] || s)
const notifyStatusType = (s) => ({ 0: 'info', 1: 'success', 2: 'danger' }[s] || 'info')
const notifyStatusLabel = (s) => ({ 0: '未通知', 1: '已通知', 2: '通知失败' }[s] || '未知')

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getRecords({ page: page.value, size: size.value, severity: filters.severity })
    records.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  gap: 12px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
