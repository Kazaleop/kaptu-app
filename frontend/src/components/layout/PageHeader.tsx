import type { ReactNode } from 'react'

interface PageHeaderProps {
  /** Sobretítulo em caixa alta (ex.: "PATRIMÔNIO E LOCAÇÕES") */
  eyebrow?: string
  title: string
  description?: ReactNode
  /** Botões à direita do título (ex.: "Novo contrato de aluguel") */
  actions?: ReactNode
}

/** Topo das páginas internas (Gestão imobiliária, Acervo, Financeiro). */
export function PageHeader({ eyebrow, title, description, actions }: PageHeaderProps) {
  return (
    <div className="flex flex-wrap items-end justify-between gap-4">
      <div>
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h1 className="mt-1 text-[30px] leading-9 font-semibold tracking-[-1px] text-text">{title}</h1>
        {description && <p className="mt-1.5 text-sm leading-5 text-muted">{description}</p>}
      </div>
      {actions && <div className="flex gap-2">{actions}</div>}
    </div>
  )
}
