import { useId, type SelectHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'
import { Icon } from './Icon'
import { fieldClass, labelClass } from './fieldStyles'

export interface SelectOption {
  value: string | number
  label: string
  disabled?: boolean
}

interface SelectProps extends SelectHTMLAttributes<HTMLSelectElement> {
  label: string
  options: SelectOption[]
  placeholder?: string
  error?: string
}

/** Ex.: escolher o imóvel ao criar um contrato. */
export function Select({ label, options, placeholder, error, id, className, ...props }: SelectProps) {
  const generatedId = useId()
  const selectId = id ?? generatedId

  return (
    <div className="flex flex-col gap-1.5">
      <label htmlFor={selectId} className={labelClass}>
        {label}
      </label>
      <div className="relative">
        <select
          id={selectId}
          aria-invalid={!!error}
          className={cn(fieldClass(!!error), 'cursor-pointer appearance-none pr-9', className)}
          {...props}
        >
          {placeholder !== undefined && <option value="">{placeholder}</option>}
          {options.map((opt) => (
            <option key={opt.value} value={opt.value} disabled={opt.disabled}>
              {opt.label}
            </option>
          ))}
        </select>
        <Icon
          name="chevronDown"
          size={16}
          className="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-[#352b4f]"
        />
      </div>
      {error && <span className="text-xs text-danger">{error}</span>}
    </div>
  )
}
