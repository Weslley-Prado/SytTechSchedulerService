# Guia de Integração para o Front-end — SytTech Scheduler

Documento **dedicado ao time de front-end** (web/mobile). Cobre, do começo ao fim,
como consumir a API: base URL, autenticação, formato de erros, jornada de
agendamento, endpoints com exemplos `curl` + payloads, contratos das telas
e dicas de UX.

> Referências internas:
> - Contratos OpenAPI: `src/main/resources/contract/input/*.yaml` (fonte da verdade).
> - Fluxos visuais: [`flows.md`](./flows.md).
> - Glossário de campos: [`glossario-campos.md`](./glossario-campos.md).
> - Coleção Postman: `postman/SytTech-Scheduler.postman_collection.json`.

---

## 0. TL;DR

| Item                    | Valor                                                                 |
|-------------------------|------------------------------------------------------------------------|
| **Base URL (dev)**      | `http://localhost:8082/api/v1`                                         |
| **Base URL (HML)**      | `https://hml.scheduler.syttech.com/api/v1`                             |
| **Base URL (PROD)**     | `https://scheduler.syttech.com/api/v1`                                 |
| **Content-Type**        | `application/json; charset=utf-8`                                      |
| **Datas**               | ISO-8601 com offset (`2026-05-18T09:00:00-03:00`)                      |
| **Autenticação**        | `Authorization: Bearer <accessToken>` (JWT HS256)                      |
| **TTL access token**    | 3600 s (1 h)                                                           |
| **TTL refresh token**   | 30 dias                                                                |
| **TTL hold (pré-reserva)** | 10 min (default)                                                    |
| **Erros**               | `application/problem+json` (RFC 7807)                                  |
| **Health**              | `GET /actuator/health`                                                 |

---

## 1. Setup do cliente HTTP

### 1.1 axios (recomendado)

```ts
// api/client.ts
import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,   // ex.: http://localhost:8082/api/v1
  timeout: 10_000,
  headers: { 'Content-Type': 'application/json' },
});

// 1) injeta accessToken
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// 2) refresh automático no 401
let refreshing: Promise<string> | null = null;
api.interceptors.response.use(
  (r) => r,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !original._retry) {
      original._retry = true;
      refreshing ??= refreshAccessToken();          // dedupe concorrente
      try {
        const newToken = await refreshing;
        original.headers.Authorization = `Bearer ${newToken}`;
        return api(original);
      } finally {
        refreshing = null;
      }
    }
    return Promise.reject(error);
  },
);

async function refreshAccessToken(): Promise<string> {
  const refreshToken = localStorage.getItem('refreshToken');
  if (!refreshToken) throw new Error('no refresh token');
  const { data } = await axios.post(
    `${import.meta.env.VITE_API_BASE_URL}/auth/refresh`,
    { refreshToken },
  );
  localStorage.setItem('accessToken', data.accessToken);
  localStorage.setItem('refreshToken', data.refreshToken);
  return data.accessToken;
}
```

### 1.2 fetch (sem dependência)

```ts
const BASE = import.meta.env.VITE_API_BASE_URL;
async function http<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('accessToken');
  const res = await fetch(`${BASE}${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...init.headers,
    },
  });
  if (!res.ok) throw await res.json();    // ProblemDetail
  if (res.status === 204) return undefined as T;
  return res.json();
}
```

> Recomendado: gerar o cliente automaticamente a partir do OpenAPI
> (`openapi-typescript-codegen`, `orval`, ou `openapi-generator-cli typescript-axios`).

---

## 2. Autenticação ponta a ponta

### 2.1 Quem precisa de token?

| Tipo de endpoint                                  | Precisa de Bearer? |
|---------------------------------------------------|--------------------|
| Catálogo (`/units`, `/categories`, `/services`, `/professionals`) | ❌ não |
| Disponibilidade (`/availability`)                 | ❌ não             |
| Criar hold (`POST /appointments/holds`)           | ❌ não             |
| Confirmar agendamento (`POST /appointments`)      | ❌ não (com guest) / ✅ se quiser vincular ao usuário logado |
| Consultar por código (`GET /appointments/by-code/{code}`) | ❌ não — link público do e-mail |
| Cancelar / remarcar / reenviar código             | ⚠️ depende da política da unidade — hoje a API não exige |
| **`GET /customers/me/appointments`**              | ✅ **sim** — único endpoint hoje que **exige** JWT (`CurrentCustomer.requireId`) |

### 2.2 Fluxo de login

```bash
curl -X POST http://localhost:8082/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"maria@gmail.com","password":"S3nh@Forte!"}'
```

Resposta 200:
```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "customer": {
    "id": "8d2c...",
    "fullName": "Maria",
    "email": "maria@gmail.com",
    "emailVerified": true
  }
}
```

**Front guarda em**: localStorage (web), Keychain (iOS), Keystore (Android),
`SecureStore` (React Native), `flutter_secure_storage` (Flutter).

### 2.3 Refresh

Use **antes do `exp`** (ex.: `expiresIn * 0.8 = 2880s`) ou reativamente em cima do **primeiro 401** (snippet acima).

```bash
curl -X POST .../auth/refresh -d '{"refreshToken":"..."}'
```

Resposta = mesmo formato do login. Substitua os dois tokens no storage.

### 2.4 Logout

Hoje **não há `/auth/logout`** (JWTs HS256 não revogáveis no servidor). Logout = limpar tokens do storage:

```ts
localStorage.removeItem('accessToken');
localStorage.removeItem('refreshToken');
```

> Quando endpoint de revogação existir, atualizar este passo.

---

## 3. Formato de erros (Problem Details)

Todas as falhas devolvem `application/problem+json`:

```json
{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 422,
  "detail": "acceptTerms must be true",
  "instance": "/api/v1/customers",
  "code": "DOMAIN_VALIDATION",
  "errors": [
    { "field": "acceptTerms", "message": "must be true" }
  ]
}
```

### 3.1 Tabela de status

| Status | Quando aparece                          | O que o front deve fazer |
|--------|-----------------------------------------|--------------------------|
| 400    | JSON malformado / parâmetro obrigatório faltando | mostrar erro técnico (log + toast genérico) |
| 401    | sem token / token expirado / credenciais inválidas | tentar refresh; se falhar, redirecionar pro login |
| 404    | recurso inexistente                     | tela de "não encontrado" |
| 409    | slot já reservado / e-mail já cadastrado | re-buscar `/availability`; no cadastro pedir outro e-mail |
| 410    | hold expirou                            | recriar hold (`POST /holds`) e voltar pro passo de confirmação |
| 422    | regra de negócio (acceptTerms, senha curta, `to <= from`) | mostrar `detail`/`errors` por campo |
| 429    | rate-limit em `/resend-code`            | desabilitar botão por N segundos |
| 5xx    | bug no backend                          | toast genérico + Sentry/log |

### 3.2 Helper genérico

```ts
type Problem = { status: number; title: string; detail?: string; code?: string; errors?: Array<{ field: string; message: string }> };
export function describeError(e: any): Problem | null {
  return e?.response?.data ?? e ?? null;
}
```

---

## 4. Jornada do cliente (passo a passo)

Esta sequência cobre 95% dos casos do app. Os números batem com `docs/flows.md §1`.

### 4.1 Visão geral

```
1) Login OU Cadastro (opcional — pode confirmar como guest)
2) Escolher unidade   → GET /units
3) Escolher categoria → GET /units/{unitId}/categories
4) Escolher serviço   → GET /units/{unitId}/categories/{categoryId}/services
5) Escolher profissional (ou "qualquer") → GET /units/{unitId}/services/{serviceId}/professionals
6) Ver horários       → GET /availability?...
7) Reservar           → POST /appointments/holds
8) Confirmar          → POST /appointments
9) Receber código por e-mail
10) (depois) ver/cancelar/remarcar
```

### 4.2 Endpoint a endpoint

> Em todos os exemplos, `BASE = http://localhost:8082/api/v1`.

#### 4.2.1 Cadastro de cliente

```bash
curl -X POST $BASE/customers \
  -H 'Content-Type: application/json' \
  -d '{
    "fullName": "Maria Souza",
    "email": "maria@gmail.com",
    "phone": "+5511999999999",
    "password": "S3nh@Forte!",
    "acceptTerms": true
  }'
```

- **201** → `{ id, fullName, email, phone, emailVerified: false }` + e-mail com link.
- **409** → e-mail já existe.
- **422** → `acceptTerms != true` ou senha < 8 chars.

#### 4.2.2 Verificação de e-mail

O link no e-mail é `${SYTTECH_APP_BASE_URL}/auth/verify?token=...`. O **front** pega esse `token` da query string e dispara:

```bash
curl -X POST $BASE/customers/verify-email -d '{"token":"..."}'
```

- **204** → marcar `emailVerified=true` e redirecionar pro login.
- **404 / 422** → mostrar "link inválido ou expirado, reenvie".

#### 4.2.3 Listar unidades

```bash
curl "$BASE/units?q=centro&city=Sao%20Paulo&page=0&size=20"
```

Resposta (paginada padrão Spring):
```json
{
  "content": [{ "id": "...", "name": "SytTech Centro", "address": "...", "city": "São Paulo", "coverImageUrl": "..." }],
  "page": 0, "size": 20, "totalElements": 7, "totalPages": 1
}
```

#### 4.2.4 Detalhe da unidade (com horários)

```bash
curl "$BASE/units/{unitId}"
```

Retorna `businessHours` com `dayOfWeek (1..7)`, `opensAt`, `closesAt` — útil para mostrar "Hoje: 09h–18h".

#### 4.2.5 Categorias da unidade

```bash
curl "$BASE/units/{unitId}/categories"
# [{ "id": "...", "name": "Cabelo", "iconUrl": "..." }, ...]
```

#### 4.2.6 Serviços da categoria

```bash
curl "$BASE/units/{unitId}/categories/{categoryId}/services"
# [{ "id":"...", "name":"Corte masculino", "durationMinutes":45, "price":"60.00", "currency":"BRL" }, ...]
```

**UX**: já mostre na lista o preço formatado e o tempo (`45 min`).

#### 4.2.7 Profissionais do serviço

```bash
curl "$BASE/units/{unitId}/services/{serviceId}/professionals"
# [{ "id":"...", "name":"Lucas", "avatarUrl":"...", "rating":4.8 }, ...]
```

**UX**: adicionar uma opção sintética **"Qualquer disponível"** no topo — ela não vem da API, o front passa `professionalId = null` adiante.

#### 4.2.8 Disponibilidade (slots)

```bash
curl "$BASE/availability?unitId=...&serviceId=...&from=2026-05-18T00:00:00-03:00&to=2026-05-19T00:00:00-03:00&timezone=America/Sao_Paulo"
```

- `professionalId` opcional. Se omitido → "qualquer disponível".
- **Janela máxima recomendada**: 1 dia (resposta cresce rápido).
- Resposta:
```json
{
  "slots": [
    { "start": "2026-05-18T09:00:00-03:00", "end": "2026-05-18T09:45:00-03:00", "professionalId": "..." },
    { "start": "2026-05-18T09:45:00-03:00", "end": "2026-05-18T10:30:00-03:00", "professionalId": "..." }
  ]
}
```
- **422** se `to <= from`.

**UX**: agrupe slots por hora ("9h", "10h", "11h"); chips clicáveis. Cache curto (15–30 s).

#### 4.2.9 Criar pré-reserva (hold)

```bash
curl -X POST $BASE/appointments/holds \
  -H 'Content-Type: application/json' \
  -d '{
    "unitId": "...",
    "serviceId": "...",
    "professionalId": null,
    "start": "2026-05-18T09:00:00-03:00"
  }'
```

Resposta 201:
```json
{
  "holdId": "9b...",
  "professionalId": "...",     // backend resolve quando vem null
  "start": "2026-05-18T09:00:00-03:00",
  "end":   "2026-05-18T09:45:00-03:00",
  "expiresAt": "2026-05-18T08:10:00-03:00"
}
```

**UX crítica**:
1. Mostre um **timer regressivo** baseado em `expiresAt`.
2. Se o cliente abandonar a tela, dispare `DELETE /appointments/holds/{holdId}` (libera o slot para outros).
3. Em `409 Conflict`, mostre toast "Esse horário acabou de ser pego, escolha outro" e re-busque `/availability`.

#### 4.2.10 Liberar hold (cliente desistiu)

```bash
curl -X DELETE $BASE/appointments/holds/{holdId}
# 204 No Content
```

Idempotente — chame ao sair da tela / no `beforeunload` do browser.

#### 4.2.11 Confirmar agendamento

Dois modos:

##### a) **Guest** (sem cadastro)
```bash
curl -X POST $BASE/appointments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: 6e2c-...-uuid-do-cliente' \
  -d '{
    "holdId": "9b...",
    "customer": {
      "fullName": "Maria Souza",
      "email": "maria@gmail.com",
      "phone": "+5511999999999",
      "notes": "alergia a x"
    }
  }'
```

##### b) **Autenticado** (com Bearer; `customer` pode ser omitido)
```bash
curl -X POST $BASE/appointments \
  -H 'Authorization: Bearer <accessToken>' \
  -d '{"holdId":"9b..."}'
```

Resposta 201:
```json
{
  "appointmentId": "...",
  "code": "ABC-1234",
  "status": "CONFIRMED",
  "start": "2026-05-18T09:00:00-03:00",
  "end":   "2026-05-18T09:45:00-03:00"
}
```

- **410 Gone** → hold expirou → recriar hold e tentar de novo.
- **422** → validar payload e mostrar `detail`.
- **`Idempotency-Key`**: o backend aceita o header (atualmente não reaproveita resposta, mas em deduplicação futura ele evita criar dois agendamentos se o cliente clicar duas vezes). **Gere um UUID por tentativa do usuário** (não por request).

**UX pós-201**:
- Tela de sucesso com o `code` em destaque ("seu código: **ABC-1234**").
- Botão "Reenviar e-mail" → `POST /appointments/{id}/resend-code` (rate-limited).
- QR code com a URL pública: `${appBaseUrl}/appointments/by-code/${code}`.

#### 4.2.12 Consulta pública pelo código

```bash
curl "$BASE/appointments/by-code/ABC-1234"
```

Retorna `AppointmentDetails` (nome do serviço, profissional, unidade, customer).
**Não precisa de token** — é o link enviado por e-mail.

#### 4.2.13 Cancelar

```bash
curl -X DELETE $BASE/appointments/{appointmentId}
# 204
```

Front: pedir confirmação (modal "Tem certeza?") antes. Mostrar tela "Agendamento cancelado".

#### 4.2.14 Remarcar

Duas variantes:

```bash
# Preferido: criar novo hold antes e mandar o holdId
curl -X PATCH $BASE/appointments/{id}/reschedule -d '{"holdId":"novo..."}'

# Alternativo: mandar só o novo start
curl -X PATCH $BASE/appointments/{id}/reschedule -d '{"start":"2026-05-20T14:00:00-03:00"}'
```

`id` e `code` permanecem os mesmos. Cliente recebe e-mail com o novo horário.

#### 4.2.15 Reenviar código

```bash
curl -X POST $BASE/appointments/{id}/resend-code
# 202 Accepted
```

- **429** → desabilite o botão por 60 s e mostre contador.

#### 4.2.16 Meus agendamentos (autenticado)

```bash
curl -H 'Authorization: Bearer <accessToken>' \
     "$BASE/customers/me/appointments?status=CONFIRMED&from=2026-05-01T00:00:00-03:00"
```

Resposta: array de `AppointmentSummary` (`appointmentId, code, status, start, end, serviceName, professionalName, unitName`).

---

## 5. Contratos de tela → endpoints

Mapeamento direto pro time de design / PM:

| Tela / Componente                    | Endpoint(s) chamado(s)                                              | Notas |
|--------------------------------------|---------------------------------------------------------------------|-------|
| Splash / Home                        | `GET /units` (paginado)                                             | Cache 5 min. |
| Login                                | `POST /auth/login`                                                  | Storage seguro de tokens. |
| Cadastro                             | `POST /customers` → e-mail → `POST /customers/verify-email`         | Validar `acceptTerms` no front também. |
| Detalhe da unidade                   | `GET /units/{id}` + `GET /units/{id}/categories`                    | Render dos `businessHours`. |
| Lista de serviços                    | `GET /units/{id}/categories/{catId}/services`                       | Mostrar duração e preço. |
| Lista de profissionais               | `GET /units/{id}/services/{svcId}/professionals` + opção "Qualquer" | "Qualquer" → `professionalId=null`. |
| Calendário de horários               | `GET /availability` (1 dia por vez)                                 | Cache 15–30 s. Refetch ao mudar profissional/data. |
| Modal de confirmação (3 min de TTL)  | `POST /appointments/holds` → timer → `POST /appointments`           | `DELETE /holds/{id}` no cancel. |
| Tela de sucesso                      | resposta do `POST /appointments`                                    | Exibir `code`, QR, botão "Reenviar". |
| Página pública por código            | `GET /appointments/by-code/{code}`                                  | Sem autenticação. |
| Meus agendamentos                    | `GET /customers/me/appointments`                                    | Requer Bearer. |
| Ações (cancelar/remarcar/reenviar)   | `DELETE`, `PATCH …/reschedule`, `POST …/resend-code`                | Confirmar antes. |

---

## 6. Boas práticas de UX

1. **Timer do hold** — sempre visível na tela de confirmação. Conte a partir de `expiresAt`. Quando zerar, refaça hold automaticamente OU volte ao calendário.
2. **Race condition de slot** — tratar `409` no hold com `toast` + refetch transparente. **Não** mostre stack trace.
3. **Idempotência** — gere um `Idempotency-Key` (UUIDv4) na hora que o usuário **abre** a tela de confirmação; reutilize-o em todos os retries dessa tentativa. Troque ao iniciar nova tentativa.
4. **Refresh proativo** — refresh do token quando `now > issuedAt + 0.8 * expiresIn`. Evita 401 no meio da jornada.
5. **Empty states**:
   - `availability` vazio → "Sem horários para esse dia. Tente outro.".
   - `customers/me/appointments` vazio → CTA "Agendar agora".
6. **Datas** — sempre exibir no fuso do dispositivo (`Intl.DateTimeFormat`), mas **mandar para a API** com offset (ISO-8601). Nunca mande `Z` se o usuário escolheu no fuso local — confunde a query de disponibilidade.
7. **Mensagens de erro** — use o `detail` do Problem Details quando vier do backend; senão fallback genérico.
8. **Acessibilidade** — slots como botões com `aria-label="09:00, profissional Lucas, 45 minutos"`.

---

## 7. Variáveis de ambiente do front

```env
# .env.development
VITE_API_BASE_URL=http://localhost:8082/api/v1
VITE_APP_BASE_URL=http://localhost:5173

# .env.hml
VITE_API_BASE_URL=https://hml.scheduler.syttech.com/api/v1
VITE_APP_BASE_URL=https://hml.scheduler.syttech.com

# .env.production
VITE_API_BASE_URL=https://scheduler.syttech.com/api/v1
VITE_APP_BASE_URL=https://scheduler.syttech.com
```

> O `VITE_APP_BASE_URL` precisa **bater com** o `SYTTECH_APP_BASE_URL` do backend, porque é a URL para a qual o e-mail aponta (`/auth/verify`, `/appointments/by-code/...`).

---

## 8. CORS

A API hoje **não tem CORS configurado**. Em dev, use:
- **mesma origem** (proxy do Vite/CRA) — recomendado;
- ou habilite `WebMvcConfigurer.addCorsMappings(...)` no backend (alinhar com o time de back).

Exemplo `vite.config.ts`:
```ts
export default defineConfig({
  server: {
    proxy: {
      '/api': { target: 'http://localhost:8082', changeOrigin: true },
    },
  },
});
```

---

## 9. Geração automática do client (recomendado)

Para evitar escrever DTOs à mão, gere a partir dos YAMLs:

```bash
# typescript-axios
npx @openapitools/openapi-generator-cli generate \
  -i ../backend/src/main/resources/contract/input/appointments-api.yaml \
  -g typescript-axios \
  -o src/api/appointments
```

Ou usando `orval` (mais idiomático para React/SWR/React-Query):
```bash
npx orval --input ../backend/src/main/resources/contract/input/units-api.yaml \
          --output src/api/units.ts \
          --client react-query
```

Os 5 contratos (`auth`, `units`, `availability`, `appointments`, `customers`) podem ser combinados em um único bundle via `redocly bundle`.

---

## 10. Roteiro de teste manual rápido (Postman / Bruno)

```text
1) POST /customers                     → 201 (anote id)
2) (no e-mail) POST /customers/verify-email  → 204
3) POST /auth/login                    → guarda accessToken
4) GET /units                          → escolhe unitId
5) GET /units/{unitId}/categories      → escolhe categoryId
6) GET /units/{unitId}/categories/{cat}/services  → escolhe serviceId
7) GET /availability?unitId&serviceId&from&to → escolhe um slot
8) POST /appointments/holds            → guarda holdId
9) POST /appointments (Bearer ou guest) → guarda code (ABC-1234)
10) GET /appointments/by-code/ABC-1234 → confere detalhes
11) GET /customers/me/appointments     → aparece na lista
12) PATCH /appointments/{id}/reschedule → confere e-mail
13) DELETE /appointments/{id}           → confere cancelamento
```

A coleção `postman/SytTech-Scheduler.postman_collection.json` já tem todos esses
requests parametrizados; configure as variáveis `baseUrl`, `accessToken`,
`unitId`, etc.

---

## 11. Checklist do front antes de subir pra HML/PROD

- [ ] `baseURL` via env (não hardcoded).
- [ ] Tokens em storage **seguro** (não em cookie sem `httpOnly` se possível).
- [ ] Interceptor de **refresh** funcionando (logout no fallback).
- [ ] Tratamento de `409`, `410`, `422`, `429` com mensagens amigáveis.
- [ ] Timer do hold + auto-release no abandono.
- [ ] `Idempotency-Key` no `POST /appointments`.
- [ ] ISO-8601 com offset em todas as datas enviadas.
- [ ] Tela de e-mail-verificação tratando link expirado.
- [ ] Página pública `/appointments/by-code/{code}` linkando o QR / link do e-mail.
- [ ] Erros enviados pro Sentry/observabilidade.
- [ ] Build do front consumindo o mesmo `appBaseUrl` configurado no backend.

