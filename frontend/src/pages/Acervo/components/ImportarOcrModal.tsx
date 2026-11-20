import { useState, type FormEvent } from 'react'
import { Badge, Button, FileDropzone, FormError, Modal, Select } from '@/components/ui'
import { upload } from '@/config/app'
import { useImoveis, useImportarDocumento } from '@/hooks'
import { TipoDocumento, type DocumentoDTO } from '@/types'
import { statusClassificacaoLabel, tipoDocumentoLabel } from '@/utils/labels'

interface ImportarOcrModalProps {
  open: boolean
  onClose(): void
}

const opcoesTipo = Object.values(TipoDocumento).map((t) => ({ value: t, label: tipoDocumentoLabel[t] }))

/**
 * Popup "Importar PDF via OCR" -> POST /api/v1/documentos/upload
 * O backend lê o arquivo na hora e já devolve o documento classificado.
 */
export function ImportarOcrModal({ open, onClose }: ImportarOcrModalProps) {
  return open ? <Formulario onClose={onClose} /> : null
}

function Formulario({ onClose }: { onClose(): void }) {
  const [arquivo, setArquivo] = useState<File | null>(null)
  const [imovelId, setImovelId] = useState('')
  const [tipo, setTipo] = useState<TipoDocumento | ''>('')
  const [resultado, setResultado] = useState<DocumentoDTO | null>(null)

  const importar = useImportarDocumento()
  const { data: imoveis = [] } = useImoveis()

  function enviar(e: FormEvent) {
    e.preventDefault()
    if (!arquivo) return
    importar.mutate(
      { arquivo, tipo: tipo || undefined, imovelId: imovelId ? Number(imovelId) : undefined },
      { onSuccess: setResultado },
    )
  }

  if (resultado) {
    const status = statusClassificacaoLabel[resultado.statusClassificacao]
    return (
      <Modal
        open
        onClose={onClose}
        title="Documento lido"
        description="A Kaptu extraiu e classificou os dados do arquivo."
        footer={
          <Button size="lg" onClick={onClose}>
            Concluir
          </Button>
        }
      >
        <dl className="grid grid-cols-2 gap-x-6 gap-y-4 py-2 text-sm">
          <div className="col-span-2">
            <dt className="text-xs text-subtle">Arquivo</dt>
            <dd className="mt-1 font-medium text-text">{resultado.titulo}</dd>
          </div>
          <div>
            <dt className="text-xs text-subtle">Tipo identificado</dt>
            <dd className="mt-1 text-text">{tipoDocumentoLabel[resultado.tipo] ?? resultado.tipo}</dd>
          </div>
          <div>
            <dt className="text-xs text-subtle">Status</dt>
            <dd className="mt-1">
              <Badge variant={status?.variant ?? 'neutral'}>{status?.label ?? resultado.statusClassificacao}</Badge>
            </dd>
          </div>
          {resultado.resumo && (
            <div className="col-span-2">
              <dt className="text-xs text-subtle">Resumo</dt>
              <dd className="mt-1 text-text">{resultado.resumo}</dd>
            </div>
          )}
        </dl>
      </Modal>
    )
  }

  return (
    <Modal
      open
      onClose={onClose}
      title="Importar PDF via OCR"
      description="A Kaptu irá ler, extrair e classificar os dados."
      footer={
        <Button
          type="submit"
          form="form-importar-ocr"
          size="lg"
          disabled={!arquivo}
          loading={importar.isPending}
          className="disabled:bg-[#b9a6e8] disabled:opacity-100"
        >
          {importar.isPending ? 'Lendo o documento...' : 'Enviar para leitura'}
        </Button>
      }
    >
      <form id="form-importar-ocr" onSubmit={enviar} noValidate className="flex flex-col gap-4">
        <FileDropzone
          file={arquivo}
          onChange={setArquivo}
          accept={upload.accept}
          maxMb={upload.maxMb}
          title="Selecione ou arraste um arquivo"
          hint={upload.descricao}
        />
        <div className="grid grid-cols-2 gap-4">
          {/* ⚠ O backend ainda não grava o imóvel no upload (campo enviado para quando existir) */}
          <Select
            label="Localização"
            placeholder="Selecione"
            value={imovelId}
            onChange={(e) => setImovelId(e.target.value)}
            options={imoveis.map((i) => ({ value: i.id, label: i.nome }))}
          />
          <Select
            label="Tipo"
            placeholder="Detectar automaticamente"
            value={tipo}
            onChange={(e) => setTipo(e.target.value as TipoDocumento | '')}
            options={opcoesTipo}
          />
        </div>
      </form>
      <FormError error={importar.error} />
    </Modal>
  )
}
