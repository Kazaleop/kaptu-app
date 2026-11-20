import { useEffect, useId, useRef, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { cn } from '@/utils/cn'
import { Icon } from './Icon'

interface ModalProps {
  open: boolean
  title: string
  /** Subtítulo abaixo do título (ex.: "3 situações precisam da sua atenção.") */
  description?: string
  onClose(): void
  children: ReactNode
  /** Rodapé com os botões de ação */
  footer?: ReactNode
  /**
   * "muted": faixa lilás com botão à direita (popups de formulário do Figma)
   * "plain": fundo branco com botão ocupando a largura (popup de alertas)
   */
  footerTone?: 'muted' | 'plain'
  /** Largura máxima da caixa */
  size?: 'md' | 'lg'
}

/** Pilha de modais abertos: ESC fecha só o de cima quando há um modal dentro do outro. */
const pilha: string[] = []

/**
 * Modal padrão do sistema (estilo dos popups do Figma).
 * Fecha com ESC, clique fora ou no X.
 */
export function Modal({
  open,
  title,
  description,
  onClose,
  children,
  footer,
  footerTone = 'muted',
  size = 'md',
}: ModalProps) {
  const titleId = useId()
  const onCloseRef = useRef(onClose)
  useEffect(() => {
    onCloseRef.current = onClose
  })

  useEffect(() => {
    if (!open) return
    pilha.push(titleId)
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape' && pilha[pilha.length - 1] === titleId) onCloseRef.current()
    }
    document.addEventListener('keydown', onKey)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', onKey)
      pilha.splice(pilha.indexOf(titleId), 1)
      if (pilha.length === 0) document.body.style.overflow = ''
    }
  }, [open, titleId])

  if (!open) return null

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-[#180c3b8c] p-4"
      onMouseDown={(e) => e.target === e.currentTarget && onClose()}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className={cn(
          'flex max-h-[90vh] w-full flex-col overflow-hidden rounded-2xl bg-surface shadow-[0_24px_60px_#180c3b40]',
          size === 'md' ? 'max-w-lg' : 'max-w-2xl',
        )}
      >
        <header className="flex items-start justify-between gap-4 border-b border-divider px-6 pt-6 pb-4">
          <div>
            <h2 id={titleId} className="text-xl leading-7 font-semibold tracking-[-0.4px] text-text">
              {title}
            </h2>
            {description && <p className="mt-1 text-sm text-muted">{description}</p>}
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Fechar"
            className="-mr-1 cursor-pointer rounded-lg p-1 text-subtle transition-colors hover:bg-primary-soft hover:text-text"
          >
            <Icon name="close" size={18} />
          </button>
        </header>
        <div className="overflow-y-auto px-6 py-4">{children}</div>
        {footer && (
          <footer
            className={cn(
              'flex items-center gap-2 px-6',
              footerTone === 'muted'
                ? 'justify-end border-t border-divider bg-[#f9f7fc] py-4'
                : 'pt-2 pb-6',
            )}
          >
            {footer}
          </footer>
        )}
      </div>
    </div>,
    document.body,
  )
}
