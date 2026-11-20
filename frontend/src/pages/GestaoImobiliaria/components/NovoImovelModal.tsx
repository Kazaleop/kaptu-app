import { useState, type FormEvent } from 'react'
import { Button, FormError, Input, Modal } from '@/components/ui'
import { useCreateImovel } from '@/hooks'
import type { ImovelDTO, ImovelRequest } from '@/types'

interface NovoImovelModalProps {
  open: boolean
  onClose(): void
  /** Chamado com o imóvel criado (ex.: para já selecioná-lo no contrato) */
  onCreated?(imovel: ImovelDTO): void
}

const vazio = { nome: '', endereco: '', bairro: '', quantResidentes: '', valorAluguelMensal: '' }
type Campos = typeof vazio

/**
 * Cadastro de imóvel -> POST /api/v1/imoveis
 * (Sem tela no Figma: segue o visual do popup "Novo contrato de aluguel".)
 */
export function NovoImovelModal({ open, onClose, onCreated }: NovoImovelModalProps) {
  return open ? <Formulario onClose={onClose} onCreated={onCreated} /> : null
}

/** Montado só com o modal aberto, para começar sempre com o formulário limpo. */
function Formulario({ onClose, onCreated }: Omit<NovoImovelModalProps, 'open'>) {
  const [form, setForm] = useState<Campos>(vazio)
  const [erros, setErros] = useState<Partial<Campos>>({})
  const criar = useCreateImovel()

  const set = (campo: keyof Campos) => (e: { target: { value: string } }) =>
    setForm((f) => ({ ...f, [campo]: e.target.value }))

  function validar(): ImovelRequest | null {
    const e: Partial<Campos> = {}
    if (!form.nome.trim()) e.nome = 'Informe o nome do imóvel.'
    if (!form.endereco.trim()) e.endereco = 'Informe o endereço.'
    if (!form.bairro.trim()) e.bairro = 'Informe o bairro.'
    const residentes = Number(form.quantResidentes)
    if (!Number.isInteger(residentes) || residentes < 1) e.quantResidentes = 'Mínimo de 1.'
    const valor = Number(form.valorAluguelMensal)
    if (!(valor >= 0.01)) e.valorAluguelMensal = 'Informe um valor maior que zero.'
    setErros(e)
    if (Object.keys(e).length) return null
    return {
      nome: form.nome.trim(),
      endereco: form.endereco.trim(),
      bairro: form.bairro.trim(),
      quantResidentes: residentes,
      valorAluguelMensal: valor,
    }
  }

  function salvar(e: FormEvent) {
    e.preventDefault()
    const request = validar()
    if (!request) return
    criar.mutate(request, {
      onSuccess: (imovel) => {
        onCreated?.(imovel)
        onClose()
      },
    })
  }

  return (
    <Modal
      open
      onClose={onClose}
      title="Cadastrar imóvel"
      description="Adicione um imóvel ao patrimônio da Fundação."
      footer={
        <Button type="submit" form="form-novo-imovel" size="lg" loading={criar.isPending}>
          Salvar imóvel
        </Button>
      }
    >
      <form id="form-novo-imovel" onSubmit={salvar} noValidate className="grid grid-cols-2 gap-4">
        <div className="col-span-2">
          <Input label="Nome" placeholder="Ex.: Casa Institucional 037" value={form.nome} onChange={set('nome')} error={erros.nome} autoFocus />
        </div>
        <Input label="Endereço" placeholder="Rua, número" value={form.endereco} onChange={set('endereco')} error={erros.endereco} />
        <Input label="Bairro" placeholder="Bairro" value={form.bairro} onChange={set('bairro')} error={erros.bairro} />
        <Input label="Residentes" type="number" min={1} step={1} placeholder="0" value={form.quantResidentes} onChange={set('quantResidentes')} error={erros.quantResidentes} />
        <Input label="Aluguel mensal (R$)" type="number" min={0.01} step={0.01} placeholder="R$ 0,00" value={form.valorAluguelMensal} onChange={set('valorAluguelMensal')} error={erros.valorAluguelMensal} />
      </form>
      <FormError error={criar.error} />
    </Modal>
  )
}
