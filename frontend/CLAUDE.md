# Kaptu Frontend

Frontend do Kaptu (sistema de gestão da Fundação: imóveis, documentos, financeiro).
O usuário escreve em português: responda em português.

## Stack
Vite + React 19 + TypeScript + Tailwind CSS v4 (`@theme` em `src/index.css`, sem tailwind.config) + React Query 5 + Axios + React Router 7.
`tsconfig` usa `verbatimModuleSyntax` e `erasableSyntaxOnly`: use `import type` e enums como `const` + union (nunca `enum`).

## Backend
- Spring Boot em `../backend` (mesmo repositório do front desde 2026-11-19). Banco: PostgreSQL/Supabase em produção e H2 no perfil `local`.
- Controllers em `src/main/java/br/fmc/kaptu/web/`, DTOs em `web/dto/`, enums em `domain/`.
- Base da API: `VITE_API_URL` (`.env.local`, padrão `http://localhost:8080`) + `/api/v1`.
- **Não há login/autenticação** (decisão do projeto). Nome do usuário exibido vem de `src/config/app.ts`.
- CORS do backend libera só `http://localhost:5173`. Por isso `vite.config.ts` tem `strictPort: true`. Não deixe um `npm run dev` próprio rodando em segundo plano: ele ocupa a 5173 e o do usuário falha ("Port 5173 is already in use"). No Windows, parar a tarefa em segundo plano **não** encerra o processo `node` filho: depois de testar, confira quem escuta na 5173 (`Get-NetTCPConnection -LocalPort 5173`) e encerre o processo do Vite que você iniciou (`Stop-Process`).

## Arquitetura (fluxo de dados)
`pages/` → `hooks/` (React Query) → `services/` (um por recurso) → `services/api.ts` (Axios único, converte erros em `ApiError` em português) → backend.
- `src/types/`: espelho dos DTOs Java. Campos opcionais marcados no comentário existem no backend e podem faltar na resposta.
- `src/hooks/queryKeys.ts`: todas as chaves; mutações invalidam pelo prefixo (`['financeiro']`, `['documentos']`, `['contratos']`...).
- `src/components/ui/`: componentes reutilizáveis (Button, Input, Select, Modal com pilha, Table/Th/Tr/Td, MetricCard, Badge, FileDropzone, Pagination, SearchInput, QueryState...). Reaproveite antes de criar.
- `src/utils/format.ts` (moeda, datas, CPF/CNPJ) e `src/utils/labels.ts` (texto e cor de cada enum).

## Endpoints e mocks
`src/config/endpoints.ts` → `endpointsProntos`: `true` = backend real, `false` = dados de `src/mocks/`. `VITE_USE_MOCKS=true` força mock em tudo (modo demonstração; hoje a Vercel usa o backend real).

Situação em 2026-11-19 (backend em `../backend`): **tudo `true`**. Controllers: Imobiliario, Alerta, Dashboard, Documento, Financeiro, Relatorio. Erros vêm como `{ mensagem, ... }` (GlobalExceptionHandler). Alguns dados são **derivados no front** (resumo financeiro, gráfico, contratos a vencer, resumo do acervo, paginação/busca): ver `docs/PASSO-A-PASSO.md` Etapa 8. Os mocks seguem o formato real dos DTOs.

## Quando o backend mudar (fluxo esperado)
1. Ler os controllers/DTOs alterados em `../backend`.
2. Comparar com `src/types/` e listar as diferenças para o usuário.
3. Ajustar tipo e serviço; trocar a flag em `endpointsProntos` para `true`.
4. Se um campo novo chegar (ex.: `dataValidade`, `imovelId`), revisar onde o front tinha contorno (ver docs).
5. Testar com o backend rodando (`curl` na API + navegador em `localhost:5173`); não gravar dados no banco do usuário sem pedir.
6. `npm run build` e `npm run lint` sem erros.
7. Registrar o que mudou em `docs/PASSO-A-PASSO.md`.

## Telas
- 01 Visão geral (`/`), 02 Gestão imobiliária (`/gestao-imobiliaria`), 03 Acervo (`/acervo`), 04 Financeiro (`/financeiro`): prontas.
- Gestão de pessoas (`/pessoas`): pós-MVP, só placeholder.
- Novas telas vêm como `.svg` exportados do Figma (às vezes com arquivos de um plugin, que ajudam pouco). Para ver o SVG: copiar temporariamente para `public/__design/`, abrir no navegador, e **apagar a pasta ao final**.

## Pendências conhecidas
- Deploy (2026-11-19): Vercel `https://kaptu-frontend-11-11.vercel.app` usa o backend do Railway via `.env.production` (`VITE_USE_MOCKS=false`). O CORS do Railway precisa da variável `APP_CORS_ALLOWED_ORIGINS` com o domínio da Vercel. Modo demonstração: `VITE_USE_MOCKS=true`. Ver docs Etapa 9.
- Repositório Git criado em 2026-11-11 (branch main).

Histórico detalhado de tudo que foi feito: `docs/PASSO-A-PASSO.md`.
