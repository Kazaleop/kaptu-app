import type { ReactNode } from 'react'
import { Button } from './Button'
import { Spinner } from './Spinner'

interface QueryStateProps {
  isLoading: boolean
  error: Error | null
  isEmpty?: boolean
  emptyMessage?: string
  onRetry?: () => void
  children: ReactNode
}

/**
 * Trata os 3 estados de qualquer lista vinda do backend:
 * carregando, erro (com "tentar novamente") e vazio.
 */
export function QueryState({
  isLoading,
  error,
  isEmpty,
  emptyMessage = 'Nenhum registro encontrado.',
  onRetry,
  children,
}: QueryStateProps) {
  if (isLoading) {
    return (
      <div className="flex justify-center py-12 text-primary">
        <Spinner />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex flex-col items-center gap-3 py-12 text-center">
        <p className="text-sm text-danger">{error.message}</p>
        {onRetry && (
          <Button variant="secondary" size="sm" onClick={onRetry}>
            Tentar novamente
          </Button>
        )}
      </div>
    )
  }

  if (isEmpty) {
    return <p className="py-12 text-center text-sm text-muted">{emptyMessage}</p>
  }

  return <>{children}</>
}
