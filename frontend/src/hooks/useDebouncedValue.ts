import { useEffect, useState } from 'react'

/**
 * Devolve o valor só depois que ele para de mudar por `ms` milissegundos.
 * Usado na busca: evita uma chamada ao backend a cada tecla.
 */
export function useDebouncedValue<T>(value: T, ms = 350): T {
  const [debounced, setDebounced] = useState(value)
  useEffect(() => {
    const t = setTimeout(() => setDebounced(value), ms)
    return () => clearTimeout(t)
  }, [value, ms])
  return debounced
}
