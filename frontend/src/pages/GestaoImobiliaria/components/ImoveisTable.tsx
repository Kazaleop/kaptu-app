import { useMemo, useState } from 'react'
import {
  Avatar,
  Badge,
  Button,
  Card,
  Icon,
  QueryState,
  SearchInput,
  Table,
  Td,
  Th,
  Tr,
} from '@/components/ui'
import { formatCurrencySmart } from '@/utils/format'
import { filtrarLinhas, statusDaLinha, type LinhaImovel } from '../linhas'

interface ImoveisTableProps {
  linhas: LinhaImovel[]
  isLoading: boolean
  error: Error | null
  onRetry(): void
  onSelecionar(linha: LinhaImovel): void
  onCadastrarImovel(): void
}

/** Card "Imóveis e contratos" com busca e tabela. */
export function ImoveisTable({
  linhas,
  isLoading,
  error,
  onRetry,
  onSelecionar,
  onCadastrarImovel,
}: ImoveisTableProps) {
  const [busca, setBusca] = useState('')
  const visiveis = useMemo(() => filtrarLinhas(linhas, busca), [linhas, busca])

  return (
    <Card padded={false} className="overflow-hidden">
      <div className="flex flex-wrap items-center justify-between gap-4 px-6 pt-5 pb-4">
        <div>
          <h2 className="text-base leading-6 font-semibold text-text">Imóveis e contratos</h2>
          <p className="mt-0.5 text-xs leading-4 text-muted">
            Acompanhe ocupação, responsáveis e vencimentos.
          </p>
        </div>
        <SearchInput
          label="Buscar imóvel ou cliente"
          placeholder="Buscar imóvel ou cliente"
          value={busca}
          onChange={setBusca}
          className="w-full sm:w-[260px]"
        />
      </div>

      <QueryState isLoading={isLoading} error={error} onRetry={onRetry}>
        {linhas.length === 0 ? (
          <div className="flex flex-col items-center gap-3 border-t border-divider py-12 text-center">
            <p className="text-sm text-muted">Nenhum imóvel cadastrado ainda.</p>
            <Button variant="soft" onClick={onCadastrarImovel}>
              <Icon name="plus" size={16} /> Cadastrar imóvel
            </Button>
          </div>
        ) : visiveis.length === 0 ? (
          <p className="border-t border-divider py-12 text-center text-sm text-muted">
            Nenhum resultado para “{busca}”.
          </p>
        ) : (
          <Table>
            <thead>
              <tr>
                <Th>Imóvel</Th>
                <Th>Locatário</Th>
                <Th>Status</Th>
                <Th>Valor</Th>
                <Th className="w-12">
                  <span className="sr-only">Ações</span>
                </Th>
              </tr>
            </thead>
            <tbody>
              {visiveis.map((linha) => {
                const { imovel, contrato } = linha
                const status = statusDaLinha(linha)
                const valor = contrato?.valor ?? imovel.valorAluguelMensal
                return (
                  <Tr key={imovel.id} clickable onClick={() => onSelecionar(linha)}>
                    <Td>
                      <p className="font-medium">{imovel.nome}</p>
                      <p className="mt-0.5 text-xs text-subtle">
                        {imovel.endereco} · {imovel.bairro}
                      </p>
                    </Td>
                    <Td>
                      {contrato ? (
                        <span className="flex items-center gap-2.5">
                          <Avatar nome={contrato.locatario} />
                          {contrato.locatario}
                        </span>
                      ) : (
                        <span className="text-subtle">—</span>
                      )}
                    </Td>
                    <Td>
                      <Badge variant={status.variant}>{status.label}</Badge>
                    </Td>
                    <Td className="whitespace-nowrap">{formatCurrencySmart(valor)}/mês</Td>
                    <Td>
                      <button
                        type="button"
                        aria-label={`Ver detalhes de ${imovel.nome}`}
                        onClick={(e) => {
                          e.stopPropagation()
                          onSelecionar(linha)
                        }}
                        className="cursor-pointer rounded-lg p-1 text-subtle hover:bg-primary-soft hover:text-text"
                      >
                        <Icon name="chevronRight" size={18} />
                      </button>
                    </Td>
                  </Tr>
                )
              })}
            </tbody>
          </Table>
        )}
      </QueryState>
    </Card>
  )
}
