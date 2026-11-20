import type { BadgeVariant } from '@/components/ui'
import type { ContratoLinhaDTO, ImovelDTO } from '@/types'
import { diasAte } from '@/utils/format'

/** Uma linha da tabela "Imóveis e contratos": o imóvel + o contrato atual dele (se houver). */
export interface LinhaImovel {
  imovel: ImovelDTO
  contrato?: ContratoLinhaDTO
}

/** Contratos com até N dias para vencer recebem o selo laranja "Vence em N dias". */
export const DIAS_ALERTA_VENCIMENTO = 30

/**
 * Junta imóveis e contratos.
 *
 * ⚠ O ContratoLinhaDTO do backend traz o NOME do imóvel, não o id,
 * então a ligação é feita pelo nome. Se o backend passar a enviar
 * "imovelId" no DTO, troque a comparação abaixo por id.
 *
 * Contrato "atual" = VIGENTE (ou VENCIDO, que ainda precisa de atenção).
 * Contratos ENCERRADOS não aparecem: o imóvel volta a ficar disponível.
 */
export function montarLinhas(imoveis: ImovelDTO[], contratos: ContratoLinhaDTO[]): LinhaImovel[] {
  const prioridade = { VIGENTE: 0, VENCIDO: 1, ENCERRADO: 2 } as const

  return imoveis.map((imovel) => {
    const contrato = contratos
      .filter((c) => c.imovel === imovel.nome && c.status !== 'ENCERRADO')
      .sort((a, b) => prioridade[a.status] - prioridade[b.status] || b.id - a.id)[0]
    return { imovel, contrato }
  })
}

export function filtrarLinhas(linhas: LinhaImovel[], busca: string): LinhaImovel[] {
  const termo = normalizar(busca)
  if (!termo) return linhas
  return linhas.filter(({ imovel, contrato }) =>
    [imovel.nome, imovel.endereco, imovel.bairro, contrato?.locatario ?? ''].some((campo) =>
      normalizar(campo).includes(termo),
    ),
  )
}

/** Texto e cor do selo da coluna STATUS. */
export function statusDaLinha({ contrato }: LinhaImovel): { label: string; variant: BadgeVariant } {
  if (!contrato) return { label: 'Disponível', variant: 'success' }
  if (contrato.status === 'VENCIDO') return { label: 'Vencido', variant: 'danger' }

  if (contrato.dataValidade) {
    const dias = diasAte(contrato.dataValidade)
    if (dias >= 0 && dias <= DIAS_ALERTA_VENCIMENTO) {
      return {
        label: dias === 0 ? 'Vence hoje' : `Vence em ${dias} ${dias === 1 ? 'dia' : 'dias'}`,
        variant: 'warning',
      }
    }
  }
  return { label: 'Contrato ativo', variant: 'primary' }
}

/** Remove acentos e caixa para a busca: "Aracajú" encontra "aracaju". */
function normalizar(texto: string): string {
  return texto
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .trim()
}
