import { formatNumber } from '@/utils/format'
import { Icon } from './Icon'

interface PaginationProps {
  /** Página atual, começando em 0 (padrão Spring) */
  pagina: number
  totalPaginas: number
  totalItens: number
  itensNaPagina: number
  /** Ex.: "documentos" -> "Exibindo 4 de 12.400 documentos" */
  rotulo: string
  onChange(pagina: number): void
}

/** Rodapé de tabela: "Exibindo X de Y ..." à esquerda, "Página N de M" + setas à direita. */
export function Pagination({
  pagina,
  totalPaginas,
  totalItens,
  itensNaPagina,
  rotulo,
  onChange,
}: PaginationProps) {
  const botao =
    'grid size-7 cursor-pointer place-items-center rounded-lg text-muted transition-colors hover:bg-primary-soft hover:text-text disabled:cursor-not-allowed disabled:opacity-40 disabled:hover:bg-transparent'

  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-t border-divider px-6 py-4 text-xs text-subtle">
      <span>
        Exibindo {formatNumber(itensNaPagina)} de {formatNumber(totalItens)} {rotulo}
      </span>
      <div className="flex items-center gap-2">
        <span>
          Página {formatNumber(pagina + 1)} de {formatNumber(Math.max(1, totalPaginas))}
        </span>
        <button
          type="button"
          className={botao}
          disabled={pagina <= 0}
          onClick={() => onChange(pagina - 1)}
          aria-label="Página anterior"
        >
          <Icon name="chevronRight" size={16} className="rotate-180" />
        </button>
        <button
          type="button"
          className={botao}
          disabled={pagina >= totalPaginas - 1}
          onClick={() => onChange(pagina + 1)}
          aria-label="Próxima página"
        >
          <Icon name="chevronRight" size={16} />
        </button>
      </div>
    </div>
  )
}
