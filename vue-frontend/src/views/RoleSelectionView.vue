<template>
  <div class="min-h-screen bg-background flex flex-col">
    <div class="max-w-md mx-auto w-full px-4 flex flex-col flex-1 justify-center py-12 space-y-8">

      <!-- Logo -->
      <div class="text-center space-y-2">
        <div class="flex justify-center mb-4">
          <div class="size-14 rounded-2xl bg-primary/10 flex items-center justify-center">
            <DropletsIcon class="size-7 text-primary" />
          </div>
        </div>
        <h1 class="text-2xl font-bold text-foreground tracking-tight">Welcome to GlucoPal</h1>
        <p class="text-sm text-muted-foreground">Who are you signing in as?</p>
      </div>

      <!-- Role Cards -->
      <div class="space-y-4">
        <button
          @click="selectRole('patient')"
          :class="[
            'w-full p-5 rounded-2xl border-2 text-left transition-all space-y-2',
            selected === 'patient'
              ? 'border-primary bg-primary/5 shadow-md'
              : 'border-border hover:border-primary/40 hover:bg-muted/50'
          ]"
        >
          <div class="flex items-center gap-4">
            <div :class="['size-12 rounded-xl flex items-center justify-center',
              selected === 'patient' ? 'bg-primary/10' : 'bg-muted']">
              <UserIcon :class="['size-6', selected === 'patient' ? 'text-primary' : 'text-muted-foreground']" />
            </div>
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <h3 class="font-semibold text-foreground">I'm a Patient</h3>
                <div v-if="selected === 'patient'"
                  class="size-5 rounded-full bg-primary flex items-center justify-center">
                  <CheckIcon class="size-3 text-white" />
                </div>
              </div>
              <p class="text-xs text-muted-foreground mt-0.5">Track glucose, manage diabetes, chat with AI</p>
            </div>
          </div>
          <div class="flex gap-2 flex-wrap pl-16">
            <span v-for="tag in ['Glucose Tracking', 'AI Chat', 'Health Records']" :key="tag"
              class="text-xs px-2 py-0.5 rounded-full bg-primary/10 text-primary">
              {{ tag }}
            </span>
          </div>
        </button>

        <button
          @click="selectRole('provider')"
          :class="[
            'w-full p-5 rounded-2xl border-2 text-left transition-all space-y-2',
            selected === 'provider'
              ? 'border-blue-500 bg-blue-500/5 shadow-md'
              : 'border-border hover:border-blue-400/40 hover:bg-muted/50'
          ]"
        >
          <div class="flex items-center gap-4">
            <div :class="['size-12 rounded-xl flex items-center justify-center',
              selected === 'provider' ? 'bg-blue-500/10' : 'bg-muted']">
              <StethoscopeIcon :class="['size-6', selected === 'provider' ? 'text-blue-500' : 'text-muted-foreground']" />
            </div>
            <div class="flex-1">
              <div class="flex items-center justify-between">
                <h3 class="font-semibold text-foreground">I'm a Health Provider</h3>
                <div v-if="selected === 'provider'"
                  class="size-5 rounded-full bg-blue-500 flex items-center justify-center">
                  <CheckIcon class="size-3 text-white" />
                </div>
              </div>
              <p class="text-xs text-muted-foreground mt-0.5">Monitor patients, review records, clinical tools</p>
            </div>
          </div>
          <div class="flex gap-2 flex-wrap pl-16">
            <span v-for="tag in ['Patient Monitor', 'Clinical Notes', 'Analytics']" :key="tag"
              class="text-xs px-2 py-0.5 rounded-full bg-blue-500/10 text-blue-500">
              {{ tag }}
            </span>
          </div>
        </button>
      </div>

      <!-- Continue Button -->
      <div class="space-y-3">
        <button
          @click="proceed"
          :disabled="!selected"
          class="btn-primary w-full py-3 disabled:opacity-40 disabled:cursor-not-allowed"
        >
          Continue
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Droplets as DropletsIcon,
  User as UserIcon,
  Stethoscope as StethoscopeIcon,
  Check as CheckIcon,
} from 'lucide-vue-next'

const router = useRouter()
const selected = ref<'patient' | 'provider' | null>(null)

function selectRole(role: 'patient' | 'provider') {
  selected.value = role
}

function proceed() {
  if (!selected.value) return
  // Pass role as query param so signup/login knows which flow
  router.push({ path: '/signup', query: { role: selected.value } })
}

function goToLogin() {
  router.push({ path: '/login', query: { role: selected.value ?? 'patient' } })
}
</script>
