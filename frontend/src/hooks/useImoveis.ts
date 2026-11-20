import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { imovelService } from '@/services'
import type { ImovelRequest } from '@/types'
import { queryKeys } from './queryKeys'

/** Lista de imóveis (equivalente ao useFoodData do modelo). */
export function useImoveis() {
  return useQuery({
    queryKey: queryKeys.imoveis,
    queryFn: imovelService.listar,
  })
}

/** Um imóvel pelo id. Só busca quando o id existe. */
export function useImovel(id: number | undefined) {
  return useQuery({
    queryKey: queryKeys.imovel(id ?? 0),
    queryFn: () => imovelService.buscarPorId(id as number),
    enabled: id !== undefined,
  })
}

/** Cadastro de imóvel (equivalente ao useFoodDataMutate do modelo). */
export function useCreateImovel() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ImovelRequest) => imovelService.cadastrar(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.imoveis }),
  })
}
