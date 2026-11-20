import type { DocumentoDTO, FiltrosDocumentosApi, TipoDocumento } from '@/types'

/**
 * Acervo de exemplo (os 4 primeiros são os do Figma), no formato do DocumentoDTO do backend.
 * Fica em memória: um PDF "importado" aparece na lista até recarregar a página.
 */
const base: Omit<DocumentoDTO, 'id'>[] = [
  { titulo: 'Contrato de locação — Sala 204', tipo: 'CONTRATO', dataEmissao: '2026-09-18', valorReferencia: 4800, statusClassificacao: 'CLASSIFICADO', resumo: 'Documento de contrato - válido até 18/09/2027', textoExtraido: null, imovelId: 1, imovelNome: 'Edifício Sede — Sala 204', dataValidade: '2027-09-18', criadoEm: '2026-09-18T10:00:00', possuiArquivo: true },
  { titulo: 'Nota fiscal — Manutenção elétrica', tipo: 'NOTA_FISCAL', dataEmissao: '2026-09-16', valorReferencia: 8420, statusClassificacao: 'CLASSIFICADO', resumo: 'Nota fiscal 0928 - Valor: 8420.00', textoExtraido: null, imovelId: 1, imovelNome: 'Edifício Sede — Sala 204', dataValidade: null, criadoEm: '2026-09-16T10:00:00', possuiArquivo: true },
  { titulo: 'Certidão negativa municipal', tipo: 'CERTIDAO', dataEmissao: '2026-09-12', valorReferencia: null, statusClassificacao: 'REVISAO_MANUAL', resumo: 'Certidão MUNICIPAL - Válida até 2026-12-12', textoExtraido: null, imovelId: 3, imovelNome: 'Loja Comercial 015', dataValidade: '2026-12-12', criadoEm: '2026-09-12T10:00:00', possuiArquivo: true },
  { titulo: 'Recibo de aluguel — Setembro', tipo: 'RECIBO', dataEmissao: '2026-09-10', valorReferencia: 2900, statusClassificacao: 'CLASSIFICADO', resumo: 'Recibo - Loja Comercial 015 - Valor: 2900.00', textoExtraido: null, imovelId: 3, imovelNome: 'Loja Comercial 015', dataValidade: null, criadoEm: '2026-09-10T10:00:00', possuiArquivo: true },
]

const nomes: Record<TipoDocumento, string[]> = {
  CONTRATO: ['Contrato de locação', 'Aditivo contratual', 'Contrato de prestação de serviço'],
  NOTA_FISCAL: ['Nota fiscal — Material de expediente', 'Nota fiscal — Serviço de vigilância', 'Nota fiscal — Reparo hidráulico'],
  RECIBO: ['Recibo de aluguel', 'Recibo de condomínio'],
  CERTIDAO: ['Certidão negativa de débitos', 'Certidão de matrícula do imóvel'],
}
const tipos = Object.keys(nomes) as TipoDocumento[]

const nomesImoveis = ['Edifício Sede — Sala 204', 'Casa Institucional 037', 'Loja Comercial 015', 'Galpão 078']

const gerados: Omit<DocumentoDTO, 'id'>[] = Array.from({ length: 38 }, (_, i) => {
  const tipo = tipos[i % tipos.length]
  const lista = nomes[tipo]
  const data = new Date(2026, 8, 9 - i * 3)
  const valor = tipo === 'CERTIDAO' ? null : 300 + ((i * 937) % 7000)
  const imovelId = (i % 4) + 1
  return {
    titulo: `${lista[i % lista.length]} — ${String(100 + i).padStart(4, '0')}`,
    tipo,
    dataEmissao: `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}`,
    valorReferencia: valor,
    statusClassificacao: i % 7 === 3 ? 'REVISAO_MANUAL' : 'CLASSIFICADO',
    resumo: null,
    textoExtraido: null,
    imovelId,
    imovelNome: nomesImoveis[imovelId - 1],
    dataValidade: tipo === 'CERTIDAO' ? `${data.getFullYear() + 1}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}` : null,
    criadoEm: `${data.getFullYear()}-${String(data.getMonth() + 1).padStart(2, '0')}-${String(data.getDate()).padStart(2, '0')}T10:00:00`,
    possuiArquivo: true,
  }
})

export const mockDocumentos: DocumentoDTO[] = [...base, ...gerados].map((d, i) => ({ ...d, id: i + 1 }))

export function filtrarMockDocumentos(f: FiltrosDocumentosApi): DocumentoDTO[] {
  return mockDocumentos.filter(
    (d) =>
      (!f.tipo || d.tipo === f.tipo) &&
      (!f.imovelId || d.imovelId === f.imovelId) &&
      (!f.dataInicio || (d.dataEmissao !== null && d.dataEmissao >= f.dataInicio)) &&
      (!f.dataFim || (d.dataEmissao !== null && d.dataEmissao <= f.dataFim)) &&
      (f.valorMinimo === undefined || (d.valorReferencia !== null && d.valorReferencia >= f.valorMinimo)) &&
      (f.valorMaximo === undefined || (d.valorReferencia !== null && d.valorReferencia <= f.valorMaximo)),
  )
}
