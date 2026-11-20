import type { ReactNode } from 'react'
import { cn } from '@/utils/cn'

/**
 * Cores dos selos de status do design:
 *  - primary: "Contrato ativo", "Classificado", "Anexado"
 *  - warning: "Vence em 5 dias", "Em revisão", "Pendente"
 *  - info:    "Em renovação"
 */
export type BadgeVariant = 'primary' | 'warning' | 'info' | 'success' | 'danger' | 'neutral'

const variants: Record<BadgeVariant, string> = {
  primary: 'bg-[#efe9fb] text-[#5b3fb0]',
  warning: 'bg-badge-bg text-badge-text',
  info: 'bg-[#ece8fb] text-[#6a52c2]',
  success: 'bg-[#e6f6ee] text-[#1d7a4c]',
  danger: 'bg-[#fdecea] text-[#b42318]',
  neutral: 'bg-[#f1eff5] text-muted',
}

/** Etiqueta de status. Os textos/cores por enum ficam em utils/labels.ts */
export function Badge({ variant = 'neutral', children }: { variant?: BadgeVariant; children: ReactNode }) {
  return (
    <span
      className={cn(
        'inline-flex rounded-full px-2.5 py-1 text-[11px] leading-3 font-medium whitespace-nowrap',
        variants[variant],
      )}
    >
      {children}
    </span>
  )
}
