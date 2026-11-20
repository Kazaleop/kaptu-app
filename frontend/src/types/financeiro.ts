/**
 * Espelho de domain/financeiro/* e web/dto/Transacao*.java / ConciliacaoDTO.java
 *
 * GET  /api/v1/financeiro/transacoes  -> TransacaoDTO[] (lista completa, sem paginação)
 * POST /api/v1/financeiro/transacoes  -> TransacaoCriacaoRequest -> TransacaoDTO
 * POST /api/v1/financeiro/conciliar   -> ConciliacaoRequest -> TransacaoDTO
 * GET  /api/v1/financeiro/relatorio-mpse?inicio=AAAA-MM-DD&fim=AAAA-MM-DD -> PDF
 */

export const TipoTransacao = {
  ENTRADA: 'ENTRADA',
  SAIDA: 'SAIDA',
} as const
export type TipoTransacao = (typeof TipoTransacao)[keyof typeof TipoTransacao]

export const StatusConciliacao = {
  PENDENTE: 'PENDENTE',
  CONCILIADA: 'CONCILIADA',
} as const
export type StatusConciliacao = (typeof StatusConciliacao)[keyof typeof StatusConciliacao]

/** web/dto/TransacaoRespostaDTO.java */
export interface TransacaoDTO {
  id: number
  descricao: string
  valor: number
  /** LocalDate "AAAA-MM-DD" */
  dataTransacao: string
  tipoTransacao: TipoTransacao
  statusConciliacao: StatusConciliacao
  /** Documento (comprovante) vinculado, quando conciliada */
  documentoId: number | null
}

/** web/dto/TransacaoCriacaoDTO.java */
export interface TransacaoCriacaoRequest {
  descricao: string
  valor: number
  dataTransacao: string
  tipoTransacao: TipoTransacao
}

/** web/dto/ConciliacaoDTO.java */
export interface ConciliacaoRequest {
  transacaoId: number
  documentoId: number
}

/**
 * "Anexar comprovante" no front = 2 chamadas ao backend:
 * 1) POST /documentos/upload (OCR) -> documentoId
 * 2) POST /financeiro/conciliar { transacaoId, documentoId }
 */
export interface AnexarComprovanteRequest {
  transacaoId: number
  arquivo: File
}

/**
 * Resumo da tela Financeiro. NÃO é um endpoint: o front calcula a partir de
 * GET /dashboard/resumo (entradas/saídas do mês) + GET /financeiro/transacoes.
 */
export interface FinanceiroResumo {
  /** "AAAA-MM" do mês corrente */
  mesReferencia: string
  receitaMes: number
  despesaMes: number
  saldoMes: number
  transacoesConciliadas: number
  totalTransacoes: number
  /** 0 a 100 */
  percentualConciliado: number
  pendencias: number
}

/**
 * Receitas x despesas por mês (gráfico do dashboard).
 * Calculado no front agrupando GET /financeiro/transacoes por mês.
 */
export interface FluxoMensal {
  /** "AAAA-MM" */
  mes: string
  receitas: number
  despesas: number
}

/** Opções do seletor de período do gráfico */
export type PeriodoFluxo = '6' | '12' | 'ano'

/** Paginação feita no front (o backend devolve a lista inteira). */
export interface FiltrosTransacoes {
  status?: StatusConciliacao
  pagina: number
  tamanho: number
}
