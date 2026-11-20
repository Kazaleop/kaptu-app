import type { InputHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'
import { Icon } from './Icon'

interface SearchInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'onChange'> {
  value: string
  onChange(value: string): void
  /** Texto lido por leitores de tela */
  label: string
}

/** Campo de busca ("Buscar imóvel ou cliente", "Buscar por nome, conteúdo..."). */
export function SearchInput({ value, onChange, label, className, ...props }: SearchInputProps) {
  return (
    <label className={cn('relative block', className)}>
      <span className="sr-only">{label}</span>
      <Icon
        name="search"
        size={16}
        className="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-[#a59fb3]"
      />
      <input
        type="search"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="h-10 w-full rounded-xl border border-[#e1dbee] bg-surface pr-3 pl-10 text-sm text-text outline-none placeholder:text-[#a59fb3] focus:border-primary focus:ring-3 focus:ring-primary/15"
        {...props}
      />
    </label>
  )
}
