import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { alertaService } from '@/services'
import { queryKeys } from './queryKeys'

export function useAlertas() {
  const query = useQuery({
    queryKey: queryKeys.alertas,
    queryFn: alertaService.listar,
  })
  const naoLidos = query.data?.filter((a) => !a.lido).length ?? 0
  return { ...query, naoLidos }
}

/** Marca vários alertas como lidos (PATCH /alertas/{id}/lido para cada um). */
export function useMarcarAlertasLidos() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (ids: number[]) => Promise.all(ids.map((id) => alertaService.marcarComoLido(id))),
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.alertas }),
        queryClient.invalidateQueries({ queryKey: queryKeys.dashboard }),
      ]),
  })
}
