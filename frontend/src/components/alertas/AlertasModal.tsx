import { Button, FormError, Modal, QueryState } from '@/components/ui'
import { useAlertas, useMarcarAlertasLidos } from '@/hooks'
import { AlertList } from './AlertList'

interface AlertasModalProps {
  open: boolean
  onClose(): void
}

/**
 * Popup "Alertas inteligentes" (aberto pelo sino ou por "Ver todos os alertas").
 * "Entendi" marca os alertas novos como lidos (PATCH /alertas/{id}/lido).
 */
export function AlertasModal({ open, onClose }: AlertasModalProps) {
  const { data: alertas = [], naoLidos, isLoading, error, refetch } = useAlertas()
  const marcar = useMarcarAlertasLidos()

  // Novos primeiro; os já lidos aparecem depois, apagados
  const ordenados = [...alertas].sort((a, b) => Number(a.lido) - Number(b.lido))

  const descricao =
    naoLidos === 0
      ? 'Nenhuma situação nova precisa da sua atenção.'
      : naoLidos === 1
        ? '1 situação precisa da sua atenção.'
        : `${naoLidos} situações precisam da sua atenção.`

  function entendi() {
    const ids = alertas.filter((a) => !a.lido).map((a) => a.id)
    if (ids.length === 0) return onClose()
    marcar.mutate(ids, { onSuccess: onClose })
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Alertas inteligentes"
      description={isLoading ? 'Carregando...' : descricao}
      footerTone="plain"
      footer={
        <Button size="lg" fullWidth onClick={entendi} loading={marcar.isPending}>
          Entendi
        </Button>
      }
    >
      <QueryState
        isLoading={isLoading}
        error={error}
        isEmpty={alertas.length === 0}
        emptyMessage="Tudo em dia por aqui."
        onRetry={refetch}
      >
        <AlertList alertas={ordenados} />
      </QueryState>
      <FormError error={marcar.error} />
    </Modal>
  )
}
