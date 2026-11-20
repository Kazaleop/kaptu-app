import type { ReactNode } from 'react'

interface SectionTitleProps {
  /** Texto pequeno em caixa alta acima do título (ex.: "DESEMPENHO FINANCEIRO") */
  eyebrow: string
  title: string
  id?: string
  /** Elemento à direita (ex.: seletor de período, selo "3 novos") */
  aside?: ReactNode
}

/** Padrão "sobretítulo + título" usado nos cards e seções do design. */
export function SectionTitle({ eyebrow, title, id, aside }: SectionTitleProps) {
  return (
    <div className="flex w-full items-start justify-between gap-4">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h2 id={id} className="mt-1.5 text-xl leading-7 font-semibold tracking-[-0.6px] text-text">
          {title}
        </h2>
      </div>
      {aside}
    </div>
  )
}
