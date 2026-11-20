/**
 * Ponto único de leitura das variáveis de ambiente.
 * O resto do código importa daqui, nunca direto de import.meta.env.
 */
const apiUrl = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/+$/, '')

export const env = {
  /** URL do backend, ex.: http://localhost:8080 */
  apiUrl,
  /** Prefixo dos endpoints definido no @RequestMapping do controller */
  apiBasePath: '/api/v1',
  /** Quando true, os serviços respondem com dados de exemplo */
  useMocks: import.meta.env.VITE_USE_MOCKS === 'true',
} as const
