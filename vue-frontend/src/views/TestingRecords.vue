<template>
  <div class="min-h-full bg-background">
    <div class="max-w-md mx-auto p-4 space-y-6">
      <!-- Header -->
      <div class="space-y-1">
        <h2 class="text-xl font-medium text-foreground">Testing Records</h2>
        <p class="text-sm text-muted-foreground">Track and monitor your glucose readings</p>
      </div>

      <!-- Error Banner -->
      <div v-if="glucose.error" class="p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-700">
        {{ glucose.error }}
      </div>

      <!-- Add Reading -->
      <button @click="showAddModal = true" class="btn-primary w-full flex items-center justify-center gap-2 py-3">
        <PlusIcon class="size-4" />
        Add New Reading
      </button>

      <!-- Stats Overview -->
      <div class="grid grid-cols-3 gap-3">
        <div class="card shadow-sm p-3 text-center">
          <div class="text-xs text-muted-foreground mb-1">Today</div>
          <div class="text-xl font-medium text-foreground">{{ glucose.avgToday || '—' }}</div>
          <div class="text-xs text-muted-foreground">avg</div>
        </div>
        <div class="card shadow-sm p-3 text-center">
          <div class="text-xs text-muted-foreground mb-1">This Week</div>
          <div class="text-xl font-medium text-foreground">{{ glucose.avgWeek || '—' }}</div>
          <div class="text-xs text-muted-foreground">avg</div>
        </div>
        <div class="card shadow-sm p-3 text-center">
          <div class="text-xs text-muted-foreground mb-1">Readings</div>
          <div class="text-xl font-medium text-foreground">{{ glucose.records.length }}</div>
          <div class="text-xs text-muted-foreground">total</div>
        </div>
      </div>

      <!-- Tabs -->
      <div>
        <div class="flex gap-1 bg-muted/50 rounded-lg p-1">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            @click="activeTab = tab.key"
            :class="[
              'flex-1 py-1.5 text-sm rounded-md transition-all',
              activeTab === tab.key ? 'bg-card shadow-sm font-medium' : 'text-muted-foreground'
            ]"
          >
            {{ tab.label }}
          </button>
        </div>

        <!-- Loading -->
        <div v-if="glucose.loading" class="text-center py-10 text-muted-foreground text-sm">
          Loading records...
        </div>

        <!-- Records -->
        <div v-else class="space-y-3 mt-4">
          <div v-if="filteredRecords.length === 0" class="text-center py-10 text-muted-foreground text-sm">
            No records found.
          </div>

          <div
            v-for="(record, index) in filteredRecords"
            :key="record.id"
            class="card shadow-sm p-4"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <div class="size-12 rounded-full bg-primary/10 flex items-center justify-center">
                  <ActivityIcon class="size-6 text-primary" />
                </div>
                <div class="space-y-1">
                  <div class="flex items-center gap-2">
                    <span :class="['text-xl font-semibold', glucose.getStatusInfo(record.value).colorClass]">
                      {{ record.value }}
                    </span>
                    <span class="text-sm text-muted-foreground">mg/dL</span>
                    <component :is="trendIcon(record.value, filteredRecords[index + 1]?.value)" class="size-4" />
                  </div>
                  <div class="flex items-center gap-2 text-xs text-muted-foreground">
                    <CalendarIcon class="size-3" />
                    <span>{{ glucose.formatDate(record.recordedAt) }} • {{ glucose.formatTime(record.recordedAt) }}</span>
                  </div>
                </div>
              </div>
              <div class="text-right space-y-1">
                <span :class="['inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium', glucose.getStatusInfo(record.value).badgeClass]">
                  {{ glucose.getStatusInfo(record.value).label }}
                </span>
                <div v-if="record.mealContext" class="text-xs text-muted-foreground">
                  {{ glucose.getMealContextLabel(record.mealContext) }}
                </div>
              </div>
            </div>
            <div v-if="record.note" class="mt-3 p-2 bg-muted/50 rounded-md">
              <p class="text-xs text-muted-foreground">{{ record.note }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Reading Modal -->
     <div v-if="showAddModal" class="fixed inset-0 bg-black/50 z-50 flex items-end justify-center" @click.self="showAddModal = false">
      <div class="bg-card w-full max-w-md rounded-t-2xl p-6 space-y-4">
        <h3 class="text-lg font-medium text-foreground">Add New Reading</h3>
        <div class="space-y-3">
          <div>
            <label class="text-sm text-muted-foreground">Glucose Value (mg/dL)</label>
            <input v-model.number="newReading.value" type="number" class="w-full mt-1 px-3 py-2 border border-border rounded-lg bg-background text-sm" placeholder="e.g. 120" />
          </div>
          <div>
            <label class="text-sm text-muted-foreground">Meal Context</label>
            <select v-model="newReading.mealContext" class="w-full mt-1 px-3 py-2 border border-border rounded-lg bg-background text-sm">
              <option value="fasting">Fasting</option>
              <option value="before">Before Meal</option>
              <option value="after">After Meal</option>
            </select>
          </div>
          <div>
            <label class="text-sm text-muted-foreground">Note (optional)</label>
            <input v-model="newReading.note" type="text" class="w-full mt-1 px-3 py-2 border border-border rounded-lg bg-background text-sm" placeholder="Add a note..." />
          </div>
        </div>
        <div class="flex gap-3">
          <button @click="saveReading" :disabled="glucose.loading" class="btn-primary flex-1 py-2.5">
            {{ glucose.loading ? 'Saving...' : 'Save Reading' }}
          </button>
          <button @click="showAddModal = false" class="flex-1 py-2.5 border border-border rounded-lg text-sm hover:bg-muted transition-colors">Cancel</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useGlucoseStore } from '../stores/glucose'
import {
  Activity as ActivityIcon,
  Calendar as CalendarIcon,
  Plus as PlusIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Minus as MinusIcon,
} from 'lucide-vue-next'

const glucose = useGlucoseStore()
const activeTab = ref('all')
const showAddModal = ref(false)
const newReading = ref({ value: 0, mealContext: 'fasting', note: '' })

const tabs = [
  { key: 'all', label: 'All' },
  { key: 'today', label: 'Today' },
]

const filteredRecords = computed(() => {
  if (activeTab.value === 'today') return glucose.todayRecords
  return glucose.records
})

function trendIcon(value: number, prev?: number) {
  if (!prev) return MinusIcon
  if (value > prev) return TrendingUpIcon
  if (value < prev) return TrendingDownIcon
  return MinusIcon
}

async function saveReading() {
  if (!newReading.value.value) return
  await glucose.addRecord({
    value: newReading.value.value,
    mealContext: newReading.value.mealContext,
    note: newReading.value.note || undefined,
  })
  if (!glucose.error) {
    newReading.value = { value: 0, mealContext: 'fasting', note: '' }
    showAddModal.value = false
  }
}

// Fetch records when the page loads
onMounted(() => {
  glucose.fetchRecords()
})
</script>
