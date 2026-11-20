import type { ContratoLinhaDTO, ImovelDTO, LocatarioDTO } from '@/types'

/**
 * "Banco" em memória usado quando VITE_USE_MOCKS=true (ex.: site na Vercel sem backend).
 * Os dados seguem o design do Figma (tela 02 Gestão imobiliária).
 * Cadastros feitos na tela ficam aqui até recarregar a página.
 */
export const mockDb: {
  imoveis: ImovelDTO[]
  locatarios: LocatarioDTO[]
  contratos: ContratoLinhaDTO[]
} = {
  imoveis: [
    { id: 1, nome: 'Edifício Sede — Sala 204', endereco: 'Av. Barão de Maruim, 410', bairro: 'Centro', quantResidentes: 6, valorAluguelMensal: 4800, statusOcupacao: 'OCUPADO' },
    { id: 2, nome: 'Casa Institucional 037', endereco: 'Rua Itabaiana, 77', bairro: 'Atalaia', quantResidentes: 4, valorAluguelMensal: 3250, statusOcupacao: 'OCUPADO' },
    { id: 3, nome: 'Loja Comercial 015', endereco: 'Av. Gonçalo Rollemberg, 1200', bairro: 'São José', quantResidentes: 2, valorAluguelMensal: 2900, statusOcupacao: 'OCUPADO' },
    { id: 4, nome: 'Galpão 078', endereco: 'Rod. BR-235, km 4', bairro: 'Industrial', quantResidentes: 1, valorAluguelMensal: 6400, statusOcupacao: 'OCUPADO' },
    { id: 5, nome: 'Apartamento 302', endereco: 'Av. Beira Mar, 900', bairro: 'Coroa do Meio', quantResidentes: 3, valorAluguelMensal: 2300, statusOcupacao: 'DISPONIVEL' },
  ],
  locatarios: [
    { id: 1, nome: 'Marcos Carvalho', cpfCnpj: '11122233344', telefone: '79999990011', email: 'marcos@exemplo.com', dataCadastro: diasAPartirDeHoje(-420) },
    { id: 2, nome: 'Ana Ribeiro', cpfCnpj: '22233344455', telefone: '79999990022', email: 'ana@exemplo.com', dataCadastro: diasAPartirDeHoje(-380) },
    { id: 3, nome: 'João Santos', cpfCnpj: '33344455566', telefone: '79999990033', email: 'joao@exemplo.com', dataCadastro: diasAPartirDeHoje(-300) },
    { id: 4, nome: 'Paula Lima', cpfCnpj: '44455566677', telefone: '79999990044', email: 'paula@exemplo.com', dataCadastro: diasAPartirDeHoje(-250) },
  ],
  contratos: [
    { id: 1, imovelId: 1, imovel: 'Edifício Sede — Sala 204', locatario: 'Marcos Carvalho', status: 'VIGENTE', valor: 4800, dataValidade: diasAPartirDeHoje(305) },
    { id: 2, imovelId: 2, imovel: 'Casa Institucional 037', locatario: 'Ana Ribeiro', status: 'VIGENTE', valor: 3250, dataValidade: diasAPartirDeHoje(5) },
    { id: 3, imovelId: 3, imovel: 'Loja Comercial 015', locatario: 'João Santos', status: 'EM_RENOVACAO', valor: 2900, dataValidade: diasAPartirDeHoje(22) },
    { id: 4, imovelId: 4, imovel: 'Galpão 078', locatario: 'Paula Lima', status: 'VIGENTE', valor: 6400, dataValidade: diasAPartirDeHoje(190) },
  ],
}

/** Data "AAAA-MM-DD" daqui a N dias (para o exemplo "Vence em 5 dias"). */
function diasAPartirDeHoje(dias: number): string {
  const d = new Date()
  d.setDate(d.getDate() + dias)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** Simula a latência de rede para os estados de carregamento aparecerem. */
export function delay<T>(value: T, ms = 400): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(structuredClone(value)), ms))
}

export function nextId(list: { id: number }[]): number {
  return list.reduce((max, item) => Math.max(max, item.id), 0) + 1
}
