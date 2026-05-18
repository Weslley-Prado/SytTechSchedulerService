# Planejamento de Endpoints — SytTech Scheduler

Documento derivado de [`business-rule/flow.drawio`](../business-rule/flow.drawio)
(diagrama **"1. Fluxo Principal do Cliente"**). O diagrama **"2. Fluxo do Cadastro
do Cliente"** está vazio no momento — quando for detalhado, a seção
[Cadastro do Cliente](#5-cadastro-do-cliente-fluxo-2) será expandida.

> Convenção: **toda documentação em português**, **todo código/identificador
> (paths, schemas, campos, enums) em inglês**.
> Prefixo base: `/api/v1`.

---

## 1. Mapa fluxo → endpoint

Cada passo do diagrama é mapeado para um endpoint REST.

| # | Passo no diagrama                                          | Método + Path                                                                 | Tipo       |
|---|------------------------------------------------------------|-------------------------------------------------------------------------------|------------|
| 1 | LANDPAGE / "Escolhe unidade ou salão"                      | `GET  /api/v1/units`                                                          | Catálogo   |
| 2 | Detalhe da unidade                                         | `GET  /api/v1/units/{unitId}`                                                 | Catálogo   |
| 3 | "Escolhe categoria"                                        | `GET  /api/v1/units/{unitId}/categories`                                      | Catálogo   |
| 4 | "Escolhe serviço"                                          | `GET  /api/v1/units/{unitId}/categories/{categoryId}/services`                | Catálogo   |
| 5 | "Escolhe profissional ou qualquer disponível"              | `GET  /api/v1/units/{unitId}/services/{serviceId}/professionals`              | Catálogo   |
| 6 | CONSULTA DISPONIBILIDADE (horários)                        | `GET  /api/v1/availability`                                                   | Core       |
| 7 | "Sistema cria pré-reserva temporária"                      | `POST /api/v1/appointments/holds`                                             | Core       |
| 8 | Liberar pré-reserva (TTL/desistência)                      | `DELETE /api/v1/appointments/holds/{holdId}`                                  | Core       |
| 9 | Decisão "Cliente tem cadastro?" — ramo **SIM** (login)     | `POST /api/v1/auth/login`                                                     | Auth       |
| 10| Decisão "Cliente tem cadastro?" — ramo **NÃO** (cadastro)  | `POST /api/v1/customers`                                                      | Cadastro   |
| 11| (Opcional) verificar e-mail no cadastro                    | `POST /api/v1/customers/verify-email`                                         | Cadastro   |
| 12| "Preenche dados de confirmação" → confirma agendamento     | `POST /api/v1/appointments`                                                   | Core       |
| 13| "Sistema envia e-mail com código" — reenvio sob demanda    | `POST /api/v1/appointments/{appointmentId}/resend-code`                       | Core       |
| 14| FINALIZAÇÃO — cliente consulta agendamento pelo código     | `GET  /api/v1/appointments/by-code/{code}`                                    | Core       |
| 15| Cancelar agendamento                                       | `DELETE /api/v1/appointments/{appointmentId}`                                 | Core       |
| 16| Remarcar agendamento                                       | `PATCH  /api/v1/appointments/{appointmentId}/reschedule`                      | Core       |
| 17| Listar agendamentos do cliente logado                      | `GET  /api/v1/customers/me/appointments`                                      | Cliente    |

---

## 2. Resumo numérico

| Escopo                                                                                   | Qtd. de endpoints |
|------------------------------------------------------------------------------------------|-------------------|
| **Mínimo** (caminho feliz exato do diagrama)                                             | **9**             |
| **Recomendado** (mínimo + liberar hold + reenviar código + consultar por código + detalhe de unidade) | **13**            |
| **Completo** (recomendado + cancelar + remarcar + listar do cliente + verify-email)      | **17**            |

### 2.1 Conjunto mínimo (9)
1. `GET  /units`
2. `GET  /units/{unitId}/categories`
3. `GET  /units/{unitId}/categories/{categoryId}/services`
4. `GET  /units/{unitId}/services/{serviceId}/professionals`
5. `GET  /availability`
6. `POST /appointments/holds`
7. `POST /auth/login`
8. `POST /customers`
9. `POST /appointments`

### 2.2 Adicionais do "Recomendado" (+4)
- `DELETE /appointments/holds/{holdId}`
- `POST   /appointments/{appointmentId}/resend-code`
- `GET    /appointments/by-code/{code}`
- `GET    /units/{unitId}`

### 2.3 Adicionais do "Completo" (+4)
- `DELETE /appointments/{appointmentId}`
- `PATCH  /appointments/{appointmentId}/reschedule`
- `GET    /customers/me/appointments`
- `POST   /customers/verify-email`

---

## 3. Agrupamento por contrato OpenAPI

Seguindo o padrão **contract-first** descrito no `README.md`, os endpoints serão
organizados em **5 specs** dentro de `src/main/resources/contract/input/`:

| Arquivo (sugerido)                | Endpoints cobertos                                                                 |
|-----------------------------------|------------------------------------------------------------------------------------|
| `units-api.yaml`                  | `/units`, `/units/{unitId}`, `/units/{unitId}/categories`, `/units/{unitId}/categories/{categoryId}/services`, `/units/{unitId}/services/{serviceId}/professionals` |
| `availability-api.yaml`           | `/availability`                                                                    |
| `appointments-api.yaml`           | `/appointments/holds`, `/appointments/holds/{holdId}`, `/appointments`, `/appointments/{appointmentId}`, `/appointments/{appointmentId}/reschedule`, `/appointments/{appointmentId}/resend-code`, `/appointments/by-code/{code}` |
| `customers-api.yaml`              | `/customers`, `/customers/verify-email`, `/customers/me/appointments`              |
| `auth-api.yaml`                   | `/auth/login` (futuro: `/auth/refresh`, `/auth/logout`, `/auth/password/forgot`, `/auth/password/reset`) |

---

## 4. Detalhamento dos endpoints

> Todos os payloads em `application/json`. Datas em ISO-8601 (`2026-05-17T13:30:00-03:00`).
> Erros de validação seguem `DomainValidationException` → **422**;
> recursos inexistentes seguem `ResourceNotFoundException` → **404**
> (ambos vindos do `shared::kernel`, conforme `README.md`).

### 4.1 Catálogo (navegação)

#### `GET /api/v1/units`
- **Descrição**: lista unidades/salões ativos.
- **Query params**: `q?`, `city?`, `page?`, `size?`.
- **Resposta 200**: `Page<UnitSummary>` com `id`, `name`, `address`, `coverImageUrl`.

#### `GET /api/v1/units/{unitId}`
- **Descrição**: detalhe da unidade (horário de funcionamento, contato, foto).
- **Resposta 200**: `UnitDetails`. **404** se não existir.

#### `GET /api/v1/units/{unitId}/categories`
- **Descrição**: categorias oferecidas pela unidade (ex.: Cabelo, Barba, Estética).
- **Resposta 200**: `List<Category>` com `id`, `name`, `iconUrl`.

#### `GET /api/v1/units/{unitId}/categories/{categoryId}/services`
- **Descrição**: serviços de uma categoria, com duração e preço.
- **Resposta 200**: `List<Service>` com `id`, `name`, `durationMinutes`, `price`, `currency`.

#### `GET /api/v1/units/{unitId}/services/{serviceId}/professionals`
- **Descrição**: profissionais que executam o serviço na unidade.
- **Resposta 200**: `List<Professional>` com `id`, `name`, `avatarUrl`, `rating?`.
- Front pode mostrar a opção sintética **"any available"** sem chamada extra.

### 4.2 Disponibilidade

#### `GET /api/v1/availability`
- **Descrição**: retorna os slots disponíveis para um serviço, opcionalmente
  filtrando por profissional. Se `professionalId` for omitido, o backend
  considera **qualquer profissional disponível**.
- **Query params (obrigatórios)**: `unitId`, `serviceId`, `from` (date-time), `to` (date-time).
- **Query params (opcionais)**: `professionalId`, `timezone`.
- **Resposta 200**:
  ```json
  {
    "slots": [
      {
        "start": "2026-05-18T09:00:00-03:00",
        "end":   "2026-05-18T09:45:00-03:00",
        "professionalId": "..."
      }
    ]
  }
  ```
- **Cache** recomendado: 15–30 s (alto volume de consulta).

### 4.3 Pré-reserva (hold)

#### `POST /api/v1/appointments/holds`
- **Descrição**: cria pré-reserva temporária com **TTL** (sugerido 5–10 min) para
  evitar que dois clientes confirmem o mesmo slot.
- **Request**:
  ```json
  {
    "unitId": "...",
    "serviceId": "...",
    "professionalId": null,
    "start": "2026-05-18T09:00:00-03:00"
  }
  ```
- **Resposta 201**:
  ```json
  {
    "holdId": "...",
    "professionalId": "...",
    "start": "2026-05-18T09:00:00-03:00",
    "end":   "2026-05-18T09:45:00-03:00",
    "expiresAt": "2026-05-18T08:10:00-03:00"
  }
  ```
- **409 Conflict** se o slot já estiver em hold/reservado.

#### `DELETE /api/v1/appointments/holds/{holdId}`
- Libera a pré-reserva (cliente desistiu / TTL expira via job de background).
- **204 No Content**.

### 4.4 Confirmação do agendamento

#### `POST /api/v1/appointments`
- **Descrição**: converte um `hold` em agendamento confirmado. Dispara o evento
  de domínio `AppointmentConfirmedEvent`, consumido por um listener que envia
  o e-mail com o **código de agendamento** (Spring Modulith).
- **Request**:
  ```json
  {
    "holdId": "...",
    "customer": {
      "fullName": "...",
      "email": "...",
      "phone": "...",
      "notes": null
    }
  }
  ```
  - Se houver `Authorization: Bearer ...`, `customer` pode ser omitido (usa cliente logado).
- **Resposta 201**:
  ```json
  {
    "appointmentId": "...",
    "code": "ABC-1234",
    "status": "CONFIRMED",
    "start": "2026-05-18T09:00:00-03:00",
    "end":   "2026-05-18T09:45:00-03:00"
  }
  ```
- **410 Gone** se o `hold` expirou.

#### `POST /api/v1/appointments/{appointmentId}/resend-code`
- Reenvia o e-mail com o código (rate-limit recomendado).

#### `GET /api/v1/appointments/by-code/{code}`
- Consulta pública do agendamento pelo código recebido por e-mail.
- **200** com `AppointmentDetails`. **404** se não existir.

#### `DELETE /api/v1/appointments/{appointmentId}`
- Cancela o agendamento (regras: até X horas antes; configurável por unidade).

#### `PATCH /api/v1/appointments/{appointmentId}/reschedule`
- Remarca para um novo `holdId` ou novo `start`.

### 4.5 Cliente

#### `POST /api/v1/customers`
- Cria conta de cliente (ramo **NÃO** do diagrama).
- **Request**: `fullName`, `email`, `phone`, `password`, `acceptTerms`.
- **201** com `customerId`. Dispara e-mail de verificação.

#### `POST /api/v1/customers/verify-email`
- Confirma o e-mail via token recebido no cadastro.

#### `GET /api/v1/customers/me/appointments`
- Lista os agendamentos do cliente autenticado. Suporta filtros `status?`, `from?`, `to?`.

### 4.6 Autenticação

#### `POST /api/v1/auth/login`
- **Request**: `email`, `password`.
- **Resposta 200**: `accessToken`, `refreshToken`, `expiresIn`, `customer`.

---

## 5. Cadastro do Cliente (Fluxo 2)

O diagrama **"2. Fluxo do Cadastro do Cliente"** está vazio. Endpoints
previstos quando ele for detalhado:

- `POST /api/v1/customers` (já listado)
- `POST /api/v1/customers/verify-email` (já listado)
- `POST /api/v1/auth/login` (já listado)
- `POST /api/v1/auth/password/forgot`
- `POST /api/v1/auth/password/reset`
- `PATCH /api/v1/customers/me` (atualizar dados pessoais)

---

## 6. Decisões de modelagem importantes

1. **Hold separado de Appointment**: o diagrama mostra "cria pré-reserva
   temporária" **antes** da decisão de cadastro/login. Modelar `Hold` como
   entidade própria com `expiresAt` evita race condition entre dois clientes
   pegando o mesmo slot enquanto um preenche dados.
2. **"Qualquer disponível"**: representado como `professionalId = null` na
   consulta de disponibilidade e na criação do hold. O backend decide o
   profissional efetivo na criação do hold e devolve no response.
3. **Código de agendamento**: gerado no `POST /appointments`, persistido junto
   ao agendamento e enviado por e-mail via **evento de domínio +
   listener** (alinhado ao Spring Modulith do `README.md`).
4. **Disponibilidade**: endpoint mais consultado — aplicar cache curto
   (15–30 s) e parâmetros de janela (`from`/`to`).
5. **Idempotência**: `POST /appointments` deve aceitar header
   `Idempotency-Key` para evitar duplicidade em retries do cliente.
6. **Fuso horário**: API trabalha em ISO-8601 com offset; persistência em UTC.

---

## 7. Próximos passos sugeridos

- [ ] Criar os 5 arquivos OpenAPI em `src/main/resources/contract/input/`.
- [ ] Adicionar execução do `openapi-generator-maven-plugin` no `pom.xml`
      para gerar as interfaces dos controllers (padrão `CustomerController`
      do `SytTechPortalService`).
- [ ] Criar migrations Flyway iniciais em `src/main/resources/db/migration/`:
      `V1__units.sql`, `V2__categories_services.sql`, `V3__professionals.sql`,
      `V4__customers.sql`, `V5__holds.sql`, `V6__appointments.sql`.
- [ ] Detalhar o diagrama **"2. Fluxo do Cadastro do Cliente"**.

