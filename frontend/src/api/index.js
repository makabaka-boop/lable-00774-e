import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  response => {
    if (response.data.code !== 200) {
      ElMessage.error(response.data.message || '请求失败')
      return Promise.reject(response.data)
    }
    return response.data
  },
  error => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else if (error.response?.status === 403) {
      ElMessage.error('无权限访问')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default {
  // 认证
  login: data => api.post('/auth/login', data),
  register: data => api.post('/auth/register', data),
  
  // 用户
  getCurrentUser: () => api.get('/users/me'),
  getUsers: params => api.get('/users', { params }),
  createUser: data => api.post('/users', data),
  updateUser: (id, data) => api.put(`/users/${id}`, data),
  deleteUser: id => api.delete(`/users/${id}`),
  updateUserStatus: (id, status) => api.put(`/users/${id}/status`, null, { params: { status } }),
  updateUserRole: (id, role) => api.put(`/users/${id}/role`, null, { params: { role } }),
  
  // 通知渠道
  getChannels: params => api.get('/channels', { params }),
  getAllChannels: () => api.get('/channels/all'),
  createChannel: data => api.post('/channels', data),
  updateChannel: (id, data) => api.put(`/channels/${id}`, data),
  deleteChannel: id => api.delete(`/channels/${id}`),
  
  // 告警规则
  getRules: params => api.get('/rules', { params }),
  getRule: id => api.get(`/rules/${id}`),
  createRule: data => api.post('/rules', data),
  updateRule: (id, data) => api.put(`/rules/${id}`, data),
  deleteRule: id => api.delete(`/rules/${id}`),
  checkRule: id => api.post(`/rules/${id}/check`),
  
  // 告警记录
  getRecords: params => api.get('/records', { params }),
  
  // 系统配置
  getConfigs: () => api.get('/configs'),
  updateConfig: (key, value) => api.put(`/configs/${key}`, value, { headers: { 'Content-Type': 'text/plain' } }),
  
  // 操作日志
  getLogs: params => api.get('/logs', { params })
}
