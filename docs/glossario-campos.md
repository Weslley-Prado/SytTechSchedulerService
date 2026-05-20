# Glossário de Variáveis e Campos — SytTech Scheduler

Documento de referência rápida com **o significado de cada variável / campo /
parâmetro** usados na aplicação. Para variáveis de ambiente (config), ver
[`environment-variables.md`](./environment-variables.md). Para colunas de banco,
ver [`database.md`](./database.md). Aqui ficam **os campos que circulam pela
API, eventos e camada de domínio**.

> Convenção: documentação em **português**, identificadores em **inglês**
> (como aparecem no código e nos contratos OpenAPI).

---

## 1. Identificadores

| Variável          | Tipo    | Onde aparece                          | Significado |
|-------------------|---------|---------------------------------------|-------------|
| `id`              | UUID    | Todos os recursos (Unit, Service, …)  | Identificador interno gerado pela aplicação. Imutável. |
| `unitId`          | UUID    | Path/query/body                       | Referência à `units.id`. |
| `categoryId`      | UUID    | Path                                  | Referência à `categories.id`. |
| `serviceId`       | UUID    | Path/query/body                       | Referência à `services.id`. |
| `professionalId`  | UUID    | Query/body — opcional                 | Referência à `professionals.id`. **`null` = "qualquer disponível"** na consulta de disponibilidade e na criação do hold. |
| `customerId`      | UUID    | Body/JWT                              | Referência à `customers.id`. No JWT vai no claim `sub`. |
| `holdId`          | UUID    | Path/body                             | Identificador do pré-bloqueio. Consumido em `POST /appointments`. |
| `appointmentId`   | UUID    | Path                                  | Identificador do agendamento confirmado. |
| `code`            | String  | Path `by-code/{code}` + e-mail        | Código público curto (ex.: `ABC-1234`). Gerado por `CodeGenerator.shortAppointmentCode()`. Único. |

---

## 2. Unidade (Unit)

| Campo            | Tipo    | Significado |
|------------------|---------|-------------|
| `name`           | String  | Nome de exibição do salão/filial. |
| `address`        | String  | Endereço completo. |
| `city`           | String  | Cidade — filtro principal da busca. |
| `phone`          | String  | Telefone de contato. |
| `email`          | String  | E-mail de contato (não confundir com cliente). |
| `coverImageUrl`  | String  | URL absoluta da imagem de capa. |
| `active`         | Boolean | `false` esconde a unidade do catálogo sem deletar. |
| `businessHours`  | Lista   | Janela comercial: `dayOfWeek (1..7)`, `opensAt`, `closesAt`. |

---

## 3. Categoria (Category)

| Campo     | Tipo   | Significado |
|-----------|--------|-------------|
| `name`    | String | Rótulo amigável: "Cabelo", "Barba", "Estética". |
| `iconUrl` | String | Ícone exibido na UI. |

---

## 4. Serviço (Service)

| Campo              | Tipo          | Significado |
|--------------------|---------------|-------------|
| `name`             | String        | Nome do serviço (ex.: "Corte masculino"). |
| `description`      | String        | Texto longo opcional. |
| `durationMinutes`  | Integer       | **Duração em minutos** — usada para calcular `end = start + durationMinutes` em holds/appointments. |
| `price`            | Decimal(12,2) | Preço base. |
| `currency`         | String(3)     | Moeda ISO-4217 (default `BRL`). |
| `active`           | Boolean       | Permite "aposentar" o serviço. |

---

## 5. Profissional (Professional)

| Campo       | Tipo          | Significado |
|-------------|---------------|-------------|
| `name`      | String        | Nome do profissional. |
| `avatarUrl` | String        | Foto exibida na escolha. |
| `rating`    | Decimal(2,1)  | Média de avaliação (0–5). Opcional. |
| `active`    | Boolean       | Profissional fora da escala fica `false`. |

---

## 6. Cliente (Customer)

| Campo                | Tipo    | Significado |
|----------------------|---------|-------------|
| `fullName`           | String  | Nome completo. Obrigatório no cadastro. |
| `email`              | String  | E-mail (também é o login). **Único**. |
| `phone`              | String  | Telefone (opcional). |
| `password`           | String  | Senha em texto puro — **só na requisição**. Nunca persistida. |
| `passwordHash`       | String  | Hash BCrypt persistido em `customers.password_hash`. |
| `acceptTerms`        | Boolean | Obrigatório `true` no `POST /customers` (senão `422`). |
| `emailVerified`      | Boolean | `true` após `POST /customers/verify-email`. |
| `verifyToken`        | String  | Token enviado por e-mail. Apagado após uso. |
| `emailVerifyExpires` | Instant | Expiração do `verifyToken` (default `+7 dias`). |
| `notes`              | String  | Observações livres do cliente (opcional, em `POST /appointments`). |

---

## 7. Disponibilidade (Availability)

| Variável         | Tipo            | Onde aparece                         | Significado |
|------------------|-----------------|--------------------------------------|-------------|
| `from`           | ISO-8601 instant| Query `GET /availability`            | Início da janela de busca (inclusive). |
| `to`             | ISO-8601 instant| Query                                | Fim da janela (exclusive). Deve ser `> from`. |
| `timezone`       | String          | Query (opcional)                     | Ex.: `America/Sao_Paulo`. Default = TZ da aplicação. Usado para alinhar os slots ao fuso do cliente. |
| `slot.start`     | ISO-8601        | Resposta                             | Início do slot disponível. |
| `slot.end`       | ISO-8601        | Resposta                             | Fim do slot (= `start + durationMinutes`). |
| `slot.professionalId` | UUID       | Resposta                             | Profissional que cobre o slot. Quando o cliente não escolheu, o backend decide. |

---

## 8. Pré-reserva (Hold)

| Campo            | Tipo            | Significado |
|------------------|-----------------|-------------|
| `holdId`         | UUID            | Identificador (devolvido no 201). |
| `start`          | ISO-8601 instant| Início do slot reservado. |
| `end`            | ISO-8601 instant| Fim do slot (`start + durationMinutes`). |
| `expiresAt`      | ISO-8601 instant| Quando o hold deixa de valer (default `now + scheduler.hold.ttl-minutes = 10 min`). |
| `consumed`       | Boolean         | `true` após confirmar / liberar / expirar. |
| `professionalId` | UUID            | Profissional efetivo (resolvido pelo backend se o cliente mandou `null`). |

### Estados lógicos
| Estado     | Como atingir                                                   |
|------------|----------------------------------------------------------------|
| `ACTIVE`   | `consumed=false` e `expires_at > now`.                         |
| `CONSUMED` | `POST /appointments` ou `PATCH /reschedule` referenciou o hold.|
| `RELEASED` | `DELETE /appointments/holds/{id}` (cliente desistiu).          |
| `EXPIRED`  | Job periódico marcou `consumed=true` após TTL.                 |

---

## 9. Agendamento (Appointment)

| Campo            | Tipo            | Significado |
|------------------|-----------------|-------------|
| `appointmentId`  | UUID            | Identificador interno. |
| `code`           | String          | Código público curto enviado por e-mail (`ABC-1234`). |
| `status`         | Enum            | `CONFIRMED`, `CANCELLED`, `COMPLETED`, `NO_SHOW`. |
| `start`          | ISO-8601        | Início do atendimento. |
| `end`            | ISO-8601        | Fim. |
| `cancelledAt`    | ISO-8601        | Preenchido apenas quando `status=CANCELLED`. |
| `customerId`     | UUID            | Dono do agendamento. |
| `Idempotency-Key`| Header          | Aceito em `POST /appointments`. **Hoje não é reutilizado** (TODO). |

### Máquina de estados (resumo)
```
[*] -> CONFIRMED  (POST /appointments)
CONFIRMED -> CONFIRMED  (PATCH /reschedule; id e code se mantêm)
CONFIRMED -> CANCELLED  (DELETE /appointments/{id})
CONFIRMED -> COMPLETED  (futuro job pós-horário)
CONFIRMED -> NO_SHOW    (futuro job pós-horário)
```

---

## 10. Autenticação

| Variável        | Tipo    | Onde         | Significado |
|-----------------|---------|--------------|-------------|
| `email`         | String  | Body         | Login do cliente. |
| `password`      | String  | Body         | Senha em texto puro (apenas na request, comparada com `passwordHash`). |
| `accessToken`   | JWT     | Resposta     | Bearer token. TTL = `SYTTECH_JWT_TTL` (default 3600 s = 1 h). |
| `refreshToken`  | JWT     | Resposta     | Token de renovação. TTL fixo = **30 dias** (hardcoded). |
| `tokenType`     | String  | Resposta     | Sempre `Bearer`. |
| `expiresIn`     | Integer | Resposta     | Segundos até o `accessToken` expirar. |
| `iss` (claim)   | String  | JWT          | Issuer — `syttech.security.jwt.issuer` (default `syttech.scheduler`). |
| `sub` (claim)   | UUID    | JWT          | `customerId`. |
| `iat` / `exp`   | Long    | JWT          | Emitido em / expira em (epoch seconds). |

> Header esperado: `Authorization: Bearer <accessToken>`. O filtro
> `JwtAuthenticationFilter` apenas **lê** o token; a obrigatoriedade vem do
> `CurrentCustomer.requireId(...)` chamado por cada endpoint protegido.

---

## 11. Paginação e busca (catálogo)

Aplica-se a `/units`, `/units/{id}/categories`, `/services`, `/professionals`.

| Variável          | Tipo    | Default | Significado |
|-------------------|---------|---------|-------------|
| `page`            | Integer | `0`     | Página (0-based, padrão Spring). |
| `size`            | Integer | `20`    | Itens por página. Máx. recomendado: 100. |
| `q`               | String  | —       | Filtro textual (`name LIKE %q%`). |
| `city`            | String  | —       | Filtro por cidade (apenas `/units`). |
| `content`         | Lista   | —       | Resposta — itens da página. |
| `totalElements`   | Long    | —       | Resposta — total absoluto no filtro. |
| `totalPages`      | Integer | —       | Resposta — total de páginas. |

---

## 12. Eventos de domínio (Spring Modulith)

Persistidos em `event_publication`. Listeners rodam **AFTER_COMMIT**.

| Evento                          | Disparado por                    | Carga útil               | Listener                  | Efeito |
|---------------------------------|----------------------------------|--------------------------|---------------------------|--------|
| `CustomerRegisteredEvent`       | `RegisterCustomerUseCase`        | `customerId`             | `CustomerEmailListener`   | E-mail com link de verificação. |
| `AppointmentConfirmedEvent`     | `ConfirmAppointmentUseCase`      | `appointmentId`          | `AppointmentEmailListener`| E-mail com `code`. |
| `AppointmentRescheduledEvent`   | `RescheduleAppointmentUseCase`   | `appointmentId`          | `AppointmentEmailListener`| E-mail com novo horário. |
| `AppointmentCancelledEvent`     | `CancelAppointmentUseCase`       | `appointmentId`          | `AppointmentEmailListener`| E-mail de cancelamento. |

---

## 13. Erros (formato)

Todas as exceções são tratadas por `GlobalExceptionHandler` e devolvem um
`ProblemDetail` (RFC 7807).

| Variável     | Tipo          | Significado |
|--------------|---------------|-------------|
| `type`       | URI           | Categoria do erro. |
| `title`      | String        | Título curto. |
| `status`     | Integer       | HTTP status. |
| `detail`     | String        | Mensagem legível. |
| `instance`   | URI           | URI da requisição. |
| `code`       | String        | Código interno (`DOMAIN_VALIDATION`, `RESOURCE_NOT_FOUND`, `CONFLICT`, etc.). |
| `errors`     | Lista         | Para erros de validação por campo. |

| Exceção                       | HTTP | Quando |
|-------------------------------|------|--------|
| `DomainValidationException`   | 422  | Regra de negócio violada (ex.: `to <= from`, senha errada, hold expirado). |
| `ResourceNotFoundException`   | 404  | Recurso inexistente ou já consumido. |
| `ConflictException`           | 409  | Slot já em hold/confirmado. |
| `MethodArgumentNotValidException` | 422 | Bean Validation (`@NotNull`, `@Email`, etc.). |
| `Exception` (genérica)        | 500  | Bug não previsto — logado com stack trace. |

---

## 14. Atributos de request (internos)

Definidos por `JwtAuthenticationFilter` / `CurrentCustomer`:

| Atributo                       | Tipo | Significado |
|--------------------------------|------|-------------|
| `syttech.auth.customerId`      | UUID | ID do cliente logado, extraído do JWT. `null` quando anônimo. |

---

## 15. Convenções gerais

- **Datas**: SEMPRE ISO-8601 com offset na entrada/saída (`2026-05-18T09:00:00-03:00`). Persistência em UTC.
- **Strings vazias** ≠ `null`. A API rejeita strings vazias quando `@NotBlank`.
- **Booleans** sempre explícitos no JSON (sem `0/1`).
- **Money**: `price` é `Decimal`, nunca `float`/`double`.
- **Códigos públicos** (`code`): case-sensitive na busca.
- **Headers customizados**:
  - `Idempotency-Key` — aceito em `POST /appointments` (reservado para implementação futura).
  - `Authorization: Bearer ...` — autenticação JWT.

