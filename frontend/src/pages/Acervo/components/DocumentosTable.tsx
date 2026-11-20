import { Badge, Icon, Table, Td, Th, Tr } from '@/components/ui'
import type { DocumentoDTO, ImovelDTO } from '@/types'
import { formatCurrency, formatDataCurta } from '@/utils/format'
import { statusClassificacaoLabel, tipoDocumentoLabel } from '@/utils/labels'

/**
 * Tabela DOCUMENTO · TIPO · LOCALIZAÇÃO · DATA · VALOR · STATUS
 * A localização vem do imovelId vinculado no upload do documento.
 */
export function DocumentosTable({
  documentos,
  imoveis,
}: {
  documentos: DocumentoDTO[]
  imoveis: ImovelDTO[]
}) {
  const nomeImovel = new Map(imoveis.map((i) => [i.id, i.nome]))

  return (
    <Table>
      <thead>
        <tr>
          <Th>Documento</Th>
          <Th>Tipo</Th>
          <Th>Localização</Th>
          <Th>Data</Th>
          <Th>Valor</Th>
          <Th>Status</Th>
        </tr>
      </thead>
      <tbody>
        {documentos.map((doc) => {
          const status = statusClassificacaoLabel[doc.statusClassificacao]
          return (
            <Tr key={doc.id}>
              <Td>
                <span className="flex items-center gap-3">
                  <span className="grid size-8 shrink-0 place-items-center rounded-lg bg-[#efe9fb] text-primary-text">
                    <Icon name="document" size={16} />
                  </span>
                  <span className="min-w-0">
                    <span className="block truncate font-medium">{doc.titulo}</span>
                    {doc.resumo && (
                      <span className="mt-0.5 block max-w-[360px] truncate text-xs text-subtle" title={doc.resumo}>
                        {doc.resumo}
                      </span>
                    )}
                  </span>
                </span>
              </Td>
              <Td className="text-muted">{tipoDocumentoLabel[doc.tipo] ?? doc.tipo}</Td>
              <Td className="whitespace-nowrap text-muted">
                {doc.imovelId != null ? (nomeImovel.get(doc.imovelId) ?? '—') : '—'}
              </Td>
              <Td className="whitespace-nowrap text-muted">
                {doc.dataEmissao ? formatDataCurta(doc.dataEmissao) : '—'}
              </Td>
              <Td className="whitespace-nowrap text-muted">
                {doc.valorReferencia != null ? formatCurrency(doc.valorReferencia) : '—'}
              </Td>
              <Td>
                <Badge variant={status?.variant ?? 'neutral'}>{status?.label ?? doc.statusClassificacao}</Badge>
              </Td>
            </Tr>
          )
        })}
      </tbody>
    </Table>
  )
}
