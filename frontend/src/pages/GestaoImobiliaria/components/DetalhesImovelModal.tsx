import { useState, type ReactNode } from 'react'
import { Avatar, Badge, Button, FormError, Modal } from '@/components/ui'
import { useEncerrarContrato } from '@/hooks'
import { formatCurrencySmart } from '@/utils/format'
import { statusOcupacaoLabel } from '@/utils/labels'
import { statusDaLinha, type LinhaImovel } from '../linhas'

interface DetalhesImovelModalProps {
  linha: LinhaImovel | null
  onClose(): void
  /** Abre o "Novo contrato" com este imóvel já escolhido */
  onNovoContrato(imovelId: number): void
}

/**
 * Aberto ao clicar numa linha da tabela.
 * (Sem tela no Figma: mostra os dados do imóvel/contrato e permite
 * encerrar o contrato -> POST /api/v1/contratos/{id}/encerrar.)
 */
export function DetalhesImovelModal({ linha, onClose, onNovoContrato }: DetalhesImovelModalProps) {
  return linha ? (
    <Conteudo key={linha.imovel.id} linha={linha} onClose={onClose} onNovoContrato={onNovoContrato} />
  ) : null
}

function Conteudo({ linha, onClose, onNovoContrato }: DetalhesImovelModalProps & { linha: LinhaImovel }) {
  const { imovel, contrato } = linha
  const [confirmando, setConfirmando] = useState(false)
  const encerrar = useEncerrarContrato()
  const status = statusDaLinha(linha)
  const ocupacao = statusOcupacaoLabel[imovel.statusOcupacao]

  return (
    <Modal
      open
      onClose={onClose}
      title={imovel.nome}
      description={`${imovel.endereco} · ${imovel.bairro}`}
      footer={
        contrato?.status === 'VIGENTE' ? (
          confirmando ? (
            <>
              <span className="mr-auto text-sm text-muted">Encerrar este contrato?</span>
              <Button variant="secondary" onClick={() => setConfirmando(false)}>
                Cancelar
              </Button>
              <Button
                variant="danger"
                loading={encerrar.isPending}
                onClick={() => encerrar.mutate(contrato.id, { onSuccess: onClose })}
              >
                Confirmar encerramento
              </Button>
            </>
          ) : (
            <Button variant="secondary" onClick={() => setConfirmando(true)}>
              Encerrar contrato
            </Button>
          )
        ) : (
          !contrato && (
            <Button size="lg" onClick={() => onNovoContrato(imovel.id)}>
              Novo contrato para este imóvel
            </Button>
          )
        )
      }
    >
      <dl className="grid grid-cols-2 gap-x-6 gap-y-4 text-sm">
        <Item rotulo="Ocupação">
          <Badge variant={ocupacao.variant}>{ocupacao.label}</Badge>
        </Item>
        <Item rotulo="Residentes">{imovel.quantResidentes}</Item>
        <Item rotulo="Aluguel de referência">{formatCurrencySmart(imovel.valorAluguelMensal)}/mês</Item>
        <Item rotulo="Código">#{String(imovel.id).padStart(3, '0')}</Item>
      </dl>

      <div className="mt-5 border-t border-divider pt-5">
        <p className="eyebrow">Contrato atual</p>
        {contrato ? (
          <dl className="mt-3 grid grid-cols-2 gap-x-6 gap-y-4 text-sm">
            <Item rotulo="Locatário">
              <span className="flex items-center gap-2">
                <Avatar nome={contrato.locatario} />
                {contrato.locatario}
              </span>
            </Item>
            <Item rotulo="Status">
              <Badge variant={status.variant}>{status.label}</Badge>
            </Item>
            <Item rotulo="Valor">{formatCurrencySmart(contrato.valor)}/mês</Item>
            <Item rotulo="Nº do contrato">#{contrato.id}</Item>
          </dl>
        ) : (
          <p className="mt-2 text-sm text-muted">Nenhum contrato ativo para este imóvel.</p>
        )}
      </div>
      <FormError error={encerrar.error} />
    </Modal>
  )
}

function Item({ rotulo, children }: { rotulo: string; children: ReactNode }) {
  return (
    <div>
      <dt className="text-xs text-subtle">{rotulo}</dt>
      <dd className="mt-1 text-text">{children}</dd>
    </div>
  )
}
