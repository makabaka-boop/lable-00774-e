<template>
  <div class="login-container">
    <div class="login-bg"></div>
    
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="48"><Bell /></el-icon>
        </div>
        <h1 class="brand-title">告警系统</h1>
        <p class="brand-subtitle">多渠道智能告警平台</p>
        
        <div class="login-features">
          <div class="feature-item">
            <el-icon :size="20"><Message /></el-icon>
            <div>
              <h3>多渠道通知</h3>
              <p>支持邮件、短信、钉钉、企业微信、飞书</p>
            </div>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><Setting /></el-icon>
            <div>
              <h3>规则引擎</h3>
              <p>灵活的告警条件配置</p>
            </div>
          </div>
          <div class="feature-item">
            <el-icon :size="20"><DataAnalysis /></el-icon>
            <div>
              <h3>实时监控</h3>
              <p>告警记录实时追踪</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录区域 -->
    <div class="login-main">
      <div class="login-card">
        <div class="login-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号信息</p>
        </div>
        
        <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
          <el-form-item prop="username">
            <el-input 
              v-model="form.username" 
              placeholder="请输入用户名" 
              size="large" 
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input 
              v-model="form.password" 
              type="password" 
              placeholder="请输入密码" 
              size="large" 
              :prefix-icon="Lock" 
              show-password 
              @keyup.enter="handleLogin"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              size="large" 
              :loading="loading" 
              @click="handleLogin" 
              class="login-button"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <div class="login-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>默认账号: <strong>admin</strong> / <strong>admin123</strong></span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Bell, InfoFilled, Message, Setting, DataAnalysis } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  background: #f5f7fa;
  z-index: 0;
}

/* 左侧品牌区域 - 与主页侧边栏风格一致 */
.login-brand {
  flex: 1;
  min-width: 500px;
  background: rgba(22, 33, 62, 0.98);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.login-brand::before {
  content: '';
  position: absolute;
  inset: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(233, 69, 96, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(15, 52, 96, 0.3) 0%, transparent 50%);
  pointer-events: none;
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px;
}

.brand-logo {
  width: 88px;
  height: 88px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #e94560 0%, #c73e54 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(233, 69, 96, 0.4);
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  color: #ffffff;
  margin-bottom: 8px;
}

.brand-subtitle {
  color: rgba(255, 255, 255, 0.6);
  font-size: 15px;
  margin-bottom: 48px;
}

.login-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  text-align: left;
}

.feature-item {
  display: flex;
  gap: 14px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(233, 69, 96, 0.3);
  transform: translateX(4px);
}

.feature-item .el-icon {
  color: #e94560;
  flex-shrink: 0;
  margin-top: 2px;
}

.feature-item h3 {
  font-size: 14px;
  font-weight: 600;
  color: #ffffff;
  margin-bottom: 4px;
}

.feature-item p {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  line-height: 1.4;
}

/* 右侧登录区域 - 与主页内容区风格一致 */
.login-main {
  width: 480px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  z-index: 1;
  background: #f5f7fa;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 48px 40px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  border: 1px solid #e4e7ed;
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
}

.login-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.login-header p {
  color: #909399;
  font-size: 14px;
}

.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  border-radius: 8px;
  background: #ffffff !important;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
  background: #ffffff !important;
}

.login-form :deep(.el-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 1px #e94560 inset;
  background: #ffffff !important;
}

.login-form :deep(.el-input__inner) {
  background: transparent !important;
  color: #303133 !important;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #909399 !important;
}

.login-form :deep(input::placeholder) {
  color: #909399 !important;
}

.login-form :deep(.el-input__prefix) {
  color: #909399;
}

.login-form :deep(.el-input__suffix) {
  color: #909399;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-button {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 500;
  border-radius: 8px;
  background: linear-gradient(135deg, #e94560 0%, #c73e54 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(233, 69, 96, 0.3);
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(233, 69, 96, 0.4);
}

.login-footer {
  text-align: center;
}

.login-tip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 18px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 13px;
  color: #606266;
  border: 1px solid #e4e7ed;
}

.login-tip .el-icon {
  color: #909399;
}

.login-tip strong {
  color: #e94560;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 968px) {
  .login-container {
    flex-direction: column;
  }
  
  .login-brand {
    width: 100%;
    padding: 40px 20px;
  }
  
  .brand-content {
    padding: 20px;
  }
  
  .brand-logo {
    width: 72px;
    height: 72px;
  }
  
  .brand-title {
    font-size: 26px;
  }
  
  .brand-subtitle {
    margin-bottom: 32px;
  }
  
  .login-features {
    display: none;
  }
  
  .login-main {
    padding: 32px 20px;
  }
  
  .login-card {
    padding: 32px 24px;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 28px 20px;
  }
  
  .login-header h2 {
    font-size: 20px;
  }
}
</style>
