# Kaptu Frontend

Frontend do Kaptu: React 19 + TypeScript + Vite + Tailwind CSS v4 + React Query + Axios.
Consome a API Spring Boot do Kaptu em `/api/v1`.

## Dois modos de uso

| Onde | Modo | Dados |
|---|---|---|
| Site na **Vercel** | com backend (Railway) | `.env.production` → `VITE_API_URL` do Railway |
| **`npm run dev`** no computador | com backend | API em `http://localhost:8080` |

## Rodar no computador (com o backend)

Pré-requisitos: Node.js 22 LTS (mínimo 20.19) e o backend Kaptu rodando na porta 8080.

```bash
git clone <url-deste-repositorio>
cd kaptu-frontend
npm install
npm run dev
```

Abra **http://localhost:5173** (precisa ser essa porta: é a única liberada no CORS do backend).

Para ver os dados de exemplo sem o backend, crie um arquivo `.env.local` (copie de `.env.example`) com `VITE_USE_MOCKS=true` e rode `npm run dev` de novo.

## Ligar o backend no site da Vercel

Vercel → projeto → **Settings → Environment Variables**:

| Nome | Valor |
|---|---|
| `VITE_USE_MOCKS` | `false` |
| `VITE_API_URL` | URL pública **HTTPS** do backend (sem `/` no final, sem `/api/v1`) |

Depois: **Deployments → ⋯ → Redeploy**. O backend precisa liberar o domínio da Vercel no CORS.

## Comandos

```bash
npm run dev       # desenvolvimento
npm run build     # build de produção (pasta dist/)
npm run preview   # serve a pasta dist/ localmente
npm run lint      # verifica o código
```

Documentação completa: [docs/PASSO-A-PASSO.md](docs/PASSO-A-PASSO.md)
