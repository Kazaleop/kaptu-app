import type { PageDTO } from '@/types'

/** Remove acentos e caixa para buscas: "Certidão" encontra "certidao". */
export function normalizar(texto: string | null | undefined): string {
  return (texto ?? '')
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .trim()
}

/** true se algum dos campos contém o termo (ignorando acentos e caixa). */
export function contemTermo(termo: string, ...campos: (string | null | undefined)[]): boolean {
  const alvo = normalizar(termo)
  return !alvo || campos.some((c) => normalizar(c).includes(alvo))
}

/**
 * Monta uma página a partir da lista inteira (o backend não pagina).
 * Se a página pedida não existe mais (ex.: a lista diminuiu), volta para a última.
 */
export function paginar<T>(lista: T[], pagina: number, tamanho: number): PageDTO<T> {
  const totalPages = Math.max(1, Math.ceil(lista.length / tamanho))
  const number = Math.min(Math.max(0, pagina), totalPages - 1)
  return {
    content: lista.slice(number * tamanho, number * tamanho + tamanho),
    totalElements: lista.length,
    totalPages,
    number,
    size: tamanho,
  }
}
