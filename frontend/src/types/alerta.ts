/**
 * Espelho de web/dto/AlertaDTO.java e domain/alerta/TipoAlerta.java
 * GET   /api/v1/alertas            -> AlertaDTO[] (mais recentes primeiro)
 * GET   /api/v1/alertas/nao-lidos  -> AlertaDTO[]
 * PATCH /api/v1/alertas/{id}/lido  -> 200 sem corpo
 *
 * Os alertas são gerados pelo backend todo dia às 7h (AlertaJob):
 * contratos VIGENTES e certidões que vencem nos próximos 30 dias.
 */
export const TipoAlerta = {
  CONTRATO_A_VENCER: 'CONTRATO_A_VENCER',
  CERTIDAO_A_VENCER: 'CERTIDAO_A_VENCER',
  ANOMALIA_FINANCEIRA: 'ANOMALIA_FINANCEIRA',
} as const
export type TipoAlerta = (typeof TipoAlerta)[keyof typeof TipoAlerta]

export interface AlertaDTO {
  id: number
  tipo: TipoAlerta
  /** Ex.: "Contrato de Ana Ribeiro (Casa 037) vence em 2026-11-20" */
  mensagem: string
  /** Id do item que gerou o alerta (contrato ou certidão) */
  referenciaId: number
  /** LocalDateTime ISO, ex.: "2026-11-17T07:00:00" */
  dataGeracao: string
  lido: boolean
}
