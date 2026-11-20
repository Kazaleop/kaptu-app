import { useCallback, useMemo, useState, type ReactNode } from 'react'
import { AlertasModal } from './AlertasModal'
import { AlertasContext } from './alertasModalContext'

/**
 * O popup de alertas pode ser aberto de dois lugares:
 * o sino do cabeçalho e o botão "Ver todos os alertas" do dashboard.
 * Este provider guarda o estado aberto/fechado num lugar só.
 */
export function AlertasProvider({ children }: { children: ReactNode }) {
  const [aberto, setAberto] = useState(false)
  const abrirAlertas = useCallback(() => setAberto(true), [])
  const fechar = useCallback(() => setAberto(false), [])
  const value = useMemo(() => ({ abrirAlertas }), [abrirAlertas])

  return (
    <AlertasContext.Provider value={value}>
      {children}
      <AlertasModal open={aberto} onClose={fechar} />
    </AlertasContext.Provider>
  )
}
