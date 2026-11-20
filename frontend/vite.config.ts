import path from 'node:path'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      // Permite importar com "@/..." em vez de "../../.."
      '@': path.resolve(import.meta.dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    // Se a 5173 estiver ocupada, falha em vez de pular para 5174.
    // O CORS do backend só libera a 5173; em outra porta todas as chamadas dariam erro.
    strictPort: true,
  },
})
