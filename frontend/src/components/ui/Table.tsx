import type { HTMLAttributes, TdHTMLAttributes, ThHTMLAttributes } from 'react'
import { cn } from '@/utils/cn'

/**
 * Peças de tabela no estilo do design (usadas em Imóveis, Documentos e Transações).
 * Uso:
 *   <Table>
 *     <thead><tr><Th>Imóvel</Th></tr></thead>
 *     <tbody><Tr><Td>...</Td></Tr></tbody>
 *   </Table>
 */
export function Table({ className, ...props }: HTMLAttributes<HTMLTableElement>) {
  return (
    <div className="w-full overflow-x-auto">
      <table className={cn('w-full border-collapse text-left', className)} {...props} />
    </div>
  )
}

export function Th({ className, ...props }: ThHTMLAttributes<HTMLTableCellElement>) {
  return (
    <th
      scope="col"
      className={cn(
        'border-y border-divider bg-[#faf8fd] px-6 py-3 text-[10px] leading-[15px] font-semibold tracking-[1.2px] whitespace-nowrap text-subtle uppercase',
        className,
      )}
      {...props}
    />
  )
}

interface TrProps extends HTMLAttributes<HTMLTableRowElement> {
  /** Deixa a linha clicável (cursor + destaque ao passar o mouse) */
  clickable?: boolean
}

export function Tr({ className, clickable, ...props }: TrProps) {
  return (
    <tr
      className={cn(
        'border-b border-divider last:border-b-0',
        clickable && 'cursor-pointer transition-colors hover:bg-[#faf8fd]',
        className,
      )}
      {...props}
    />
  )
}

export function Td({ className, ...props }: TdHTMLAttributes<HTMLTableCellElement>) {
  return <td className={cn('px-6 py-4 text-sm text-text', className)} {...props} />
}
