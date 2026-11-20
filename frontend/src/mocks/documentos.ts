import type { DocumentoDTO, FiltrosDocumentosApi, TipoDocumento } from '@/types'

/**
 * Acervo de exemplo (os 4 primeiros são os do Figma), no formato do DocumentoDTO do backend.
 * Fica em memória: um PDF "importado" aparece na lista até recarregar a página.
 */
const base: Omit<DocumentoDTO, 'id'>[] = [
  { titulo: 'Contrato de locação — Sala 204', tipo: 'CONTRATO', dataEmissao: '2026-09-18', valorReferencia: 4800, statusClassificacao: 'CLASSIFICADO', resumo: 'Documento de contrato - válido até 18/09/2027', textoExtraido: null },
  { titulo: 'Nota fiscal — Manutenção elétrica', tipo: 'NOTA_FISCAL', dataEmissao: '2026-09-16', valorReferencia: 8420, statusClassificacao: 'CLASSIFICADO', resumo: 'Nota fiscal 0928 - Valor: 8420.00', textoExtraido: null },
  { titulo: 'Certidão negativa municipal', tipo: 'CERTIDAO', dataEmissao: '2026-09-12', valorReferencia: null, statusClassificacao: 'REVISAO_MANUAL', resumo: 'Certidão MUNICIPAL - Válida até 2026-12-12', textoExtraido: null },
  { titulo: 'Recibo de aluguel — Setembro', tipo: 'RECIBO', dataEmissao: '2026-09-10', valorReferencia: 2900, statusClassificacao: 'CLASSIFICADO', resumo: 'Recibo - Loja Comercial 015 - Valor: 2900.00', textoExtraido: null },
]

const nomes: Record<TipoDocumento, string[]> = {
  CONTRATO: ['Contrato de locação', 'Aditivo contratual', 'Contrato de prestação de serviço'],
  NOTA_FISCAL: ['Nota fiscal — Material de expediente', 'Nota fiscal — Serviço de vigilância', 'Nota fiscal — Reparo hidráulico'],
  RECIBO: ['Recibo de aluguel', 'Recibo de condomínio'],
  CERTIDAO: ['Certidão negativa de débitos', 'Certidão de matrícula do imóvel'],
}
const tipos = Object.keys(nomes) as TipoDocumento[]

const gerados: Omit<DocumentoDTO, 'id'>[] = Array.from({ length: 38 }, (_, i) => {
  const tipo = tipos[i % tipos.length]
  const lista = nomes[tipo]
  const data = new Date(2026, 8, 9 - i * 3)
  const valor = tipo === 'CERTIDAO' ? null : 300 + ((i * 937) % 7000)
  return {
    titulo: `${lista[i % lista.length]} — ${String(100 + i).padStart(4, '0')}`,
    tipo,
    dataEmissao: `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}`,
    valorReferencia: valor,
    statusClassificacao: i % 7 === 3 ? 'REVISAO_MANUAL' : 'CLASSIFICADO',
    resumo: null,
    textoExtraido: null,
  }
})

export const mockDocumentos: DocumentoDTO[] = [...base, ...gerados].map((d, i) => ({ ...d, id: i + 1 }))

/** Imita GET /documentos/filtros (o filtro por imóvel não tem efeito nos dados de exemplo). */
export function filtrarMockDocumentos(f: FiltrosDocumentosApi): DocumentoDTO[] {
  return mockDocumentos.filter(
    (d) =>
      (!f.tipo || d.tipo === f.tipo) &&
      (!f.dataInicio || (d.dataEmissao !== null && d.dataEmissao >= f.dataInicio)) &&
      (!f.dataFim || (d.dataEmissao !== null && d.dataEmissao <= f.dataFim)),
  )
}
