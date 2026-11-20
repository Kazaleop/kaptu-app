import { useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, FormError, Icon, MetricCard } from '@/components/ui'
import { useFinanceiroResumo, useRelatorio } from '@/hooks'
import { formatCurrencyCompact, formatNumber, toIsoDate } from '@/utils/format'
import { AnexarComprovanteModal } from './components/AnexarComprovanteModal'
import { TransacoesCard } from './components/TransacoesCard'

const percentual = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 })

/** "2026-09" -> "Setembro de 2026" */
function nomeDoMes(anoMes: string): string {
  const [a, m] = anoMes.split('-').map(Number)
  const texto = new Date(a, m - 1, 1).toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
  return texto.charAt(0).toUpperCase() + texto.slice(1)
}

/**
 * Tela 04 · Financeiro e auditoria (F-1, F-6) -> FinanceiroController + RelatorioController
 *  Cards: GET /dashboard/resumo (entradas/saídas do mês) + GET /financeiro/transacoes
 *  Tabela: GET /financeiro/transacoes
 *  Anexar: POST /documentos/upload + POST /financeiro/conciliar
 *  Relatório: GET /financeiro/relatorio-mpse (PDF) do ano atual até hoje
 */
export function FinanceiroPage() {
  const resumo = useFinanceiroResumo()
  const relatorio = useRelatorio()
  const [anexar, setAnexar] = useState<{ transacaoId?: number } | null>(null)

  const r = resumo.data
  const estado = { isLoading: resumo.isLoading, isError: resumo.isError }

  return (
    <div className="flex flex-col gap-8">
      <div>
        <PageHeader
          eyebrow="Prestação de contas"
          title="Financeiro e auditoria"
          description="Concilie transações e documentos para uma prestação de contas precisa e transparente."
          actions={
            <Button
              size="lg"
              loading={relatorio.isPending}
              onClick={() => {
                const hoje = new Date()
                relatorio.mutate({ inicio: `${hoje.getFullYear()}-01-01`, fim: toIsoDate(hoje) })
              }}
            >
              {!relatorio.isPending && <Icon name="download" size={16} />}
              {relatorio.isPending ? 'Gerando relatório...' : 'Gerar relatório de auditoria'}
            </Button>
          }
        />
        <FormError error={relatorio.error} />
      </div>

      <section aria-label="Indicadores financeiros" className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard
          label="Receitas"
          icon="trendingUp"
          iconBg="bg-icon-violet"
          iconColor="text-primary"
          {...estado}
          value={r && formatCurrencyCompact(r.receitaMes)}
          detail={r && nomeDoMes(r.mesReferencia)}
        />
        <MetricCard
          label="Despesas"
          icon="calendar"
          iconBg="bg-icon-orange"
          iconColor="text-alert-orange"
          {...estado}
          value={r && formatCurrencyCompact(r.despesaMes)}
          detail={r && (r.receitaMes > 0 ? `${percentual.format((r.despesaMes / r.receitaMes) * 100)}% da receita` : '—')}
        />
        <MetricCard
          label="Conciliado"
          icon="check"
          iconBg="bg-icon-yellow"
          iconColor="text-[#a07a00]"
          {...estado}
          value={r && (r.totalTransacoes ? `${Math.round(r.percentualConciliado)}%` : "—")}
          detail={r && `${formatNumber(r.transacoesConciliadas)} de ${formatNumber(r.totalTransacoes)} transações`}
        />
        <MetricCard
          label="Pendências"
          icon="paperclip"
          iconBg="bg-icon-lilac"
          iconColor="text-primary"
          {...estado}
          value={r && formatNumber(r.pendencias)}
          detail={r && (r.pendencias === 1 ? 'Comprovante ausente' : 'Comprovantes ausentes')}
        />
      </section>

      <TransacoesCard onAnexar={(transacaoId) => setAnexar({ transacaoId })} />

      <AnexarComprovanteModal
        open={anexar !== null}
        transacaoInicial={anexar?.transacaoId}
        onClose={() => setAnexar(null)}
      />
    </div>
  )
}
