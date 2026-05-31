import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<'light' | 'dark' | 'system'>(
    (localStorage.getItem('app-theme') as 'light' | 'dark' | 'system') || 'light'
  )

  function applyTheme(val: string) {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    const resolved = val === 'system' ? (prefersDark ? 'dark' : 'light') : val
    document.documentElement.setAttribute('data-theme', resolved)
  }

  function setTheme(val: 'light' | 'dark' | 'system') {
    theme.value = val
    localStorage.setItem('app-theme', val)
    applyTheme(val)
  }

  function initTheme() {
    if (!localStorage.getItem('app-theme')) {
      setTheme('light')
    } else {
      applyTheme(theme.value)
    }
  }

  watch(theme, applyTheme)

  return { theme, setTheme, initTheme }
})
