<template>
<SkeletonLoader v-if="profileLoading" type="profile" />
<div v-else>
  <div class="min-h-screen bg-background">
    <div class="max-w-md mx-auto px-4 py-6 space-y-6">


      <!-- Profile Header -->
      <div class="card border-primary/20 p-6">
        <div class="flex flex-col items-center gap-4">
          <div class="relative">
            <div class="size-24 rounded-full border-4 border-primary/20 bg-primary/10 flex items-center justify-center text-primary text-3xl font-semibold">
              {{ initials }}
            </div>
            <button class="absolute bottom-0 right-0 size-8 rounded-full bg-primary flex items-center justify-center shadow-lg text-white">
              <CameraIcon class="size-4" />
            </button>
          </div>
          <div class="text-center">
            <h2 class="text-xl font-medium text-foreground">{{ form.name }}</h2>
            <p class="text-sm text-muted-foreground">{{ auth.user?.email }}</p>
          </div>
        </div>
      </div>

      <!-- Personal Information -->
      <div class="card">
        <div class="p-4 pb-2 flex items-center justify-between">
          <div>
            <h3 class="font-medium text-foreground">Personal Information</h3>
            <p class="text-sm text-muted-foreground">Manage your personal details</p>
          </div>
          <button @click="isEditing = !isEditing" class="p-2 rounded-lg hover:bg-muted transition-colors text-primary">
            <Edit2Icon class="size-4" />
          </button>
        </div>
        <div class="p-4 pt-2 space-y-4">

          <!-- Name -->
          <div class="space-y-1">
            <label class="text-sm text-muted-foreground">Full Name</label>
            <input v-model="form.name" type="text" :disabled="!isEditing"
              class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40" />
          </div>

          <!-- Email (read-only — set at signup) -->
          <div class="space-y-1">
            <label class="text-sm text-muted-foreground flex items-center gap-2">
              <MailIcon class="size-4" /> Email
            </label>
            <input :value="auth.user?.email" type="email" disabled
              class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm opacity-60 focus:outline-none" />
          </div>

          <!-- Phone -->
          <div class="space-y-1">
            <label class="text-sm text-muted-foreground flex items-center gap-2">
              <PhoneIcon class="size-4" /> Phone
            </label>
            <input v-model="form.phone" type="tel" :disabled="!isEditing"
              class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40" />
          </div>

          <!-- Date of Birth -->
          <div class="space-y-1">
            <label class="text-sm text-muted-foreground flex items-center gap-2">
              <CalendarIcon class="size-4" /> Date of Birth
            </label>
            <input v-model="form.dateOfBirth" type="date" :disabled="!isEditing"
              class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40" />
          </div>

          <!-- Location -->
          <div class="space-y-1">
            <label class="text-sm text-muted-foreground flex items-center gap-2">
              <MapPinIcon class="size-4" /> Location
            </label>
            <input v-model="form.location" type="text" :disabled="!isEditing"
              class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40" />
          </div>

          <p v-if="saveError" class="text-xs text-red-500">{{ saveError }}</p>
          <p v-if="saveSuccess" class="text-xs text-green-600">Profile updated successfully!</p>

          <div v-if="isEditing" class="flex gap-3 pt-2">
            <button @click="saveProfile" :disabled="saving" class="btn-primary flex-1 py-2.5">
              {{ saving ? 'Saving...' : 'Save Changes' }}
            </button>
            <button @click="cancelEdit" class="flex-1 py-2.5 border border-border rounded-lg text-sm hover:bg-muted transition-colors">
              Cancel
            </button>
          </div>
        </div>
      </div>

      <!-- Health Information -->
      <div class="card">
        <div class="p-4 pb-2 flex items-center justify-between">
          <div>
            <h3 class="font-medium text-foreground">Health Information</h3>
            <p class="text-sm text-muted-foreground">Diabetes management details</p>
          </div>
          <button @click="isEditingHealth = !isEditingHealth" class="p-2 rounded-lg hover:bg-muted transition-colors text-primary">
            <Edit2Icon class="size-4" />
          </button>
        </div>
        <div class="p-4 pt-2 space-y-4">
          <div class="grid grid-cols-2 gap-4">

            <!-- Diabetes Type -->
            <div class="space-y-1 col-span-2">
              <label class="text-xs text-muted-foreground">Diabetes Type</label>
              <select v-model="form.diabetesType" :disabled="!isEditingHealth"
                class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40">
                <option value="">Not specified</option>
                <option value="TYPE_1">Type 1</option>
                <option value="TYPE_2">Type 2</option>
                <option value="GESTATIONAL">Gestational</option>
                <option value="PREDIABETES">Prediabetes</option>
                <option value="OTHER">Other</option>
              </select>
            </div>

            <!-- Diagnosed Year -->
            <div class="space-y-1">
              <label class="text-xs text-muted-foreground">Diagnosed Year</label>
              <input v-model="form.diagnosedYear" type="number" :disabled="!isEditingHealth" placeholder="e.g. 2015"
                class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40" />
            </div>

            <!-- HbA1c Goal -->
            <div class="space-y-1">
              <label class="text-xs text-muted-foreground">HbA1c Goal (%)</label>
              <input v-model="form.hba1cGoal" type="number" step="0.1" :disabled="!isEditingHealth" placeholder="e.g. 7.0"
                class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40" />
            </div>

            <!-- Target Range Low -->
            <div class="space-y-1">
              <label class="text-xs text-muted-foreground">Target Low (mg/dL)</label>
              <input v-model="form.targetRangeLow" type="number" :disabled="!isEditingHealth" placeholder="e.g. 70"
                class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40" />
            </div>

            <!-- Target Range High -->
            <div class="space-y-1">
              <label class="text-xs text-muted-foreground">Target High (mg/dL)</label>
              <input v-model="form.targetRangeHigh" type="number" :disabled="!isEditingHealth" placeholder="e.g. 140"
                class="w-full px-3 py-2 border border-border rounded-lg bg-card text-sm disabled:opacity-60 focus:outline-none focus:ring-2 focus:ring-primary/40" />
            </div>
          </div>

          <div v-if="isEditingHealth" class="flex gap-3 pt-2">
            <button @click="saveProfile" :disabled="saving" class="btn-primary flex-1 py-2.5">
              {{ saving ? 'Saving...' : 'Save Changes' }}
            </button>
            <button @click="isEditingHealth = false" class="flex-1 py-2.5 border border-border rounded-lg text-sm hover:bg-muted transition-colors">
              Cancel
            </button>
          </div>
        </div>
      </div>

      <!-- Logout -->
      <button @click="handleLogout" class="w-full py-3 border border-red-200 rounded-lg text-sm text-red-500 hover:bg-red-50 transition-colors">
        Sign Out
      </button>

    </div>
  </div>
</div>
</template>

<script setup lang="ts">
import SkeletonLoader from '@/components/ui/SkeletonLoader.vue'
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import axios from '../lib/axios'
import {
  Camera as CameraIcon,
  Edit2 as Edit2Icon,
  Mail as MailIcon,
  Phone as PhoneIcon,
  Calendar as CalendarIcon,
  MapPin as MapPinIcon,
} from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()
const profileLoading = ref(true)
const isEditing = ref(false)
const isEditingHealth = ref(false)
const saving = ref(false)
const saveError = ref('')
const saveSuccess = ref(false)

const form = ref({
  name: (auth.user?.name as string) ?? '',
  phone: (auth.user?.phone as string) ?? '',
  dateOfBirth: (auth.user?.dateOfBirth as string) ?? '',
  location: (auth.user?.location as string) ?? '',
  diabetesType: (auth.user?.diabetesType as string) ?? '',
  diagnosedYear: (auth.user?.diagnosedYear as number | '') ?? '',
  targetRangeLow: (auth.user?.targetRangeLow as number | '') ?? '',
  targetRangeHigh: (auth.user?.targetRangeHigh as number | '') ?? '',
  hba1cGoal: (auth.user?.hba1cGoal as number | '') ?? '',
})

onMounted(async () => {
  profileLoading.value= true
  try {
    const { data } = await axios.get(`/users/${auth.user?.id}`)
    if (auth.user) {
      auth.user = { ...auth.user, ...data }
      localStorage.setItem('user', JSON.stringify(auth.user))
    }
    form.value.name = data.name ?? ''
    form.value.phone = data.phone ?? ''
    form.value.dateOfBirth = data.dateOfBirth ?? ''
    form.value.location = data.location ?? ''
    form.value.diabetesType = data.diabetesType ?? ''
    form.value.diagnosedYear = data.diagnosedYear ?? ''
    form.value.targetRangeLow = data.targetRangeLow ?? ''
    form.value.targetRangeHigh = data.targetRangeHigh ?? ''
    form.value.hba1cGoal = data.hba1cGoal ?? ''
  }
  catch (e) {
    console.error('Failed to load profile', e)}
    finally{
    profileLoading.value=false
  }
})

const initials = computed(() =>
  form.value.name
    .split(' ')
    .map((n: string) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2) || '?'
)

function cancelEdit() {
  isEditing.value = false
  // Reset to auth store values
  form.value.name = (auth.user?.name as string) ?? ''
  form.value.phone = (auth.user?.phone as string) ?? ''
  form.value.dateOfBirth = (auth.user?.dateOfBirth as string) ?? ''
  form.value.location = (auth.user?.location as string) ?? ''
}

async function saveProfile() {
  saveError.value = ''
  saveSuccess.value = false
  saving.value = true
  try {
    const { data } = await axios.put(`/users/${auth.user?.id}/profile`, {
      name: form.value.name,
      phone: form.value.phone || null,
      dateOfBirth: form.value.dateOfBirth || null,
      location: form.value.location || null,
      diabetesType: form.value.diabetesType || null,
      diagnosedYear: form.value.diagnosedYear || null,
      targetRangeLow: form.value.targetRangeLow || null,
      targetRangeHigh: form.value.targetRangeHigh || null,
      hba1cGoal: form.value.hba1cGoal || null,
    })
    if (auth.user) {
      auth.user = { ...auth.user, ...data }
      localStorage.setItem('user', JSON.stringify(auth.user))
    }
    saveSuccess.value = true
    isEditing.value = false
    isEditingHealth.value = false
    setTimeout(() => saveSuccess.value = false, 3000)
  } catch (e: any) {
    saveError.value = e?.response?.data?.error ?? 'Failed to save. Please try again.'
  } finally {
    saving.value = false
  }
}

async function handleLogout() {
  await auth.logout()
  router.push('/login')
}
</script>
