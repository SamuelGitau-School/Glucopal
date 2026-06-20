<template>
  <div class="size-full flex flex-col bg-background min-h-screen">
    <!-- Header -->
    <header class="bg-card border-b border-border shadow-sm sticky top-0 z-10">
      <div class="max-w-md sm:max-w-2xl lg:max-w-3xl mx-auto px-4 py-3 flex items-center justify-between">
        <!-- Profile Section -->
        <div class="flex items-center gap-3">
          <div class="size-10 rounded-full border-2 border-primary/20 bg-primary/10 flex items-center justify-center text-primary font-semibold">
            {{ initials }}
          </div>
          <div>
            <h3 class="text-sm font-medium text-foreground">{{ displayName }}</h3>
            <p class="text-xs text-muted-foreground">Managing well 💪</p>
          </div>
        </div>

        <!-- More Menu -->
        <div class="relative" ref="menuRef">
          <button
            @click="menuOpen = !menuOpen"
            class="p-2 rounded-lg hover:bg-muted transition-colors text-foreground"
          >
            <MoreVerticalIcon class="size-5" />
          </button>
          <div
            v-if="menuOpen"
            class="absolute right-0 top-full mt-1 w-48 bg-card border border-border rounded-lg shadow-lg z-20 py-1"
          >
            <button
              @click="goTo('/profile')"
              class="w-full flex items-center gap-2 px-4 py-2 text-sm hover:bg-muted transition-colors"
            >
              <UserIcon class="size-4" /> Profile
            </button>
            <button
              @click="goTo('/setting')"
              class="w-full flex items-center gap-2 px-4 py-2 text-sm hover:bg-muted transition-colors">
              <SettingsIcon class="size-4" /> Settings
            </button>
            <hr class="border-border my-1" />
            <button
              @click="handleLogout"
              class="w-full flex items-center gap-2 px-4 py-2 text-sm text-red-500 hover:bg-muted transition-colors"
            >
              <LogOutIcon class="size-4" /> Logout
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- Page Content -->
    <main class="flex-1 overflow-auto pb-20">
      <RouterView />
    </main>

<!-- Bottom Navigation -->
    <nav class="fixed bottom-0 left-0 right-0 bg-card border-t border-border shadow-lg pb-[env(safe-area-inset-bottom)]">
      <div class="max-w-md sm:max-w-2xl lg:max-w-3xl mx-auto px-1 sm:px-4 py-2 sm:py-3">
        <div class="flex items-center">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            custom
            v-slot="{ isActive, navigate: go }"
            class="flex-1 min-w-0"
          >
            <button
              @click="go"
              :class="[
                'flex flex-col items-center gap-0.5 sm:gap-1 px-1 py-1.5 sm:px-4 sm:py-2 rounded-lg transition-colors w-full min-w-0',
                isActive ? 'text-primary bg-primary/10' : 'text-muted-foreground hover:text-foreground'
              ]"
            >
              <component :is="item.icon" class="size-5 sm:size-6 shrink-0" />
              <span class="text-[10px] sm:text-xs truncate w-full text-center leading-tight">{{ item.label }}</span>
            </button>
          </RouterLink>
        </div>
      </div>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { RouterView, RouterLink, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Utensils as UtensilsIcon } from 'lucide-vue-next'
import {
  LayoutDashboard as LayoutDashboardIcon,
  MessageCircle as MessageCircleIcon,
  BookOpen as BookOpenIcon,
  Activity as ActivityIcon,
  MoreVertical as MoreVerticalIcon,
  Settings as SettingsIcon,
  LogOut as LogOutIcon,
  User as UserIcon,
} from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()
const menuOpen = ref(false)
const menuRef = ref<HTMLElement | null>(null)

const displayName = computed(() => (auth.user?.name as string) ?? 'User')

const initials = computed(() =>
  displayName.value
    .split(' ')
    .map((n: string) => n[0])
    .join('')
    .toUpperCase()
    .slice(0, 2)
)

const navItems = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboardIcon },
  { to: '/chat', label: 'Chat', icon: MessageCircleIcon },
  { to: '/books', label: 'Books', icon: BookOpenIcon },
  { to: '/testing-records', label: 'Records', icon: ActivityIcon },
  { to: '/meals', label: 'Meals', icon: UtensilsIcon },
]

function goTo(path: string) {
  menuOpen.value = false
  router.push(path)
}

async function handleLogout() {
  menuOpen.value = false
  await auth.logout()
  router.push('/login')
}

function handleClickOutside(e: MouseEvent) {
  if (menuRef.value && !menuRef.value.contains(e.target as Node)) {
    menuOpen.value = false
  }
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onUnmounted(() => document.removeEventListener('click', handleClickOutside))
</script>

