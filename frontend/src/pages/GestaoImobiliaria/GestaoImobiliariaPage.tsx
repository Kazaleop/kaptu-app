import { useMemo, useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, Icon, MetricCard } from '@/components/ui'
import { useContratos, useImoveis, useLocatarios } from '@/hooks'
import { formatNumber } from '@/utils/format'
import { DetalhesImovelModal } from './components/DetalhesImovelModal'
import { ImoveisTable } from './components/ImoveisTable'
import { NovoContratoModal } from './components/NovoContratoModal'
import { NovoImovelModal } from './components/NovoImovelModal'
import { montarLinhas, type LinhaImovel } from './linhas'

const percentual = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 })

/**
 * Tela 02 · Gestão imobiliária (F-3). 100% backend real:
 *  GET /imoveis, GET /locatarios, GET /contratos,
 *  POST /contratos, POST /imoveis, POST /locatarios, POST /contratos/{id}/encerrar
 */
export function GestaoImobiliariaPage() {
  const imoveis = useImoveis()
  const contratos = useContratos()
  const locatarios = useLocatarios()

  const [novoContrato, setNovoContrato] = useState<{ imovelId?: number } | null>(null)
  const [novoImovel, setNovoImovel] = useState(false)
  const [selecionada, setSelecionada] = useState<LinhaImovel | null>(null)

  const linhas = useMemo(
    () => montarLinhas(imoveis.data ?? [], contratos.data ?? []),
    [imoveis.data, contratos.data],
  )

  // Indicadores
  const total = imoveis.data?.length ?? 0
  const ocupados = imoveis.data?.filter((i) => i.statusOcupacao === 'OCUPADO').length ?? 0
  const vigentes = contratos.data?.filter((c) => c.status === 'VIGENTE') ?? []
  const clientesComContrato = new Set(vigentes.map((c) => c.locatario)).size
  const taxaOcupacao = total ? (ocupados / total) * 100 : 0

  return (
    <div className="flex flex-col gap-8">
      <PageHeader
        eyebrow="Patrimônio e locações"
        title="Gestão imobiliária"
        description={
          imoveis.data
            ? `Centralize os ${formatNumber(total)} ${total === 1 ? 'imóvel' : 'imóveis'}, clientes e contratos ativos da Fundação.`
            : 'Centralize os imóveis, clientes e contratos ativos da Fundação.'
        }
        actions={
          <Button size="lg" onClick={() => setNovoContrato({})}>
            <Icon name="plus" size={16} /> Novo contrato de aluguel
          </Button>
        }
      />

      <section aria-label="Indicadores" className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <MetricCard
          label="Imóveis cadastrados"
          icon="building"
          iconBg="bg-icon-violet"
          iconColor="text-primary"
          isLoading={imoveis.isLoading}
          isError={imoveis.isError}
          value={formatNumber(total)}
          detail={`${formatNumber(ocupados)} ${ocupados === 1 ? 'ocupado' : 'ocupados'} · ${formatNumber(total - ocupados)} ${total - ocupados === 1 ? 'disponível' : 'disponíveis'}`}
        />
        <MetricCard
          label="Contratos ativos"
          icon="document"
          iconBg="bg-icon-lilac"
          iconColor="text-primary"
          isLoading={contratos.isLoading || imoveis.isLoading}
          isError={contratos.isError}
          value={formatNumber(vigentes.length)}
          detail={`${percentual.format(taxaOcupacao)}% de ocupação`}
        />
        <MetricCard
          label="Clientes ativos"
          icon="users"
          iconBg="bg-icon-yellow"
          iconColor="text-[#a07a00]"
          isLoading={locatarios.isLoading || contratos.isLoading}
          isError={locatarios.isError}
          value={formatNumber(locatarios.data?.length ?? 0)}
          detail={`${formatNumber(clientesComContrato)} com contrato vigente`}
        />
      </section>

      <ImoveisTable
        linhas={linhas}
        isLoading={imoveis.isLoading || contratos.isLoading}
        error={imoveis.error ?? contratos.error}
        onRetry={() => {
          imoveis.refetch()
          contratos.refetch()
        }}
        onSelecionar={setSelecionada}
        onCadastrarImovel={() => setNovoImovel(true)}
      />

      <NovoContratoModal
        open={novoContrato !== null}
        imovelInicial={novoContrato?.imovelId}
        onClose={() => setNovoContrato(null)}
      />
      <NovoImovelModal open={novoImovel} onClose={() => setNovoImovel(false)} />
      <DetalhesImovelModal
        linha={selecionada}
        onClose={() => setSelecionada(null)}
        onNovoContrato={(imovelId) => {
          setSelecionada(null)
          setNovoContrato({ imovelId })
        }}
      />
    </div>
  )
}
