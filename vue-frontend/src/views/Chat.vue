<template>
  <div class="h-full flex flex-col bg-background" style="min-height: calc(100vh - 140px)">
    <div class="max-w-md mx-auto w-full flex flex-col h-full">
      <!-- Messages -->
      <div ref="messagesContainer" class="flex-1 overflow-y-auto p-4 space-y-4">
        <div
          v-for="message in messages"
          :key="message.id"
          :class="['flex gap-3', message.sender === 'user' ? 'flex-row-reverse' : 'flex-row']"
        >
          <!-- Avatar -->
          <div
            :class="[
              'size-8 rounded-full flex items-center justify-center flex-shrink-0',
              message.sender === 'user'
                ? 'bg-primary text-primary-foreground'
                : 'bg-muted text-muted-foreground'
            ]"
          >
            <component :is="message.sender === 'user' ? UserIcon : BotIcon" class="size-4" />
          </div>

          <!-- Bubble -->
          <div
            :class="[
              'max-w-[75%] rounded-xl p-3',
              message.sender === 'user'
                ? 'bg-primary text-primary-foreground'
                : 'bg-card border border-border'
            ]"
          >
            <p :class="['text-sm', message.sender === 'user' ? 'text-primary-foreground' : 'text-card-foreground']">
              {{ message.text }}
            </p>
            <p :class="['text-xs mt-1', message.sender === 'user' ? 'text-primary-foreground/70' : 'text-muted-foreground']">
              {{ formatTime(message.timestamp) }}
            </p>
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="isTyping" class="flex gap-3">
          <div class="size-8 rounded-full bg-muted flex items-center justify-center">
            <BotIcon class="size-4 text-muted-foreground" />
          </div>
          <div class="bg-card border border-border rounded-xl p-3">
            <div class="flex gap-1 items-center h-4">
              <span class="size-2 bg-muted-foreground rounded-full animate-bounce" style="animation-delay:0ms" />
              <span class="size-2 bg-muted-foreground rounded-full animate-bounce" style="animation-delay:150ms" />
              <span class="size-2 bg-muted-foreground rounded-full animate-bounce" style="animation-delay:300ms" />
            </div>
          </div>
        </div>
      </div>

      <!-- Input -->
      <div class="bg-card border-t border-border p-4">
        <div class="flex gap-2">
          <input
            v-model="inputValue"
            @keydown.enter="handleSend"
            placeholder="Type your message..."
            class="flex-1 px-3 py-2 text-sm rounded-lg border border-border bg-background focus:outline-none focus:ring-2 focus:ring-primary/40"
          />
          <button
            @click="handleSend"
            class="btn-primary size-10 flex items-center justify-center rounded-lg flex-shrink-0"
          >
            <SendIcon class="size-4" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { Send as SendIcon, Bot as BotIcon, User as UserIcon } from 'lucide-vue-next'
import { useAuthStore } from '../stores/auth';

interface Message {
  id: string
  text: string
  sender: 'user' | 'bot'
  timestamp: Date
}

const messages = ref<Message[]>([
  {
    id: '1',
    text: "Hello! I'm your diabetes care assistant. How can I help you today?",
    sender: 'bot',
    timestamp: new Date(),
  },
])

const inputValue = ref('')
const isTyping = ref(false)
const messagesContainer = ref<HTMLElement | null>(null)

function formatTime(date: Date) {
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

async function scrollToBottom() {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}


async function handleSend() {
  if (!inputValue.value.trim()) return

  messages.value.push({
    id: Date.now().toString(),
    text: inputValue.value,
    sender: 'user',
    timestamp: new Date(),
  })

  const userMessage = inputValue.value
  inputValue.value = ''
  await scrollToBottom()
  isTyping.value = true

  try {
    const apiUrl = import.meta.env.VITE_API_URL
    const userId = localStorage.getItem('userId') || '1'
    const response = await fetch(`${apiUrl}/users/${userId}/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${auth.accessToken}`
  },
  body: JSON.stringify({ message: userMessage }),
    })

    const data = await response.json()

    messages.value.push({
      id: (Date.now() + 1).toString(),
      text: data.reply,
      sender: 'bot',
      timestamp: new Date(),
    })
  } catch (error) {
    messages.value.push({
      id: (Date.now() + 1).toString(),
      text: 'Sorry I am unable to connect right now. Please try again later.',
      sender: 'bot',
      timestamp: new Date(),
    })
  } finally {
    isTyping.value = false
    await scrollToBottom()
  }
}
</script>
