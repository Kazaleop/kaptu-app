import { usarMock } from '@/config/endpoints'
import { env } from '@/config/env'
import { mockContratosAVencer } from '@/mocks/dashboard'
import { delay, mockDb, nextId } from '@/mocks/db'
import type { AlertaDTO, ContratoLinhaDTO, ContratoRequest } from '@/types'
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
      const novo: ContratoLinhaDTO = {
        id: nextId(mockDb.contratos),
        imovel: imovel.nome,
        locatario: locatario.nome,
        status: 'VIGENTE',
        valor: request.valorAluguel,
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
    const [{ data: alertas }, { data: contratos }] = await Promise.all([
      api.get<AlertaDTO[]>('/alertas'),
      api.get<ContratoLinhaDTO[]>('/contratos'),
    ])
    const vigentes = new Set(contratos.filter((c) => c.status === 'VIGENTE').map((c) => c.id))
    const aVencer = new Set(
      alertas
        .filter((a) => a.tipo === 'CONTRATO_A_VENCER' && vigentes.has(a.referenciaId))
        .map((a) => a.referenciaId),
    )
    return aVencer.size
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
