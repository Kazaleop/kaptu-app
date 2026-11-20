const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })

/** 1500 -> "R$ 1.500,00" */
export function formatCurrency(value: number | null | undefined): string {
  return value == null ? '—' : moeda.format(value)
}

const moedaCompacta = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  notation: 'compact',
  maximumFractionDigits: 1,
})
const numero = new Intl.NumberFormat('pt-BR')
const numeroCompacto = new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 })
const percentual = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 1 })

/** 428600 -> "R$ 428,6 mil" */
export function formatCurrencyCompact(value: number): string {
  return moedaCompacta.format(value)
}

const moedaInteira = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
  maximumFractionDigits: 0,
})

/** 4800 -> "R$ 4.800" | 1347.8 -> "R$ 1.347,80" (sem centavos quando é valor redondo) */
export function formatCurrencySmart(value: number): string {
  return Number.isInteger(value) ? moedaInteira.format(value) : moeda.format(value)
}

/** Dias inteiros de hoje até a data "AAAA-MM-DD" (negativo se já passou). */
export function diasAte(isoDate: string, hoje = new Date()): number {
  const [a, m, d] = isoDate.slice(0, 10).split('-').map(Number)
  const alvo = Date.UTC(a, m - 1, d)
  const base = Date.UTC(hoje.getFullYear(), hoje.getMonth(), hoje.getDate())
  return Math.round((alvo - base) / 86_400_000)
}

/** Meses completos entre duas datas "AAAA-MM-DD" (ex.: 01/06 -> 01/06 do ano seguinte = 12). */
export function mesesEntre(inicioIso: string, fimIso: string): number {
  const [a1, m1, d1] = inicioIso.split('-').map(Number)
  const [a2, m2, d2] = fimIso.split('-').map(Number)
  return (a2 - a1) * 12 + (m2 - m1) - (d2 < d1 ? 1 : 0)
}

/** "2026-06-01" + 12 meses -> "2027-06-01" (mesma regra do LocalDate.plusMonths do Java) */
export function somarMeses(isoDate: string, meses: number): string {
  const [a, m, d] = isoDate.split('-').map(Number)
  const ultimoDia = new Date(a, m - 1 + meses + 1, 0).getDate()
  return toIsoDate(new Date(a, m - 1 + meses, Math.min(d, ultimoDia)))
}

/** 12400 -> "12.400" */
export function formatNumber(value: number): string {
  return numero.format(value)
}

/** 12400 -> "12,4 mil" */
export function formatNumberCompact(value: number): string {
  return numeroCompacto.format(value)
}

/** 8.4 -> "+8,4%" | -3 -> "-3%" */
export function formatPercentSigned(value: number): string {
  return `${value > 0 ? '+' : ''}${percentual.format(value)}%`
}

const capitalize = (s: string) => s.charAt(0).toUpperCase() + s.slice(1)

/** "2026-04" -> "Abr" */
export function formatMesCurto(anoMes: string): string {
  const [ano, mes] = anoMes.split('-').map(Number)
  const nome = new Date(ano, mes - 1, 1).toLocaleDateString('pt-BR', { month: 'short' })
  return capitalize(nome.replace('.', ''))
}

/** "2026-09-18" -> "18 set. 2026" (formato das tabelas do design) */
export function formatDataCurta(isoDate: string): string {
  const [a, m, d] = isoDate.slice(0, 10).split('-').map(Number)
  const mes = new Date(a, m - 1, 1).toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')
  return `${d} ${mes}. ${a}`
}

/** 2_500_000 -> "2,4 MB" */
export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 ** 2) return `${percentual.format(bytes / 1024)} KB`
  return `${percentual.format(bytes / 1024 ** 2)} MB`
}

/** Date -> "Segunda-feira, 22 de setembro" */
export function formatDataExtenso(date: Date): string {
  return capitalize(
    date.toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' }),
  )
}

/** "Bom dia" / "Boa tarde" / "Boa noite" conforme a hora */
export function saudacao(date: Date): string {
  const h = date.getHours()
  if (h < 12) return 'Bom dia'
  if (h < 18) return 'Boa tarde'
  return 'Boa noite'
}

/** "Marina Costa" -> "MC" */
export function iniciais(nome: string): string {
  const partes = nome.trim().split(/\s+/)
  return ((partes[0]?.[0] ?? '') + (partes.length > 1 ? partes[partes.length - 1][0] : '')).toUpperCase()
}

/** "2026-11-11" (LocalDate) -> "11/11/2026" sem erro de fuso horário */
export function formatDate(isoDate: string | null | undefined): string {
  if (!isoDate) return '—'
  const [ano, mes, dia] = isoDate.slice(0, 10).split('-')
  return `${dia}/${mes}/${ano}`
}

/** Date -> "AAAA-MM-DD" no fuso local (formato que o LocalDate espera) */
export function toIsoDate(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

export function onlyDigits(value: string): string {
  return value.replace(/\D/g, '')
}

/** "12345678901" -> "123.456.789-01" | 14 dígitos -> "12.345.678/0001-90" */
export function formatCpfCnpj(value: string): string {
  const d = onlyDigits(value)
  if (d.length === 11) return d.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4')
  if (d.length === 14) return d.replace(/(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, '$1.$2.$3/$4-$5')
  return value
}

/** "79999990000" -> "(79) 99999-0000" */
export function formatTelefone(value: string): string {
  const d = onlyDigits(value)
  if (d.length === 11) return d.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3')
  if (d.length === 10) return d.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3')
  return value
}
