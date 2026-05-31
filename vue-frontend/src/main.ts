import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'
import { useAuthStore } from '@/stores/auth'
import { useThemeStore } from '@/stores/theme'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

const themeStore = useThemeStore()
themeStore.initTheme()

const auth = useAuthStore()
auth.init().finally(() => {
  app.mount('#app')
})
