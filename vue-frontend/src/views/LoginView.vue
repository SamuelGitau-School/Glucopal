<template>
  <div class="min-h-screen bg-background flex flex-col">
    <div class="max-w-md mx-auto w-full px-4 flex flex-col flex-1 justify-center py-12 space-y-6">

      <!-- Header -->
      <div class="space-y-1 text-center">
        <div class="flex justify-center mb-4">
          <div class="size-12 rounded-2xl bg-primary/10 flex items-center justify-center">
            <DropletsIcon class="size-6 text-primary" />
          </div>
        </div>
        <h1 class="text-2xl font-bold text-foreground tracking-tight">Welcome back</h1>
        <p class="text-sm text-muted-foreground">Sign in to your GlucoseTrack account</p>
      </div>

      <!-- MFA screen (shown after password login if required) -->
      <div v-if="step === 'mfa'" class="space-y-5">
        <div class="card p-5 space-y-4">
          <div class="text-center space-y-1">
            <ShieldCheckIcon class="size-8 text-primary mx-auto" />
            <p class="text-sm font-medium text-foreground">Two-factor authentication</p>
            <p class="text-xs text-muted-foreground">Enter the 6-digit code from your authenticator app</p>
          </div>
          <input
            v-model="mfaCode"
            type="text"
            inputmode="numeric"
            maxlength="6"
            placeholder="000000"
            class="w-full text-center text-2xl tracking-widest font-mono border border-border rounded-lg px-4 py-3 bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary/30"
            @keyup.enter="submitMfa"
          />
          <button @click="submitMfa" :disabled="loading || mfaCode.length < 6" class="btn-primary w-full py-3">
            <span v-if="loading">Verifying...</span>
            <span v-else>Verify</span>
          </button>
        </div>
        <button @click="step = 'login'" class="w-full text-center text-sm text-muted-foreground hover:text-foreground">
          ← Back to sign in
        </button>
      </div>

      <!-- Main login form -->
      <div v-else class="space-y-5">

        <!-- OAuth buttons -->
        <div class="space-y-2">
          <button @click="auth.redirectToOAuth('google')"
            class="w-full flex items-center justify-center gap-3 py-3 px-4 card border border-border hover:bg-muted/50 transition-colors text-sm font-medium text-foreground">
            <svg class="size-4" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
            Continue with Google
          </button>
          <button @click="auth.redirectToSaml()"
            class="w-full flex items-center justify-center gap-3 py-3 px-4 card border border-border hover:bg-muted/50 transition-colors text-sm font-medium text-foreground">
            <BuildingIcon class="size-4 text-muted-foreground" />
            Continue with SSO
          </button>
        </div>

        <!-- Divider -->
        <div class="flex items-center gap-3">
          <div class="flex-1 h-px bg-border" />
          <span class="text-xs text-muted-foreground">or</span>
          <div class="flex-1 h-px bg-border" />
        </div>

        <!-- Email/password form -->
        <div class="space-y-3">
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Email</label>
            <input
              v-model="email"
              type="email"
              placeholder="you@example.com"
              class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
              @keyup.enter="submitLogin"
            />
          </div>
          <div class="space-y-1.5">
            <div class="flex items-center justify-between">
              <label class="text-sm font-medium text-foreground">Password</label>
              <button class="text-xs text-primary hover:underline">Forgot password?</button>
            </div>
            <div class="relative">
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 pr-10"
                @keyup.enter="submitLogin"
              />
              <button @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground">
                <EyeOffIcon v-if="showPassword" class="size-4" />
                <EyeIcon v-else class="size-4" />
              </button>
            </div>
          </div>

          <!-- Error -->
          <p v-if="error" class="text-xs text-red-500 flex items-center gap-1.5">
            <AlertCircleIcon class="size-3.5 shrink-0" />
            {{ error }}
          </p>

          <button @click="submitLogin" :disabled="loading || !email || !password"
            class="btn-primary w-full py-3 mt-1">
            <span v-if="loading">Signing in...</span>
            <span v-else>Sign in</span>
          </button>
        </div>

        <p class="text-center text-sm text-muted-foreground">
          Don't have an account?
          <button @click="$router.push('/signup')" class="text-primary underline underline-offset-2">Sign up</button>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
  Droplets as DropletsIcon,
  ShieldCheck as ShieldCheckIcon,
  Building2 as BuildingIcon,
  Eye as EyeIcon,
  EyeOff as EyeOffIcon,
  AlertCircle as AlertCircleIcon,
} from 'lucide-vue-next'

const router = useRouter()
const auth = useAuthStore()

const step = ref<'login' | 'mfa'>('login')
const email = ref('')
const password = ref('')
const mfaCode = ref('')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

async function submitLogin() {
  if (!email.value || !password.value) return
  loading.value = true
  error.value = ''
  try {
    const result = await auth.login({ email: email.value, password: password.value })
    if (result.mfaRequired) {
      step.value = 'mfa'
    } else {
      router.push('/dashboard')
    }
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'Invalid email or password'
  } finally {
    loading.value = false
  }
}

async function submitMfa() {
  if (mfaCode.value.length < 6) return
  loading.value = true
  error.value = ''
  try {
    await auth.verifyMfa(mfaCode.value)
    router.push('/dashboard')
  } catch (e: any) {
    error.value = 'Invalid code. Please try again.'
    mfaCode.value = ''
  } finally {
    loading.value = false
  }
}
</script>
