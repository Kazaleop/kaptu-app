import { Link } from 'react-router-dom'
import { Icon, Skeleton, type IconName } from '@/components/ui'

interface ModuleCardProps {
  to: string
  icon: IconName
  title: string
  description: string
  /** Ex.: "124 IMÓVEIS". undefined = carregando */
  statistic?: string
}

/** Card de atalho da seção "Módulos da gestão". */
export function ModuleCard({ to, icon, title, description, statistic }: ModuleCardProps) {
  return (
    <Link
      to={to}
      className="group flex items-start gap-4 rounded-2xl border border-border bg-surface p-6 shadow-card transition-shadow hover:shadow-[0_6px_20px_#4b22ba14]"
    >
      <div className="grid size-12 shrink-0 place-items-center rounded-2xl bg-[#eee9ff] text-primary">
        <Icon name={icon} size={20} />
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-base leading-6 font-semibold text-text">{title}</p>
        <p className="mt-1 text-xs leading-5 text-[#7d758f]">{description}</p>
        {statistic === undefined ? (
          <Skeleton className="mt-4 h-3 w-24" />
        ) : (
          <p className="mt-4 text-[11px] leading-4 font-semibold tracking-[0.88px] text-[#725ba7] uppercase">
            {statistic}
          </p>
        )}
      </div>
      <Icon
        name="chevronRight"
        size={18}
        className="mt-1 shrink-0 text-subtle transition-transform group-hover:translate-x-0.5"
      />
    </Link>
  )
}
