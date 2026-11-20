import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { financeiroService } from '@/services'
import type { AnexarComprovanteRequest, PeriodoFluxo } from '@/types'
import { baixarArquivo } from '@/utils/download'
import { queryKeys } from './queryKeys'

/** Receitas x despesas por mês. Mantém o gráfico anterior enquanto troca de período. */
export function useFluxoMensal(periodo: PeriodoFluxo) {
  return useQuery({
    queryKey: queryKeys.fluxoMensal(periodo),
    queryFn: () => financeiroService.fluxoMensal(periodo),
    placeholderData: keepPreviousData,
  })
}

export function useFinanceiroResumo() {
  return useQuery({
    queryKey: queryKeys.financeiroResumo,
    queryFn: financeiroService.resumo,
  })
}

/** Todas as transações (mais recentes primeiro). A paginação é feita na tela. */
export function useTransacoes() {
  return useQuery({
    queryKey: queryKeys.transacoes,
    queryFn: financeiroService.transacoes,
  })
}

/** Upload do comprovante + conciliação. Recarrega financeiro e documentos. */
export function useAnexarComprovante() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: AnexarComprovanteRequest) => financeiroService.anexarComprovante(request),
    onSuccess: () =>
      Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.financeiro }),
        queryClient.invalidateQueries({ queryKey: queryKeys.documentos }),
      ]),
  })
}

/** Gera o relatório do período e dispara o download. */
export function useRelatorio() {
  return useMutation({
    mutationFn: ({ inicio, fim }: { inicio: string; fim: string }) => financeiroService.relatorio(inicio, fim),
    onSuccess: ({ blob, nome }) => baixarArquivo(blob, nome),
  })
}
