<template>
  <div class="min-h-screen bg-background flex flex-col">
    <div class="max-w-md mx-auto w-full px-4 flex flex-col flex-1 justify-center py-12 space-y-6">

      <div class="space-y-1 text-center">
        <div class="flex justify-center mb-4">
          <div class="size-12 rounded-2xl bg-primary/10 flex items-center justify-center">
            <DropletsIcon class="size-6 text-primary" />
          </div>
        </div>
        <h1 class="text-2xl font-bold text-foreground tracking-tight">Create your account</h1>
        <p class="text-sm text-muted-foreground">Start tracking your glucose in minutes</p>
      </div>

      <div class="space-y-5">

        <div class="space-y-2">
          <button @click="auth.redirectToOAuth('google')"
            class="w-full flex items-center justify-center gap-3 py-3 px-4 card border border-border hover:bg-muted/50 transition-colors text-sm font-medium text-foreground">
            <svg class="size-4" viewBox="0 0 24 24"><path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/><path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l3.66-2.84z"/><path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
            Sign up with Google
          </button>
        </div>

        <div class="flex items-center gap-3">
          <div class="flex-1 h-px bg-border" />
          <span class="text-xs text-muted-foreground">or</span>
          <div class="flex-1 h-px bg-border" />
        </div>

        <div class="space-y-3">
          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">First name</label>
              <input v-model="firstName" type="text" placeholder="Jane"
                class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Last name</label>
              <input v-model="lastName" type="text" placeholder="Smith"
                class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Email</label>
            <input v-model="email" type="email" placeholder="you@example.com"
              class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Password</label>
            <div class="relative">
              <input v-model="password" :type="showPassword ? 'text' : 'password'" placeholder="Min. 8 characters"
                class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 pr-10" />
              <button @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground">
                <EyeOffIcon v-if="showPassword" class="size-4" />
                <EyeIcon v-else class="size-4" />
              </button>
            </div>

            <div v-if="password" class="flex gap-1 mt-1">
              <div v-for="i in 4" :key="i"
                class="h-1 flex-1 rounded-full transition-colors"
                :class="i <= passwordStrength ? strengthColor : 'bg-muted'" />
            </div>
            <p v-if="password" class="text-xs" :class="strengthTextColor">{{ strengthLabel }}</p>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Diabetes type <span class="text-muted-foreground font-normal">(optional)</span></label>
            <select v-model="diabetesType"
              class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30">
              <option value="">Prefer not to say</option>
              <option value="TYPE_1">Type 1</option>
              <option value="TYPE_2">Type 2</option>
              <option value="GESTATIONAL">Gestational</option>
              <option value="PREDIABETES">Prediabetes</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <p v-if="error" class="text-xs text-red-500 flex items-center gap-1.5">
            <AlertCircleIcon class="size-3.5 shrink-0" />
            {{ error }}
          </p>

          <button @click="submitSignup" :disabled="loading || !canSubmit" class="btn-primary w-full py-3 mt-1">
            <span v-if="loading">Creating account...</span>
            <span v-else>Create account</span>
          </button>

          <p class="text-center text-xs text-muted-foreground leading-relaxed">
            By signing up you agree to our
            <a href="#" class="text-primary underline underline-offset-2">Terms</a> and
            <a href="#" class="text-primary underline underline-offset-2">Privacy Policy</a>
          </p>
        </div>

        <p class="text-center text-sm text-muted-foreground">
          Already have an account?
          <button @click="$router.push('/login')" class="text-primary underline underline-offset-2">Sign in</button>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from '@/lib/axios'
import {
  Droplets as DropletsIcon,
  Eye as EyeIcon,
  EyeOff as EyeOffIcon,
  AlertCircle as AlertCircleIcon,
} from 'lucide-vue-next'

const router = useRouter()
const auth = useAuthStore()

const firstName = ref('')
const lastName = ref('')
const email = ref('')
const password = ref('')
const diabetesType = ref('')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

const canSubmit = computed(() =>
  firstName.value && email.value && password.value.length >= 8
)

const passwordStrength = computed(() => {
  const p = password.value
  let score = 0
  if (p.length >= 8) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  return score
})

const strengthColor = computed(() => {
  const colors = ['bg-red-400', 'bg-orange-400', 'bg-yellow-400', 'bg-primary']
  return colors[passwordStrength.value - 1] ?? 'bg-muted'
})

const strengthTextColor = computed(() => {
  const colors = ['text-red-500', 'text-orange-500', 'text-yellow-600', 'text-primary']
  return colors[passwordStrength.value - 1] ?? 'text-muted-foreground'
})

const strengthLabel = computed(() => {
  const labels = ['Weak', 'Fair', 'Good', 'Strong']
  return labels[passwordStrength.value - 1] ?? ''
})

async function submitSignup() {
  if (!canSubmit.value) return
  loading.value = true
  error.value = ''
  try {
    // ✅ Combine firstName + lastName into single `name` field for backend
    await axios.post('/auth/signup', {
      name: `${firstName.value.trim()} ${lastName.value.trim()}`.trim(),
      email: email.value,
      password: password.value,
      diabetesType: diabetesType.value || undefined,
    })
    // Auto login after signup
    await auth.login({ email: email.value, password: password.value })
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e?.response?.data?.error ?? e?.response?.data?.message ?? 'Something went wrong. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>
