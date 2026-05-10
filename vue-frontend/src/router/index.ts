import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const LoginView = () => import('@/views/LoginView.vue')
const DashboardView = () => import('@/views/Dashboard.vue')
const LandingView = () => import('@/views/LandingView.vue')
const SignupView = () => import('@/views/SignupView.vue')
const ProfileView = () => import('@/views/Profile.vue')
const ChatView = () => import('@/views/Chat.vue')
const BooksView = () => import('@/views/Books.vue')
const TestingRecordsView = () => import('@/views/TestingRecords.vue')

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    roles?: string[]
  }
}

const routes: RouteRecordRaw[] = [
  { path: '/', component: LandingView },
  { path: '/login', component: LoginView },
  { path: '/signup', component: SignupView },
  { path: '/dashboard', component: DashboardView, meta: { requiresAuth: true } },
  { path: '/profile', component: ProfileView, meta: { requiresAuth: true } },
  { path: '/chat', component: ChatView, meta: { requiresAuth: true } },
  { path: '/books', component: BooksView, meta: { requiresAuth: true } },
  { path: '/testing-records', component: TestingRecordsView, meta: { requiresAuth: true } },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  if (to.meta.requiresAuth && to.meta.roles && to.meta.roles.length > 0) {
    const hasRole = to.meta.roles.some((r) => auth.user?.roles?.includes(r))
    if (!hasRole) return { path: '/dashboard' }
  }

  if ((to.path === '/login' || to.path === '/signup') && auth.isAuthenticated) {
    return { path: '/dashboard' }
  }
})

export default router
