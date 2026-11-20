# Segurança — Kaptu API

Este documento descreve como configurar, operar e corrigir problemas de
segurança do backend. **Nunca** coloque credenciais reais neste arquivo nem
em qualquer arquivo versionado.

---

## 1. Segredos e variáveis de ambiente

Toda configuração sensível é lida de variáveis de ambiente. O arquivo
`src/main/resources/application.properties` contém apenas referências
(`${DB_URL}`, `${DB_PASSWORD}`, …), sem valores reais.

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `DB_URL` | Sim | JDBC URL do PostgreSQL/Supabase (preferir pooler na porta `6543`, `sslmode=require`). |
| `DB_USERNAME` | Sim | Usuário do banco (`postgres.<project-ref>` no Supabase). |
| `DB_PASSWORD` | Sim | Senha do banco. |
| `JPA_DDL_AUTO` | Não | `validate` (padrão em produção) ou `update` (somente local). |
| `APP_CORS_ALLOWED_ORIGINS` | Não | Origens permitidas, separadas por vírgula. |
| `KAPTU_ADMIN_EMAIL` | Não | E-mail do administrador inicial. |
| `KAPTU_ADMIN_SENHA` | Não | Senha do administrador inicial (mínimo 12 caracteres). |
| `KAPTU_MAX_FILE_SIZE` / `KAPTU_MAX_REQUEST_SIZE` | Não | Limite de upload (padrão `10MB`). |
| `KAPTU_PASTA_ARQUIVOS` / `KAPTU_OCR_TESSDATA` | Não | Diretórios de arquivos e dados do OCR. |
| `H2_CONSOLE_ENABLED` | Não | Deve permanecer `false` em produção. |

Passo a passo local:

1. Copie `.env.example` para `.env` (já ignorado pelo Git).
2. Preencha os valores.
3. Suba a aplicação normalmente. O `application.properties` importa
   `optional:file:.env[.properties]`, então o `.env` da raiz é carregado
   automaticamente quando existe. Em produção o arquivo não existe e as
   variáveis de ambiente do serviço são usadas.

Formato do `DB_URL`:

- deve começar com `jdbc:postgresql://` (um único `jdbc:`);
- **não** inclua `user=` nem `password=` na URL — use `DB_USERNAME` e
  `DB_PASSWORD`;
- no pooler do Supabase, `DB_USERNAME` é `postgres.<project-ref>` e a porta
  recomendada é `6543`, com `sslmode=require`.

O administrador inicial **só** é criado quando `KAPTU_ADMIN_EMAIL` e
`KAPTU_ADMIN_SENHA` estão definidos e a senha tem 12+ caracteres. Não existe
mais senha padrão no código.

---

## 2. Credenciais que já foram versionadas (ação necessária)

Em algum momento, a URL e a senha do Supabase ficaram em
`application.properties` e permanecem no **histórico do Git**. Remover do
arquivo atual não basta: qualquer pessoa com acesso ao repositório consegue
recuperá-las.

### 2.1 Rotacionar imediatamente

1. No painel do Supabase, redefina a senha do usuário do banco.
2. Atualize a variável `DB_PASSWORD` no ambiente de produção (Railway).
3. Verifique logs de acesso do banco em busca de uso indevido.
4. Considere também rotacionar chaves de API e tokens de serviço, se houver.

A rotação invalida as credenciais antigas mesmo que alguém as tenha obtido.

### 2.2 Limpar o histórico (após rotacionar)

Use `git filter-repo` (recomendado) ou BFG Repo-Cleaner em uma cópia do
repositório:

```bash
# backup antes de reescrever o histórico
git clone --mirror <url-do-repositorio> kaptu-backup.git

# remover o arquivo do histórico inteiro
git filter-repo --path src/main/resources/application.properties --invert-paths

# ou substituir apenas o conteúdo sensível
git filter-repo --replace-text replace.txt
```

Depois:

```bash
git push --force --all
git push --force --tags
```

Avisos:

- Todos os colaboradores precisam re-clonar o repositório.
- PRs e forks antigos continuam com o conteúdo; avalie torná-los privados.
- Se o repositório for público, trate as credenciais como comprometidas
  **mesmo após** a limpeza (por isso a rotação vem primeiro).

---

## 3. O que já está protegido

- **Sem segredos versionados**: configuração por ambiente e `.env` ignorado.
- **Fail-fast**: a aplicação não inicia sem `DB_URL`, `DB_USERNAME` e
  `DB_PASSWORD`.
- **Sem senha padrão**: administrador inicial depende de variáveis de
  ambiente e senha forte.
- **Console H2 desligado** por padrão e bloqueado fora do perfil local.
- **Testes isolados**: `src/test/resources/application.properties` usa H2 em
  memória; `mvn test` nunca toca no banco de produção.
- **Erros internos**: logados no servidor e devolvidos ao cliente sem stack
  trace, mensagem original ou nome de exceção.
- **Cabeçalhos de segurança**: `X-Content-Type-Options`, `X-Frame-Options`,
  `Referrer-Policy`, `Content-Security-Policy`, `Permissions-Policy`,
  `Cache-Control: no-store` e HSTS em HTTPS (`SecurityHeadersFilter`).
- **Validação de entrada**: limites `@Size`/`@Max` nos DTOs de requisição.
- **Upload**: extensões permitidas (PDF/imagens), nome gerado por UUID e
  limite de tamanho configurável.

---

## 4. Pendências estruturais (importante)

> **A API não possui autenticação nem autorização.**
>
> Todos os endpoints `/api/v1/**` estão públicos. Qualquer pessoa com acesso
> à URL pode ler, criar, alterar e excluir dados. As classes de domínio
> (`Usuario`, `Permissao`, `GestorAdministrativo`, etc.) existem, mas não há
> login, emissão de token, filtro de autenticação nem verificação de
> permissões nas rotas.

Antes de expor a API publicamente, é necessário:

1. Implementar autenticação (ex.: Spring Security + JWT ou sessão).
2. Proteger todos os endpoints, exceto `/health`.
3. Aplicar autorização por perfil/permissão.
4. Adicionar rate limiting e proteção contra força bruta no login.
5. Revisar CORS para permitir apenas origens reais.

Enquanto isso, mantenha a API restrita por rede (VPN, IP allowlist ou
autenticação no proxy reverso).

---

## 5. Checklist de produção

- [ ] `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` definidos no ambiente (nunca no Git).
- [ ] Credenciais antigas rotacionadas e histórico do Git limpo.
- [ ] `JPA_DDL_AUTO=validate` (a aplicação não altera o schema sozinha).
- [ ] `JPA_SHOW_SQL=false` e `H2_CONSOLE_ENABLED=false`.
- [ ] `APP_CORS_ALLOWED_ORIGINS` com apenas as origens do frontend.
- [ ] HTTPS habilitado no proxy reverso (HSTS é enviado quando `https`).
- [ ] `KAPTU_ADMIN_SENHA` forte e guardada em cofre de segredos.
- [ ] Logs sem dados pessoais, senhas ou tokens.
- [ ] Backups do banco configurados e testados.
- [ ] Dependências revisadas (`mvn dependency:tree`, alertas de CVE).
- [ ] Autenticação/autorização implementadas antes da exposição pública.

---

## 6. Execução local segura

```bash
# perfil local: H2 em arquivo, sem acesso ao Supabase
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

```bash
# testes: H2 em memória, sem acesso ao banco real
./mvnw clean test
```

Para usar o Supabase em desenvolvimento, exporte as variáveis de ambiente e
mantenha `JPA_DDL_AUTO=validate` para não alterar o schema.

---

## 7. Reportar vulnerabilidades

Não abra issues públicas com detalhes de falhas de segurança. Envie o relato
diretamente aos mantenedores do projeto, incluindo passos de reprodução e
impacto estimado.
