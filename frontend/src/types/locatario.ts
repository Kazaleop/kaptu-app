/** web/dto/LocatarioDTO.java (resposta) */
export interface LocatarioDTO {
  id: number
  nome: string
  cpfCnpj: string
  telefone: string
  email: string
}

/** web/dto/LocatarioRequest.java (corpo do POST /locatarios) */
export interface LocatarioRequest {
  /** @NotBlank */
  nome: string
  /** @NotBlank */
  cpfCnpj: string
  /** @NotBlank */
  telefone: string
  /** @NotBlank @Email */
  email: string
}
