import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { documentoService } from '@/services'
import type { FiltrosDocumentosApi, ImportarDocumentoRequest } from '@/types'
import { queryKeys } from './queryKeys'

export function useDocumentosResumo() {
  return useQuery({
    queryKey: queryKeys.documentosResumo,
    queryFn: documentoService.resumo,
  })
}

/** Lista do backend com os filtros dele. Mantém a lista anterior enquanto a nova carrega. */
export function useDocumentos(filtros: FiltrosDocumentosApi) {
  return useQuery({
    queryKey: queryKeys.documentosLista(filtros),
    queryFn: () => documentoService.listar(filtros),
    placeholderData: keepPreviousData,
  })
}

/** Envio de arquivo para OCR. Recarrega a lista, o resumo do dashboard e o menu. */
export function useImportarDocumento() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (request: ImportarDocumentoRequest) => documentoService.importarOcr(request),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: queryKeys.documentos }),
  })
}
