# Kaptu Frontend: passo a passo

Registro de tudo o que foi feito, na ordem. Cada etapa nova é acrescentada aqui.

---

## Etapa 1: Estrutura e configuração inicial

### 1.1 Pré-requisitos verificados
- Node.js v24 e npm 12 (`node -v`, `npm -v`)
- Git 2.56 (`git --version`)

### 1.2 Criação do projeto Vite + React + TypeScript
Executado em `D:\`:
```bash
npm create vite@latest Kaptu-frontend -- --template react-ts
cd Kaptu-frontend
npm install
```
Isso cria o projeto com React 19, TypeScript e Vite 8.

### 1.3 Dependências instaladas
```bash
npm install @tanstack/react-query axios react-router-dom
npm install -D tailwindcss @tailwindcss/vite
```
| Pacote | Para quê |
|---|---|
| `@tanstack/react-query` | Cache e estado das chamadas ao backend (igual ao projeto modelo) |
| `axios` | Cliente HTTP (igual ao projeto modelo) |
| `react-router-dom` | Navegação entre as telas |
| `tailwindcss` + `@tailwindcss/vite` | Tailwind CSS v4 integrado ao Vite |

### 1.4 Configuração do Tailwind CSS (v4)
O Tailwind v4 não precisa mais de `tailwind.config.js` nem de `postcss.config.js`. Foram dois passos:

1. Plugin no `vite.config.ts`:
   ```ts
   import tailwindcss from '@tailwindcss/vite'
   export default defineConfig({ plugins: [react(), tailwindcss()] })
   ```
2. Import no `src/index.css`:
   ```css
   @import 'tailwindcss';
   @theme { --color-primary: #1d4ed8; /* ... */ }
   ```
   Cada variável do `@theme` vira classe. Por exemplo, `--color-primary` gera `bg-primary`, `text-primary` e `border-primary`. **As cores atuais são provisórias** e serão trocadas pelas do Figma na primeira tela.

> Se o plugin do Figma gerar um `tailwind.config.ts` (formato v3), dá para aproveitá-lo no v4 com `@config "../tailwind.config.ts";` no `index.css`, ou converter os valores para o `@theme`. Isso será feito quando os arquivos do plugin chegarem.

### 1.5 Alias `@/`
Imports como `@/components/ui` em vez de `../../components/ui`:
- `vite.config.ts` → `resolve.alias['@'] = src`
- `tsconfig.app.json` → `"paths": { "@/*": ["./src/*"] }`

### 1.6 Estrutura de pastas
```
Kaptu-frontend/
├─ .env.example          ← modelo das variáveis (vai para o GitHub)
├─ .env.local            ← suas variáveis locais (NÃO vai para o GitHub)
├─ vercel.json           ← faz as rotas do React funcionarem na Vercel
├─ docs/
│  ├─ PASSO-A-PASSO.md   ← este arquivo
│  └─ backend/           ← arquivos para entregar ao responsável pelo backend
└─ src/
   ├─ config/env.ts      ← único lugar que lê as variáveis de ambiente
   ├─ types/             ← espelho dos DTOs e enums Java
   ├─ services/          ← chamadas HTTP (uma por endpoint)
   ├─ hooks/             ← React Query (useImoveis, useCreateImovel...)
   ├─ mocks/             ← dados de exemplo (VITE_USE_MOCKS=true)
   ├─ components/
   │  ├─ ui/             ← Button, Input, Select, Modal, Card, Badge, Spinner, QueryState
   │  └─ layout/         ← AppLayout, Sidebar, PageHeader
   ├─ pages/             ← uma pasta por tela
   ├─ routes/            ← paths.ts (menu) e AppRoutes.tsx
   └─ utils/             ← formatação (R$, datas, CPF/CNPJ, telefone) e labels de status
```

### 1.7 Como o fluxo de dados funciona (comparado ao modelo)
```
Tela (pages/)  →  hook (hooks/useImoveis)  →  serviço (services/imovelService)  →  api.ts (Axios)  →  Spring Boot
```
- **No modelo:** o hook fazia o `axios.get` direto, com `API_URL` fixo em cada arquivo.
- **Aqui:** o hook só chama o serviço, e o serviço usa uma instância única do Axios (`services/api.ts`) que lê a URL do `.env`. Se `VITE_USE_MOCKS=true`, o serviço devolve dados de `src/mocks` em vez de chamar o backend.

---

## Etapa 2: Tipagem e serviços de API

### 2.1 Tipos (`src/types/`)
| Java | TypeScript |
|---|---|
| `StatusOcupacao` (enum) | `types/enums.ts` → `'DISPONIVEL' \| 'OCUPADO'` |
| `StatusContrato` (enum) | `types/enums.ts` → `'VIGENTE' \| 'VENCIDO' \| 'ENCERRADO'` |
| `Permissao` (enum) | `types/enums.ts` (sem uso: o sistema não tem login) |
| `ImovelDTO` / `ImovelRequest` | `types/imovel.ts` |
| `LocatarioDTO` / `LocatarioRequest` | `types/locatario.ts` |
| `ContratoLinhaDTO` / `ContratoRequest` | `types/contrato.ts` |

Regras de conversão:
- `Long`, `int`, `Integer`, `BigDecimal` → `number`
- `LocalDate` → `string` no formato `"AAAA-MM-DD"` (use `toIsoDate()` de `utils/format.ts`)
- As anotações de validação (`@NotBlank`, `@Positive`...) ficaram como comentário em cada campo, para guiar a validação dos formulários.

### 2.2 Serviços (`src/services/`)
Base: `VITE_API_URL` + `/api/v1`.

| Função | Endpoint |
|---|---|
| `imovelService.listar()` | `GET /imoveis` |
| `imovelService.buscarPorId(id)` | `GET /imoveis/{id}` |
| `imovelService.cadastrar(req)` | `POST /imoveis` |
| `locatarioService.listar()` | `GET /locatarios` |
| `locatarioService.cadastrar(req)` | `POST /locatarios` |
| `contratoService.listar()` | `GET /contratos` |
| `contratoService.criar(req)` | `POST /contratos` |
| `contratoService.encerrar(id)` | `POST /contratos/{id}/encerrar` |

O `services/api.ts` também converte qualquer erro (backend desligado, 400 de validação, 404...) numa mensagem em português (`ApiError`), que as telas mostram direto.

### 2.3 Hooks (`src/hooks/`)
| Hook | Tipo | Depois de salvar, recarrega |
|---|---|---|
| `useImoveis()`, `useImovel(id)` | leitura | – |
| `useCreateImovel()` | escrita | lista de imóveis |
| `useLocatarios()` / `useCreateLocatario()` | leitura / escrita | lista de locatários |
| `useContratos()` | leitura | – |
| `useCreateContrato()`, `useEncerrarContrato()` | escrita | contratos **e** imóveis (o status de ocupação muda) |

Uso numa tela:
```tsx
const { data: imoveis, isLoading, error, refetch } = useImoveis()
const criar = useCreateImovel()
criar.mutate({ nome, endereco, bairro, quantResidentes, valorAluguelMensal })
// criar.isPending → mostra "salvando..." no botão
```

### 2.4 Funcionalidades sem endpoint no backend
F-1, F-2 e F-4 a F-9 ainda não têm endpoints. Cada uma terá tipos, serviço e mock criados quando a tela correspondente chegar. Ao final de cada tela, será listado o que está **real** e o que está em **mock**.

---

## Etapa 3: Autenticação
**Não se aplica.** O sistema não tem login nem cadastro, então todas as rotas são abertas. O frontend fala apenas com o Spring Boot e nunca acessa o Supabase direto, por isso nenhuma chave do Supabase existe no frontend.

---

## Etapa 4: CORS no Spring Boot (entregar ao responsável pelo backend)

Sem isso, o navegador bloqueia as chamadas do frontend com um erro de CORS no console.

1. Copiar `docs/backend/CorsConfig.java` para
   `D:\Kaptu-main\src\main\java\br\fmc\kaptu\config\CorsConfig.java` (criar a pasta `config`).
2. Adicionar ao `src/main/resources/application.properties`:
   ```properties
   app.cors.allowed-origins=http://localhost:5173,http://127.0.0.1:5173
   ```
3. No deploy, acrescentar o domínio da Vercel. Isso será detalhado na etapa de deploy, e o modelo está em `docs/backend/application.properties.exemplo`.
4. Reiniciar o backend.

---

## Etapa 5: Variáveis de ambiente no Vite

| Variável | Desenvolvimento (`.env.local`) | Produção (Vercel) |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080` | URL pública HTTPS do backend |
| `VITE_USE_MOCKS` | `false` (ou `true` sem backend) | `false` |

Regras do Vite:
- **Prefixo `VITE_`:** só variáveis com esse prefixo chegam ao código.
- **Após editar o `.env.local`:** pare e rode de novo o `npm run dev`.
- **Sem segredos:** tudo que começa com `VITE_` fica visível no navegador. Por isso, nunca coloque senhas ou chaves do Supabase aqui.
- **Onde ler:** no código, as variáveis são lidas só em `src/config/env.ts`.

---

## Como rodar
```bash
cd D:\Kaptu-frontend
npm run dev
```
Abra http://localhost:5173.

- Sem o backend rodando: coloque `VITE_USE_MOCKS=true` no `.env.local` para ver dados de exemplo.
- Com o backend em `http://localhost:8080` e o CORS configurado: deixe `VITE_USE_MOCKS=false`.

Outros comandos:
```bash
npm run build     # compila para produção (pasta dist/) e checa os tipos
npm run preview   # serve a pasta dist/ localmente
npm run lint      # verifica o código
```

---

## Etapa 6: Telas (Figma → código)

### Como cada tela é convertida
1. Abro o `.svg` exportado do Figma no navegador para ver o design real.
2. Leio os `.tsx` do plugin e extraio textos, cores, tamanhos e espaçamentos.
3. Separo o que é **dado** (vem do backend) do que é **texto fixo** da interface.
4. Quebro a tela em componentes reutilizáveis. O plugin gera tudo num arquivo só, com `position: absolute` e larguras fixas.
5. Ligo cada dado a um hook. Endpoints que ainda não existem no backend ficam em mock (ver 6.0).
6. Rodo `npm run build` e `npm run lint` e comparo o resultado com o SVG no navegador.

### 6.0 Endpoints pendentes (mock controlado)
O arquivo `src/config/endpoints.ts` lista os endpoints que o frontend já usa, mas que o backend ainda não implementou:
```ts
export const endpointsProntos = {
  alertas: false,          // GET /api/v1/alertas
  contratosAVencer: false, // GET /api/v1/contratos/a-vencer?dias=30
  financeiroFluxo: false,  // GET /api/v1/financeiro/fluxo-mensal?meses=N
  financeiroResumo: false, // GET /api/v1/financeiro/resumo
  documentosResumo: false, // GET /api/v1/documentos/resumo
}
```
Enquanto um item estiver `false`, o serviço devolve os dados de `src/mocks/dashboard.ts`. Quando o backend entregar o endpoint **no formato dos tipos em `src/types/`** (`AlertaDTO`, `FluxoMensalDTO`, `FinanceiroResumoDTO`, `DocumentosResumoDTO`), basta trocar para `true`. Os tipos são o contrato a repassar ao responsável pelo backend.

### 6.1 Tela 01: Visão geral (Dashboard) + popup "Alertas inteligentes"

**Arquivos do plugin recebidos:** `DashboardHeaderSection.tsx`, `DashboardOverviewSection.tsx`, `SidebarNavigationSection.tsx`, `index.tsx`, `tailwind.config.js`, `tailwind.css` e a pasta do popup.

**O que foi aproveitado e o que foi descartado:**
| Arquivo do plugin | Decisão |
|---|---|
| `tailwind.config.js` | Vazio (`extend: {}`). As cores foram extraídas das classes `[#...]` dos `.tsx` e viraram tokens no `@theme` do `src/index.css` (`primary`, `accent`, `border`, `muted`, `eyebrow`...) |
| `tailwind.css` | Sintaxe do Tailwind v3 (`@tailwind base`) e animações que a tela não usa. Não foi copiado |
| `index.tsx` | Era um template genérico em inglês ("Atlas", "Jamie Smith"), sem relação com o Kaptu. Descartado |
| `vector-*.svg` | Os arquivos de ícone não vieram junto. Os ícones foram redesenhados em `components/ui/Icon.tsx` (SVG inline que herda a cor do texto) |
| Popup `index.tsx` | Era só uma imagem PNG do popup. Foi recriado como componente de verdade a partir do SVG |
| Fonte `Segoe_UI-SemiBold` | Virou `font-sans: 'Segoe UI'` + `font-semibold`. O plugin repetia a fonte em cada elemento |

**Componentes criados:**
```
src/components/
  ui/Icon.tsx            ícones do design
  ui/SectionTitle.tsx    "SOBRETÍTULO + Título" (reutilizado em todas as seções)
  ui/Skeleton.tsx        placeholder pulsante durante o carregamento
  ui/Modal.tsx           refeito no estilo do popup do Figma (título, subtítulo, X)
  ui/Button.tsx          + variante "soft" (lilás) e prop fullWidth
  layout/Sidebar.tsx     menu roxo em degradê, progresso do acervo e usuário
  layout/Header.tsx      título da rota, sino com bolinha de "não lidos" e organização
  alertas/AlertList.tsx          lista usada no card e no popup
  alertas/AlertasModal.tsx       popup "Alertas inteligentes"
  alertas/AlertasProvider.tsx    deixa o popup abrir pelo sino OU pelo botão do card
  alertas/alertasModalContext.ts hook useAlertasModal()
src/pages/Dashboard/
  DashboardPage.tsx
  components/MetricCard.tsx       os 4 cards de indicadores
  components/FinanceChartCard.tsx gráfico de barras + seletor de período
  components/AlertsCard.tsx       card de alertas (mostra os 3 primeiros)
  components/ModuleCard.tsx       atalhos "Módulos da gestão" (navegam para a rota)
```

**De onde vem cada dado da tela:**
| Elemento | Origem | Situação |
|---|---|---|
| "Quarta-feira, 11 de novembro" | Data do computador (`formatDataExtenso`) | ✅ dinâmico |
| "Bom dia / Boa tarde / Boa noite" | Hora atual (`saudacao`) | ✅ dinâmico |
| "Marina", "Marina Costa / Gestora", "Fundação Kaptu" | `src/config/app.ts` (não há login) | ✏️ editável por você |
| Imóveis ativos (número) e "124 IMÓVEIS" | `GET /imoveis` → quantidade | ✅ **backend real** |
| "117 contratos vigentes" | `GET /contratos` → status `VIGENTE` | ✅ **backend real** |
| Receita no mês e "+8,4% vs. mês anterior" | `GET /financeiro/resumo` | 🟡 mock |
| "96% CONCILIADO" | `GET /financeiro/resumo` | 🟡 mock |
| Documentos, "186 processados", "12,4 MIL ARQUIVOS" | `GET /documentos/resumo` | 🟡 mock |
| Acervo digitalizado 76% (menu) | `GET /documentos/resumo` | 🟡 mock |
| Contratos a vencer (7) | `GET /contratos/a-vencer?dias=30` | 🟡 mock |
| Gráfico Receitas x Despesas + período | `GET /financeiro/fluxo-mensal?meses=N` | 🟡 mock |
| Alertas, selo "3 novos", bolinha do sino, popup | `GET /alertas` | 🟡 mock |

**Comportamentos implementados:**
- **Seletor de período:** trocar entre 6 meses, 12 meses e este ano refaz a consulta. O gráfico anterior fica visível, levemente apagado, até os dados novos chegarem.
- **Altura das barras:** proporcional ao maior valor do período. Passar o mouse mostra o valor em R$.
- **Popup de alertas:** abre pelo **sino** ou por **"Ver todos os alertas"** e fecha com "Entendi", com o X, com ESC ou clicando fora.
- **Bolinha laranja do sino e selo "N novos":** aparecem só se houver alertas com `lido: false`.
- **Cards de módulo:** são links para as rotas de cada módulo.
- **Estados de cada bloco:** carregando (skeleton), erro ("Não foi possível carregar") e vazio.
- **Layout responsivo:** os cards se reorganizam em telas menores (4 → 2 → 1 colunas).

### 6.2 Tela 02: Gestão imobiliária + popup "Novo contrato de aluguel"

**100% ligada ao backend real.** Endpoints usados:
| Ação na tela | Endpoint |
|---|---|
| Cards e tabela | `GET /imoveis`, `GET /contratos`, `GET /locatarios` |
| "Salvar contrato" | `POST /contratos` |
| "+ Cadastrar imóvel" / "+ Cadastrar locatário" | `POST /imoveis` / `POST /locatarios` |
| "Encerrar contrato" (detalhes do imóvel) | `POST /contratos/{id}/encerrar` |

**De onde vem cada dado:**
| Elemento | Cálculo |
|---|---|
| "Centralize os N imóveis..." | quantidade de imóveis |
| Imóveis cadastrados / "X ocupados · Y disponíveis" | `statusOcupacao` de cada imóvel |
| Contratos ativos / "% de ocupação" | contratos `VIGENTE` / (ocupados ÷ total) |
| Clientes ativos / "N com contrato vigente" | locatários / locatários distintos em contratos vigentes |
| Tabela | cada imóvel + seu contrato atual (`VIGENTE` ou `VENCIDO`) |

**Adaptações em relação ao Figma (por limitação do backend atual):**
- **"6 novos este mês" virou "N com contrato vigente":** o `LocatarioDTO` não tem data de cadastro.
- **"Vence em 5 dias" só aparece se o backend enviar `dataValidade`:** o `ContratoLinhaDTO` não traz esse campo. Já deixei o tipo e a lógica prontos (`dataValidade?` em `types/contrato.ts`), então basta o backend incluir o campo.
- **"Em renovação" não existe no backend:** o `StatusContrato` só tem `VIGENTE`, `VENCIDO` e `ENCERRADO`.
- **"Aracaju · Centro" virou "endereço · bairro":** o imóvel não tem campo de cidade.
- **A tabela liga contrato e imóvel pelo nome:** o `ContratoLinhaDTO` não envia `imovelId`, então é bom manter os nomes dos imóveis únicos.

**Popup "Novo contrato de aluguel" → `ContratoRequest`:**
| Campo do popup | Campo enviado |
|---|---|
| Imóvel | `imovelId`. Imóveis ocupados aparecem desabilitados. Ao escolher um, o valor mensal é preenchido automaticamente |
| Cliente | `locatarioId`. Campo com autocompletar dos locatários cadastrados |
| Data de início | `dataExpedicao` |
| Vencimento | `prazoMeses`, calculado em meses entre início e vencimento. O campo mostra "Prazo de N meses · validade dd/mm/aaaa" |
| Valor mensal | `valorAluguel` |
| Índice de reajuste | `indiceReajuste`. ⚠ O backend ainda não tem esse campo e ignora o valor até ele ser criado |

**Acréscimos fora do design (necessários para usar o backend):**
- **"+ Cadastrar imóvel" e "+ Cadastrar locatário" dentro do popup:** sem eles não haveria como criar imóveis ou clientes pelo sistema. Os formulários seguem o mesmo visual do popup.
- **Clique numa linha da tabela:** abre os detalhes do imóvel, com "Encerrar contrato" (com confirmação) ou "Novo contrato para este imóvel".
- **Validações:** os erros aparecem em cada campo e somem quando você corrige o campo. Erros do backend aparecem numa caixa vermelha.

**Componentes novos reutilizáveis** (servem também para as telas 03 e 04): `Table/Th/Tr/Td`, `SearchInput`, `Avatar`, `FormError`, `MetricCard` (movido para `ui/`), `PageHeader` com sobretítulo, `Modal` com rodapé lilás e modais empilhados (o ESC fecha só o de cima), e mais variantes de `Badge`.

### 6.3 Tela 03: Acervo de documentos + popup "Importar PDF via OCR"

**O backend ainda não tem documentos**, então a tela inteira funciona com dados de exemplo (`src/mocks/documentos.ts`, 42 documentos). Os 4 primeiros são os do Figma.

**Contrato de API proposto (repassar ao responsável pelo backend):**
| Endpoint | Recebe | Devolve |
|---|---|---|
| `GET /api/v1/documentos` | `busca`, `tipo`, `localizacao`, `dataInicio`, `dataFim`, `page`, `size` (query) | `Page<DocumentoDTO>` do Spring (`content`, `totalElements`, `totalPages`, `number`, `size`) |
| `GET /api/v1/documentos/localizacoes` | – | `string[]` |
| `POST /api/v1/documentos/ocr` | `multipart/form-data`: `arquivo` (PDF), `localizacao`, `tipo` (opcional) | `DocumentoDTO` com status `PROCESSANDO` |

- **`DocumentoDTO`:** `{ id, nome, tipo, data, localizacao, status }`
- **`TipoDocumento`:** `CONTRATO`, `NOTA_FISCAL`, `RECIBO`, `CERTIDAO`, `OUTRO`
- **`StatusDocumento`:** `PROCESSANDO`, `EM_REVISAO`, `CLASSIFICADO`

Tudo está documentado em `src/types/documento.ts`. Quando o backend implementar, troque `documentos` e `documentosOcr` para `true` em `src/config/endpoints.ts`.

**Comportamentos implementados:**
- **Busca:** espera você parar de digitar (350 ms) antes de consultar, com o hook `useDebouncedValue`. Assim o backend não recebe uma chamada a cada tecla.
- **"Todos os tipos":** filtra por tipo.
- **"Mais filtros":** abre uma faixa com Localização e período (De / Até), mais o botão Limpar. O botão mostra quantos filtros extras estão ativos (F-5).
- **Filtro novo:** qualquer alteração de filtro volta para a página 1.
- **Paginação:** do lado do servidor, 10 por página. Mostra "Exibindo X de Y documentos · Página N de M" com setas. A página anterior continua visível, levemente apagada, enquanto a próxima carrega.
- **Popup de importação (área de arquivo):** aceita clique ou arrastar-e-soltar, só PDF, até 20 MB, com mensagem de erro para outros arquivos.
- **Popup de importação (envio):** "Enviar para leitura" fica desabilitado (lilás claro, como no Figma) até escolher o arquivo. A localização é obrigatória e o tipo é opcional ("Detectar automaticamente").
- **Opções de localização:** os locais já usados no acervo mais os **imóveis cadastrados no backend real**.
- **Depois do envio:** aparece uma tela de confirmação, e o documento entra na lista como "Processando". O dashboard e o menu lateral são atualizados.

**Componentes novos reutilizáveis:** `FileDropzone` (versões grande e compacta, esta para o popup da tela 04), `Pagination` e o hook `useDebouncedValue`.

### 6.4 Tela 04: Financeiro e auditoria + popup "Anexar comprovante"

**O backend ainda não tem o módulo financeiro**, então a tela funciona com dados de exemplo (`src/mocks/financeiro.ts`, 30 transações; as 4 primeiras são as do Figma). Receita e despesa são valores fixos iguais ao Figma. Conciliado e Pendências são **calculados** a partir das transações, por isso mudam quando você anexa um comprovante.

**Contrato de API proposto (repassar ao responsável pelo backend):**
| Endpoint | Recebe | Devolve |
|---|---|---|
| `GET /api/v1/financeiro/resumo` | – | `FinanceiroResumoDTO` (`mesReferencia`, `receitaMes`, `variacaoReceitaPercentual`, `despesaMes`, `transacoesConciliadas`, `totalTransacoes`, `percentualConciliado`, `pendencias`) |
| `GET /api/v1/financeiro/transacoes` | `comprovante` (opcional), `page`, `size` | `Page<TransacaoDTO>` |
| `POST /api/v1/financeiro/transacoes/{id}/comprovante` | `multipart/form-data`: `arquivo` (PDF ou XML da NF-e) | `TransacaoDTO` atualizada |
| `GET /api/v1/financeiro/relatorio-auditoria?mes=AAAA-MM` | – | arquivo PDF (`application/pdf`) |

- **`TransacaoDTO`:** `{ id, codigo, descricao, data, valor, tipo (RECEITA/DESPESA), comprovante (ANEXADO/PENDENTE), comprovanteNome? }`
- **Formatos:** tudo está documentado em `src/types/financeiro.ts`.
- **Ao implementar:** troque `financeiroResumo`, `financeiroTransacoes`, `financeiroComprovante` e `financeiroRelatorio` para `true` em `src/config/endpoints.ts`.

**De onde vem cada dado:**
| Elemento | Origem |
|---|---|
| Receitas e "Setembro de 2026" | `receitaMes` e `mesReferencia` |
| Despesas e "71,4% da receita" | `despesaMes` ÷ `receitaMes` |
| Conciliado e "287 de 299 transações" | `percentualConciliado`, `transacoesConciliadas` e `totalTransacoes` |
| Pendências e "Comprovantes ausentes" | `pendencias` |
| Tabela | `GET /financeiro/transacoes`, 8 por página |

**Comportamentos implementados:**
- **"Anexar" numa linha pendente:** abre o popup já com aquela transação selecionada.
- **Botão "Anexar comprovante" do card:** abre com a primeira transação pendente selecionada, como no Figma.
- **Seletor de transação:** lista só as transações **pendentes**.
- **Arquivo aceito:** PDF ou XML (NF-e), até 20 MB, por clique ou arrastando.
- **Depois de anexar:** a linha passa a "Anexado", e os cards Conciliado e Pendências, o dashboard e os alertas são atualizados.
- **"Gerar relatório de auditoria":** baixa o relatório do mês de referência. Enquanto o endpoint não existe, o mock gera um **CSV** com as transações, que abre no Excel com acentos corretos. Com o backend pronto, baixa o PDF.
- **Receitas na tabela:** aparecem em verde com "+".

**Rotas atualizadas (menu do design):** `/` Visão geral · `/gestao-imobiliaria` · `/acervo` · `/financeiro` · `/pessoas`.

## Etapa 7: GitHub + Vercel

### 7.1 Estratégia: dois modos
| Onde | Comando/Lugar | Arquivo de configuração | Resultado |
|---|---|---|---|
| Computador | `npm run dev` | `.env.local` (ou nada) | usa o **backend** em `localhost:8080` |
| Vercel | build de produção | `.env.production` (`VITE_USE_MOCKS=true`) | usa **dados de exemplo** |

O Vite escolhe o arquivo sozinho: `npm run dev` lê `.env.local`, e `npm run build` (o que a Vercel roda) lê `.env.production`. As variáveis cadastradas no painel da Vercel **têm prioridade** sobre o `.env.production`.

Os dados de exemplo da Gestão imobiliária (`src/mocks/db.ts`) foram trocados pelos do Figma (Edifício Sede, Casa Institucional 037...) para a demonstração ficar completa.

### 7.2 Git e GitHub
```bash
git init
git add .
git commit -m "Frontend Kaptu: telas 01-04"
git branch -M main
git remote add origin https://github.com/<usuario>/<repositorio>.git
git push -u origin main
```
Não vão para o GitHub (estão no `.gitignore`): `node_modules/`, `dist/`, `.env.local` e `.claude/`.

### 7.3 Vercel (importar o repositório)
1. Acesse https://vercel.com e entre com a conta do GitHub.
2. **Add New… → Project → Import** no repositório do frontend. Se ele não aparecer, clique em "Adjust GitHub App Permissions" e libere o repositório.
3. A Vercel detecta **Vite** sozinha: Build Command `npm run build`, Output Directory `dist`. Não mude nada.
4. **Não cadastre variáveis agora** (o `.env.production` já liga o modo sem backend).
5. Clique em **Deploy**. Em cerca de 1 minuto sai a URL, por exemplo `https://kaptu-frontend.vercel.app`.
6. Todo `git push` na `main` gera um deploy novo automaticamente.

### 7.4 Quando quiser o site da Vercel usando o backend
Pré-requisito: o backend publicado com **HTTPS** (Railway, Render ou túnel) e o domínio da Vercel liberado no CORS dele (`APP_CORS_ALLOWEDORIGINS=https://seu-projeto.vercel.app`).
1. Vercel → projeto → **Settings → Environment Variables** → adicionar:
   - `VITE_USE_MOCKS` = `false`
   - `VITE_API_URL` = `https://url-do-backend` (sem `/` no final, sem `/api/v1`)
2. **Deployments → ⋯ (último deploy) → Redeploy**. Sem redeploy, nada muda, porque o Vite grava as variáveis durante o build.
3. Para voltar ao modo sem backend: apague as duas variáveis (ou ponha `VITE_USE_MOCKS=true`) e faça Redeploy.

### 7.5 Em outro computador (front com backend, sem Vercel)
```bash
git clone https://github.com/<usuario>/<repositorio>.git
cd <repositorio>
npm install
npm run dev
```
- Precisa de **Node.js 22 LTS** (mínimo 20.19) instalado.
- O backend tem que estar rodando na porta **8080**, e o front tem que abrir na **5173** (a única liberada no CORS). Se a 5173 estiver ocupada, o Vite avisa em vez de trocar de porta.
- Não precisa criar `.env.local`: sem ele, o `npm run dev` já usa `http://localhost:8080` com dados reais.

---

## Etapa 8: Sincronização com o backend (2026-11-17)

O backend recebeu (versão em `F:\Kaptu-main`): alertas, dashboard, documentos com OCR, financeiro, relatório PDF e tratamento global de erros. O frontend foi ajustado para usar tudo isso.

### 8.1 O que mudou no backend
| Novo | Endpoint |
|---|---|
| Tratamento de erros | `GlobalExceptionHandler` → `{ dataHora, status, erro, mensagem, caminho }` (400 regra de negócio, 404 não encontrado) |
| Alertas | `GET /alertas`, `GET /alertas/nao-lidos`, `PATCH /alertas/{id}/lido` · gerados todo dia às 7h pelo `AlertaJob` |
| Dashboard | `GET /dashboard/resumo` → imóveis, ocupados, contratos vigentes, alertas não lidos, entradas/saídas/saldo do mês |
| Documentos | `POST /documentos/upload` (OCR síncrono), `GET /documentos/{id}`, `GET /documentos/filtros` |
| Financeiro | `GET`/`POST /financeiro/transacoes`, `POST /financeiro/conciliar` |
| Relatório | `GET /financeiro/relatorio-mpse?inicio=&fim=` (PDF) |
| Upload | PDF ou imagem (png, jpg, tif, bmp), até **10 MB** |

`ImobiliarioController`, `ContratoLinhaDTO` e `LocatarioDTO` **não mudaram**.

### 8.2 O que mudou no frontend
- **Erros:** `services/api.ts` passou a ler `mensagem` (formato do novo handler). Também lê erros de download (relatório).
- **Tipos** (`src/types/`): reescritos para espelhar os DTOs reais (`AlertaDTO`, `DashboardDTO` novo, `DocumentoDTO`, `TransacaoDTO`, enums `TipoAlerta`, `TipoDocumento`, `StatusClassificacao`, `TipoTransacao`, `StatusConciliacao`).
- **Flags:** todas as entradas de `src/config/endpoints.ts` agora são `true`.

Alguns dados não têm endpoint próprio e são **calculados no front**:

| Dado na tela | Como é calculado |
|---|---|
| Cards do Financeiro | `/dashboard/resumo` (entradas/saídas do mês) + `/financeiro/transacoes` (conciliadas/pendentes) |
| Gráfico Receitas x Despesas | transações agrupadas por mês |
| Contratos a vencer | alertas `CONTRATO_A_VENCER` cujo contrato ainda está `VIGENTE` |
| Documentos / acervo | contagens sobre `/documentos/filtros` |
| Busca e paginação do acervo e das transações | feitas no front (o backend devolve listas inteiras) |

**Ajustes por tela:**
- **Alertas:**
  - O título vem do tipo do alerta, e o texto vem do backend, com as datas convertidas para dd/mm/aaaa.
  - "Entendi" marca os alertas novos como lidos (`PATCH`). O sino e o selo "N novos" usam `lido`.
- **Dashboard:**
  - "Receita no mês" = entradas do mês, e o detalhe virou "Saldo do mês: R$ …", porque o backend não calcula a variação vs. mês anterior.
  - "Documentos" mostra "N aguardando revisão".
- **Menu lateral:** "Acervo digitalizado" virou "Acervo **classificado**" (documentos classificados ÷ total). O backend não sabe quantos documentos físicos existem.
- **Acervo:**
  - A coluna "Localização" virou **"Valor"** (valor lido pelo OCR), porque o `DocumentoDTO` não informa o imóvel.
  - O filtro "Localização" virou **"Imóvel"** (`imovelId`).
  - A busca procura no título, no resumo e no **texto lido pelo OCR**.
  - O popup de importação aceita PDF ou imagem até 10 MB e mostra o resultado da leitura (tipo, status e resumo).
- **Financeiro:**
  - O código `#TRX-0926` é gerado a partir do id, porque o backend não tem código.
  - "Anexar comprovante" faz o upload (OCR) e depois `POST /financeiro/conciliar`.
  - "Gerar relatório de auditoria" baixa o PDF de 01/01 do ano atual até hoje.

### 8.3 Ainda falta no backend (o front já está preparado ou contornando)
| Falta | Efeito hoje |
|---|---|
| `dataValidade`/`imovelId` no `ContratoLinhaDTO` | tabela da Gestão imobiliária não mostra "Vence em N dias" e liga contrato↔imóvel pelo nome |
| Imóvel no upload e no `DocumentoDTO` | campo "Localização" do popup é enviado, mas ignorado; tabela sem coluna de localização |
| Gerador de `ANOMALIA_FINANCEIRA` | o tipo existe, mas nenhum código cria esse alerta |
| Tela/endpoint para lançar transações no design | `POST /financeiro/transacoes` existe; o front tem o serviço (`financeiroService.lancar`), mas não há tela |
| Supabase, console H2, CORS de produção | ver análise do backend |

---

## Etapa 9: Vercel ligada ao backend no Railway (2026-11-19)

| | URL |
|---|---|
| Frontend (Vercel) | https://kaptu-frontend-11-11.vercel.app |
| Backend (Railway) | https://kaptu-production-905b28d7.up.railway.app |

**No front:** o `.env.production` passou a ser:
```
VITE_USE_MOCKS=false
VITE_API_URL=https://kaptu-production-905b28d7.up.railway.app
```
O build da Vercel usa esse arquivo, então não é preciso cadastrar variáveis no painel. Se elas forem cadastradas lá, têm prioridade.

**Testes feitos:**
- Todas as rotas do Railway respondem 200, e os erros vêm com `mensagem`.
- O build de produção, servido em `localhost:5173`, carregou as 4 telas lendo do Railway, sem erros.

**Obrigatório no backend (Railway → serviço → Variables):** liberar o domínio da Vercel no CORS. Sem isso, o navegador bloqueia todas as chamadas do site (403).
```
APP_CORS_ALLOWEDORIGINS=https://kaptu-frontend-11-11.vercel.app,https://kaptu-frontend-11-11-*.vercel.app,http://localhost:5173
```
O `localhost:5173` continua na lista para o desenvolvimento local funcionar.

**Voltar ao modo de demonstração (sem backend):** trocar para `VITE_USE_MOCKS=true` no `.env.production`, fazer commit e push.

**Se a URL do Railway mudar:** atualizar `VITE_API_URL` no `.env.production` (sem `/` no final), fazer commit e push.
