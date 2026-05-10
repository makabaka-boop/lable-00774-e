<template>
  <div class="layout">
    <aside class="sidebar">
      <div class="logo">
        <el-icon :size="32" color="#e94560"><Bell /></el-icon>
        <span class="brand">告警系统</span>
      </div>
      <el-menu
        :default-active="route.path"
        router
        class="sidebar-menu"
        background-color="transparent"
        text-color="#a0a0a0"
        active-text-color="#e94560"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/rules">
          <el-icon><List /></el-icon>
          <span>告警规则</span>
        </el-menu-item>
        <el-menu-item index="/channels">
          <el-icon><Message /></el-icon>
          <span>通知渠道</span>
        </el-menu-item>
        <el-menu-item index="/records">
          <el-icon><Document /></el-icon>
          <span>告警记录</span>
        </el-menu-item>
        <el-divider v-if="userStore.isAdmin()" />
        <el-menu-item v-if="userStore.isAdmin()" index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin()" index="/configs">
          <el-icon><Setting /></el-icon>
          <span>系统配置</span>
        </el-menu-item>
        <el-menu-item v-if="userStore.isAdmin()" index="/logs">
          <el-icon><Tickets /></el-icon>
          <span>操作日志</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <div class="main-container">
      <header class="header">
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ pageTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="36" class="avatar">
                {{ userStore.user?.username?.charAt(0).toUpperCase() }}
              </el-avatar>
              <div class="user-detail">
                <span class="username">{{ userStore.user?.nickname || userStore.user?.username }}</span>
                <el-tag size="small" :type="userStore.isAdmin() ? 'danger' : 'info'">
                  {{ userStore.isAdmin() ? '管理员' : '用户' }}
                </el-tag>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { Bell, DataAnalysis, List, Message, Document, User, Setting, Tickets, SwitchButton, ArrowDown } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitles = {
  '/dashboard': '仪表盘',
  '/rules': '告警规则',
  '/channels': '通知渠道',
  '/records': '告警记录',
  '/users': '用户管理',
  '/configs': '系统配置',
  '/logs': '操作日志'
}

const pageTitle = computed(() => pageTitles[route.path] || '告警系统')

const handleCommand = (cmd) => {
  if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
}

.sidebar {
  width: 220px;
  background: rgba(22, 33, 62, 0.98);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 100;
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: 1px solid var(--border-color);
}

.brand {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-color);
}

.sidebar-menu {
  flex: 1;
  border-right: none !important;
  padding: 12px 0;
}

.sidebar-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
  margin: 4px 12px;
  border-radius: 8px;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(233, 69, 96, 0.1) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(90deg, rgba(233, 69, 96, 0.2) 0%, transparent 100%) !important;
  border-left: 3px solid var(--highlight-color);
}

.el-divider {
  border-color: var(--border-color);
}

.main-container {
  flex: 1;
  margin-left: 220px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  color: #303133;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info:hover {
  background: #f5f7fa;
}

.avatar {
  background: linear-gradient(135deg, var(--highlight-color) 0%, #c73e54 100%);
  color: white;
  font-weight: 600;
}

.user-detail {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.username {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.el-breadcrumb {
  font-size: 14px;
}

.el-breadcrumb__item:last-child .el-breadcrumb__inner {
  color: #303133;
  font-weight: 500;
}

.main {
  flex: 1;
  padding: 24px;
  background: #f5f7fa;
}
</style>
