import type { FiltrosDocumentosApi, PeriodoFluxo } from '@/types'

/**
 * Chaves do React Query num lugar só.
 * Evita erro de digitação ao invalidar listas depois de um POST.
 */
export const queryKeys = {
  imoveis: ['imoveis'] as const,
  imovel: (id: number) => ['imoveis', id] as const,
  locatarios: ['locatarios'] as const,
  contratos: ['contratos'] as const,
  contratosAVencer: ['contratos', 'a-vencer'] as const,
  alertas: ['alertas'] as const,
  dashboard: ['dashboard'] as const,
  /** Prefixo de tudo que é financeiro: invalidar isto recarrega cards, gráfico e tabela */
  financeiro: ['financeiro'] as const,
  financeiroResumo: ['financeiro', 'resumo'] as const,
  transacoes: ['financeiro', 'transacoes'] as const,
  fluxoMensal: (periodo: PeriodoFluxo) => ['financeiro', 'fluxo-mensal', periodo] as const,
  /** Prefixo de tudo que é documento: invalidar isto recarrega lista e resumo */
  documentos: ['documentos'] as const,
  documentosResumo: ['documentos', 'resumo'] as const,
  /** Só os filtros enviados ao backend: busca e página são aplicadas na tela */
  documentosLista: (filtros: FiltrosDocumentosApi) => ['documentos', 'lista', filtros] as const,
}
