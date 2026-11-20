import { iniciais } from '@/utils/format'

/** Bolinha com as iniciais do nome (coluna Locatário). */
export function Avatar({ nome }: { nome: string }) {
  return (
    <span
      className="grid size-7 shrink-0 place-items-center rounded-full bg-[#efe9fb] text-[9px] font-semibold text-primary-text"
      aria-hidden="true"
    >
      {iniciais(nome)}
    </span>
  )
}
