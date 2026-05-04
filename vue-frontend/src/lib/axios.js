import axios from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
})

instance.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

let refreshing = null

instance.interceptors.response.use(null, async error => {
  const auth = useAuthStore()
  if (error.response?.status === 401 && !error.config._retry) {
    error.config._retry = true
    if (!refreshing) refreshing = auth.refreshToken().finally(() => refreshing = null)
    const newToken = await refreshing
    error.config.headers.Authorization = `Bearer ${newToken}`
    return instance(error.config)
  }
  if (error.response?.status === 401) {
    await auth.logout()
    router.push('/login')
  }
  return Promise.reject(error)
})

export default instance
