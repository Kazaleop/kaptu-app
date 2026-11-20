import { useState, type FormEvent } from 'react'
import { Button, FileDropzone, FormError, Modal, Select } from '@/components/ui'
import { upload } from '@/config/app'
import { useAnexarComprovante, useTransacoes } from '@/hooks'
import { codigoTransacao } from '@/services'
import { formatCurrency } from '@/utils/format'

interface AnexarComprovanteModalProps {
  open: boolean
  onClose(): void
  /** Transação já escolhida (ao clicar em "Anexar" numa linha) */
  transacaoInicial?: number
}

/**
 * Popup "Anexar comprovante". No backend são 2 passos:
 * POST /documentos/upload (OCR do arquivo) e POST /financeiro/conciliar.
 */
export function AnexarComprovanteModal({ open, onClose, transacaoInicial }: AnexarComprovanteModalProps) {
  return open ? <Formulario onClose={onClose} transacaoInicial={transacaoInicial} /> : null
}

function Formulario({ onClose, transacaoInicial }: Omit<AnexarComprovanteModalProps, 'open'>) {
  // Só transações sem comprovante aparecem no seletor
  const transacoes = useTransacoes()
  const pendentes = (transacoes.data ?? []).filter((t) => t.statusConciliacao === 'PENDENTE')
  const anexar = useAnexarComprovante()

  const [escolhida, setEscolhida] = useState<string>(transacaoInicial ? String(transacaoInicial) : '')
  const [arquivo, setArquivo] = useState<File | null>(null)
  const [erros, setErros] = useState<{ transacao?: string; arquivo?: string }>({})

  const opcoes = pendentes.map((t) => ({
    value: t.id,
    label: `#${codigoTransacao(t.id)} — ${t.descricao} · ${formatCurrency(t.valor)}`,
  }))
  // Sem escolha manual, usa a primeira pendente (como no Figma)
  const transacaoId = escolhida || (opcoes[0] ? String(opcoes[0].value) : '')

  function salvar(e: FormEvent) {
    e.preventDefault()
    const novos = {
      transacao: transacaoId ? undefined : 'Selecione a transação.',
      arquivo: arquivo ? undefined : 'Selecione o arquivo do comprovante.',
    }
    setErros(novos)
    if (!transacaoId || !arquivo) return
    anexar.mutate({ transacaoId: Number(transacaoId), arquivo }, { onSuccess: onClose })
  }

  const semPendentes = !transacoes.isLoading && opcoes.length === 0

  return (
    <Modal
      open
      onClose={onClose}
      title="Anexar comprovante"
      description="Vincule uma NF-e ou PDF à transação financeira."
      footer={
        <Button
          type="submit"
          form="form-anexar-comprovante"
          size="lg"
          loading={anexar.isPending}
          disabled={semPendentes}
        >
          Anexar comprovante
        </Button>
      }
    >
      {semPendentes ? (
        <p className="py-4 text-center text-sm text-muted">
          Todas as transações já têm comprovante. Nada pendente por aqui.
        </p>
      ) : (
        <form id="form-anexar-comprovante" onSubmit={salvar} noValidate className="flex flex-col gap-4">
          <Select
            label="Transação"
            value={transacaoId}
            disabled={transacoes.isLoading}
            onChange={(e) => {
              setEscolhida(e.target.value)
              setErros((x) => ({ ...x, transacao: undefined }))
            }}
            options={transacoes.isLoading ? [{ value: '', label: 'Carregando...' }] : opcoes}
            error={erros.transacao}
          />
          <FileDropzone
            variant="compact"
            file={arquivo}
            onChange={(f) => {
              setArquivo(f)
              setErros((x) => ({ ...x, arquivo: undefined }))
            }}
            accept={upload.accept}
            maxMb={upload.maxMb}
            title="Selecionar nota fiscal ou recibo"
            hint={upload.descricao}
            error={erros.arquivo}
          />
        </form>
      )}
      <FormError error={anexar.error} />
    </Modal>
  )
}
