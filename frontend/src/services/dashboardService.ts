import { usarMock } from '@/config/endpoints'
import { mockDashboard } from '@/mocks/dashboard'
import { delay } from '@/mocks/db'
import type { DashboardDTO } from '@/types'
import { api } from './api'

/** F-9 Dashboard -> DashboardController */
export const dashboardService = {
  /** GET /api/v1/dashboard/resumo (mês corrente) */
  async resumo(): Promise<DashboardDTO> {
    if (usarMock('dashboard')) return delay(mockDashboard)
    const { data } = await api.get<DashboardDTO>('/dashboard/resumo')
    return data
  },
}
