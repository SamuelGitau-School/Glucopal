import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import axios from '@/lib/axios'

interface AuthUser {
  id: string | number
  email: string
  roles?: string[]
  [key: string]: unknown
}

interface LoginCredentials {
  email: string
  password: string
}

interface AuthResponse {
  accessToken: string
  user: AuthUser
  mfaRequired?: boolean
  mfaSessionToken?: string
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(null)
  const user = ref<AuthUser | null>(null)
  const mfaPending = ref<boolean>(false)
  const mfaSessionToken = ref<string | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value && !mfaPending.value)
  const requiresMfa = computed(() => mfaPending.value)

  async function init(): Promise<void> {
    try {
      const { data } = await axios.post<AuthResponse>('/auth/refresh')
      accessToken.value = data.accessToken
      user.value = data.user
    } catch {
      accessToken.value = null
      user.value = null
    }
  }

  async function login(credentials: LoginCredentials): Promise<{ mfaRequired: boolean }> {
    const { data } = await axios.post<AuthResponse>('/auth/login', credentials)

    if (data.mfaRequired) {
      mfaPending.value = true
      mfaSessionToken.value = data.mfaSessionToken ?? null
      return { mfaRequired: true }
    }

    accessToken.value = data.accessToken
    user.value = data.user
    return { mfaRequired: false }
  }

  async function verifyMfa(code: string): Promise<void> {
    const { data } = await axios.post<AuthResponse>('/auth/mfa/verify', {
      code,
      mfaSessionToken: mfaSessionToken.value,
    })
    accessToken.value = data.accessToken
    user.value = data.user
    mfaPending.value = false
    mfaSessionToken.value = null
  }

  async function handleOAuthCallback(
    code: string,
    provider: string
  ): Promise<{ mfaRequired: boolean }> {
    const { data } = await axios.post<AuthResponse>('/auth/oauth/callback', { code, provider })

    if (data.mfaRequired) {
      mfaPending.value = true
      mfaSessionToken.value = data.mfaSessionToken ?? null
      return { mfaRequired: true }
    }

    accessToken.value = data.accessToken
    user.value = data.user
    return { mfaRequired: false }
  }

  function redirectToOAuth(provider: string): void {
    window.location.href = `${import.meta.env.VITE_API_URL}/oauth2/authorization/${provider}`
  }

  async function handleSamlCallback(): Promise<void> {
    const { data } = await axios.post<AuthResponse>('/auth/saml/callback')
    accessToken.value = data.accessToken
    user.value = data.user
  }

  function redirectToSaml(): void {
    window.location.href = `${import.meta.env.VITE_API_URL}/saml2/authenticate/default`
  }

  async function refreshToken(): Promise<string> {
    const { data } = await axios.post<AuthResponse>('/auth/refresh')
    accessToken.value = data.accessToken
    user.value = data.user
    return data.accessToken
  }

  async function logout(): Promise<void> {
    try {
      await axios.post('/auth/logout')
    } finally {
      accessToken.value = null
      user.value = null
      mfaPending.value = false
      mfaSessionToken.value = null
    }
  }

  return {
    user,
    accessToken,
    mfaPending,
    isAuthenticated,
    requiresMfa,
    init,
    login,
    verifyMfa,
    handleOAuthCallback,
    redirectToOAuth,
    handleSamlCallback,
    redirectToSaml,
    refreshToken,
    logout,
  }
})
