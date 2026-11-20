/**
 * Espelho de domain/documento/* e web/dto/DocumentoDTO.java
 *
 * GET  /api/v1/documentos/filtros?dataInicio=&dataFim=&tipo=&imovelId=&valorMinimo=&valorMaximo=
 *      -> DocumentoDTO[] (lista completa, sem paginação nem busca por texto)
 * GET  /api/v1/documentos/{id} -> DocumentoDTO
 * POST /api/v1/documentos/upload (multipart: arquivo, tipo?) -> DocumentoDTO
 *      OCR síncrono: a resposta já vem classificada (ou com erro).
 */

export const TipoDocumento = {
  CONTRATO: 'CONTRATO',
  NOTA_FISCAL: 'NOTA_FISCAL',
  RECIBO: 'RECIBO',
  CERTIDAO: 'CERTIDAO',
} as const
export type TipoDocumento = (typeof TipoDocumento)[keyof typeof TipoDocumento]

/** domain/documento/StatusClassificacao.java */
export const StatusClassificacao = {
  PENDENTE: 'PENDENTE',
  CLASSIFICADO: 'CLASSIFICADO',
  REVISAO_MANUAL: 'REVISAO_MANUAL',
  ERRO: 'ERRO',
} as const
export type StatusClassificacao = (typeof StatusClassificacao)[keyof typeof StatusClassificacao]

/** web/dto/DocumentoDTO.java */
export interface DocumentoDTO {
  id: number
  /** Nome do arquivo enviado, ex.: "nota-0926.pdf" */
  titulo: string
  tipo: TipoDocumento
  statusClassificacao: StatusClassificacao
  /** LocalDate "AAAA-MM-DD" extraída pelo OCR (pode faltar) */
  dataEmissao: string | null
  /** Valor encontrado no documento (pode faltar) */
  valorReferencia: number | null
  /** Ex.: "Nota fiscal 123 - Valor: 1347.80" */
  resumo: string | null
  /** Texto completo lido pelo OCR */
  textoExtraido: string | null
}

/** Filtros enviados ao backend (query string de /documentos/filtros). */
export interface FiltrosDocumentosApi {
  tipo?: TipoDocumento
  imovelId?: number
  /** "AAAA-MM-DD" */
  dataInicio?: string
  /** "AAAA-MM-DD" */
  dataFim?: string
}

/** Filtros da tela: os do backend + busca por texto e paginação (feitas no front). */
export interface FiltrosDocumentos extends FiltrosDocumentosApi {
  busca?: string
  /** Começa em 0 */
  pagina: number
  tamanho: number
}

/** Página montada no front (mesmo formato do Page do Spring). */
export interface PageDTO<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ImportarDocumentoRequest {
  arquivo: File
  /** Vazio = o backend detecta pelo conteúdo */
  tipo?: TipoDocumento
  /**
   * ⚠ PROPOSTO: o backend ainda não recebe o imóvel no upload.
   * É enviado mesmo assim (o Spring ignora) para funcionar quando ele for criado.
   */
  imovelId?: number
}

/**
 * Resumo do acervo (dashboard e menu lateral).
 * NÃO é um endpoint: o front calcula a partir de GET /documentos/filtros.
 */
export interface DocumentosResumo {
  total: number
  classificados: number
  /** PENDENTE + REVISAO_MANUAL + ERRO */
  aguardandoRevisao: number
}
