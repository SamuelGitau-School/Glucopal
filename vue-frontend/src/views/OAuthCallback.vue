<template>
  <div class="min-h-screen bg-background flex items-center justify-center">
    <div class="text-center space-y-3">
      <div class="size-12 rounded-2xl bg-primary/10 flex items-center justify-center mx-auto">
        <DropletsIcon class="size-6 text-primary animate-pulse" />
      </div>
      <p class="text-sm text-muted-foreground">Signing you in...</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from '@/lib/axios'
import { Droplets as DropletsIcon } from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

onMounted(async () => {
  const token = route.query.token as string
  const redirect = (route.query.redirect as string) || '/dashboard'

  if (!token) {
    router.push('/login')
    return
  }

  auth.accessToken = token

  try {
    const { data } = await axios.get('/auth/me')
    auth.user = data
  } catch {
  }

  router.push(redirect)
})
</script>
