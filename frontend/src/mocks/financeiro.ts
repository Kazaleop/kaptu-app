import type { TransacaoDTO } from '@/types'

/**
 * Transações de exemplo no formato do TransacaoRespostaDTO do backend
 * (as 4 primeiras são as do Figma). Em memória: anexar um comprovante
 * muda o status até recarregar a página.
 */
const base: Omit<TransacaoDTO, 'id'>[] = [
  { descricao: 'Manutenção predial', dataTransacao: '2026-09-18', valor: 8420, tipoTransacao: 'SAIDA', statusConciliacao: 'CONCILIADA', documentoId: 2 },
  { descricao: 'Serviço de vigilância', dataTransacao: '2026-09-17', valor: 12180, tipoTransacao: 'SAIDA', statusConciliacao: 'CONCILIADA', documentoId: 6 },
  { descricao: 'Material de expediente', dataTransacao: '2026-09-16', valor: 1347.8, tipoTransacao: 'SAIDA', statusConciliacao: 'PENDENTE', documentoId: null },
  { descricao: 'Reparo hidráulico', dataTransacao: '2026-09-15', valor: 3290, tipoTransacao: 'SAIDA', statusConciliacao: 'CONCILIADA', documentoId: 10 },
]

const descricoes = [
  ['Aluguel — Casa Institucional 037', 'ENTRADA'],
  ['Conta de energia', 'SAIDA'],
  ['Aluguel — Loja Comercial 015', 'ENTRADA'],
  ['Limpeza e conservação', 'SAIDA'],
  ['Internet e telefonia', 'SAIDA'],
  ['Aluguel — Edifício Sede, Sala 204', 'ENTRADA'],
  ['Honorários contábeis', 'SAIDA'],
] as const

const gerados: Omit<TransacaoDTO, 'id'>[] = Array.from({ length: 26 }, (_, i) => {
  const [descricao, tipoTransacao] = descricoes[i % descricoes.length]
  const data = new Date(2026, 8, 14 - Math.floor(i * 1.5))
  const pendente = i % 13 === 5
  return {
    descricao,
    dataTransacao: `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}`,
    valor: tipoTransacao === 'ENTRADA' ? 2900 + (i % 5) * 650 : 380 + ((i * 731) % 4200),
    tipoTransacao,
    statusConciliacao: pendente ? 'PENDENTE' : 'CONCILIADA',
    documentoId: pendente ? null : 100 + i,
  }
})

/** Ids decrescentes a partir de 928 (o Figma mostra #TRX-0928, 0927...). */
export const mockTransacoes: TransacaoDTO[] = [...base, ...gerados].map((t, i) => ({
  ...t,
  id: 928 - i,
}))
