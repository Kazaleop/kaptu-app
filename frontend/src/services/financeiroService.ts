import { usarMock } from '@/config/endpoints'
import { gerarFluxoMensal, mockDashboard } from '@/mocks/dashboard'
import { delay } from '@/mocks/db'
import { mockTransacoes } from '@/mocks/financeiro'
import type {
  AnexarComprovanteRequest,
  ConciliacaoRequest,
  FinanceiroResumo,
  FluxoMensal,
  PeriodoFluxo,
  TransacaoCriacaoRequest,
  TransacaoDTO,
} from '@/types'
import { toIsoDate } from '@/utils/format'
import { api, ApiError } from './api'
import { dashboardService } from './dashboardService'
import { documentoService } from './documentoService'

/** "6" -> 6 | "12" -> 12 | "ano" -> meses decorridos no ano atual */
export function mesesDoPeriodo(periodo: PeriodoFluxo): number {
  return periodo === 'ano' ? new Date().getMonth() + 1 : Number(periodo)
}

/** Arquivo pronto para download (relatório). */
export interface ArquivoGerado {
  blob: Blob
  nome: string
}

const anoMes = (iso: string) => iso.slice(0, 7)

/** Mais recentes primeiro (data e, no empate, id). */
const porDataDesc = (a: TransacaoDTO, b: TransacaoDTO) =>
  b.dataTransacao.localeCompare(a.dataTransacao) || b.id - a.id

/** F-1 / F-6 Financeiro e auditoria -> FinanceiroController + RelatorioController */
export const financeiroService = {
  /** GET /api/v1/financeiro/transacoes (lista completa) */
  async transacoes(): Promise<TransacaoDTO[]> {
    const lista = usarMock('financeiroTransacoes')
      ? await delay(mockTransacoes, 300)
      : (await api.get<TransacaoDTO[]>('/financeiro/transacoes')).data
    return [...lista].sort(porDataDesc)
  },

  /** POST /api/v1/financeiro/transacoes (sem tela no design por enquanto) */
  async lancar(request: TransacaoCriacaoRequest): Promise<TransacaoDTO> {
    const { data } = await api.post<TransacaoDTO>('/financeiro/transacoes', request)
    return data
  },

  /**
   * Cards da tela Financeiro. Não existe endpoint de resumo:
   * receitas/despesas do mês vêm do /dashboard/resumo e a conciliação é
   * contada sobre todas as transações.
   */
  async resumo(): Promise<FinanceiroResumo> {
    const [dashboard, transacoes] = await Promise.all([
      usarMock('financeiroResumo') ? delay(mockDashboard) : dashboardService.resumo(),
      financeiroService.transacoes(),
    ])
    const conciliadas = transacoes.filter((t) => t.statusConciliacao === 'CONCILIADA').length
    return {
      mesReferencia: toIsoDate(new Date()).slice(0, 7),
      receitaMes: dashboard.entradasMes,
      despesaMes: dashboard.saidasMes,
      saldoMes: dashboard.saldoMes,
      transacoesConciliadas: conciliadas,
      totalTransacoes: transacoes.length,
      percentualConciliado: transacoes.length ? (conciliadas / transacoes.length) * 100 : 100,
      pendencias: transacoes.length - conciliadas,
    }
  },

  /** Gráfico do dashboard: soma ENTRADA/SAIDA por mês, dos últimos N meses. */
  async fluxoMensal(periodo: PeriodoFluxo): Promise<FluxoMensal[]> {
    const meses = mesesDoPeriodo(periodo)
    if (usarMock('financeiroFluxo')) return delay(gerarFluxoMensal(meses))

    const transacoes = await financeiroService.transacoes()
    const hoje = new Date()
    const serie: FluxoMensal[] = Array.from({ length: meses }, (_, i) => {
      const d = new Date(hoje.getFullYear(), hoje.getMonth() - (meses - 1 - i), 1)
      return { mes: toIsoDate(d).slice(0, 7), receitas: 0, despesas: 0 }
    })
    const porMes = new Map(serie.map((m) => [m.mes, m]))
    for (const t of transacoes) {
      const mes = porMes.get(anoMes(t.dataTransacao))
      if (!mes) continue
      if (t.tipoTransacao === 'ENTRADA') mes.receitas += t.valor
      else mes.despesas += t.valor
    }
    return serie
  },

  /** POST /api/v1/financeiro/conciliar */
  async conciliar(request: ConciliacaoRequest): Promise<TransacaoDTO> {
    const { data } = await api.post<TransacaoDTO>('/financeiro/conciliar', request)
    return data
  },

  /**
   * "Anexar comprovante" = enviar o arquivo para o OCR (vira um Documento)
   * e depois vincular esse documento à transação.
   */
  async anexarComprovante({ transacaoId, arquivo }: AnexarComprovanteRequest): Promise<TransacaoDTO> {
    if (usarMock('financeiroComprovante')) {
      const t = mockTransacoes.find((x) => x.id === transacaoId)
      if (!t) throw new ApiError('Transação não encontrada.', 404)
      t.statusConciliacao = 'CONCILIADA'
      t.documentoId = 999
      return delay(t, 900)
    }
    const documento = await documentoService.importarOcr({ arquivo })
    return financeiroService.conciliar({ transacaoId, documentoId: documento.id })
  },

  /**
   * GET /api/v1/financeiro/relatorio-mpse?inicio=AAAA-MM-DD&fim=AAAA-MM-DD -> PDF
   * Sem backend (dados de exemplo), gera um CSV com as transações.
   */
  async relatorio(inicio: string, fim: string): Promise<ArquivoGerado> {
    if (usarMock('financeiroRelatorio')) {
      const linhas = [
        ['Código', 'Descrição', 'Data', 'Tipo', 'Valor', 'Conciliação'],
        ...mockTransacoes
          .filter((t) => t.dataTransacao >= inicio && t.dataTransacao <= fim)
          .map((t) => [
            codigoTransacao(t.id),
            t.descricao,
            t.dataTransacao,
            t.tipoTransacao,
            t.valor.toFixed(2).replace('.', ','),
            t.statusConciliacao,
          ]),
      ]
      // ";" + BOM: o Excel em português abre com acentos e colunas corretas
      const csv = '﻿' + linhas.map((l) => l.map((c) => `"${c.replace(/"/g, '""')}"`).join(';')).join('\r\n')
      return delay({ blob: new Blob([csv], { type: 'text/csv;charset=utf-8' }), nome: `relatorio-mpse-${inicio}_${fim}.csv` }, 800)
    }
    const { data } = await api.get<Blob>('/financeiro/relatorio-mpse', {
      params: { inicio, fim },
      responseType: 'blob',
      timeout: 120_000,
    })
    return { blob: data, nome: `relatorio-mpse-${inicio}_${fim}.pdf` }
  },
}

/** 926 -> "TRX-0926" (o backend não tem código: usamos o id) */
export function codigoTransacao(id: number): string {
  return `TRX-${String(id).padStart(4, '0')}`
}
