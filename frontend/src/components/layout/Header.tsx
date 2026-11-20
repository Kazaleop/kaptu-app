import { useLocation } from 'react-router-dom'
import { useAlertasModal } from '@/components/alertas/alertasModalContext'
import { Icon } from '@/components/ui'
import { appConfig } from '@/config/app'
import { useAlertas } from '@/hooks'
import { tituloDaRota } from '@/routes/paths'

/** Cabeçalho fixo: título da página atual, sino de alertas e organização. */
export function Header() {
  const { pathname } = useLocation()
  const { naoLidos } = useAlertas()
  const { abrirAlertas } = useAlertasModal()

  return (
    <header className="sticky top-0 z-10 flex h-20 items-center justify-between border-b border-[#e5e0ef] bg-white/80 px-10 backdrop-blur">
      <div>
        <p className="text-[10px] leading-[15px] font-semibold tracking-[1.6px] text-subtle">KAPTU</p>
        <h1 className="text-lg leading-7 font-semibold tracking-[-0.36px] text-text">
          {tituloDaRota(pathname)}
        </h1>
      </div>

      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={abrirAlertas}
          aria-label={naoLidos > 0 ? `Abrir alertas (${naoLidos} novos)` : 'Abrir alertas'}
          className="relative grid size-10 cursor-pointer place-items-center rounded-full border border-[#e5e0ef] bg-white text-muted transition-colors hover:bg-primary-soft"
        >
          <Icon name="bell" size={18} />
          {naoLidos > 0 && (
            <span
              className="absolute top-[7px] left-[25px] size-2 rounded-full border-2 border-white bg-alert-orange"
              aria-hidden="true"
            />
          )}
        </button>
        <span className="rounded-full bg-chip px-3 py-2 text-xs leading-4 text-[#756d8a]">
          {appConfig.organizacao}
        </span>
      </div>
    </header>
  )
}
