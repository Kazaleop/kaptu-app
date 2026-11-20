import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { locatarioService } from '@/services'
import type { LocatarioRequest } from '@/types'
import { queryKeys } from './queryKeys'

export function useLocatarios() {
  return useQuery({
    queryKey: queryKeys.locatarios,
    queryFn: locatarioService.listar,
  })
}

export function useCreateLocatario() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: LocatarioRequest) => locatarioService.cadastrar(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.locatarios }),
  })
}
