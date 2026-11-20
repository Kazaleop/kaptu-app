import { useState } from 'react'
import { Card, Icon, QueryState, SectionTitle } from '@/components/ui'
import { useFluxoMensal } from '@/hooks'
import type { PeriodoFluxo } from '@/types'
import { cn } from '@/utils/cn'
import { formatCurrency, formatMesCurto } from '@/utils/format'

const periodos: { value: PeriodoFluxo; label: string }[] = [
  { value: '6', label: 'Últimos 6 meses' },
  { value: '12', label: 'Últimos 12 meses' },
  { value: 'ano', label: 'Este ano' },
]

/** Card "Desempenho financeiro · Receitas e despesas" com gráfico de barras. */
export function FinanceChartCard() {
  const [periodo, setPeriodo] = useState<PeriodoFluxo>('6')
  const { data = [], isLoading, error, refetch, isFetching } = useFluxoMensal(periodo)

  // Altura das barras proporcional ao maior valor do período
  const maior = Math.max(1, ...data.flatMap((m) => [m.receitas, m.despesas]))
  const altura = (valor: number) => `${(valor / maior) * 100}%`

  return (
    <Card className="flex flex-col">
      <SectionTitle
        eyebrow="Desempenho financeiro"
        title="Receitas e despesas"
        aside={
          <label className="relative">
            <span className="sr-only">Período do gráfico</span>
            <select
              value={periodo}
              onChange={(e) => setPeriodo(e.target.value as PeriodoFluxo)}
              className="h-[41px] cursor-pointer appearance-none rounded-xl border border-[#e1dbee] bg-white pr-9 pl-4 text-base text-[#352b4f] outline-none focus:border-primary"
            >
              {periodos.map((p) => (
                <option key={p.value} value={p.value}>
                  {p.label}
                </option>
              ))}
            </select>
            <Icon
              name="chevronDown"
              size={16}
              className="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-[#352b4f]"
            />
          </label>
        }
      />

      <div className="mt-8 h-56">
        <QueryState
          isLoading={isLoading}
          error={error}
          isEmpty={data.length === 0}
          emptyMessage="Sem movimentações no período."
          onRetry={refetch}
        >
          <div
            className={cn(
              'flex h-full items-end justify-between gap-3 transition-opacity',
              isFetching && 'opacity-60',
            )}
          >
            {data.map((m) => (
              <div key={m.mes} className="flex h-full min-w-0 flex-1 flex-col justify-end gap-1.5">
                <div className="flex h-full items-end justify-center gap-1">
                  <div
                    className="w-full max-w-[37px] rounded-t-md bg-primary transition-[height] duration-500"
                    style={{ height: altura(m.receitas) }}
                    title={`${formatMesCurto(m.mes)} · Receitas: ${formatCurrency(m.receitas)}`}
                  />
                  <div
                    className="w-full max-w-[37px] rounded-t-md bg-accent transition-[height] duration-500"
                    style={{ height: altura(m.despesas) }}
                    title={`${formatMesCurto(m.mes)} · Despesas: ${formatCurrency(m.despesas)}`}
                  />
                </div>
                <span className="text-center text-[10px] leading-[15px] text-[#8a8297]">
                  {formatMesCurto(m.mes)}
                </span>
              </div>
            ))}
          </div>
        </QueryState>
      </div>

      <div className="mt-5 flex gap-5 border-t border-divider pt-4 text-xs leading-4 text-muted">
        <span className="inline-flex items-center gap-2">
          <span className="size-2 rounded bg-primary" aria-hidden="true" /> Receitas
        </span>
        <span className="inline-flex items-center gap-2">
          <span className="size-2 rounded bg-accent" aria-hidden="true" /> Despesas
        </span>
      </div>
    </Card>
  )
}
