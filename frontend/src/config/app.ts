/**
 * Textos fixos da interface que NÃO vêm do backend.
 *
 * O sistema não tem login, então não existe "usuário logado" para o backend
 * devolver. O nome exibido na saudação ("Bom dia, Marina.") e no rodapé do
 * menu vem daqui. Edite à vontade. Se deixar `nome` vazio, a saudação fica
 * só "Bom dia." e o rodapé do menu some.
 */
/**
 * Regras de upload do backend (DocumentoService + application.properties):
 * PDF ou imagem, até 10 MB (spring.servlet.multipart.max-file-size=10MB).
 */
export const upload = {
  maxMb: 10,
  accept: 'application/pdf,.pdf,image/png,.png,image/jpeg,.jpg,.jpeg,image/tiff,.tif,.tiff,image/bmp,.bmp',
  descricao: 'PDF ou imagem de até 10 MB',
} as const

export const appConfig = {
  organizacao: 'Fundação Kaptu',
  usuario: {
    nome: 'Marina Costa',
    cargo: 'Gestora',
  },
} as const
