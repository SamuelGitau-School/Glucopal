<template>
  <!-- Single line skeleton -->
  <div v-if="type === 'line'" :style="{ width, height }" class="skeleton" />

  <!-- Card skeleton -->
  <div v-else-if="type === 'card'" class="card p-4 space-y-3">
    <div class="skeleton h-4 w-1/3" />
    <div class="skeleton h-8 w-1/2" />
    <div class="skeleton h-3 w-1/4" />
  </div>

  <!-- Stat card skeleton (2x2 grid item) -->
  <div v-else-if="type === 'stat'" class="card border-primary/20 shadow-sm p-4 space-y-2">
    <div class="skeleton h-3 w-1/2" />
    <div class="skeleton h-8 w-2/3" />
    <div class="skeleton h-3 w-1/3" />
  </div>

  <!-- List item skeleton -->
  <div v-else-if="type === 'list-item'" class="card p-4 flex items-center gap-3">
    <div class="skeleton size-10 rounded-full shrink-0" />
    <div class="flex-1 space-y-2">
      <div class="skeleton h-4 w-3/4" />
      <div class="skeleton h-3 w-1/2" />
    </div>
    <div class="skeleton h-6 w-16 rounded-full" />
  </div>

  <!-- Profile skeleton -->
  <div v-else-if="type === 'profile'" class="space-y-4">
    <div class="flex items-center gap-4">
      <div class="skeleton size-20 rounded-full shrink-0" />
      <div class="space-y-2 flex-1">
        <div class="skeleton h-5 w-1/2" />
        <div class="skeleton h-4 w-2/3" />
      </div>
    </div>
    <div class="skeleton h-12 w-full rounded-lg" />
    <div class="skeleton h-12 w-full rounded-lg" />
    <div class="skeleton h-12 w-full rounded-lg" />
  </div>

  <!-- Chart skeleton -->
  <div v-else-if="type === 'chart'" class="card shadow-sm p-4 space-y-3">
    <div class="skeleton h-5 w-1/3" />
    <div class="skeleton h-3 w-1/2" />
    <div class="skeleton h-40 w-full rounded-lg" />
  </div>

  <!-- Text block skeleton -->
  <div v-else class="space-y-2">
    <div class="skeleton h-4 w-full" />
    <div class="skeleton h-4 w-5/6" />
    <div class="skeleton h-4 w-4/6" />
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'line' | 'card' | 'stat' | 'list-item' | 'profile' | 'chart' | 'text'
  width?: string
  height?: string
}>(), {
  type: 'text',
  width: '100%',
  height: '1rem',
})
</script>
