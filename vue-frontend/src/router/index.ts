import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/layout/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import Chat from '../views/Chat.vue'
import Books from '../views/Books.vue'
import TestingRecords from '../views/TestingRecords.vue'
import Profile from '../views/Profile.vue'
import LandingView from '../views/LandingView.vue'
import LoginView   from '../views/LoginView.vue'
import SignupView  from '../views/SignupView.vue'

const router = createRouter({
  history: createWebHistory(),

  routes: [
    {
      path: '/',
      component: Layout,
      children: [
        { path: '', name: 'dashboard', component: Dashboard,meta: { requiresAuth: true } },
        { path: 'chat', name: 'chat', component: Chat },
        { path: 'books', name: 'books', component: Books },
        { path: 'records', name: 'records', component: TestingRecords },
        { path: 'profile', name: 'profile', component: Profile },
      ],
    },
    { path: '/',          component: LandingView },
    { path: '/login',     component: LoginView },
    { path: '/signup',    component: SignupView },
  ],
})

export default router
