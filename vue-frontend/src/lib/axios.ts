import axios from 'axios'
import type { AxiosError, InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'


interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
})

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

let refreshing: Promise<string> | null = null

instance.interceptors.response.use(null, async (error: AxiosError) => {
  const auth = useAuthStore()
  const config = error.config as RetryableRequestConfig

  if (error.response?.status === 401 && !config._retry) {
    config._retry = true
    if (!refreshing) {
      refreshing = auth.refreshToken().finally(() => {
        refreshing = null
      })
    }
    const newToken = await refreshing
    config.headers!.Authorization = `Bearer ${newToken}`
    return instance(config)
  }

  if (error.response?.status === 401) {
    await auth.logout()
    router.push('/login')
  }

  return Promise.reject(error)
})

export default instance
