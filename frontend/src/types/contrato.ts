import type { StatusContrato } from './enums'

/**
 * web/dto/ContratoLinhaDTO.java (resposta de GET/POST /contratos)
 * "imovel" e "locatario" vêm como NOMES (string), não objetos.
 * Use "imovelId" para ligar o contrato ao imóvel da tela.
 */
export interface ContratoLinhaDTO {
  id: number
  imovelId: number
  imovel: string
  locatario: string
  status: StatusContrato
  valor: number
  /** LocalDate "AAAA-MM-DD" calculada pelo backend (dataExpedicao + prazoMeses). */
  dataValidade: string
}

/** Índices de reajuste oferecidos no popup "Novo contrato de aluguel". */
export const IndiceReajuste = {
  IPCA: 'IPCA',
  IGPM: 'IGPM',
  INPC: 'INPC',
} as const
export type IndiceReajuste = (typeof IndiceReajuste)[keyof typeof IndiceReajuste]

/** web/dto/ContratoRequest.java (corpo do POST /contratos) */
export interface ContratoRequest {
  /** @NotNull */
  imovelId: number
  /** @NotNull */
  locatarioId: number
  /** @NotNull @Positive */
  prazoMeses: number
  /** @NotNull @DecimalMin("0.01") */
  valorAluguel: number
  /**
   * LocalDate no formato "AAAA-MM-DD". Opcional:
   * se omitido, o backend usa a data de hoje.
   */
  dataExpedicao?: string
  /**
   * Informativo: o backend atual não persiste o índice de reajuste.
   * O campo continua opcional para não quebrar a tela enquanto o
   * domínio não tiver essa coluna.
   */
  indiceReajuste?: IndiceReajuste
}
