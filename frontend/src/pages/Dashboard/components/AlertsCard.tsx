import { AlertList } from '@/components/alertas/AlertList'
import { useAlertasModal } from '@/components/alertas/alertasModalContext'
import { Button, Card, QueryState, SectionTitle } from '@/components/ui'
import { useAlertas } from '@/hooks'

/** Quantos alertas aparecem no card; o resto fica no popup. */
const LIMITE_CARD = 3

/** Card "Atenção necessária · Alertas inteligentes". */
export function AlertsCard() {
  const { data: alertas = [], naoLidos, isLoading, error, refetch } = useAlertas()
  const { abrirAlertas } = useAlertasModal()

  return (
    <Card className="flex flex-col">
      <SectionTitle
        eyebrow="Atenção necessária"
        title="Alertas inteligentes"
        aside={
          naoLidos > 0 && (
            <span className="rounded-lg bg-badge-bg px-2 py-1 text-xs leading-4 font-semibold whitespace-nowrap text-badge-text">
              {naoLidos} {naoLidos === 1 ? 'novo' : 'novos'}
            </span>
          )
        }
      />

      <div className="mt-5 flex-1">
        <QueryState
          isLoading={isLoading}
          error={error}
          isEmpty={alertas.length === 0}
          emptyMessage="Nenhum alerta no momento."
          onRetry={refetch}
        >
          {/* Novos primeiro; se não houver novos, mostra os mais recentes */}
          <AlertList
            alertas={[...alertas].sort((a, b) => Number(a.lido) - Number(b.lido)).slice(0, LIMITE_CARD)}
          />
        </QueryState>
      </div>

      <Button variant="soft" size="lg" fullWidth className="mt-4" onClick={abrirAlertas}>
        Ver todos os alertas
      </Button>
    </Card>
  )
}
