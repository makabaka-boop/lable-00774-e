<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">告警规则</h2>
      <el-button v-if="userStore.isAdmin()" type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新建规则
      </el-button>
    </div>

    <el-card>
      <el-table :data="rules" v-loading="loading" stripe>
        <el-table-column prop="name" label="规则名称" width="180" />
        <el-table-column prop="dataSourceType" label="数据源" width="120" />
        <el-table-column prop="ruleExpression" label="规则表达式" show-overflow-tooltip />
        <el-table-column prop="severity" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)">{{ severityLabel(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="checkInterval" label="检查间隔" width="100">
          <template #default="{ row }">{{ row.checkInterval }}秒</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="checkRule(row.id)">
              <el-icon><VideoPlay /></el-icon>测试
            </el-button>
            <el-button v-if="userStore.isAdmin()" link type="primary" @click="openDialog(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button v-if="userStore.isAdmin()" link type="danger" @click="handleDelete(row.id)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑规则' : '新建规则'" width="700px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" placeholder="规则描述" />
        </el-form-item>
        <el-form-item label="数据源类型" prop="dataSourceType">
          <el-select v-model="form.dataSourceType" style="width: 100%">
            <el-option label="Mock数据" value="MOCK" />
            <el-option label="API接口" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源配置" prop="dataSourceConfig">
          <el-input v-model="form.dataSourceConfig" type="textarea" :rows="3" 
                    placeholder='{"url": "http://xxx"} 或 {}' />
        </el-form-item>
        <el-form-item label="规则表达式" prop="ruleExpression">
          <el-input v-model="form.ruleExpression" placeholder="#cpu > 80 或 #responseTime > 3000" />
          <div class="form-tip">使用SpEL表达式，变量使用#前缀，如: #cpu > 80 and #memory > 90</div>
        </el-form-item>
        <el-form-item label="告警级别" prop="severity">
          <el-select v-model="form.severity" style="width: 100%">
            <el-option label="提示" value="INFO" />
            <el-option label="警告" value="WARNING" />
            <el-option label="严重" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知渠道">
          <el-select v-model="form.channelIds" multiple style="width: 100%" placeholder="选择通知渠道">
            <el-option v-for="c in channels" :key="c.id" :label="c.name" :value="String(c.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="通知模板">
          <el-input v-model="form.notifyTemplate" type="textarea" :rows="2" 
                    placeholder="CPU: ${cpu}%, Memory: ${memory}%" />
        </el-form-item>
        <el-form-item label="检查间隔">
          <el-input-number v-model="form.checkInterval" :min="10" :max="3600" />
          <span style="margin-left: 8px; color: var(--text-muted)">秒</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../api'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, VideoPlay } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const rules = ref([])
const channels = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const formRef = ref()
const submitting = ref(false)
const form = reactive({
  id: null, name: '', description: '', dataSourceType: 'MOCK',
  dataSourceConfig: '{}', ruleExpression: '', severity: 'WARNING',
  channelIds: [], notifyTemplate: '', checkInterval: 60, status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  dataSourceType: [{ required: true, message: '请选择数据源类型', trigger: 'change' }],
  ruleExpression: [{ required: true, message: '请输入规则表达式', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择告警级别', trigger: 'change' }]
}

const severityType = (s) => ({ CRITICAL: 'danger', WARNING: 'warning', INFO: 'info' }[s] || 'info')
const severityLabel = (s) => ({ CRITICAL: '严重', WARNING: '警告', INFO: '提示' }[s] || s)

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getRules({ page: page.value, size: size.value })
    rules.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const loadChannels = async () => {
  const res = await api.getAllChannels()
  channels.value = res.data || []
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { ...row, channelIds: row.channelIds ? row.channelIds.split(',') : [] })
  } else {
    Object.assign(form, {
      id: null, name: '', description: '', dataSourceType: 'MOCK',
      dataSourceConfig: '{}', ruleExpression: '', severity: 'WARNING',
      channelIds: [], notifyTemplate: '', checkInterval: 60, status: 1
    })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = { ...form, channelIds: form.channelIds.join(',') }
    if (form.id) {
      await api.updateRule(form.id, data)
    } else {
      await api.createRule(data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该规则?', '提示', { type: 'warning' })
  await api.deleteRule(id)
  ElMessage.success('删除成功')
  loadData()
}

const checkRule = async (id) => {
  await api.checkRule(id)
  ElMessage.success('已触发规则检查')
}

onMounted(() => {
  loadData()
  loadChannels()
})
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.form-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
}
</style>
