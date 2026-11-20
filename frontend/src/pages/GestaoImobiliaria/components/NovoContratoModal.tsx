import { useId, useState, type FormEvent } from 'react'
import { Button, FormError, Input, Modal, Select } from '@/components/ui'
import { fieldClass, labelClass } from '@/components/ui/fieldStyles'
import { useCreateContrato, useImoveis, useLocatarios } from '@/hooks'
import { IndiceReajuste, type ContratoRequest, type ImovelDTO, type LocatarioDTO } from '@/types'
import {
  formatCpfCnpj,
  formatCurrencySmart,
  formatDate,
  mesesEntre,
  somarMeses,
  toIsoDate,
} from '@/utils/format'
import { NovoImovelModal } from './NovoImovelModal'
import { NovoLocatarioModal } from './NovoLocatarioModal'

interface NovoContratoModalProps {
  open: boolean
  onClose(): void
  /** Imóvel já escolhido (ao abrir pelos detalhes de um imóvel disponível) */
  imovelInicial?: number
}

/** Popup "Novo contrato de aluguel" -> POST /api/v1/contratos */
export function NovoContratoModal({ open, onClose, imovelInicial }: NovoContratoModalProps) {
  return open ? <Formulario onClose={onClose} imovelInicial={imovelInicial} /> : null
}

interface Campos {
  imovelId: string
  cliente: string
  dataInicio: string
  vencimento: string
  valor: string
  indice: IndiceReajuste
}

type Erros = Partial<Record<keyof Campos, string>>

const indices = Object.values(IndiceReajuste).map((i) => ({
  value: i,
  label: i === 'IGPM' ? 'IGP-M' : i,
}))

function Formulario({ onClose, imovelInicial }: Omit<NovoContratoModalProps, 'open'>) {
  const listaClientesId = useId()
  const { data: imoveis = [] } = useImoveis()
  const { data: locatarios = [] } = useLocatarios()
  const criar = useCreateContrato()

  const imovelPre = imoveis.find((i) => i.id === imovelInicial)
  const [form, setForm] = useState<Campos>(() => ({
    imovelId: imovelPre ? String(imovelPre.id) : '',
    cliente: '',
    dataInicio: toIsoDate(new Date()),
    vencimento: '',
    valor: imovelPre ? String(imovelPre.valorAluguelMensal) : '',
    indice: 'IPCA',
  }))
  const [erros, setErros] = useState<Erros>({})
  const [subModal, setSubModal] = useState<'imovel' | 'locatario' | null>(null)

  /** Atualiza um campo e apaga o erro dele (o erro some assim que o usuário corrige). */
  const set = <K extends keyof Campos>(campo: K, valor: Campos[K]) => {
    setForm((f) => ({ ...f, [campo]: valor }))
    setErros((e) => ({ ...e, [campo]: undefined }))
  }

  /** Ao escolher o imóvel, sugere o aluguel cadastrado nele. */
  function escolherImovel(id: string) {
    const imovel = imoveis.find((i) => String(i.id) === id)
    setForm((f) => ({ ...f, imovelId: id, valor: imovel ? String(imovel.valorAluguelMensal) : f.valor }))
    setErros((e) => ({ ...e, imovelId: undefined, valor: undefined }))
  }

  const locatarioEscolhido = encontrarLocatario(locatarios, form.cliente)
  const prazoMeses =
    form.dataInicio && form.vencimento ? mesesEntre(form.dataInicio, form.vencimento) : 0

  function validar(): ContratoRequest | null {
    const e: Erros = {}
    if (!form.imovelId) e.imovelId = 'Selecione o imóvel.'
    if (!form.cliente.trim()) e.cliente = 'Informe o locatário.'
    else if (!locatarioEscolhido) e.cliente = 'Locatário não cadastrado. Cadastre-o abaixo.'
    if (!form.dataInicio) e.dataInicio = 'Informe a data de início.'
    if (!form.vencimento) e.vencimento = 'Informe o vencimento.'
    else if (prazoMeses < 1) e.vencimento = 'O contrato precisa ter pelo menos 1 mês.'
    const valor = Number(form.valor)
    if (!(valor >= 0.01)) e.valor = 'Informe um valor maior que zero.'
    setErros(e)
    if (Object.keys(e).length || !locatarioEscolhido) return null
    return {
      imovelId: Number(form.imovelId),
      locatarioId: locatarioEscolhido.id,
      prazoMeses,
      valorAluguel: valor,
      dataExpedicao: form.dataInicio,
      indiceReajuste: form.indice,
    }
  }

  function salvar(e: FormEvent) {
    e.preventDefault()
    const request = validar()
    if (request) criar.mutate(request, { onSuccess: onClose })
  }

  // O backend calcula a validade como dataInicio + prazoMeses; mostramos o resultado.
  const validadeCalculada = prazoMeses >= 1 ? somarMeses(form.dataInicio, prazoMeses) : null

  return (
    <>
      <Modal
        open
        onClose={onClose}
        title="Novo contrato de aluguel"
        description="Cadastre o imóvel, cliente e os dados do contrato."
        footer={
          <Button type="submit" form="form-novo-contrato" size="lg" loading={criar.isPending}>
            Salvar contrato
          </Button>
        }
      >
        <form id="form-novo-contrato" onSubmit={salvar} noValidate className="grid grid-cols-2 gap-x-4 gap-y-4">
          <div>
            <Select
              label="Imóvel"
              placeholder="Selecione"
              value={form.imovelId}
              onChange={(e) => escolherImovel(e.target.value)}
              error={erros.imovelId}
              options={opcoesImoveis(imoveis)}
            />
            <LinkCadastro onClick={() => setSubModal('imovel')}>+ Cadastrar imóvel</LinkCadastro>
          </div>

          <div className="flex flex-col gap-1.5">
            <label htmlFor="contrato-cliente" className={labelClass}>
              Cliente
            </label>
            <input
              id="contrato-cliente"
              list={listaClientesId}
              placeholder="Nome do locatário"
              autoComplete="off"
              value={form.cliente}
              onChange={(e) => set('cliente', e.target.value)}
              aria-invalid={!!erros.cliente}
              className={fieldClass(!!erros.cliente)}
            />
            <datalist id={listaClientesId}>
              {locatarios.map((l) => (
                <option key={l.id} value={l.nome}>
                  {formatCpfCnpj(l.cpfCnpj)}
                </option>
              ))}
            </datalist>
            {erros.cliente && <span className="text-xs text-danger">{erros.cliente}</span>}
            <LinkCadastro onClick={() => setSubModal('locatario')}>+ Cadastrar locatário</LinkCadastro>
          </div>

          <Input
            label="Data de início"
            type="date"
            value={form.dataInicio}
            onChange={(e) => set('dataInicio', e.target.value)}
            error={erros.dataInicio}
          />
          <Input
            label="Vencimento"
            type="date"
            min={form.dataInicio || undefined}
            value={form.vencimento}
            onChange={(e) => set('vencimento', e.target.value)}
            error={erros.vencimento}
            hint={
              validadeCalculada
                ? `Prazo de ${prazoMeses} ${prazoMeses === 1 ? 'mês' : 'meses'} · validade ${formatDate(validadeCalculada)}`
                : undefined
            }
          />
          <Input
            label="Valor mensal"
            type="number"
            min={0.01}
            step={0.01}
            placeholder="R$ 0,00"
            value={form.valor}
            onChange={(e) => set('valor', e.target.value)}
            error={erros.valor}
          />
          <Select
            label="Índice de reajuste"
            value={form.indice}
            onChange={(e) => set('indice', e.target.value as IndiceReajuste)}
            options={indices}
          />
        </form>
        <FormError error={criar.error} />
      </Modal>

      <NovoImovelModal
        open={subModal === 'imovel'}
        onClose={() => setSubModal(null)}
        onCreated={(imovel) => escolherImovelCriado(imovel)}
      />
      <NovoLocatarioModal
        open={subModal === 'locatario'}
        nomeInicial={locatarioEscolhido ? '' : form.cliente}
        onClose={() => setSubModal(null)}
        onCreated={(locatario) => set('cliente', locatario.nome)}
      />
    </>
  )

  function escolherImovelCriado(imovel: ImovelDTO) {
    setForm((f) => ({ ...f, imovelId: String(imovel.id), valor: String(imovel.valorAluguelMensal) }))
  }
}

/** Imóveis ocupados aparecem desabilitados: só dá para alugar um disponível. */
function opcoesImoveis(imoveis: ImovelDTO[]) {
  return [...imoveis]
    .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
    .map((i) => ({
      value: i.id,
      label:
        i.statusOcupacao === 'OCUPADO'
          ? `${i.nome} (ocupado)`
          : `${i.nome} · ${formatCurrencySmart(i.valorAluguelMensal)}`,
      disabled: i.statusOcupacao === 'OCUPADO',
    }))
}

function encontrarLocatario(locatarios: LocatarioDTO[], nome: string) {
  const alvo = nome.trim().toLocaleLowerCase('pt-BR')
  return alvo ? locatarios.find((l) => l.nome.toLocaleLowerCase('pt-BR') === alvo) : undefined
}

function LinkCadastro({ onClick, children }: { onClick(): void; children: string }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="mt-1 cursor-pointer self-start text-xs font-medium text-primary-text hover:underline"
    >
      {children}
    </button>
  )
}
