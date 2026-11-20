import { usarMock } from '@/config/endpoints'
import { delay } from '@/mocks/db'
import { filtrarMockDocumentos, mockDocumentos } from '@/mocks/documentos'
import type {
  DocumentoDTO,
  DocumentosResumo,
  FiltrosDocumentosApi,
  ImportarDocumentoRequest,
} from '@/types'
import { toIsoDate } from '@/utils/format'
import { api } from './api'

/** F-2 / F-4 / F-5 / F-8 Acervo de documentos -> DocumentoController */
export const documentoService = {
  /**
   * GET /api/v1/documentos/filtros?dataInicio=&dataFim=&tipo=&imovelId=
   * O backend devolve a lista inteira (sem paginação); a busca por texto
   * e a paginação são feitas na tela.
   */
  async listar(filtros: FiltrosDocumentosApi = {}): Promise<DocumentoDTO[]> {
    if (usarMock('documentos')) return delay(filtrarMockDocumentos(filtros), 300)
    const { data } = await api.get<DocumentoDTO[]>('/documentos/filtros', {
      params: semVazios(filtros),
    })
    return data
  },

  /** Contagens do acervo (dashboard e menu lateral), calculadas sobre a lista. */
  async resumo(): Promise<DocumentosResumo> {
    const docs = usarMock('documentosResumo') ? await delay(mockDocumentos) : await documentoService.listar()
    const classificados = docs.filter((d) => d.statusClassificacao === 'CLASSIFICADO').length
    return { total: docs.length, classificados, aguardandoRevisao: docs.length - classificados }
  },

  /**
   * POST /api/v1/documentos/upload (multipart/form-data: arquivo, tipo?)
   * O OCR roda na hora: a resposta já vem CLASSIFICADO, REVISAO_MANUAL ou com erro 400.
   */
  async importarOcr({ arquivo, tipo, imovelId }: ImportarDocumentoRequest): Promise<DocumentoDTO> {
    if (usarMock('documentosOcr')) {
      const novo: DocumentoDTO = {
        id: Math.max(0, ...mockDocumentos.map((d) => d.id)) + 1,
        titulo: arquivo.name,
        tipo: tipo ?? 'NOTA_FISCAL',
        statusClassificacao: 'CLASSIFICADO',
        dataEmissao: toIsoDate(new Date()),
        valorReferencia: null,
        resumo: null,
        textoExtraido: null,
      }
      mockDocumentos.unshift(novo)
      return delay(novo, 1200)
    }

    const form = new FormData()
    form.append('arquivo', arquivo)
    if (tipo) form.append('tipo', tipo)
    if (imovelId) form.append('imovelId', String(imovelId)) // ⚠ ainda ignorado pelo backend
    const { data } = await api.post<DocumentoDTO>('/documentos/upload', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 180_000, // OCR de PDF escaneado pode demorar
    })
    return data
  },
}

function semVazios<T extends object>(obj: T): Partial<T> {
  return Object.fromEntries(Object.entries(obj).filter(([, v]) => v !== undefined && v !== '')) as Partial<T>
}
