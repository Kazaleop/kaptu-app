# Kaptu

Pasta unificada do projeto Kaptu, contendo o backend e o frontend.

## Estrutura

```
kaptu-app/
├── backend/    API Spring Boot (Java 17 + PostgreSQL/Supabase + OCR)
└── frontend/   Interface React 19 + Vite + TypeScript + Tailwind CSS 4
```

## Backend

Pré-requisitos: Java 17 ou superior e acesso a um banco PostgreSQL/Supabase.

```bash
cd backend
copy .env.example .env   # Windows; no Linux/macOS use: cp .env.example .env
# edite o .env com DB_URL, DB_USERNAME, DB_PASSWORD etc.
mvnw.cmd spring-boot:run
```

A API sobe em `http://localhost:8080`. O arquivo `backend/.env` foi copiado
para esta pasta, mas continua sendo ignorado pelo Git.

## Frontend

Pré-requisitos: Node.js 20.19+ (recomendado 22 LTS).

```bash
cd frontend
npm install
npm run dev
```

A interface sobe em `http://localhost:5173` (porta exigida pelo CORS padrão
do backend).

## Observações

- O CORS do backend libera, por padrão, apenas `http://localhost:5173`.
- Para usar dados de exemplo no frontend, defina `VITE_USE_MOCKS=true` em
  `frontend/.env.local`.
- As inconsistências identificadas na análise dos dois projetos foram
  apenas registradas; nenhum código foi alterado.
