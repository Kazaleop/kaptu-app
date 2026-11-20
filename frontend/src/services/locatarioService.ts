import { env } from '@/config/env'
import { delay, mockDb, nextId } from '@/mocks/db'
import type { LocatarioDTO, LocatarioRequest } from '@/types'
import { api } from './api'

/** Endpoints de ImobiliarioController relativos a locatários. */
export const locatarioService = {
  /** GET /api/v1/locatarios */
  async listar(): Promise<LocatarioDTO[]> {
    if (env.useMocks) return delay(mockDb.locatarios)
    const { data } = await api.get<LocatarioDTO[]>('/locatarios')
    return data
  },

  /** POST /api/v1/locatarios -> 201 com LocatarioDTO */
  async cadastrar(request: LocatarioRequest): Promise<LocatarioDTO> {
    if (env.useMocks) {
      const novo: LocatarioDTO = { ...request, id: nextId(mockDb.locatarios) }
      mockDb.locatarios.push(novo)
      return delay(novo)
    }
    const { data } = await api.post<LocatarioDTO>('/locatarios', request)
    return data
  },
}
