import { env } from './env'

/**
 * Endpoints que o front usa e se o backend já os oferece.
 *
 * - true  -> chama o backend de verdade
 * - false -> o serviço devolve dados de exemplo (src/mocks)
 * VITE_USE_MOCKS=true força dados de exemplo em TUDO (ex.: site na Vercel sem backend).
 *
 * Sincronizado com o backend em 2026-11-17: todos os recursos abaixo existem.
 * Alguns são "derivados": o front calcula a partir de outros endpoints (ver comentário).
 */
export const endpointsProntos = {
  /** GET /alertas, PATCH /alertas/{id}/lido -> services/alertaService.ts */
  alertas: true,
  /** GET /dashboard/resumo -> services/dashboardService.ts */
  dashboard: true,
  /** Derivado: alertas CONTRATO_A_VENCER de contratos ainda VIGENTES -> services/contratoService.ts */
  contratosAVencer: true,
  /** GET /financeiro/transacoes -> services/financeiroService.ts */
  financeiroTransacoes: true,
  /** Derivado: transações agrupadas por mês -> services/financeiroService.ts */
  financeiroFluxo: true,
  /** Derivado: GET /dashboard/resumo + GET /financeiro/transacoes -> services/financeiroService.ts */
  financeiroResumo: true,
  /** POST /documentos/upload + POST /financeiro/conciliar -> services/financeiroService.ts */
  financeiroComprovante: true,
  /** GET /financeiro/relatorio-mpse?inicio=&fim= (PDF) -> services/financeiroService.ts */
  financeiroRelatorio: true,
  /** GET /documentos/filtros -> services/documentoService.ts */
  documentos: true,
  /** Derivado: contagens sobre GET /documentos/filtros -> services/documentoService.ts */
  documentosResumo: true,
  /** POST /documentos/upload (multipart) -> services/documentoService.ts */
  documentosOcr: true,
} as const

export type EndpointPendente = keyof typeof endpointsProntos

/** true quando o serviço deve responder com dados de exemplo. */
export function usarMock(endpoint?: EndpointPendente): boolean {
  if (env.useMocks) return true
  return endpoint ? !endpointsProntos[endpoint] : false
}
