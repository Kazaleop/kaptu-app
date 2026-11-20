import { useQuery } from '@tanstack/react-query'
import { dashboardService } from '@/services'
import { queryKeys } from './queryKeys'

/** GET /dashboard/resumo: totais de imóveis/contratos e entradas/saídas do mês. */
export function useDashboardResumo() {
  return useQuery({
    queryKey: queryKeys.dashboard,
    queryFn: dashboardService.resumo,
  })
}
