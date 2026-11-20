import { cn } from '@/utils/cn'

/** Classes compartilhadas por Input, Select e campos de arquivo (estilo dos popups do Figma). */
export const fieldClass = (hasError: boolean) =>
  cn(
    'h-11 w-full rounded-xl border bg-surface px-3.5 text-[15px] text-text outline-none transition-colors',
    'placeholder:text-[#a59fb3] focus:border-primary focus:ring-3 focus:ring-primary/15',
    'disabled:cursor-not-allowed disabled:bg-background',
    hasError ? 'border-danger' : 'border-[#e1dbee]',
  )

export const labelClass = 'text-xs leading-4 font-medium text-[#4a4160]'
