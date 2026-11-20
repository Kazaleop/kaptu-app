import type { AlertaDTO, DashboardDTO, FluxoMensal } from '@/types'

/**
 * Dados de exemplo do dashboard (usados com VITE_USE_MOCKS=true, ex.: Vercel).
 * Valores copiados do design do Figma (tela 01 Dashboard).
 */

export const mockAlertas: AlertaDTO[] = [
  {
    id: 1,
    tipo: 'CONTRATO_A_VENCER',
    mensagem: 'Contrato de Ana Ribeiro (Casa Institucional 037) vence em 5 dias',
    referenciaId: 2,
    dataGeracao: '2026-11-17T07:00:00',
    lido: false,
  },
  {
    id: 2,
    tipo: 'ANOMALIA_FINANCEIRA',
    mensagem: 'Manutenção predial · 32% acima da média mensal',
    referenciaId: 1,
    dataGeracao: '2026-11-16T07:00:00',
    lido: false,
  },
  {
    id: 3,
    tipo: 'CERTIDAO_A_VENCER',
    mensagem: 'Certidão negativa municipal (Imóvel 078) vence em 12 dias',
    referenciaId: 3,
    dataGeracao: '2026-11-15T07:00:00',
    lido: false,
  },
]

export const mockDashboard: DashboardDTO = {
  totalImoveis: 5,
  imoveisOcupados: 4,
  contratosVigentes: 4,
  alertasNaoLidos: 3,
  entradasMes: 428600,
  saidasMes: 306200,
  saldoMes: 122400,
}

export const mockContratosAVencer = 7

/** Gera N meses terminando no mês atual, com valores parecidos com os do design. */
export function gerarFluxoMensal(meses: number): FluxoMensal[] {
  const base = [
    [325, 212],
    [375, 262],
    [281, 169],
    [425, 312],
    [387, 275],
    [494, 381],
  ]
  const hoje = new Date()
  return Array.from({ length: meses }, (_, i) => {
    const data = new Date(hoje.getFullYear(), hoje.getMonth() - (meses - 1 - i), 1)
    const [receitas, despesas] = base[(i + 6 - (meses % 6)) % 6]
    return {
      mes: `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}`,
      receitas: receitas * 1000,
      despesas: despesas * 1000,
    }
  })
}
