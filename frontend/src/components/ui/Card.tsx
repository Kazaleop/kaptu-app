import type { HTMLAttributes } from 'react'
import { cn } from '@/utils/cn'

interface CardProps extends HTMLAttributes<HTMLDivElement> {
  /** false = sem espaçamento interno (ex.: card com tabela encostando nas bordas) */
  padded?: boolean
}

/** Cartão branco padrão do design (borda lilás clara + sombra suave). */
export function Card({ className, padded = true, ...props }: CardProps) {
  return (
    <div
      className={cn(
        'rounded-2xl border border-border bg-surface shadow-card',
        padded && 'p-6',
        className,
      )}
      {...props}
    />
  )
}
