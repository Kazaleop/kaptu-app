import { useId, useRef, useState, type DragEvent } from 'react'
import { cn } from '@/utils/cn'
import { formatBytes } from '@/utils/format'
import { Icon } from './Icon'

interface FileDropzoneProps {
  file: File | null
  onChange(file: File | null): void
  /** Ex.: "application/pdf" ou "application/pdf,.xml" */
  accept: string
  /** Tamanho máximo em MB */
  maxMb: number
  /** Texto principal, ex.: "Selecione ou arraste um arquivo" */
  title: string
  /** Texto menor, ex.: "PDF de até 20 MB" */
  hint: string
  /**
   * "large": área tracejada grande (popup Importar PDF via OCR)
   * "compact": linha com clipe (popup Anexar comprovante)
   */
  variant?: 'large' | 'compact'
  error?: string
}

/** Seleção de arquivo por clique ou arrastar-e-soltar, com validação de tipo e tamanho. */
export function FileDropzone({
  file,
  onChange,
  accept,
  maxMb,
  title,
  hint,
  variant = 'large',
  error,
}: FileDropzoneProps) {
  const inputId = useId()
  const inputRef = useRef<HTMLInputElement>(null)
  const [arrastando, setArrastando] = useState(false)
  const [erroLocal, setErroLocal] = useState<string>()

  function escolher(f: File | undefined) {
    if (!f) return
    const tipos = accept.split(',').map((t) => t.trim().toLowerCase())
    const okTipo = tipos.some((t) =>
      t.startsWith('.') ? f.name.toLowerCase().endsWith(t) : f.type === t,
    )
    if (!okTipo) return setErroLocal('Formato de arquivo não aceito.')
    if (f.size > maxMb * 1024 * 1024) return setErroLocal(`O arquivo passa de ${maxMb} MB.`)
    setErroLocal(undefined)
    onChange(f)
  }

  function onDrop(e: DragEvent) {
    e.preventDefault()
    setArrastando(false)
    escolher(e.dataTransfer.files[0])
  }

  const mensagemErro = erroLocal ?? error
  const grande = variant === 'large'

  return (
    <div>
      <label
        htmlFor={inputId}
        onDragOver={(e) => {
          e.preventDefault()
          setArrastando(true)
        }}
        onDragLeave={() => setArrastando(false)}
        onDrop={onDrop}
        className={cn(
          'flex cursor-pointer rounded-xl border bg-[#faf8fd] transition-colors hover:border-primary/50',
          grande
            ? 'flex-col items-center justify-center gap-1 border-dashed px-6 py-8 text-center'
            : 'items-center gap-3 px-4 py-3.5',
          arrastando ? 'border-primary bg-primary-soft' : mensagemErro ? 'border-danger' : 'border-[#d9d1ea]',
        )}
      >
        <span
          className={cn(
            'grid shrink-0 place-items-center rounded-full bg-[#ebe5fa] text-primary-text',
            grande ? 'mb-2 size-10' : 'size-9 rounded-xl',
          )}
        >
          <Icon name={file ? 'check' : grande ? 'upload' : 'paperclip'} size={18} />
        </span>
        <span className="min-w-0">
          <span className="block truncate text-sm font-semibold text-text">
            {file ? file.name : title}
          </span>
          <span className="block text-xs text-subtle">
            {file ? `${formatBytes(file.size)} · clique para trocar` : hint}
          </span>
        </span>
        <input
          ref={inputRef}
          id={inputId}
          type="file"
          accept={accept}
          className="sr-only"
          onChange={(e) => {
            escolher(e.target.files?.[0])
            e.target.value = '' // permite escolher o mesmo arquivo de novo
          }}
        />
      </label>
      {mensagemErro && <p className="mt-1.5 text-xs text-danger">{mensagemErro}</p>}
    </div>
  )
}
