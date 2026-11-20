import { usarMock } from '@/config/endpoints'
import { env } from '@/config/env'
import { mockContratosAVencer } from '@/mocks/dashboard'
import { delay, mockDb, nextId } from '@/mocks/db'
import type { ContratoLinhaDTO, ContratoRequest } from '@/types'
import { somarMeses, toIsoDate } from '@/utils/format'
import { api, ApiError } from './api'

/** Endpoints de ImobiliarioController relativos a contratos. */
export const contratoService = {
  /** GET /api/v1/contratos */
  async listar(): Promise<ContratoLinhaDTO[]> {
    if (env.useMocks) return delay(mockDb.contratos)
    const { data } = await api.get<ContratoLinhaDTO[]>('/contratos')
    return data
  },

  /**
   * POST /api/v1/contratos -> 201 com ContratoLinhaDTO
   * O backend calcula a data de validade (dataExpedicao + prazoMeses).
   */
  async criar(request: ContratoRequest): Promise<ContratoLinhaDTO> {
    if (env.useMocks) {
      const imovel = mockDb.imoveis.find((i) => i.id === request.imovelId)
      const locatario = mockDb.locatarios.find((l) => l.id === request.locatarioId)
      if (!imovel || !locatario) throw new ApiError('Imóvel ou locatário não encontrado.', 404)
      imovel.statusOcupacao = 'OCUPADO'
      const dataExpedicao = request.dataExpedicao ?? toIsoDate(new Date())
      const dataValidade = somarMeses(dataExpedicao, request.prazoMeses)
      const novo: ContratoLinhaDTO = {
        id: nextId(mockDb.contratos),
        imovelId: imovel.id,
        locatarioId: locatario.id,
        imovel: imovel.nome,
        locatario: locatario.nome,
        status: 'VIGENTE',
        valor: request.valorAluguel,
        dataExpedicao,
        prazoMeses: request.prazoMeses,
        dataValidade,
        indiceReajuste: request.indiceReajuste,
      }
      mockDb.contratos.push(novo)
      return delay(novo)
    }
    const { data } = await api.post<ContratoLinhaDTO>('/contratos', request)
    return data
  },

  /**
   * Contratos que vencem nos próximos 30 dias.
   * O backend não tem um endpoint para isso: o AlertaJob gera um alerta
   * CONTRATO_A_VENCER (referenciaId = id do contrato) quando faltam até 30 dias.
   * Contamos esses alertas cujo contrato ainda está VIGENTE.
   */
  async contarAVencer(): Promise<number> {
    if (usarMock('contratosAVencer')) return delay(mockContratosAVencer)
    const { data: contratos } = await api.get<ContratoLinhaDTO[]>('/contratos', {
      params: { status: 'VIGENTE', venceEmDias: 30 },
    })
    return contratos.length
  },

  /** POST /api/v1/contratos/{id}/encerrar -> 204 sem corpo */
  async encerrar(id: number): Promise<void> {
    if (env.useMocks) {
      const contrato = mockDb.contratos.find((c) => c.id === id)
      if (!contrato) throw new ApiError('Contrato não encontrado.', 404)
      contrato.status = 'ENCERRADO'
      await delay(null)
      return
    }
    await api.post(`/contratos/${id}/encerrar`)
  },
}
