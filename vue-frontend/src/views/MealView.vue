<template>
  <div class="min-h-full bg-background">
    <div class="max-w-md mx-auto p-4 space-y-6">

      <!-- Header -->
      <div class="flex items-center justify-between pt-2">
        <div>
          <h1 class="text-xl font-bold text-foreground">Meal Tracker</h1>
          <p class="text-sm text-muted-foreground">Log and analyse your meals</p>
        </div>
        <button @click="showAddModal = true" class="btn-primary px-4 py-2 flex items-center gap-2">
          <PlusIcon class="size-4" />
          Add Meal
        </button>
      </div>

      <!-- Meals List -->
      <div v-if="meal.loading" class="space-y-3">
        <SkeletonLoader v-for="i in 3" :key="i" type="list-item" />
      </div>

      <div v-else-if="!meal.meals.length" class="card p-8 text-center space-y-3">
        <UtensilsIcon class="size-10 text-muted-foreground mx-auto" />
        <p class="text-muted-foreground text-sm">No meals logged yet</p>
        <button @click="showAddModal = true" class="btn-primary px-4 py-2">
          Log your first meal
        </button>
      </div>

      <div v-else class="space-y-3">
        <div v-for="m in meal.meals" :key="m.id" class="card p-4 space-y-3">
          <!-- Meal header -->
          <div class="flex items-start gap-3">
            <img v-if="m.photo_url" :src="m.photo_url"
              class="size-16 rounded-lg object-cover shrink-0" />
            <div v-else class="size-16 rounded-lg bg-muted flex items-center justify-center shrink-0">
              <UtensilsIcon class="size-6 text-muted-foreground" />
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="font-medium text-foreground truncate">{{ m.meal_name || 'Unnamed meal' }}</h3>
              <p class="text-xs text-muted-foreground">{{ formatMealTime(m.eaten_at) }}</p>
              <span v-if="m.ai_suggested"
                class="inline-flex items-center gap-1 text-xs bg-primary/10 text-primary px-2 py-0.5 rounded-full mt-1">
                <SparklesIcon class="size-3" /> AI analysed
              </span>
            </div>
            <button @click="meal.deleteMeal(m.id!)"
              class="text-muted-foreground hover:text-red-500 transition-colors shrink-0">
              <TrashIcon class="size-4" />
            </button>
          </div>

          <!-- Nutrients -->
          <div class="grid grid-cols-4 gap-2">
            <div class="text-center p-2 bg-muted rounded-lg">
              <div class="text-sm font-semibold text-foreground">{{ m.carbs_g ?? '—' }}</div>
              <div class="text-xs text-muted-foreground">Carbs g</div>
            </div>
            <div class="text-center p-2 bg-muted rounded-lg">
              <div class="text-sm font-semibold text-foreground">{{ m.protein_g ?? '—' }}</div>
              <div class="text-xs text-muted-foreground">Protein g</div>
            </div>
            <div class="text-center p-2 bg-muted rounded-lg">
              <div class="text-sm font-semibold text-foreground">{{ m.vitamins_mg ?? '—' }}</div>
              <div class="text-xs text-muted-foreground">Vitamins mg</div>
            </div>
            <div class="text-center p-2 bg-muted rounded-lg">
              <div class="text-sm font-semibold text-foreground">{{ m.calories ?? '—' }}</div>
              <div class="text-xs text-muted-foreground">kcal</div>
            </div>
          </div>

          <p v-if="m.notes" class="text-xs text-muted-foreground">{{ m.notes }}</p>
        </div>
      </div>
    </div>

    <!-- Add Meal Modal -->
    <div v-if="showAddModal" class="fixed inset-0 bg-black/50 z-50 flex items-end justify-center"
      @click.self="closeModal">
      <div class="bg-card w-full max-w-md rounded-t-2xl p-6 space-y-4 max-h-[90vh] overflow-y-auto">
        <h3 class="text-lg font-medium text-foreground">Log a Meal</h3>

        <!-- Photo upload -->
        <div class="space-y-2">
          <label class="text-sm font-medium text-foreground">Meal Photo</label>
          <div class="relative">
            <div v-if="photoPreview" class="relative">
              <img :src="photoPreview" class="w-full h-48 object-cover rounded-lg" />
              <button @click="clearPhoto"
                class="absolute top-2 right-2 bg-black/50 text-white rounded-full p-1">
                <XIcon class="size-4" />
              </button>
              <button v-if="!aiLoading && !aiAnalysed" @click="analysePhoto"
                class="absolute bottom-2 right-2 btn-primary px-3 py-1.5 text-xs flex items-center gap-1">
                <SparklesIcon class="size-3" /> Analyse with AI
              </button>
              <div v-if="aiLoading"
                class="absolute bottom-2 right-2 bg-primary text-primary-foreground px-3 py-1.5 rounded-lg text-xs flex items-center gap-1">
                <span class="animate-pulse">Analysing...</span>
              </div>
              <div v-if="aiAnalysed"
                class="absolute bottom-2 right-2 bg-green-500 text-white px-3 py-1.5 rounded-lg text-xs flex items-center gap-1">
                <SparklesIcon class="size-3" /> AI filled nutrients
              </div>
            </div>
            <label v-else
              class="flex flex-col items-center justify-center w-full h-48 border-2 border-dashed border-border rounded-lg cursor-pointer hover:bg-muted transition-colors">
              <CameraIcon class="size-8 text-muted-foreground mb-2" />
              <span class="text-sm text-muted-foreground">Take or upload a photo</span>
              <span class="text-xs text-muted-foreground mt-1">Tap to open camera or gallery</span>
              <input type="file" accept="image/*" capture="environment"
                class="hidden" @change="handlePhotoSelect" />
            </label>
          </div>
        </div>

        <!-- Meal name -->
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-foreground">Meal Name</label>
          <input v-model="form.meal_name" type="text" placeholder="e.g. Ugali with sukuma wiki"
            class="w-full px-3 py-2.5 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
        </div>

        <!-- Time eaten -->
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-foreground">Time Eaten</label>
          <input v-model="form.eaten_at" type="datetime-local"
            class="w-full px-3 py-2.5 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
        </div>

        <!-- Nutrients -->
        <div class="space-y-2">
          <label class="text-sm font-medium text-foreground">Nutrients</label>
          <div class="grid grid-cols-2 gap-3">
            <div class="space-y-1">
              <label class="text-xs text-muted-foreground">Carbs (g)</label>
              <input v-model.number="form.carbs_g" type="number" min="0" step="0.5" placeholder="0"
                class="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div class="space-y-1">
              <label class="text-xs text-muted-foreground">Protein (g)</label>
              <input v-model.number="form.protein_g" type="number" min="0" step="0.5" placeholder="0"
                class="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div class="space-y-1">
              <label class="text-xs text-muted-foreground">Vitamins (mg)</label>
              <input v-model.number="form.vitamins_mg" type="number" min="0" step="0.1" placeholder="0"
                class="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div class="space-y-1">
              <label class="text-xs text-muted-foreground">Calories (kcal)</label>
              <input v-model.number="form.calories" type="number" min="0" step="1" placeholder="0"
                class="w-full px-3 py-2 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
          </div>
        </div>

        <!-- Notes -->
        <div class="space-y-1.5">
          <label class="text-sm font-medium text-foreground">Notes <span class="text-muted-foreground font-normal">(optional)</span></label>
          <textarea v-model="form.notes" rows="2" placeholder="Any notes about this meal..."
            class="w-full px-3 py-2.5 border border-border rounded-lg bg-background text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 resize-none" />
        </div>

        <p v-if="meal.error" class="text-xs text-red-500">{{ meal.error }}</p>

        <!-- Actions -->
        <div class="flex gap-3">
          <button @click="saveMeal" :disabled="meal.loading || uploading"
            class="btn-primary flex-1 py-2.5">
            {{ meal.loading || uploading ? 'Saving...' : 'Save Meal' }}
          </button>
          <button @click="closeModal"
            class="flex-1 py-2.5 border border-border rounded-lg text-sm hover:bg-muted transition-colors">
            Cancel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMealStore } from '@/stores/meal'
import SkeletonLoader from '@/components/ui/SkeletonLoader.vue'
import axios from '@/lib/axios'
import {
  Plus as PlusIcon,
  Utensils as UtensilsIcon,
  Camera as CameraIcon,
  Trash as TrashIcon,
  X as XIcon,
  Sparkles as SparklesIcon,
} from 'lucide-vue-next'

const meal = useMealStore()

onMounted(() => meal.fetchMeals())

const showAddModal = ref(false)
const photoFile = ref<File | null>(null)
const photoPreview = ref<string | null>(null)
const uploading = ref(false)
const aiLoading = ref(false)
const aiAnalysed = ref(false)

const form = ref({
  meal_name: '',
  eaten_at: new Date().toISOString().slice(0, 16),
  carbs_g: null as number | null,
  protein_g: null as number | null,
  vitamins_mg: null as number | null,
  calories: null as number | null,
  notes: '',
})

function handlePhotoSelect(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  photoFile.value = file

  // Extract time from photo EXIF if available, otherwise use now
  form.value.eaten_at = new Date().toISOString().slice(0, 16)

  const reader = new FileReader()
  reader.onload = (ev) => {
    photoPreview.value = ev.target?.result as string
  }
  reader.readAsDataURL(file)
}

async function analysePhoto() {
  if (!photoPreview.value) return
  aiLoading.value = true
  try {
    const base64 = photoPreview.value.split(',')[1]
    const { data } = await axios.post(
      `${import.meta.env.VITE_PYTHON_SERVICE_URL}/meals/analyse`,
      { image_base64: base64 }
    )
    form.value.meal_name = data.meal_name || form.value.meal_name
    form.value.carbs_g = data.carbs_g
    form.value.protein_g = data.protein_g
    form.value.vitamins_mg = data.vitamins_mg
    form.value.calories = data.calories
    form.value.notes = data.notes || form.value.notes
    aiAnalysed.value = true
  } catch (e) {
    console.error('AI analysis failed:', e)
  } finally {
    aiLoading.value = false
  }
}

async function saveMeal() {
  uploading.value = true
  let photoUrl: string | null = null

  try {
    if (photoFile.value) {
      photoUrl = await meal.uploadPhoto(photoFile.value)
    }

    await meal.addMeal({
      user_id: 0, // set by store
      photo_url: photoUrl ?? undefined,
      meal_name: form.value.meal_name || undefined,
      eaten_at: new Date(form.value.eaten_at).toISOString(),
      carbs_g: form.value.carbs_g ?? undefined,
      protein_g: form.value.protein_g ?? undefined,
      vitamins_mg: form.value.vitamins_mg ?? undefined,
      calories: form.value.calories ?? undefined,
      notes: form.value.notes || undefined,
      ai_suggested: aiAnalysed.value,
    })

    if (!meal.error) closeModal()
  } finally {
    uploading.value = false
  }
}

function clearPhoto() {
  photoFile.value = null
  photoPreview.value = null
  aiAnalysed.value = false
}

function closeModal() {
  showAddModal.value = false
  clearPhoto()
  aiAnalysed.value = false
  form.value = {
    meal_name: '',
    eaten_at: new Date().toISOString().slice(0, 16),
    carbs_g: null,
    protein_g: null,
    vitamins_mg: null,
    calories: null,
    notes: '',
  }
}

function formatMealTime(dt: string) {
  return new Date(dt).toLocaleString('en-US', {
    weekday: 'short', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>
