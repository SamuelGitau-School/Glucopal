import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useAuthStore } from './auth'
import { supabase } from '@/lib/supabase'
import axios from '@/lib/axios'

export interface MealRecord {
  id?: number
  user_id: number
  photo_url?: string
  meal_name?: string
  eaten_at: string
  carbs_g?: number
  protein_g?: number
  vitamins_mg?: number
  calories?: number
  notes?: string
  ai_suggested?: boolean
}

export const useMealStore = defineStore('meal', () => {
  const meals = ref<MealRecord[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  function getUserId() {
    const auth = useAuthStore()
    return auth.user?.id as number
  }

  function toCamel(m: any): MealRecord {
    return {
      id: m.id,
      user_id: m.userId,
      photo_url: m.photoUrl,
      meal_name: m.mealName,
      eaten_at: m.eatenAt,
      carbs_g: m.carbsG,
      protein_g: m.proteinG,
      vitamins_mg: m.vitaminsMg,
      calories: m.calories,
      notes: m.notes,
      ai_suggested: m.aiSuggested,
    }
  }

  async function fetchMeals() {
    loading.value = true
    error.value = null
    try {
      const { data } = await axios.get(`/api/users/${getUserId()}/meals`)
      meals.value = data.map(toCamel)
    } catch (e: any) {
      error.value = e.response?.data?.message ?? e.message
    } finally {
      loading.value = false
    }
  }

  // Photo upload still goes straight to Supabase Storage (not the DB table)
  async function uploadPhoto(file: File): Promise<string | null> {
    const fileName = `${getUserId()}/${Date.now()}-${file.name}`
    const { error: err } = await supabase.storage
      .from('meal-photos')
      .upload(fileName, file, { upsert: true })
    if (err) {
      error.value = err.message
      return null
    }
    const { data } = supabase.storage.from('meal-photos').getPublicUrl(fileName)
    return data.publicUrl
  }

  async function addMeal(meal: Omit<MealRecord, 'id'>) {
    loading.value = true
    error.value = null
    try {
      const { data } = await axios.post(`/api/users/${getUserId()}/meals`, {
        photoUrl: meal.photo_url,
        mealName: meal.meal_name,
        eatenAt: meal.eaten_at,
        carbsG: meal.carbs_g,
        proteinG: meal.protein_g,
        vitaminsMg: meal.vitamins_mg,
        calories: meal.calories,
        notes: meal.notes,
        aiSuggested: meal.ai_suggested,
      })
      const record = toCamel(data)
      meals.value.unshift(record)
      return record
    } catch (e: any) {
      error.value = e.response?.data?.message ?? e.message
      return null
    } finally {
      loading.value = false
    }
  }

  async function deleteMeal(id: number) {
    try {
      await axios.delete(`/api/users/${getUserId()}/meals/${id}`)
      meals.value = meals.value.filter(m => m.id !== id)
    } catch (e: any) {
      error.value = e.response?.data?.message ?? e.message
    }
  }

  return { meals, loading, error, fetchMeals, uploadPhoto, addMeal, deleteMeal }
})
