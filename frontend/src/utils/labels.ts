import type { BadgeVariant } from '@/components/ui/Badge'
import type {
  StatusClassificacao,
  StatusConciliacao,
  StatusContrato,
  StatusOcupacao,
  TipoAlerta,
  TipoDocumento,
} from '@/types'

/** Texto e cor exibidos para cada valor de enum vindo do backend. */

export const statusConciliacaoLabel: Record<StatusConciliacao, { label: string; variant: BadgeVariant }> = {
  CONCILIADA: { label: 'Anexado', variant: 'primary' },
  PENDENTE: { label: 'Pendente', variant: 'warning' },
}

export const tipoDocumentoLabel: Record<TipoDocumento, string> = {
  CONTRATO: 'Contrato',
  NOTA_FISCAL: 'Nota fiscal',
  RECIBO: 'Recibo',
  CERTIDAO: 'Certidão',
}

export const statusClassificacaoLabel: Record<StatusClassificacao, { label: string; variant: BadgeVariant }> = {
  PENDENTE: { label: 'Processando', variant: 'info' },
  CLASSIFICADO: { label: 'Classificado', variant: 'primary' },
  REVISAO_MANUAL: { label: 'Em revisão', variant: 'warning' },
  ERRO: { label: 'Erro na leitura', variant: 'danger' },
}

/** Título e cor da bolinha de cada tipo de alerta (a descrição vem do backend). */
export const tipoAlertaLabel: Record<TipoAlerta, { titulo: string; cor: string }> = {
  CONTRATO_A_VENCER: { titulo: 'Contrato próximo do vencimento', cor: 'bg-alert-orange' },
  ANOMALIA_FINANCEIRA: { titulo: 'Despesa acima do padrão', cor: 'bg-alert-amber' },
  CERTIDAO_A_VENCER: { titulo: 'Certidão próxima do vencimento', cor: 'bg-alert-purple' },
}

export const statusOcupacaoLabel: Record<StatusOcupacao, { label: string; variant: BadgeVariant }> = {
  DISPONIVEL: { label: 'Disponível', variant: 'success' },
  OCUPADO: { label: 'Ocupado', variant: 'primary' },
}

export const statusContratoLabel: Record<StatusContrato, { label: string; variant: BadgeVariant }> = {
  VIGENTE: { label: 'Contrato ativo', variant: 'primary' },
  EM_RENOVACAO: { label: 'Em renovação', variant: 'warning' },
  VENCIDO: { label: 'Vencido', variant: 'danger' },
  ENCERRADO: { label: 'Encerrado', variant: 'neutral' },
}
