<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">概览</h2>
    </div>
    
    <el-row :gutter="20" class="stat-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.ruleCount }}</div>
          <div class="stat-label">告警规则数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.activeRuleCount }}</div>
          <div class="stat-label">启用规则数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.channelCount }}</div>
          <div class="stat-label">通知渠道数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayAlertCount }}</div>
          <div class="stat-label">今日告警数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <span>最近告警记录</span>
          </template>
          <el-table :data="recentRecords" style="width: 100%">
            <el-table-column prop="ruleName" label="规则名称" width="200" />
            <el-table-column prop="severity" label="级别" width="100">
              <template #default="{ row }">
                <el-tag :type="severityType(row.severity)">{{ severityLabel(row.severity) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
            <el-table-column prop="triggeredAt" label="触发时间" width="180" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>通知渠道状态</span>
          </template>
          <div class="channel-list">
            <div v-for="channel in channels" :key="channel.id" class="channel-item">
              <span class="channel-name">
                <el-icon><component :is="channelIcon(channel.type)" /></el-icon>
                {{ channel.name }}
              </span>
              <el-tag :type="channel.status === 1 ? 'success' : 'danger'" size="small">
                {{ channel.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </div>
            <el-empty v-if="channels.length === 0" description="暂无通知渠道" :image-size="60" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'
import { Message, ChatDotRound, Promotion } from '@element-plus/icons-vue'

const stats = ref({ ruleCount: 0, activeRuleCount: 0, channelCount: 0, todayAlertCount: 0 })
const recentRecords = ref([])
const channels = ref([])

const severityType = (s) => ({ CRITICAL: 'danger', WARNING: 'warning', INFO: 'info' }[s] || 'info')
const severityLabel = (s) => ({ CRITICAL: '严重', WARNING: '警告', INFO: '提示' }[s] || s)
const channelIcon = (type) => ({ EMAIL: Message, SMS: ChatDotRound, DINGTALK: Promotion, WECOM: Promotion, FEISHU: Promotion }[type] || Message)

onMounted(async () => {
  const [rulesRes, channelsRes, recordsRes] = await Promise.all([
    api.getRules({ page: 1, size: 1000 }),
    api.getAllChannels(),
    api.getRecords({ page: 1, size: 10 })
  ])
  
  const rules = rulesRes.data.records || []
  stats.value.ruleCount = rules.length
  stats.value.activeRuleCount = rules.filter(r => r.status === 1).length
  
  channels.value = channelsRes.data || []
  stats.value.channelCount = channels.value.length
  
  recentRecords.value = recordsRes.data.records || []
  stats.value.todayAlertCount = recentRecords.value.length
})
</script>

<style scoped>
.stat-row {
  margin-bottom: 20px;
}

.channel-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.channel-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.channel-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #303133;
  font-weight: 500;
}
</style>
