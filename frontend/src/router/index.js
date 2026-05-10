import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'rules', name: 'Rules', component: () => import('../views/Rules.vue') },
      { path: 'channels', name: 'Channels', component: () => import('../views/Channels.vue') },
      { path: 'records', name: 'Records', component: () => import('../views/Records.vue') },
      { path: 'users', name: 'Users', component: () => import('../views/Users.vue'), meta: { admin: true } },
      { path: 'configs', name: 'Configs', component: () => import('../views/Configs.vue'), meta: { admin: true } },
      { path: 'logs', name: 'Logs', component: () => import('../views/Logs.vue'), meta: { admin: true } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  if (to.path !== '/login' && !userStore.token) {
    next('/login')
  } else if (to.path === '/login' && userStore.token) {
    next('/')
  } else {
    if (userStore.token && !userStore.user) {
      await userStore.fetchUser()
    }
    if (to.meta.admin && !userStore.isAdmin()) {
      next('/')
    } else {
      next()
    }
  }
})

export default router
