<template>
  <div class="min-h-screen bg-background flex flex-col">

    <!-- Nav -->
    <nav class="max-w-md mx-auto w-full px-4 pt-6 flex items-center justify-between">
      <div class="flex items-center gap-2">
        <DropletsIcon class="size-5 text-primary" />
        <span class="font-semibold text-foreground tracking-tight">GlucoseTrack</span>
      </div>
      <button @click="$router.push('/login')" class="btn-primary text-sm px-4 py-2">
        Sign in
      </button>
    </nav>

    <!-- Hero -->
    <main class="max-w-md mx-auto w-full px-4 flex flex-col flex-1 justify-center py-12 space-y-10">

      <div class="space-y-5">
        <div class="inline-flex items-center gap-2 bg-primary/10 text-primary text-xs font-medium px-3 py-1.5 rounded-full">
          <ActivityIcon class="size-3" />
          Smart glucose monitoring
        </div>

        <h1 class="text-4xl font-bold text-foreground leading-tight tracking-tight">
          Stay in range.<br />
          <span class="text-primary">Every day.</span>
        </h1>

        <p class="text-muted-foreground text-base leading-relaxed">
          Track your blood glucose, understand your trends, and hit your daily health goals — all in one simple app.
        </p>
      </div>

      <!-- Stats row -->
      <div class="grid grid-cols-3 gap-3">
        <div class="card p-4 text-center space-y-1">
          <div class="text-2xl font-bold text-primary">85%</div>
          <div class="text-xs text-muted-foreground">avg time in range</div>
        </div>
        <div class="card p-4 text-center space-y-1">
          <div class="text-2xl font-bold text-primary">7d</div>
          <div class="text-xs text-muted-foreground">trend charts</div>
        </div>
        <div class="card p-4 text-center space-y-1">
          <div class="text-2xl font-bold text-primary">A1C</div>
          <div class="text-xs text-muted-foreground">estimation</div>
        </div>
      </div>

      <!-- Feature list -->
      <div class="space-y-3">
        <div v-for="feature in features" :key="feature.label"
          class="flex items-center gap-3 p-3 card border-primary/10">
          <div class="size-8 rounded-full bg-primary/10 flex items-center justify-center shrink-0">
            <component :is="feature.icon" class="size-4 text-primary" />
          </div>
          <div>
            <p class="text-sm font-medium text-foreground">{{ feature.label }}</p>
            <p class="text-xs text-muted-foreground">{{ feature.desc }}</p>
          </div>
        </div>
      </div>

      <!-- CTA -->
      <div class="space-y-3 pb-8">
        <button @click="$router.push('/signup')"
          class="btn-primary w-full py-4 text-base font-medium">
          Get started free
        </button>
        <p class="text-center text-xs text-muted-foreground">
          Already have an account?
          <button @click="$router.push('/login')" class="text-primary underline underline-offset-2">Sign in</button>
        </p>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import {
  Droplets as DropletsIcon,
  Activity as ActivityIcon,
  TrendingUp as TrendingUpIcon,
  Heart as HeartIcon,
  Bell as BellIcon,
} from 'lucide-vue-next'

const features = [
  { icon: TrendingUpIcon, label: 'Trend analysis', desc: '7-day glucose charts with daily averages' },
  { icon: HeartIcon,      label: 'A1C estimation', desc: 'Estimate your A1C from logged readings' },
  { icon: BellIcon,       label: 'Daily goals',    desc: 'Track tests, water intake, and steps' },
]
</script>
