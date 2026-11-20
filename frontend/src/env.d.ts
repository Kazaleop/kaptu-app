/// <reference types="vite/client" />

// Tipagem das variáveis de ambiente (autocomplete em import.meta.env)
interface ImportMetaEnv {
  readonly VITE_API_URL: string
  readonly VITE_USE_MOCKS?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
