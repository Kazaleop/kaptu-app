import { Icon, type IconName } from './Icon'
import { Skeleton } from './Skeleton'
import { cn } from '@/utils/cn'

interface MetricCardProps {
  label: string
  icon: IconName
  /** Classe de fundo do quadradinho do ícone, ex.: "bg-icon-violet" */
  iconBg: string
  /** Classe de cor do ícone */
  iconColor: string
  value?: string
  detail?: string
  isLoading?: boolean
  isError?: boolean
}

/** Card de indicador (Receita no mês, Imóveis ativos, Documentos, Contratos a vencer). */
export function MetricCard({
  label,
  icon,
  iconBg,
  iconColor,
  value,
  detail,
  isLoading,
  isError,
}: MetricCardProps) {
  return (
    <article className="flex flex-col rounded-2xl border border-border bg-surface p-5 shadow-card">
      <div className={cn('grid size-10 place-items-center rounded-xl', iconBg, iconColor)}>
        <Icon name={icon} size={19} />
      </div>
      <p className="mt-5 text-xs leading-4 font-medium text-[#7d758f]">{label}</p>

      {isLoading ? (
        <>
          <Skeleton className="mt-2 h-7 w-28" />
          <Skeleton className="mt-3 h-3 w-36" />
        </>
      ) : isError ? (
        <>
          <p className="mt-1 text-2xl leading-8 font-semibold text-subtle">—</p>
          <p className="mt-2 text-[11px] leading-4 text-danger">Não foi possível carregar</p>
        </>
      ) : (
        <>
          <p className="mt-1 text-2xl leading-8 font-semibold tracking-[-0.96px] text-text">{value}</p>
          <p className="mt-2 text-[11px] leading-4 text-[#8a829a]">{detail}</p>
        </>
      )}
    </article>
  )
}
