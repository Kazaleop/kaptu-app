import { env } from '@/config/env'
import { delay, mockDb, nextId } from '@/mocks/db'
import type { ImovelDTO, ImovelRequest } from '@/types'
import { api, ApiError } from './api'

/** Endpoints de ImobiliarioController relativos a imóveis. */
export const imovelService = {
  /** GET /api/v1/imoveis */
  async listar(): Promise<ImovelDTO[]> {
    if (env.useMocks) return delay(mockDb.imoveis)
    const { data } = await api.get<ImovelDTO[]>('/imoveis')
    return data
  },

  /** GET /api/v1/imoveis/{id} */
  async buscarPorId(id: number): Promise<ImovelDTO> {
    if (env.useMocks) {
      const imovel = mockDb.imoveis.find((i) => i.id === id)
      if (!imovel) throw new ApiError('Imóvel não encontrado.', 404)
      return delay(imovel)
    }
    const { data } = await api.get<ImovelDTO>(`/imoveis/${id}`)
    return data
  },

  /** POST /api/v1/imoveis -> 201 com ImovelDTO */
  async cadastrar(request: ImovelRequest): Promise<ImovelDTO> {
    if (env.useMocks) {
      const novo: ImovelDTO = {
        ...request,
        id: nextId(mockDb.imoveis),
        statusOcupacao: 'DISPONIVEL',
      }
      mockDb.imoveis.push(novo)
      return delay(novo)
    }
    const { data } = await api.post<ImovelDTO>('/imoveis', request)
    return data
  },
}
