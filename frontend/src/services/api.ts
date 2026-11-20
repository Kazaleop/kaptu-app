import axios, { AxiosError } from 'axios'
import { env } from '@/config/env'

/**
 * Instância única do Axios usada por todos os serviços.
 * No projeto modelo o API_URL era repetido em cada hook; aqui fica num lugar só.
 */
export const api = axios.create({
  baseURL: `${env.apiUrl}${env.apiBasePath}`,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
})

/** Erro padronizado que as telas recebem, independente da origem. */
export class ApiError extends Error {
  status?: number

  constructor(message: string, status?: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

/**
 * Corpo de erro do backend: web/dto/ErroRespostaDTO.java
 * { dataHora, status, erro, mensagem, caminho } (GlobalExceptionHandler).
 * "message" fica como reserva para erros padrão do Spring.
 */
interface ErroResposta {
  mensagem?: string
  erro?: string
  message?: string
}

const fallback: Record<number, string> = {
  400: 'Dados inválidos. Revise os campos.',
  404: 'Registro não encontrado.',
  413: 'Arquivo grande demais para o servidor.',
  500: 'Erro interno no servidor.',
}

async function toApiError(error: AxiosError<ErroResposta | Blob>): Promise<ApiError> {
  if (!error.response) {
    return new ApiError(
      'Não foi possível conectar ao servidor. Verifique se o backend está rodando.',
    )
  }

  const { status } = error.response
  let data = error.response.data

  // Download de arquivo (responseType: 'blob'): o erro chega como Blob com JSON dentro
  if (data instanceof Blob) {
    try {
      data = JSON.parse(await data.text()) as ErroResposta
    } catch {
      data = {}
    }
  }

  const corpo = (data ?? {}) as ErroResposta
  const message = corpo.mensagem || corpo.message || fallback[status] || corpo.erro || 'Erro inesperado.'
  return new ApiError(message, status)
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ErroResposta | Blob>) => Promise.reject(await toApiError(error)),
)
