import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { contratoService } from '@/services'
import type { ContratoRequest } from '@/types'
import { queryKeys } from './queryKeys'

export function useContratos() {
  return useQuery({
    queryKey: queryKeys.contratos,
    queryFn: contratoService.listar,
  })
}

/** Quantidade de contratos vigentes que vencem nos próximos 30 dias (via alertas). */
export function useContratosAVencer() {
  return useQuery({
    queryKey: queryKeys.contratosAVencer,
    queryFn: contratoService.contarAVencer,
  })
}

/**
 * Criar ou encerrar contrato muda o status de ocupação do imóvel,
 * então invalidamos as duas listas.
 */
function useInvalidateContratosEImoveis() {
  const queryClient = useQueryClient()
  return () =>
    Promise.all([
      queryClient.invalidateQueries({ queryKey: queryKeys.contratos }),
      queryClient.invalidateQueries({ queryKey: queryKeys.imoveis }),
    ])
}

export function useCreateContrato() {
  const invalidate = useInvalidateContratosEImoveis()
  return useMutation({
    mutationFn: (request: ContratoRequest) => contratoService.criar(request),
    onSuccess: invalidate,
  })
}

export function useEncerrarContrato() {
  const invalidate = useInvalidateContratosEImoveis()
  return useMutation({
    mutationFn: (id: number) => contratoService.encerrar(id),
    onSuccess: invalidate,
  })
}
