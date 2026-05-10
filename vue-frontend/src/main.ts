import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

const { useAuthStore } = await import('@/stores/auth')
const auth = useAuthStore()
await auth.init()

app.mount('#app')
