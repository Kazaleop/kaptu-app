import type { IconName } from '@/components/ui/Icon'

/** Rotas do sistema, seguindo o menu do design (tela 01 Dashboard). */
export const paths = {
  visaoGeral: '/', // F-9 Dashboard unificado
  gestaoImobiliaria: '/gestao-imobiliaria', // F-3 Imóveis, locatários e contratos
  acervo: '/acervo', // F-2 / F-4 / F-5 / F-8 Documentos
  financeiro: '/financeiro', // F-1 / F-6 Financeiro e auditoria
  pessoas: '/pessoas', // Gestão de pessoas
} as const

export interface MenuItem {
  label: string
  to: string
  icon: IconName
}

export const menu: MenuItem[] = [
  { label: 'Visão geral', to: paths.visaoGeral, icon: 'overview' },
  { label: 'Gestão imobiliária', to: paths.gestaoImobiliaria, icon: 'building' },
  { label: 'Acervo de documentos', to: paths.acervo, icon: 'document' },
  { label: 'Financeiro e auditoria', to: paths.financeiro, icon: 'finance' },
  { label: 'Gestão de pessoas', to: paths.pessoas, icon: 'person' },
]

/** Título exibido no cabeçalho para a rota atual. */
export function tituloDaRota(pathname: string): string {
  const item = menu
    .filter((m) => (m.to === '/' ? pathname === '/' : pathname.startsWith(m.to)))
    .sort((a, b) => b.to.length - a.to.length)[0]
  return item?.label ?? 'Kaptu'
}
