import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'

export interface GlucoseRecord {
  id: string
  value: number
  recordedAt: string
  note?: string
  mealContext?: 'BEFORE_MEAL' | 'AFTER_MEAL' | 'FASTING'
  status?: string
}

export const useGlucoseStore = defineStore('glucose', () => {
  const records = ref<GlucoseRecord[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  function getHeaders() {
    const auth = useAuthStore()
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${auth.accessToken}`
    }
  }

  function getUserId() {
    const auth = useAuthStore()
    return auth.user?.id
  }

  function getBaseUrl() {
    return import.meta.env.VITE_API_URL
  }

  const weeklyData = computed(() => {
    const days: Record<string, number[]> = {}
    records.value.forEach(r => {
      const day = new Date(r.recordedAt).toLocaleDateString('en-US', { weekday: 'short' })
      if (!days[day]) days[day] = []
      days[day].push(r.value)
    })
    return Object.entries(days).map(([time, values]) => ({
      time,
      value: Math.round(values.reduce((a, b) => a + b, 0) / values.length),
    }))
  })

  const todayRecords = computed(() => {
    const today = new Date().toDateString()
    return records.value.filter(r => new Date(r.recordedAt).toDateString() === today)
  })

  const avgToday = computed(() => {
    if (!todayRecords.value.length) return 0
    return Math.round(todayRecords.value.reduce((sum, r) => sum + r.value, 0) / todayRecords.value.length)
  })

  const avgWeek = computed(() => {
    if (!records.value.length) return 0
    return Math.round(records.value.reduce((sum, r) => sum + r.value, 0) / records.value.length)
  })

  async function fetchRecords() {
    const userId = getUserId()
    if (!userId) return
    loading.value = true
    error.value = null
    try {
      const res = await fetch(`${getBaseUrl()}/users/${userId}/glucose`, {
        headers: getHeaders()
      })
      if (!res.ok) throw new Error('Failed to fetch records')
      records.value = await res.json()
    } catch (e: any) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function addRecord(record: { value: number; mealContext?: string; note?: string }) {
    const userId = getUserId()
    if (!userId) return
    loading.value = true
    error.value = null
    try {
      const res = await fetch(`${getBaseUrl()}/users/${userId}/glucose`, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify({
          value: record.value,
          mealContext: record.mealContext?.toUpperCase() ?? null,
          note: record.note ?? null,
          recordedAt: new Date().toISOString(),
        }),
      })
      if (!res.ok) throw new Error('Failed to save record')
      const saved = await res.json()
      records.value.unshift(saved)
    } catch (e: any) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

  async function deleteRecord(recordId: string) {
    const userId = getUserId()
    if (!userId) return
    try {
      const res = await fetch(`${getBaseUrl()}/users/${userId}/glucose/${recordId}`, {
        method: 'DELETE',
        headers: getHeaders()
      })
      if (!res.ok) throw new Error('Failed to delete record')
      records.value = records.value.filter(r => r.id !== recordId)
    } catch (e: any) {
      error.value = e.message
    }
  }

  function getStatusInfo(value: number) {
    if (value < 70) return { label: 'Low', colorClass: 'text-yellow-600', badgeClass: 'bg-red-100 text-red-700' }
    if (value <= 140) return { label: 'Normal', colorClass: 'text-green-600', badgeClass: 'bg-green-100 text-green-700' }
    return { label: 'High', colorClass: 'text-red-600', badgeClass: 'bg-red-100 text-red-700' }
  }

  function getMealContextLabel(ctx?: string) {
    const map: Record<string, string> = { FASTING: 'Fasting', BEFORE_MEAL: 'Before Meal', AFTER_MEAL: 'After Meal' }
    return ctx ? map[ctx] ?? '' : ''
  }

  function formatDate(recordedAt: string) {
    const date = new Date(recordedAt)
    const today = new Date()
    const yesterday = new Date()
    yesterday.setDate(today.getDate() - 1)
    if (date.toDateString() === today.toDateString()) return 'Today'
    if (date.toDateString() === yesterday.toDateString()) return 'Yesterday'
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
  }

  function formatTime(recordedAt: string) {
    return new Date(recordedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  return {
    records, loading, error,
    weeklyData, todayRecords, avgToday, avgWeek,
    fetchRecords, addRecord, deleteRecord,
    getStatusInfo, getMealContextLabel, formatDate, formatTime,
  }
})
