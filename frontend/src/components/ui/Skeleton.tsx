import { cn } from '@/utils/cn'

/** Bloco cinza pulsante exibido enquanto um dado carrega. */
export function Skeleton({ className }: { className?: string }) {
  return <span className={cn('block animate-pulse rounded-md bg-divider', className)} />
}
