import { useMemo, useState } from 'react'
import { Badge, Button, Card, Icon, Pagination, QueryState, Table, Td, Th, Tr } from '@/components/ui'
import { useTransacoes } from '@/hooks'
import { codigoTransacao } from '@/services'
import { cn } from '@/utils/cn'
import { formatCurrency, formatDataCurta } from '@/utils/format'
import { statusConciliacaoLabel } from '@/utils/labels'
import { paginar } from '@/utils/lista'

const TAMANHO_PAGINA = 8

interface TransacoesCardProps {
  /** Abre o popup "Anexar comprovante" (com a transação já escolhida, se vier o id) */
  onAnexar(transacaoId?: number): void
}

/** Card "Transações recentes · Conciliação financeiro-documental". */
export function TransacoesCard({ onAnexar }: TransacoesCardProps) {
  const [pagina, setPagina] = useState(0)
  const { data: transacoes, isLoading, error, refetch, isFetching } = useTransacoes()
  const data = useMemo(
    () => (transacoes ? paginar(transacoes, pagina, TAMANHO_PAGINA) : undefined),
    [transacoes, pagina],
  )

  return (
    <Card padded={false} className="overflow-hidden">
      <div className="flex flex-wrap items-center justify-between gap-4 px-6 pt-5 pb-4">
        <div>
          <h2 className="text-base leading-6 font-semibold text-text">Transações recentes</h2>
          <p className="mt-0.5 text-xs leading-4 text-muted">Conciliação financeiro-documental.</p>
        </div>
        <Button variant="secondary" onClick={() => onAnexar()} className="rounded-xl">
          <Icon name="paperclip" size={16} /> Anexar comprovante
        </Button>
      </div>

      <QueryState
        isLoading={isLoading}
        error={error}
        isEmpty={data?.content.length === 0}
        emptyMessage="Nenhuma transação registrada."
        onRetry={refetch}
      >
        <div className={cn('transition-opacity', isFetching && 'opacity-60')}>
          <Table>
            <thead>
              <tr>
                <Th>Descrição</Th>
                <Th>Data</Th>
                <Th>Valor</Th>
                <Th>Comprovante</Th>
                <Th className="w-24">
                  <span className="sr-only">Ações</span>
                </Th>
              </tr>
            </thead>
            <tbody>
              {data?.content.map((t) => {
                const status = statusConciliacaoLabel[t.statusConciliacao]
                return (
                  <Tr key={t.id}>
                    <Td>
                      <p className="font-medium">{t.descricao}</p>
                      <p className="mt-0.5 text-xs text-subtle">#{codigoTransacao(t.id)}</p>
                    </Td>
                    <Td className="whitespace-nowrap text-muted">{formatDataCurta(t.dataTransacao)}</Td>
                    <Td className={cn('whitespace-nowrap', t.tipoTransacao === 'ENTRADA' && 'text-success')}>
                      {t.tipoTransacao === 'ENTRADA' ? '+ ' : ''}
                      {formatCurrency(t.valor)}
                    </Td>
                    <Td>
                      <span title={t.documentoId ? `Documento #${t.documentoId}` : undefined}>
                        <Badge variant={status.variant}>{status.label}</Badge>
                      </span>
                    </Td>
                    <Td className="text-right">
                      {t.statusConciliacao === 'PENDENTE' && (
                        <button
                          type="button"
                          onClick={() => onAnexar(t.id)}
                          className="cursor-pointer text-xs font-medium text-primary-text hover:underline"
                        >
                          Anexar
                        </button>
                      )}
                    </Td>
                  </Tr>
                )
              })}
            </tbody>
          </Table>
        </div>
      </QueryState>

      {data && data.totalElements > TAMANHO_PAGINA && (
        <Pagination
          pagina={data.number}
          totalPaginas={data.totalPages}
          totalItens={data.totalElements}
          itensNaPagina={data.content.length}
          rotulo="transações"
          onChange={setPagina}
        />
      )}
    </Card>
  )
}
