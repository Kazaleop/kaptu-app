/**
 * Espelho dos enums Java (br.fmc.kaptu.domain.*).
 * O Jackson serializa enums como string com o mesmo nome da constante.
 * Usamos "as const" + union type em vez de `enum` do TS (exigência do
 * tsconfig: erasableSyntaxOnly).
 */

// domain/imovel/StatusOcupacao.java
export const StatusOcupacao = {
  DISPONIVEL: 'DISPONIVEL',
  OCUPADO: 'OCUPADO',
} as const
export type StatusOcupacao = (typeof StatusOcupacao)[keyof typeof StatusOcupacao]

// domain/imovel/StatusContrato.java
export const StatusContrato = {
  VIGENTE: 'VIGENTE',
  EM_RENOVACAO: 'EM_RENOVACAO',
  VENCIDO: 'VENCIDO',
  ENCERRADO: 'ENCERRADO',
} as const
export type StatusContrato = (typeof StatusContrato)[keyof typeof StatusContrato]

// domain/usuario/Permissao.java (sem uso no front por enquanto: não há login)
export const Permissao = {
  GERIR_USUARIOS: 'GERIR_USUARIOS',
  CADASTRAR: 'CADASTRAR',
  ENVIAR_DOCUMENTOS: 'ENVIAR_DOCUMENTOS',
  LANCAR_TRANSACOES: 'LANCAR_TRANSACOES',
  CONSULTAR: 'CONSULTAR',
  CONCILIAR: 'CONCILIAR',
  VER_RELATORIOS: 'VER_RELATORIOS',
  VER_DASHBOARD: 'VER_DASHBOARD',
} as const
export type Permissao = (typeof Permissao)[keyof typeof Permissao]
