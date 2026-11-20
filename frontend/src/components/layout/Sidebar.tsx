import { NavLink } from 'react-router-dom'
import { Icon, Skeleton } from '@/components/ui'
import { appConfig } from '@/config/app'
import { useDocumentosResumo } from '@/hooks'
import { menu } from '@/routes/paths'
import { cn } from '@/utils/cn'
import { formatNumber, iniciais } from '@/utils/format'

/** Menu lateral roxo (degradê) do design. */
export function Sidebar() {
  return (
    <aside className="bg-sidebar sticky top-0 flex h-screen w-[272px] shrink-0 flex-col p-5 text-white">
      <Logo />

      <nav aria-label="Navegação principal" className="pt-8">
        <p className="px-3 text-[10px] leading-[15px] font-semibold tracking-[1.8px] text-white/45">
          NAVEGAÇÃO
        </p>
        <ul className="mt-3 flex flex-col gap-1">
          {menu.map((item) => (
            <li key={item.to}>
              <NavLink
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  cn(
                    'flex h-11 items-center gap-3 rounded-xl px-3 text-[15px] transition-colors',
                    isActive
                      ? 'bg-white/15 text-white'
                      : 'text-white/75 hover:bg-white/8 hover:text-white',
                  )
                }
              >
                {({ isActive }) => (
                  <>
                    <Icon name={item.icon} size={19} />
                    <span className="flex-1">{item.label}</span>
                    {isActive && <span className="size-1.5 rounded-full bg-accent" aria-hidden="true" />}
                  </>
                )}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>

      <div className="mt-auto">
        <AcervoProgresso />
        <UsuarioRodape />
      </div>
    </aside>
  )
}

function Logo() {
  return (
    <div className="flex h-16 items-center gap-3 px-2">
      <div className="relative">
        <span className="text-[25px] leading-[37.5px] font-semibold tracking-[-1px]">kaptu</span>
        <span
          className="absolute -top-px -right-3.5 size-3 rounded-full bg-accent shadow-[0_0_0_5px_#ffd42a24]"
          aria-hidden="true"
        />
      </div>
      <div className="ml-5 border-l border-white/20 pl-3 text-[9px] leading-[12px] font-medium tracking-[0.72px] text-white/55">
        SISTEMA
        <br />
        INTEGRADO
      </div>
    </div>
  )
}

/**
 * Progresso do acervo: documentos classificados pelo OCR / total enviado.
 * (O Figma diz "Acervo digitalizado", mas o backend não sabe quantos documentos
 * físicos existem; por isso o texto mostra "classificado".)
 */
function AcervoProgresso() {
  const { data, isLoading, isError } = useDocumentosResumo()
  if (isError) return null

  const percentual = data && data.total > 0 ? Math.round((data.classificados / data.total) * 100) : 0

  return (
    <section aria-label="Progresso do acervo classificado" className="rounded-2xl bg-white/6 p-4">
      <div className="flex items-center justify-between text-xs leading-4">
        <span className="text-white/60">Acervo classificado</span>
        {isLoading ? <Skeleton className="h-3 w-8 bg-white/20" /> : <span>{percentual}%</span>}
      </div>
      <div
        className="mt-2 h-1.5 overflow-hidden rounded-full bg-white/10"
        role="progressbar"
        aria-valuenow={percentual}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label="Acervo classificado"
      >
        <div
          className="h-full rounded-full bg-accent transition-[width] duration-500"
          style={{ width: `${percentual}%` }}
        />
      </div>
      <p className="mt-3 text-[11px] leading-4 text-white/45">
        {data
          ? `${formatNumber(data.classificados)} de ${formatNumber(data.total)} documentos`
          : ' '}
      </p>
    </section>
  )
}

/** Nome e cargo vêm de src/config/app.ts (o sistema não tem login). */
function UsuarioRodape() {
  const { nome, cargo } = appConfig.usuario
  if (!nome) return null

  return (
    <div className="mt-4 flex items-center gap-3 border-t border-white/10 px-2 pt-5">
      <div className="grid size-9 place-items-center rounded-full bg-[#6549b8] text-xs font-semibold">
        {iniciais(nome)}
      </div>
      <div className="min-w-0">
        <p className="truncate text-sm leading-5 font-medium">{nome}</p>
        {cargo && <p className="truncate text-[11px] leading-4 text-white/45">{cargo}</p>}
      </div>
    </div>
  )
}
