import type { ButtonHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'
import { Spinner } from './Spinner'

type Variant = 'primary' | 'soft' | 'secondary' | 'danger' | 'ghost'
type Size = 'sm' | 'md' | 'lg'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant
  size?: Size
  /** Ocupa a largura toda do container */
  fullWidth?: boolean
  /** Mostra spinner e desabilita o botão (ex.: enquanto a mutação roda) */
  loading?: boolean
}

const variants: Record<Variant, string> = {
  /** Roxo cheio: "Entendi" do popup de alertas */
  primary: 'bg-primary text-white hover:bg-primary-hover',
  /** Lilás claro: "Ver todos os alertas" */
  soft: 'bg-primary-soft text-primary-text hover:bg-[#e6e0f5]',
  secondary: 'bg-surface text-text border border-border hover:bg-background',
  danger: 'bg-danger text-white hover:opacity-90',
  ghost: 'text-text hover:bg-primary-soft',
}

const sizes: Record<Size, string> = {
  sm: 'h-8 px-3 text-sm',
  md: 'h-10 px-4 text-sm',
  lg: 'h-11 px-5 text-base',
}

export function Button({
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  loading = false,
  disabled,
  className,
  children,
  type = 'button',
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-xl font-normal transition-colors',
        'cursor-pointer disabled:cursor-not-allowed disabled:opacity-60',
        'focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary',
        variants[variant],
        sizes[size],
        fullWidth && 'w-full',
        className,
      )}
      {...props}
    >
      {loading && <Spinner className="size-4" />}
      {children}
    </button>
  )
}
