import { useState } from 'react'
import { MetricCard, SectionTitle } from '@/components/ui'
import { appConfig } from '@/config/app'
import {
  useContratosAVencer,
  useDashboardResumo,
  useDocumentosResumo,
  useFinanceiroResumo,
} from '@/hooks'
import { paths } from '@/routes/paths'
import {
  formatCurrencyCompact,
  formatDataExtenso,
  formatNumber,
  formatNumberCompact,
  saudacao,
  toIsoDate,
} from '@/utils/format'
import { AlertsCard } from './components/AlertsCard'
import { FinanceChartCard } from './components/FinanceChartCard'
import { ModuleCard } from './components/ModuleCard'

/**
 * Tela 01 · Visão geral (F-9 Dashboard unificado)
 *
 * Origem de cada dado (backend sincronizado em 2026-11-17):
 *  - Receita no mês, saldo, imóveis, contratos vigentes -> GET /dashboard/resumo
 *  - % conciliado                    -> GET /financeiro/transacoes
 *  - Documentos / acervo             -> GET /documentos/filtros (contagem)
 *  - Contratos a vencer              -> GET /alertas (CONTRATO_A_VENCER) + GET /contratos
 *  - Gráfico                         -> GET /financeiro/transacoes agrupadas por mês
 *  - Alertas                         -> GET /alertas
 */
export function DashboardPage() {
  const [hoje] = useState(() => new Date())
  const primeiroNome = appConfig.usuario.nome.split(' ')[0]

  const resumo = useDashboardResumo()
  const financeiro = useFinanceiroResumo()
  const documentos = useDocumentosResumo()
  const aVencer = useContratosAVencer()

  const r = resumo.data
  const totalImoveis = r?.totalImoveis

  return (
    <div className="flex flex-col gap-8">
      <header>
        <time dateTime={toIsoDate(hoje)} className="text-sm leading-5 text-muted">
          {formatDataExtenso(hoje)}
        </time>
        <h1 className="mt-2 text-[38px] leading-[45.6px] font-semibold tracking-[-1.52px] text-text">
          {saudacao(hoje)}
          {primeiroNome ? `, ${primeiroNome}.` : '.'}
        </h1>
        <p className="mt-2 text-base leading-6 text-muted">Aqui está o panorama da Fundação hoje.</p>
      </header>

      <section aria-label="Indicadores principais" className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <MetricCard
          label="Receita no mês"
          icon="trendingUp"
          iconBg="bg-icon-violet"
          iconColor="text-primary"
          isLoading={resumo.isLoading}
          isError={resumo.isError}
          value={r && formatCurrencyCompact(r.entradasMes)}
          detail={r && `Saldo do mês: ${formatCurrencyCompact(r.saldoMes)}`}
        />
        <MetricCard
          label="Imóveis ativos"
          icon="building"
          iconBg="bg-icon-yellow"
          iconColor="text-[#a07a00]"
          isLoading={resumo.isLoading}
          isError={resumo.isError}
          value={r && formatNumber(r.totalImoveis)}
          detail={
            r && `${formatNumber(r.contratosVigentes)} ${r.contratosVigentes === 1 ? 'contrato vigente' : 'contratos vigentes'}`
          }
        />
        <MetricCard
          label="Documentos"
          icon="document"
          iconBg="bg-icon-lilac"
          iconColor="text-primary"
          isLoading={documentos.isLoading}
          isError={documentos.isError}
          value={documentos.data && formatNumber(documentos.data.total)}
          detail={
            documentos.data &&
            (documentos.data.aguardandoRevisao > 0
              ? `${formatNumber(documentos.data.aguardandoRevisao)} aguardando revisão`
              : `${formatNumber(documentos.data.classificados)} classificados`)
          }
        />
        <MetricCard
          label="Contratos a vencer"
          icon="bell"
          iconBg="bg-icon-orange"
          iconColor="text-alert-orange"
          isLoading={aVencer.isLoading}
          isError={aVencer.isError}
          value={aVencer.data !== undefined ? formatNumber(aVencer.data) : undefined}
          detail="Nos próximos 30 dias"
        />
      </section>

      <section
        aria-label="Análise financeira e alertas"
        className="grid grid-cols-1 gap-5 xl:grid-cols-[minmax(0,1.81fr)_minmax(0,1fr)]"
      >
        <FinanceChartCard />
        <AlertsCard />
      </section>

      <section aria-labelledby="modulos-titulo">
        <SectionTitle id="modulos-titulo" eyebrow="Acesso rápido" title="Módulos da gestão" />
        <div className="mt-4 grid grid-cols-1 gap-4 lg:grid-cols-3">
          <ModuleCard
            to={paths.gestaoImobiliaria}
            icon="building"
            title="Gestão imobiliária"
            description="Imóveis, clientes e contratos em um só lugar."
            statistic={
              totalImoveis !== undefined
                ? `${formatNumber(totalImoveis)} ${totalImoveis === 1 ? 'imóvel' : 'imóveis'}`
                : resumo.isError ? '' : undefined
            }
          />
          <ModuleCard
            to={paths.acervo}
            icon="document"
            title="Acervo de documentos"
            description="Busca e classificação inteligente por OCR."
            statistic={
              documentos.data
                ? `${formatNumberCompact(documentos.data.total)} arquivos`
                : documentos.isError ? '' : undefined
            }
          />
          <ModuleCard
            to={paths.financeiro}
            icon="finance"
            title="Financeiro e auditoria"
            description="Conciliação e prestação de contas simplificadas."
            statistic={
              financeiro.data
                ? financeiro.data.totalTransacoes ? `${Math.round(financeiro.data.percentualConciliado)}% conciliado` : "Sem transações"
                : financeiro.isError ? '' : undefined
            }
          />
        </div>
      </section>
    </div>
  )
}
