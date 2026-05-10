<template>
  <div class="min-h-full bg-background">
    <div class="max-w-md mx-auto p-4 space-y-6">

      <!-- Header -->
      <div class="flex items-center justify-between pt-2">
        <div class="space-y-0.5">
          <h1 class="text-xl font-bold text-foreground">
            Good {{ timeOfDay }}, {{ firstName }} 👋
          </h1>
          <p class="text-sm text-muted-foreground">{{ currentDate }}</p>
        </div>
        <button @click="$router.push('/profile')"
          class="size-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-semibold text-sm">
          {{ initials }}
        </button>
      </div>

      <!-- Quick Stats -->
      <div class="grid grid-cols-2 gap-3">
        <div class="card border-primary/20 shadow-sm p-4 space-y-2">
          <div class="flex items-center gap-2 text-primary">
            <DropletsIcon class="size-5" />
            <span class="text-xs text-muted-foreground">Current</span>
          </div>
          <div>
            <div class="text-2xl text-foreground">120</div>
            <div class="text-xs text-muted-foreground">mg/dL</div>
          </div>
        </div>

        <div class="card border-primary/20 shadow-sm p-4 space-y-2">
          <div class="flex items-center gap-2 text-primary">
            <TrendingUpIcon class="size-5" />
            <span class="text-xs text-muted-foreground">Avg (7d)</span>
          </div>
          <div>
            <div class="text-2xl text-foreground">{{ glucose.avgWeek }}</div>
            <div class="text-xs text-muted-foreground">mg/dL</div>
          </div>
        </div>

        <div class="card border-primary/20 shadow-sm p-4 space-y-2">
          <div class="flex items-center gap-2 text-primary">
            <HeartIcon class="size-5" />
            <span class="text-xs text-muted-foreground">A1C</span>
          </div>
          <div>
            <div class="text-2xl text-foreground">6.5</div>
            <div class="text-xs text-muted-foreground">%</div>
          </div>
        </div>

        <div class="card border-primary/20 shadow-sm p-4 space-y-2">
          <div class="flex items-center gap-2 text-primary">
            <ActivityIcon class="size-5" />
            <span class="text-xs text-muted-foreground">Range</span>
          </div>
          <div>
            <div class="text-2xl text-foreground">85%</div>
            <div class="text-xs text-muted-foreground">in target</div>
          </div>
        </div>
      </div>

      <!-- Glucose Chart -->
      <div class="card shadow-sm">
        <div class="p-4 pb-2">
          <h3 class="text-lg font-medium text-foreground">7-Day Glucose Trend</h3>
          <p class="text-sm text-muted-foreground">Average daily glucose levels</p>
        </div>
        <div class="p-4 pt-0">
          <svg viewBox="0 0 320 160" class="w-full" xmlns="http://www.w3.org/2000/svg">
            <line v-for="y in [20,60,100,140]" :key="y" x1="40" :y1="y" x2="310" :y2="y" stroke="#f4d9c6" stroke-width="1" />
            <text v-for="(label, i) in ['150','130','110','90']" :key="i" x="35" :y="[24,64,104,144][i]" text-anchor="end" font-size="10" fill="#8b8b8b">{{ label }}</text>
            <text v-for="(d, i) in glucose.weeklyData" :key="d.time" :x="40 + i * 40" y="158" text-anchor="middle" font-size="10" fill="#8b8b8b">{{ d.time }}</text>
            <polyline :points="chartPoints" fill="none" stroke="#e67e50" stroke-width="3" stroke-linejoin="round" />
            <circle v-for="(point, i) in chartDots" :key="i" :cx="point.cx" :cy="point.cy" r="4" fill="#e67e50" />
          </svg>
        </div>
      </div>

      <!-- Daily Goals -->
      <div class="card shadow-sm">
        <div class="p-4 pb-2">
          <h3 class="text-lg font-medium text-foreground">Today's Goals</h3>
        </div>
        <div class="p-4 pt-0 space-y-4">
          <div v-for="goal in goals" :key="goal.label" class="space-y-2">
            <div class="flex justify-between text-sm">
              <span class="text-foreground">{{ goal.label }}</span>
              <span class="text-muted-foreground">{{ goal.progress }}</span>
            </div>
            <div class="h-2 bg-muted rounded-full overflow-hidden">
              <div class="h-full bg-primary rounded-full transition-all" :style="{ width: goal.percent + '%' }" />
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="space-y-3">
        <h3 class="font-medium text-foreground">Quick Actions</h3>
        <div class="grid grid-cols-2 gap-3">
          <button @click="$router.push('/testing-records')" class="btn-primary h-auto py-4 flex flex-col gap-2 items-center">
            <PlusIcon class="size-5" />
            <span class="text-sm">Log Glucose</span>
          </button>
          <button class="btn-primary h-auto py-4 flex flex-col gap-2 items-center">
            <ActivityIcon class="size-5" />
            <span class="text-sm">Log Meal</span>
          </button>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useGlucoseStore } from '../stores/glucose'
import { useAuthStore } from '../stores/auth'
import {
  Droplets as DropletsIcon,
  TrendingUp as TrendingUpIcon,
  Heart as HeartIcon,
  Activity as ActivityIcon,
  Plus as PlusIcon,
} from 'lucide-vue-next'

const glucose = useGlucoseStore()
const auth = useAuthStore()

const currentDate = new Date().toLocaleDateString('en-US', {
  weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
})

const timeOfDay = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return 'morning'
  if (h < 17) return 'afternoon'
  return 'evening'
})

// Extract first name from full name
const firstName = computed(() => {
  const name = auth.user?.name as string | undefined
  return name ? name.split(' ')[0] : 'there'
})

const initials = computed(() => {
  const name = auth.user?.name as string | undefined
  if (!name) return '?'
  return name.split(' ').map((n: string) => n[0]).join('').toUpperCase().slice(0, 2)
})

const goals = [
  { label: 'Blood glucose tests', progress: '3/4', percent: 75 },
  { label: 'Water intake', progress: '6/8 glasses', percent: 75 },
  { label: 'Steps', progress: '7,250/10,000', percent: 72.5 },
]

function mapY(value: number) {
  return 140 - ((value - 80) / 80) * 120
}

const chartDots = computed(() =>
  glucose.weeklyData.map((d, i) => ({ cx: 40 + i * 40, cy: mapY(d.value) }))
)

const chartPoints = computed(() =>
  chartDots.value.map(p => `${p.cx},${p.cy}`).join(' ')
)
</script>
