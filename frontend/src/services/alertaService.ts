import { usarMock } from '@/config/endpoints'
import { mockAlertas } from '@/mocks/dashboard'
import { delay } from '@/mocks/db'
import type { AlertaDTO } from '@/types'
import { api } from './api'

/** F-7 Alertas inteligentes -> AlertaController */
export const alertaService = {
  /** GET /api/v1/alertas (mais recentes primeiro) */
  async listar(): Promise<AlertaDTO[]> {
    if (usarMock('alertas')) return delay(mockAlertas)
    const { data } = await api.get<AlertaDTO[]>('/alertas')
    return data
  },

  /** PATCH /api/v1/alertas/{id}/lido */
  async marcarComoLido(id: number): Promise<void> {
    if (usarMock('alertas')) {
      const alerta = mockAlertas.find((a) => a.id === id)
      if (alerta) alerta.lido = true
      await delay(null, 150)
      return
    }
    await api.patch(`/alertas/${id}/lido`)
  },
}
