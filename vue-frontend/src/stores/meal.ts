import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useAuthStore } from './auth'
import { supabase } from '@/lib/supabase'

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

  async function fetchMeals() {
    loading.value = true
    error.value = null
    try {
      const { data, error: err } = await supabase
        .from('meals')
        .select('*')
        .eq('user_id', getUserId())
        .order('eaten_at', { ascending: false })
      if (err) throw err
      meals.value = data ?? []
    } catch (e: any) {
      error.value = e.message
    } finally {
      loading.value = false
    }
  }

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
      const { data, error: err } = await supabase
        .from('meals')
        .insert({ ...meal, user_id: getUserId() })
        .select()
        .single()
      if (err) throw err
      meals.value.unshift(data)
      return data
    } catch (e: any) {
      error.value = e.message
      return null
    } finally {
      loading.value = false
    }
  }

  async function deleteMeal(id: number) {
    try {
      const { error: err } = await supabase
        .from('meals')
        .delete()
        .eq('id', id)
      if (err) throw err
      meals.value = meals.value.filter(m => m.id !== id)
    } catch (e: any) {
      error.value = e.message
    }
  }

  return { meals, loading, error, fetchMeals, uploadPhoto, addMeal, deleteMeal }
})
