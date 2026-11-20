import type { StatusOcupacao } from './enums'

/** web/dto/ImovelDTO.java (resposta) */
export interface ImovelDTO {
  id: number
  nome: string
  endereco: string
  bairro: string
  quantResidentes: number
  /** BigDecimal no Java -> number no JSON */
  valorAluguelMensal: number
  statusOcupacao: StatusOcupacao
}

/** web/dto/ImovelRequest.java (corpo do POST /imoveis) */
export interface ImovelRequest {
  /** @NotBlank */
  nome: string
  /** @NotBlank */
  endereco: string
  /** @NotBlank */
  bairro: string
  /** @Positive */
  quantResidentes: number
  /** @NotNull @DecimalMin("0.01") */
  valorAluguelMensal: number
}
