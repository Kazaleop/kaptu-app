import { useId, type InputHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'
import { fieldClass, labelClass } from './fieldStyles'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string
  /** Mensagem de erro de validação exibida abaixo do campo */
  error?: string
  /** Texto de ajuda exibido abaixo do campo quando não há erro */
  hint?: string
}


/** Campo com label + mensagem de erro (versão componentizada do Input do projeto modelo). */
export function Input({ label, error, hint, id, className, ...props }: InputProps) {
  const generatedId = useId()
  const inputId = id ?? generatedId
  const describedBy = error ? `${inputId}-error` : hint ? `${inputId}-hint` : undefined

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={inputId} className={labelClass}>
        {label}
      </label>
      <input
        id={inputId}
        aria-invalid={!!error}
        aria-describedby={describedBy}
        className={cn(fieldClass(!!error), className)}
        {...props}
      />
      {error ? (
        <span id={`${inputId}-error`} className="text-xs text-danger">
          {error}
        </span>
      ) : (
        hint && (
          <span id={`${inputId}-hint`} className="text-xs text-subtle">
            {hint}
          </span>
        )
      )}
    </div>
  )
}
