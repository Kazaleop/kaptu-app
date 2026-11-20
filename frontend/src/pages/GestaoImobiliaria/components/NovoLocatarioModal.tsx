import { useState, type FormEvent } from 'react'
import { Button, FormError, Input, Modal } from '@/components/ui'
import { useCreateLocatario } from '@/hooks'
import type { LocatarioDTO, LocatarioRequest } from '@/types'
import { onlyDigits } from '@/utils/format'

interface NovoLocatarioModalProps {
  open: boolean
  onClose(): void
  /** Nome já digitado no campo "Cliente" do contrato */
  nomeInicial?: string
  onCreated?(locatario: LocatarioDTO): void
}

const vazio = { nome: '', cpfCnpj: '', telefone: '', email: '' }
type Campos = typeof vazio

/**
 * Cadastro de locatário (cliente) -> POST /api/v1/locatarios
 * (Sem tela no Figma: segue o visual do popup "Novo contrato de aluguel".)
 */
export function NovoLocatarioModal({ open, onClose, nomeInicial, onCreated }: NovoLocatarioModalProps) {
  return open ? (
    <Formulario onClose={onClose} nomeInicial={nomeInicial} onCreated={onCreated} />
  ) : null
}

/** Montado só com o modal aberto, para começar sempre com o formulário limpo. */
function Formulario({ onClose, nomeInicial, onCreated }: Omit<NovoLocatarioModalProps, 'open'>) {
  const [form, setForm] = useState<Campos>({ ...vazio, nome: nomeInicial ?? '' })
  const [erros, setErros] = useState<Partial<Campos>>({})
  const criar = useCreateLocatario()

  const set = (campo: keyof Campos) => (e: { target: { value: string } }) =>
    setForm((f) => ({ ...f, [campo]: e.target.value }))

  function validar(): LocatarioRequest | null {
    const e: Partial<Campos> = {}
    const doc = onlyDigits(form.cpfCnpj)
    const tel = onlyDigits(form.telefone)
    if (!form.nome.trim()) e.nome = 'Informe o nome.'
    if (doc.length !== 11 && doc.length !== 14) e.cpfCnpj = 'CPF (11) ou CNPJ (14 dígitos).'
    if (tel.length < 10) e.telefone = 'Telefone com DDD.'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) e.email = 'E-mail inválido.'
    setErros(e)
    if (Object.keys(e).length) return null
    return { nome: form.nome.trim(), cpfCnpj: doc, telefone: tel, email: form.email.trim() }
  }

  function salvar(e: FormEvent) {
    e.preventDefault()
    const request = validar()
    if (!request) return
    criar.mutate(request, {
      onSuccess: (locatario) => {
        onCreated?.(locatario)
        onClose()
      },
    })
  }

  return (
    <Modal
      open
      onClose={onClose}
      title="Cadastrar locatário"
      description="Dados do cliente que vai assinar o contrato."
      footer={
        <Button type="submit" form="form-novo-locatario" size="lg" loading={criar.isPending}>
          Salvar locatário
        </Button>
      }
    >
      <form id="form-novo-locatario" onSubmit={salvar} noValidate className="grid grid-cols-2 gap-4">
        <div className="col-span-2">
          <Input label="Nome completo ou razão social" value={form.nome} onChange={set('nome')} error={erros.nome} autoFocus />
        </div>
        <Input label="CPF/CNPJ" inputMode="numeric" placeholder="Somente números" value={form.cpfCnpj} onChange={set('cpfCnpj')} error={erros.cpfCnpj} />
        <Input label="Telefone" type="tel" placeholder="(79) 99999-0000" value={form.telefone} onChange={set('telefone')} error={erros.telefone} />
        <div className="col-span-2">
          <Input label="E-mail" type="email" placeholder="nome@exemplo.com" value={form.email} onChange={set('email')} error={erros.email} />
        </div>
      </form>
      <FormError error={criar.error} />
    </Modal>
  )
}
