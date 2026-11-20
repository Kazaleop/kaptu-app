import type { StatusContrato } from './enums'

export interface ContratoLinhaDTO {
  id: number
  imovelId: number
  locatarioId: number
  imovel: string
  locatario: string
  status: StatusContrato
  valor: number
  dataExpedicao: string
  prazoMeses: number
  dataValidade: string
  indiceReajuste?: IndiceReajuste
}

export const IndiceReajuste = {
  IPCA: 'IPCA',
  IGPM: 'IGPM',
  INPC: 'INPC',
} as const
export type IndiceReajuste = (typeof IndiceReajuste)[keyof typeof IndiceReajuste]

export interface ContratoRequest {
  imovelId: number
  locatarioId: number
  prazoMeses: number
  valorAluguel: number
  dataExpedicao?: string
  indiceReajuste?: IndiceReajuste
}
