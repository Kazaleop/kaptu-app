import { createContext, useContext } from 'react'

interface AlertasContextValue {
  abrirAlertas(): void
}

export const AlertasContext = createContext<AlertasContextValue | null>(null)

/** Abre o popup de alertas de qualquer componente dentro do <AlertasProvider>. */
export function useAlertasModal(): AlertasContextValue {
  const ctx = useContext(AlertasContext)
  if (!ctx) throw new Error('useAlertasModal precisa estar dentro de <AlertasProvider>')
  return ctx
}
