<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">通知渠道</h2>
      <el-button v-if="userStore.isAdmin()" type="primary" @click="openDialog()">
        <el-icon><Plus /></el-icon>新建渠道
      </el-button>
    </div>

    <el-card>
      <el-table :data="channels" v-loading="loading" stripe>
        <el-table-column prop="name" label="渠道名称" width="200" />
        <el-table-column prop="type" label="类型" width="150">
          <template #default="{ row }">
            <el-tag>{{ typeLabels[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="config" label="配置" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right" v-if="userStore.isAdmin()">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑渠道' : '新建渠道'" width="600px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="渠道名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入渠道名称" />
        </el-form-item>
        <el-form-item label="渠道类型" prop="type">
          <el-select v-model="form.type" style="width: 100%" @change="handleTypeChange">
            <el-option label="邮件" value="EMAIL" />
            <el-option label="短信" value="SMS" />
            <el-option label="钉钉" value="DINGTALK" />
            <el-option label="企业微信" value="WECOM" />
            <el-option label="飞书" value="FEISHU" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置信息" prop="config">
          <el-input v-model="form.config" type="textarea" :rows="5" :placeholder="configPlaceholder" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import api from '../api'
import { useUserStore } from '../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const channels = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const typeLabels = { EMAIL: '邮件', SMS: '短信', DINGTALK: '钉钉', WECOM: '企业微信', FEISHU: '飞书' }

const configTemplates = {
  EMAIL: '{"host": "smtp.example.com", "port": 465, "username": "", "password": "", "to": "alert@example.com"}',
  SMS: '{"accessKeyId": "", "accessKeySecret": "", "signName": "", "templateCode": "", "phone": ""}',
  DINGTALK: '{"webhook": "https://oapi.dingtalk.com/robot/send?access_token=xxx"}',
  WECOM: '{"webhook": "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxx"}',
  FEISHU: '{"webhook": "https://open.feishu.cn/open-apis/bot/v2/hook/xxx"}'
}

const dialogVisible = ref(false)
const formRef = ref()
const submitting = ref(false)
const form = reactive({ id: null, name: '', type: 'EMAIL', config: '', status: 1 })

const formRules = {
  name: [{ required: true, message: '请输入渠道名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择渠道类型', trigger: 'change' }],
  config: [{ required: true, message: '请输入配置信息', trigger: 'blur' }]
}

const configPlaceholder = computed(() => configTemplates[form.type] || '{}')

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getChannels({ page: page.value, size: size.value })
    channels.value = res.data.records || []
    total.value = res.data.total || 0
  } finally {
    loading.value = false
  }
}

const handleTypeChange = () => {
  form.config = configTemplates[form.type] || '{}'
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
    form.config = row.config === '******' ? configTemplates[row.type] : row.config
  } else {
    Object.assign(form, { id: null, name: '', type: 'EMAIL', config: configTemplates.EMAIL, status: 1 })
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) {
      await api.updateChannel(form.id, form)
    } else {
      await api.createChannel(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该渠道?', '提示', { type: 'warning' })
  await api.deleteChannel(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>

<style scoped>
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
