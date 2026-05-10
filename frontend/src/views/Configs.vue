<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">配置管理</h2>
    </div>

    <el-card>
      <el-table :data="configs" v-loading="loading">
        <el-table-column prop="configKey" label="配置项" width="300" />
        <el-table-column prop="configValue" label="配置值">
          <template #default="{ row }">
            <el-input v-model="row.configValue" size="small" style="width: 200px" />
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="saveConfig(row)">保存</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>配置说明</span>
      </template>
      <div class="config-help">
        <p><strong>alert.check.enabled</strong> - 是否启用自动告警检查，设为 true 启用，false 禁用</p>
        <p><strong>alert.check.thread.pool.size</strong> - 告警检查线程池大小</p>
        <p><strong>notify.retry.count</strong> - 通知发送失败时的重试次数</p>
        <p class="tip">修改配置后立即生效，无需重启服务</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const configs = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getConfigs()
    configs.value = res.data || []
  } finally {
    loading.value = false
  }
}

const saveConfig = async (row) => {
  await api.updateConfig(row.configKey, row.configValue)
  ElMessage.success('配置已更新')
}

onMounted(loadData)
</script>

<style scoped>
.config-help p {
  margin-bottom: 12px;
  color: #606266;
  line-height: 1.8;
}

.config-help strong {
  color: #303133;
}

.config-help .tip {
  color: var(--highlight-color);
  margin-top: 16px;
  font-weight: 500;
}
</style>
