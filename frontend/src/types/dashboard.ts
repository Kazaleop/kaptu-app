/**
 * Espelho de web/dto/DashboardDTO.java
 * GET /api/v1/dashboard/resumo (valores do mês corrente)
 */
export interface DashboardDTO {
  totalImoveis: number
  imoveisOcupados: number
  contratosVigentes: number
  alertasNaoLidos: number
  /** Soma das transações ENTRADA do mês */
  entradasMes: number
  /** Soma das transações SAIDA do mês */
  saidasMes: number
  saldoMes: number
}
