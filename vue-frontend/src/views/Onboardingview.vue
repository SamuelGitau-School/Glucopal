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
        <h1 class="text-2xl font-bold text-foreground tracking-tight">Complete your profile</h1>
        <p class="text-sm text-muted-foreground">Help us personalise your experience</p>
      </div>

      <!-- Step indicators -->
      <div class="flex items-center justify-center gap-2">
        <div v-for="i in totalSteps" :key="i"
          :class="['h-1.5 rounded-full transition-all', i === currentStep ? 'w-8 bg-primary' : i < currentStep ? 'w-4 bg-primary/40' : 'w-4 bg-muted']" />
      </div>

      <!-- Step 1: Personal Details -->
      <div v-if="currentStep === 1" class="space-y-4">
        <div class="card p-5 space-y-4">
          <h2 class="font-medium text-foreground flex items-center gap-2">
            <UserIcon class="size-4 text-primary" /> Personal Details
          </h2>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Full Name</label>
            <input v-model="form.name" type="text" placeholder="Jane Smith"
              class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Phone <span class="text-muted-foreground font-normal">(optional)</span></label>
            <input v-model="form.phone" type="tel" placeholder="+1 234 567 8900"
              class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Date of Birth <span class="text-muted-foreground font-normal">(optional)</span></label>
            <input v-model="form.dateOfBirth" type="date"
              class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          </div>

          <!-- Location autocomplete -->
          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">
              Location <span class="text-muted-foreground font-normal">(optional)</span>
            </label>
            <div
              class="relative"
              ref="locationWrapRef"
              @mouseenter="onLocationHoverOpen"
              @mouseleave="onLocationHoverClose"
            >
              <input
                v-model="locationQuery"
                type="text"
                placeholder="City, Country"
                autocomplete="off"
                @click="onLocationFocus"
                @input="onLocationInput"
                @focus="onLocationFocus"
                @keydown="onLocationKeydown"
                class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
              />

              <!-- Spinner while loading countries -->
              <span v-if="countriesLoading" class="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">
                <svg class="animate-spin h-3.5 w-3.5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8H4z"/>
                </svg>
              </span>

              <!-- Suggestions dropdown -->
              <Transition name="drop">
                <div
                  v-if="locationOpen && locationResults.length"
                  class="absolute z-50 mt-1.5 w-full bg-white border border-border rounded-lg shadow-lg overflow-hidden"
                >
                  <ul ref="locationListRef" class="max-h-52 overflow-y-auto py-1">
                    <li
                      v-for="(item, i) in locationResults"
                      :key="item.id"
                      @mouseenter="locationActiveIndex = i"
                      @mouseleave="locationActiveIndex = -1"
                      @mousedown.prevent="selectLocation(item)"
                      :class="[
                        'flex items-center justify-between gap-3 px-3 py-2 cursor-pointer text-sm transition-colors',
                        locationActiveIndex === i
                          ? 'bg-accent text-accent-foreground'
                          : 'text-popover-foreground hover:bg-accent hover:text-accent-foreground'
                      ]"
                    >
                      <span class="flex items-center gap-2 min-w-0">
                        <span class="text-base leading-none shrink-0">{{ item.flag }}</span>
                        <span class="truncate font-medium">{{ item.capital }}</span>
                      </span>
                      <!-- Country label fades out when narrowed to one country -->
                      <span
                        class="text-xs text-muted-foreground shrink-0 transition-opacity duration-200"
                        :class="locationUniqueCountries.size > 1 ? 'opacity-100' : 'opacity-0'"
                      >
                        {{ item.country }}
                      </span>
                    </li>
                  </ul>
                  <!-- Currency hint when narrowed to one country -->
                  <div
                    v-if="locationUniqueCountries.size === 1 && locationActiveCurrency"
                    class="px-3 py-1.5 border-t border-border text-xs text-muted-foreground flex items-center gap-1.5"
                  >
                    <span>Currency:</span>
                    <span class="font-medium text-foreground">{{ locationActiveCurrency }}</span>
                  </div>
                </div>
              </Transition>

              <!-- No results -->
              <Transition name="drop">
                <div
                  v-if="locationOpen && !locationResults.length && locationQuery.trim().length >= 2 && !countriesLoading"
                  class="absolute z-50 mt-1.5 w-full bg-popover border border-border rounded-lg shadow-lg px-3 py-3 text-sm text-muted-foreground"
                >
                  No matches for "{{ locationQuery }}"
                </div>
              </Transition>
            </div>
          </div>

        </div>
      </div>

      <!-- Step 2: Diabetes Info -->
      <div v-if="currentStep === 2" class="space-y-4">
        <div class="card p-5 space-y-4">
          <h2 class="font-medium text-foreground flex items-center gap-2">
            <HeartIcon class="size-4 text-primary" /> Diabetes Information
          </h2>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Diabetes Type</label>
            <div class="grid grid-cols-2 gap-2">
              <button v-for="type in diabetesTypes" :key="type.value"
                @click="form.diabetesType = type.value"
                :class="['py-2.5 px-3 rounded-lg border text-sm transition-colors text-left',
                  form.diabetesType === type.value
                    ? 'border-primary bg-primary/10 text-primary font-medium'
                    : 'border-border text-foreground hover:bg-muted']">
                {{ type.label }}
              </button>
            </div>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">Year Diagnosed <span class="text-muted-foreground font-normal">(optional)</span></label>
            <input v-model="form.diagnosedYear" type="number" placeholder="e.g. 2015" min="1900" :max="new Date().getFullYear()"
              class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          </div>
        </div>
      </div>

      <!-- Step 3: Target Goals -->
      <div v-if="currentStep === 3" class="space-y-4">
        <div class="card p-5 space-y-4">
          <h2 class="font-medium text-foreground flex items-center gap-2">
            <ActivityIcon class="size-4 text-primary" /> Target Goals
          </h2>

          <p class="text-xs text-muted-foreground">These help us calculate how well you're managing your glucose. You can update them anytime in your profile.</p>

          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Target Low (mg/dL)</label>
              <input v-model="form.targetRangeLow" type="number" placeholder="70"
                class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div class="space-y-1.5">
              <label class="text-sm font-medium text-foreground">Target High (mg/dL)</label>
              <input v-model="form.targetRangeHigh" type="number" placeholder="140"
                class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
          </div>

          <div class="space-y-1.5">
            <label class="text-sm font-medium text-foreground">HbA1c Goal (%) <span class="text-muted-foreground font-normal">(optional)</span></label>
            <input v-model="form.hba1cGoal" type="number" step="0.1" placeholder="e.g. 7.0"
              class="w-full border border-border rounded-lg px-3 py-2.5 bg-background text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          </div>

          <p v-if="error" class="text-xs text-red-500 flex items-center gap-1.5">
            <AlertCircleIcon class="size-3.5 shrink-0" /> {{ error }}
          </p>
        </div>
      </div>

      <!-- Navigation Buttons -->
      <div class="space-y-3">
        <button @click="next" :disabled="loading || (currentStep === 1 && !form.name)"
          class="btn-primary w-full py-3">
          <span v-if="loading">Saving...</span>
          <span v-else-if="currentStep === totalSteps">Go to Dashboard →</span>
          <span v-else>Continue →</span>
        </button>

        <button v-if="currentStep > 1" @click="currentStep--"
          class="w-full text-center text-sm text-muted-foreground hover:text-foreground">
          ← Back
        </button>

        <button v-if="currentStep < totalSteps" @click="skip"
          class="w-full text-center text-sm text-muted-foreground hover:text-foreground">
          Skip for now
        </button>
      </div>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import axios from '@/lib/axios'
import {
  Droplets as DropletsIcon,
  User as UserIcon,
  Heart as HeartIcon,
  Activity as ActivityIcon,
  AlertCircle as AlertCircleIcon,
} from 'lucide-vue-next'

const router = useRouter()
const auth = useAuthStore()

const totalSteps = 3
const currentStep = ref(1)
const loading = ref(false)
const error = ref('')

const form = ref({
  name: (auth.user?.name as string) ?? '',
  phone: '',
  dateOfBirth: '',
  location: '',
  diabetesType: (auth.user?.diabetesType as string) ?? '',
  diagnosedYear: '',
  targetRangeLow: '',
  targetRangeHigh: '',
  hba1cGoal: '',
})

const diabetesTypes = [
  { value: 'TYPE_1', label: 'Type 1' },
  { value: 'TYPE_2', label: 'Type 2' },
  { value: 'GESTATIONAL', label: 'Gestational' },
  { value: 'PREDIABETES', label: 'Prediabetes' },
  { value: 'OTHER', label: 'Other' },
]

// ── Location autocomplete ──────────────────────────────────────────

interface LocationEntry {
  id: number
  country: string
  capital: string
  flag: string
  currency: string
}

const locationQuery       = ref('')
const locationOpen        = ref(false)
const locationActiveIndex = ref(-1)
const locationWrapRef     = ref<HTMLElement | null>(null)
const locationListRef     = ref<HTMLElement | null>(null)
const countriesLoading    = ref(false)
const catalogue           = ref<LocationEntry[]>([])

onMounted(async () => {
  countriesLoading.value = true
  try {
    const res  = await fetch('https://restcountries.com/v3.1/all?fields=name,capital,currencies,flag,cca2')
    const data = await res.json()
    let id = 0
    const rows: LocationEntry[] = []
    for (const c of data) {
      const country   = c.name?.common ?? ''
      const flag      = c.flag ?? ''
      const capitals: string[] = Array.isArray(c.capital) && c.capital.length ? c.capital : []
      const currency  = c.currencies
        ? Object.entries(c.currencies as Record<string, { symbol?: string; name?: string }>)
            .map(([code, info]) => `${info.symbol ?? code} ${info.name ?? code} (${code})`)
            .join(', ')
        : ''
      if (capitals.length) {
        for (const capital of capitals) rows.push({ id: id++, country, capital, flag, currency })
      } else {
        rows.push({ id: id++, country, capital: '', flag, currency })
      }
    }
    rows.sort((a, b) => a.country.localeCompare(b.country) || a.capital.localeCompare(b.capital))
    catalogue.value = rows
  } catch (e) {
    console.error('Failed to load countries:', e)
  } finally {
    countriesLoading.value = false
  }
})

const locationResults = computed<LocationEntry[]>(() => {
  const q = locationQuery.value.trim().toLowerCase()
  if (q.length < 2 || !catalogue.value.length) return []
  const scored: (LocationEntry & { score: number })[] = []
  for (const item of catalogue.value) {
    const city    = item.capital.toLowerCase()
    const country = item.country.toLowerCase()
    let score = 0
    if (city && city === q)              score = 100
    else if (city && city.startsWith(q)) score = 80
    else if (city && city.includes(q))   score = 50
    else if (country === q)              score = 45
    else if (country.startsWith(q))      score = 40
    else if (country.includes(q))        score = 20
    else continue
    scored.push({ ...item, score })
  }
  scored.sort((a, b) => b.score - a.score || a.capital.localeCompare(b.capital))
  return scored.slice(0, 10)
})

const locationUniqueCountries = computed(() => new Set(locationResults.value.map(r => r.country)))

const locationActiveCurrency = computed(() =>
  locationUniqueCountries.value.size === 1 ? (locationResults.value[0]?.currency ?? '') : ''
)

function onLocationHoverOpen() {
  if (locationQuery.value.trim().length >= 2) locationOpen.value = true
}

function onLocationHoverClose() {
  // only close if mouse left the whole wrapper and no item is being clicked
  setTimeout(() => {
    if (!locationWrapRef.value?.matches(':hover')) locationOpen.value = false
  }, 100)
}

function onLocationInput() {
  locationOpen.value = locationQuery.value.trim().length >= 2
  locationActiveIndex.value = -1
  form.value.location = locationQuery.value
}

function onLocationFocus() {
  if (locationQuery.value.trim().length >= 2) locationOpen.value = true
}

function selectLocation(item: LocationEntry) {
  const value = item.capital ? `${item.capital}, ${item.country}` : item.country
  locationQuery.value   = value
  form.value.location   = value
  locationOpen.value    = false
  locationActiveIndex.value = -1
}

function onLocationKeydown(e: KeyboardEvent) {
  if (!locationOpen.value || !locationResults.value.length) return
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    locationActiveIndex.value = (locationActiveIndex.value + 1) % locationResults.value.length
    locationListRef.value?.children[locationActiveIndex.value]?.scrollIntoView({ block: 'nearest' })
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    locationActiveIndex.value = (locationActiveIndex.value - 1 + locationResults.value.length) % locationResults.value.length
    locationListRef.value?.children[locationActiveIndex.value]?.scrollIntoView({ block: 'nearest' })
  } else if (e.key === 'Enter' && locationActiveIndex.value >= 0) {
    e.preventDefault()
    const selected = locationResults.value[locationActiveIndex.value]
        if (selected) {
        selectLocation(selected)
      }
  } else if (e.key === 'Escape') {
    locationOpen.value = false
  }
}

function onClickOutside(e: MouseEvent) {
  if (locationWrapRef.value && !locationWrapRef.value.contains(e.target as Node)) {
    locationOpen.value = false
  }
}

onMounted(() => document.addEventListener('mousedown', onClickOutside))
onBeforeUnmount(() => document.removeEventListener('mousedown', onClickOutside))

// ── Navigation ────────────────────────────────────────────────────

async function next() {
  if (currentStep.value < totalSteps) {
    currentStep.value++
    return
  }
  await saveAndContinue()
}

function skip() {
  if (currentStep.value < totalSteps) {
    currentStep.value++
  } else {
    router.push('/dashboard')
  }
}

async function saveAndContinue() {
  loading.value = true
  error.value = ''
  try {
    await axios.put(`/users/${auth.user?.id}/profile`, {
      name:            form.value.name,
      phone:           form.value.phone || null,
      dateOfBirth:     form.value.dateOfBirth || null,
      location:        form.value.location || null,
      diabetesType:    form.value.diabetesType || null,
      diagnosedYear:   form.value.diagnosedYear ? Number(form.value.diagnosedYear) : null,
      targetRangeLow:  form.value.targetRangeLow ? Number(form.value.targetRangeLow) : null,
      targetRangeHigh: form.value.targetRangeHigh ? Number(form.value.targetRangeHigh) : null,
      hba1cGoal:       form.value.hba1cGoal ? Number(form.value.hba1cGoal) : null,
    })
    if (auth.user) auth.user = { ...auth.user, ...form.value }
    router.push('/dashboard')
  } catch (e: any) {
    error.value = e?.response?.data?.error ?? 'Failed to save. You can update this in your profile later.'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.drop-enter-active,
.drop-leave-active {
  transition: opacity 0.12s ease, transform 0.12s ease;
}
.drop-enter-from,
.drop-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
