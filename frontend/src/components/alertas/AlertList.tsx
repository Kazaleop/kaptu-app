import type { AlertaDTO } from '@/types'
import { cn } from '@/utils/cn'
import { formatDate } from '@/utils/format'
import { tipoAlertaLabel } from '@/utils/labels'

/** O backend escreve datas como "2026-11-20" na mensagem: mostramos "20/11/2026". */
function formatarMensagem(mensagem: string): string {
  return mensagem.replace(/\b\d{4}-\d{2}-\d{2}\b/g, (iso) => formatDate(iso))
}

/** Lista de alertas usada no card do dashboard e no popup. */
export function AlertList({ alertas }: { alertas: AlertaDTO[] }) {
  return (
    <ul className="w-full">
      {alertas.map((alerta) => {
        const tipo = tipoAlertaLabel[alerta.tipo]
        return (
          <li
            key={alerta.id}
            className="flex items-start gap-3 border-b border-divider py-4 last:border-b-0"
          >
            <span
              className={cn(
                'mt-1.5 size-2 shrink-0 rounded-full',
                tipo?.cor ?? 'bg-alert-purple',
                alerta.lido && 'opacity-40',
              )}
              aria-hidden="true"
            />
            <div className="min-w-0">
              <p className={cn('text-sm leading-5 font-medium', alerta.lido ? 'text-muted' : 'text-text')}>
                {tipo?.titulo ?? 'Alerta'}
              </p>
              <p className="mt-1 text-xs leading-4 text-[#867e94]">{formatarMensagem(alerta.mensagem)}</p>
            </div>
          </li>
        )
      })}
    </ul>
  )
}
