import { useMemo, useState } from 'react'
import { PageHeader } from '@/components/layout/PageHeader'
import { Button, Card, Icon, Input, Pagination, QueryState, SearchInput, Select } from '@/components/ui'
import { fieldClass } from '@/components/ui/fieldStyles'
import { useDebouncedValue, useDocumentos, useImoveis } from '@/hooks'
import { TipoDocumento } from '@/types'
import { cn } from '@/utils/cn'
import { tipoDocumentoLabel } from '@/utils/labels'
import { contemTermo, paginar } from '@/utils/lista'
import { DocumentosTable } from './components/DocumentosTable'
import { ImportarOcrModal } from './components/ImportarOcrModal'

const TAMANHO_PAGINA = 10

const opcoesTipo = Object.values(TipoDocumento).map((t) => ({ value: t, label: tipoDocumentoLabel[t] }))

/**
 * Tela 03 · Acervo de documentos (F-2, F-4, F-5, F-8) -> DocumentoController
 *  GET  /documentos/filtros  (tipo, imóvel e período são filtrados no backend)
 *  POST /documentos/upload   (OCR)
 * A busca por texto (título, resumo e conteúdo lido pelo OCR) e a paginação
 * são feitas aqui, porque o backend devolve a lista inteira.
 */
export function AcervoPage() {
  const [busca, setBusca] = useState('')
  const [tipo, setTipo] = useState<TipoDocumento | ''>('')
  const [imovelId, setImovelId] = useState('')
  const [dataInicio, setDataInicio] = useState('')
  const [dataFim, setDataFim] = useState('')
  const [pagina, setPagina] = useState(0)
  const [maisFiltros, setMaisFiltros] = useState(false)
  const [importar, setImportar] = useState(false)

  const buscaDebounced = useDebouncedValue(busca)
  const { data: imoveis = [] } = useImoveis()

  const { data: documentos, isLoading, error, refetch, isFetching } = useDocumentos({
    tipo: tipo || undefined,
    imovelId: imovelId ? Number(imovelId) : undefined,
    dataInicio: dataInicio || undefined,
    dataFim: dataFim || undefined,
  })

  const data = useMemo(() => {
    if (!documentos) return undefined
    const filtrados = documentos.filter((d) =>
      contemTermo(buscaDebounced, d.titulo, d.resumo, d.textoExtraido),
    )
    return paginar(filtrados, pagina, TAMANHO_PAGINA)
  }, [documentos, buscaDebounced, pagina])

  /** Qualquer filtro novo volta para a primeira página. */
  function filtrar<T>(setter: (v: T) => void) {
    return (v: T) => {
      setter(v)
      setPagina(0)
    }
  }

  const filtrosExtrasAtivos = [imovelId, dataInicio, dataFim].filter(Boolean).length

  function limparFiltrosExtras() {
    setImovelId('')
    setDataInicio('')
    setDataFim('')
    setPagina(0)
  }

  return (
    <div className="flex flex-col gap-8">
      <PageHeader
        eyebrow="Acervo digital"
        title="Documentos"
        description="Encontre notas, recibos, contratos e certidões por localização, data ou tipo."
        actions={
          <Button size="lg" onClick={() => setImportar(true)}>
            <Icon name="upload" size={16} /> Importar PDF via OCR
          </Button>
        }
      />

      <Card padded={false} className="overflow-hidden">
        <div className="flex flex-wrap items-center gap-3 p-5">
          <SearchInput
            label="Buscar documentos"
            placeholder="Buscar por nome, conteúdo ou palavra-chave..."
            value={busca}
            onChange={filtrar(setBusca)}
            className="min-w-[240px] flex-1"
          />
          <label className="relative w-full sm:w-[190px]">
            <span className="sr-only">Tipo de documento</span>
            <select
              value={tipo}
              onChange={(e) => filtrar(setTipo)(e.target.value as TipoDocumento | '')}
              className={cn(fieldClass(false), 'h-10 cursor-pointer appearance-none pr-9 text-sm')}
            >
              <option value="">Todos os tipos</option>
              {opcoesTipo.map((o) => (
                <option key={o.value} value={o.value}>
                  {o.label}
                </option>
              ))}
            </select>
            <Icon
              name="chevronDown"
              size={16}
              className="pointer-events-none absolute top-1/2 right-3 -translate-y-1/2 text-[#352b4f]"
            />
          </label>
          <Button
            variant="secondary"
            onClick={() => setMaisFiltros((v) => !v)}
            aria-expanded={maisFiltros}
            className="h-10 rounded-xl"
          >
            <Icon name="filter" size={16} />
            Mais filtros
            {filtrosExtrasAtivos > 0 && (
              <span className="grid size-5 place-items-center rounded-full bg-primary text-[10px] text-white">
                {filtrosExtrasAtivos}
              </span>
            )}
          </Button>
        </div>

        {maisFiltros && (
          <div className="grid grid-cols-1 gap-4 border-t border-divider bg-[#faf8fd] px-5 py-4 sm:grid-cols-[1fr_1fr_1fr_auto] sm:items-end">
            <Select
              label="Imóvel"
              placeholder="Todos"
              value={imovelId}
              onChange={(e) => filtrar(setImovelId)(e.target.value)}
              options={imoveis.map((i) => ({ value: i.id, label: i.nome }))}
            />
            <Input label="De" type="date" value={dataInicio} max={dataFim || undefined} onChange={(e) => filtrar(setDataInicio)(e.target.value)} />
            <Input label="Até" type="date" value={dataFim} min={dataInicio || undefined} onChange={(e) => filtrar(setDataFim)(e.target.value)} />
            <Button variant="ghost" onClick={limparFiltrosExtras} disabled={filtrosExtrasAtivos === 0} className="h-11">
              Limpar
            </Button>
          </div>
        )}

        <QueryState
          isLoading={isLoading}
          error={error}
          isEmpty={data?.content.length === 0}
          emptyMessage="Nenhum documento encontrado com esses filtros."
          onRetry={refetch}
        >
          <div className={cn('transition-opacity', isFetching && 'opacity-60')}>
            <DocumentosTable documentos={data?.content ?? []} />
          </div>
        </QueryState>

        {data && data.totalElements > 0 && (
          <Pagination
            pagina={data.number}
            totalPaginas={data.totalPages}
            totalItens={data.totalElements}
            itensNaPagina={data.content.length}
            rotulo="documentos"
            onChange={setPagina}
          />
        )}
      </Card>

      <ImportarOcrModal open={importar} onClose={() => setImportar(false)} />
    </div>
  )
}
