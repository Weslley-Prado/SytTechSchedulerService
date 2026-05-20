# Fluxos do SytTech Scheduler

Documento gerado a partir do código (`src/main/java/.../usecase/*`) e dos contratos
OpenAPI (`src/main/resources/contract/input/*.yaml`). Todos os diagramas são em **Mermaid**.

> Convenções:
> - **Client** = navegador / app / Postman.
> - **API** = `*Controller` (camada `adapter/input/web/*`).
> - **UC** = `*UseCaseImpl` (camada de aplicação).
> - **DB** = PostgreSQL via portas `*RepositoryPort` / `*Port`.
> - **Mail** = `SmtpEmailNotificationAdapter` (ou `LoggingEmailNotificationAdapter` quando
>   `syttech.email.enabled=false`).
> - Setas tracejadas (`-->>`) = retorno; setas cheias (`->>`) = chamada.

---

## 1. Fluxograma geral — jornada do cliente

Cobre desde a entrada do cliente até o pós-confirmação (fluxo "Novo cliente" e
"Cliente com cadastro" do `business-rule/flow.drawio`).

```mermaid
flowchart TD
    A([Cliente entra na aplicacao]) --> B{Tem cadastro?}
    B -- Sim --> C[POST /auth/login]
    C --> D{Credenciais validas?}
    D -- Nao --> C
    D -- Sim --> F[Recebe accessToken + refreshToken]
    B -- Nao --> E[POST /customers]
    E --> E1[Envia e-mail de verificacao]
    E1 --> E2[POST /customers/verify-email]
    E2 --> F

    F --> G[GET /units]
    G --> H[GET /units/&#123;id&#125;/categories]
    H --> I[GET /units/&#123;id&#125;/categories/&#123;catId&#125;/services]
    I --> J[GET /units/&#123;id&#125;/services/&#123;svcId&#125;/professionals]
    J --> K[GET /availability?unitId&serviceId&from&to]
    K --> L{Escolheu slot?}
    L -- Nao --> K
    L -- Sim --> M[POST /appointments/holds]

    M --> N{Hold criado?}
    N -- 409 conflito --> K
    N -- 201 --> O[POST /appointments confirma]

    O --> P{Hold ainda valido?}
    P -- 410 expirado --> M
    P -- 201 --> Q[Status CONFIRMED + codigo publico]
    Q --> R[Evento AppointmentConfirmedEvent]
    R --> S[Envia e-mail com codigo]
    S --> T([FIM])

    Q -.-> U[GET /appointments/by-code/&#123;code&#125;]
    Q -.-> V[PATCH /appointments/&#123;id&#125;/reschedule]
    Q -.-> W[DELETE /appointments/&#123;id&#125;]
```

---

## 2. Diagramas de sequência por endpoint

### 2.1 `POST /customers` — Cadastro de cliente

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as CustomersController
    participant UC as RegisterCustomerUseCase
    participant Hash as PasswordHasher (BCrypt)
    participant DB as customers
    participant Bus as ApplicationEventPublisher
    participant L as CustomerEmailListener
    participant SMTP as SmtpEmailNotificationAdapter

    C->>API: POST /customers (fullName, email, phone, password, acceptTerms)
    API->>UC: registerCustomer(cmd)
    UC->>UC: valida acceptTerms == true
    UC->>DB: findByEmail(email)
    alt e-mail ja existe
        DB-->>UC: Customer
        UC-->>API: DomainValidationException
        API-->>C: 422 Unprocessable Entity
    else inedito
        DB-->>UC: empty
        UC->>Hash: hash(password)
        Hash-->>UC: hash
        UC->>UC: gera verifyToken (CodeGenerator)
        UC->>DB: save(Customer{ emailVerified=false, verifyToken, expires=+7d })
        DB-->>UC: Customer{id}
        UC->>Bus: publish(CustomerRegisteredEvent)
        UC-->>API: Customer
        API-->>C: 201 Created { id }
        Bus->>L: onCustomerRegistered (after commit)
        L->>SMTP: sendCustomerVerification(id, token)
        SMTP->>SMTP: monta link {appBaseUrl}/auth/verify?token=...
        SMTP-->>C: e-mail "Confirme seu e-mail"
    end
```

---

### 2.2 `POST /customers/verify-email` — Verificacao de e-mail

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as CustomersController
    participant UC as VerifyCustomerEmailUseCase
    participant DB as customers

    C->>API: POST /customers/verify-email { token }
    API->>UC: verify(token)
    UC->>DB: findByVerifyToken(token)
    alt token nao existe ou expirado
        DB-->>UC: empty | expirado
        UC-->>API: ResourceNotFoundException / DomainValidationException
        API-->>C: 404 / 422
    else valido
        DB-->>UC: Customer
        UC->>DB: update(emailVerified=true, verifyToken=null)
        UC-->>API: void
        API-->>C: 204 No Content
    end
```

---

### 2.3 `POST /auth/login` — Autenticacao

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AuthController
    participant UC as LoginUseCase
    participant DB as customers
    participant Hash as PasswordHasher
    participant JWT as HmacJwtTokenIssuer

    C->>API: POST /auth/login { email, password }
    API->>UC: login(cmd)
    UC->>DB: findByEmail(email)
    alt nao encontrado
        DB-->>UC: empty
        UC-->>API: DomainValidationException("Invalid credentials")
        API-->>C: 401 Unauthorized
    else achou
        DB-->>UC: Customer
        UC->>Hash: matches(password, passwordHash)
        alt senha invalida
            Hash-->>UC: false
            UC-->>API: DomainValidationException
            API-->>C: 401 Unauthorized
        else ok
            Hash-->>UC: true
            UC->>JWT: issueFor(customer)
            JWT-->>UC: { accessToken, refreshToken, tokenType=Bearer, expiresIn }
            UC-->>API: LoginResult
            API-->>C: 200 OK + tokens
        end
    end
```

---

### 2.4 `POST /auth/refresh` — Renovar token

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AuthController
    participant UC as RefreshTokenUseCase
    participant JWT as HmacJwtTokenVerifier
    participant DB as customers

    C->>API: POST /auth/refresh { refreshToken }
    API->>UC: refresh(refreshToken)
    UC->>JWT: verifyRefresh(token)
    alt invalido / expirado
        JWT-->>UC: empty
        UC-->>API: DomainValidationException
        API-->>C: 401 Unauthorized
    else ok
        JWT-->>UC: customerId
        UC->>DB: findById(customerId)
        DB-->>UC: Customer
        UC->>JWT: issueFor(customer)
        JWT-->>UC: novos tokens
        UC-->>API: LoginResult
        API-->>C: 200 OK + novos tokens
    end
```

---

### 2.5 `GET /units`, `/categories`, `/services`, `/professionals` — Catalogo

Fluxo identico para os 4 endpoints (so muda o use case). Exemplo com `/units`:

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as UnitsController
    participant UC as ListUnitsUseCase
    participant Cat as UnitCatalogPort (JPA)
    participant DB as units / categories / services / professionals

    C->>API: GET /units?q&city&page&size
    API->>UC: listUnits(query)
    UC->>Cat: findUnits(query)
    Cat->>DB: SELECT u FROM units WHERE active=true AND (city) AND (name LIKE q)
    DB-->>Cat: Page<UnitEntity>
    Cat-->>UC: PageResult<Unit>
    UC-->>API: PageResult<Unit>
    API-->>C: 200 OK { content, page, size, totalElements, totalPages }
```

> Os endpoints `GET /units/{id}/categories`, `/services`, `/professionals` seguem o
> mesmo formato — apenas trocam o repositorio chamado (CategoryJpaRepository,
> ServiceJpaRepository, ProfessionalJpaRepository).

---

### 2.6 `GET /availability` — Slots disponiveis

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AvailabilityController
    participant UC as GetAvailabilityUseCase
    participant Q as AvailabilityQueryPort
    participant DB as business_hours + appointments + holds

    C->>API: GET /availability?unitId&serviceId&from&to[&professionalId][&timezone]
    API->>UC: getAvailability(query)
    UC->>UC: valida 'to > from'
    UC->>Q: findAvailableSlots(query)
    Q->>DB: 1) duracao do servico (services.duration_minutes)
    Q->>DB: 2) janela comercial (business_hours por unit_id & day_of_week)
    Q->>DB: 3) appointments CONFIRMED na faixa
    Q->>DB: 4) holds NOT consumed e nao expirados
    DB-->>Q: dados brutos
    Q->>Q: gera slots no fuso, remove sobreposicoes
    Q-->>UC: List<AvailabilitySlot{ start, end, professionalId }>
    UC-->>API: lista
    API-->>C: 200 OK { slots: [...] }
```

---

### 2.7 `POST /appointments/holds` — Cria pre-reserva

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AppointmentsController
    participant UC as CreateHoldUseCase
    participant Cat as UnitCatalogPort
    participant Av as AvailabilityQueryPort
    participant DB as holds (UNIQUE professional_id, start_at WHERE consumed=false)

    C->>API: POST /appointments/holds { unitId, serviceId, [professionalId], start }
    API->>UC: createHold(cmd)
    UC->>Cat: findServiceById(serviceId)
    alt servico nao existe
        Cat-->>UC: empty
        UC-->>API: ResourceNotFoundException
        API-->>C: 404
    else ok
        Cat-->>UC: Service{ durationMinutes }
        UC->>UC: end = start + duration
        opt professionalId == null
            UC->>Av: findAvailableSlots(unit, service, start..end)
            Av-->>UC: slots
            UC->>UC: pickAny(start) ou erro 422
        end
        UC->>DB: INSERT holds(uuid, unitId, serviceId, profId, start, end,<br/>expiresAt = now + scheduler.hold.ttl-minutes, consumed=false)
        alt UNIQUE violado (slot ja reservado)
            DB-->>UC: ConstraintViolation
            UC-->>API: ConflictException
            API-->>C: 409 Conflict
        else ok
            DB-->>UC: Hold
            UC-->>API: Hold
            API-->>C: 201 Created { holdId, professionalId, start, end, expiresAt }
        end
    end
```

> **TTL do hold**: configurado em `scheduler.hold.ttl-minutes` (default `10`). Um job
> periodico (Hibernate UPDATE) marca holds expirados como `consumed=true`.

---

### 2.8 `DELETE /appointments/holds/{holdId}` — Libera pre-reserva

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AppointmentsController
    participant UC as ReleaseHoldUseCase
    participant DB as holds

    C->>API: DELETE /appointments/holds/{holdId}
    API->>UC: release(holdId)
    UC->>DB: UPDATE holds SET consumed=true WHERE id=? AND consumed=false
    alt nenhuma linha
        DB-->>UC: 0
        UC-->>API: ResourceNotFoundException
        API-->>C: 404 (ou 204 idempotente)
    else 1 linha
        DB-->>UC: 1
        UC-->>API: void
        API-->>C: 204 No Content
    end
```

---

### 2.9 `POST /appointments` — Confirma agendamento

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AppointmentsController
    participant UC as ConfirmAppointmentUseCase
    participant DBh as holds
    participant DBc as customers
    participant DBa as appointments
    participant Bus as ApplicationEventPublisher
    participant L as AppointmentEmailListener
    participant SMTP as SmtpEmailNotificationAdapter

    C->>API: POST /appointments { holdId, customer | customerId }
    API->>UC: confirmAppointment(cmd)
    UC->>DBh: consume(holdId)  // UPDATE holds SET consumed=true WHERE id=? AND consumed=false
    alt hold nao existe ou ja consumido
        DBh-->>UC: empty
        UC-->>API: ResourceNotFoundException
        API-->>C: 404 / 410
    else ok
        DBh-->>UC: Hold
        UC->>UC: valida hold.expiresAt > now
        alt expirado
            UC-->>API: DomainValidationException
            API-->>C: 410 Gone
        else
            opt guest fornecido
                UC->>DBc: findByEmail(guest.email)
                alt nao existe
                    DBc-->>UC: empty
                    UC->>DBc: save(Customer{ emailVerified=false, randomPassword })
                    UC->>Bus: publish(CustomerRegisteredEvent)
                else existe
                    DBc-->>UC: Customer
                end
            end
            UC->>UC: code = CodeGenerator.shortAppointmentCode()
            UC->>DBa: INSERT appointment(id, unit, svc, prof, customer, code,<br/>status=CONFIRMED, start, end)
            DBa-->>UC: Appointment
            UC->>Bus: publish(AppointmentConfirmedEvent)
            UC-->>API: Appointment
            API-->>C: 201 Created { appointmentId, code, status, start, end }
            Bus->>L: onConfirmed (after commit)
            L->>SMTP: sendAppointmentCode(appointmentId)
            SMTP->>DBa: findById
            SMTP->>DBc: findById(customerId)
            SMTP-->>C: e-mail "Codigo {code}"
        end
    end
```

> O header `Idempotency-Key` e aceito mas atualmente nao reaproveita resposta (TODO).

---

### 2.10 `GET /appointments/by-code/{code}` — Consulta publica

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AppointmentsController
    participant UC as GetAppointmentByCodeUseCase
    participant DB as appointments

    C->>API: GET /appointments/by-code/ABC-1234
    API->>UC: getByCode(code)
    UC->>DB: SELECT * FROM appointments WHERE code=?
    alt nao encontrado
        DB-->>UC: empty
        UC-->>API: ResourceNotFoundException
        API-->>C: 404
    else
        DB-->>UC: Appointment
        UC-->>API: Appointment
        API-->>C: 200 OK (AppointmentDetails)
    end
```

---

### 2.11 `POST /appointments/{id}/resend-code` — Reenvio de e-mail

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AppointmentsController
    participant UC as ResendAppointmentCodeUseCase
    participant DB as appointments
    participant SMTP as SmtpEmailNotificationAdapter

    C->>API: POST /appointments/{id}/resend-code
    API->>UC: resend(id)
    UC->>DB: findById(id)
    alt nao existe ou cancelado
        UC-->>API: ResourceNotFoundException
        API-->>C: 404
    else
        UC->>SMTP: sendAppointmentCode(id)
        SMTP-->>C: e-mail enviado
        UC-->>API: void
        API-->>C: 202 Accepted
    end
```

> Rate-limit pode retornar **429 Too Many Requests** (regra externa, ainda nao implementada).

---

### 2.12 `PATCH /appointments/{id}/reschedule` — Remarca

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AppointmentsController
    participant UC as RescheduleAppointmentUseCase
    participant DBa as appointments
    participant DBh as holds
    participant Cat as UnitCatalogPort
    participant Bus as ApplicationEventPublisher
    participant L as AppointmentEmailListener
    participant SMTP as SmtpEmailNotificationAdapter

    C->>API: PATCH /appointments/{id}/reschedule { holdId | start }
    API->>UC: reschedule(id, cmd)
    UC->>DBa: findById(id)
    alt nao existe
        UC-->>API: ResourceNotFoundException
        API-->>C: 404
    else
        UC->>UC: valida status == CONFIRMED
        alt usa holdId
            UC->>DBh: consume(holdId)
            DBh-->>UC: Hold (newStart, newEnd)
        else usa start direto
            UC->>Cat: findServiceById(svc)
            Cat-->>UC: Service{ duration }
            UC->>UC: newEnd = newStart + duration
        end
        UC->>DBa: UPDATE start, end, updated_at WHERE id=?
        alt UNIQUE violado
            DBa-->>UC: ConstraintViolation
            UC-->>API: ConflictException
            API-->>C: 409 Conflict
        else
            DBa-->>UC: Appointment atualizado
            UC->>Bus: publish(AppointmentRescheduledEvent)
            UC-->>API: Appointment
            API-->>C: 200 OK
            Bus->>L: onRescheduled
            L->>SMTP: sendAppointmentCode(id)
            SMTP-->>C: e-mail com novo horario
        end
    end
```

---

### 2.13 `DELETE /appointments/{id}` — Cancela

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant API as AppointmentsController
    participant UC as CancelAppointmentUseCase
    participant DB as appointments
    participant Bus as ApplicationEventPublisher
    participant L as AppointmentEmailListener
    participant SMTP as SmtpEmailNotificationAdapter

    C->>API: DELETE /appointments/{id}
    API->>UC: cancel(id)
    UC->>DB: findById(id)
    alt nao existe
        UC-->>API: ResourceNotFoundException
        API-->>C: 404
    else
        UC->>UC: valida status == CONFIRMED
        alt ja cancelado/concluido
            UC-->>API: DomainValidationException
            API-->>C: 422
        else
            UC->>DB: UPDATE status=CANCELLED, cancelled_at=now WHERE id=?
            UC->>Bus: publish(AppointmentCancelledEvent)
            UC-->>API: void
            API-->>C: 204 No Content
            Bus->>L: onCancelled
            L->>SMTP: sendAppointmentCode(id)
            SMTP-->>C: e-mail "agendamento cancelado"
        end
    end
```

---

### 2.14 `GET /customers/me/appointments` — Meus agendamentos (autenticado)

```mermaid
sequenceDiagram
    autonumber
    participant C as Client
    participant Filter as JwtAuthenticationFilter
    participant API as CustomersController
    participant UC as ListMyAppointmentsUseCase
    participant DB as appointments

    C->>Filter: GET /customers/me/appointments<br/>Authorization: Bearer ...
    Filter->>Filter: valida JWT, extrai customerId<br/>(seta request attribute syttech.auth.customerId)
    alt sem token / invalido
        Filter-->>C: prossegue sem atributo
        API->>API: CurrentCustomer.requireId() -> null
        API-->>C: 401 Unauthorized
    else valido
        Filter-->>API: prossegue com atributo
        API->>UC: listMyAppointments(customerId, filter)
        UC->>DB: SELECT * FROM appointments WHERE customer_id=? ORDER BY start_at DESC
        DB-->>UC: List<Appointment>
        UC-->>API: lista
        API-->>C: 200 OK
    end
```

---

## 3. Maquina de estados do `Appointment`

```mermaid
stateDiagram-v2
    [*] --> CONFIRMED: POST /appointments (a partir de Hold)
    CONFIRMED --> CONFIRMED: PATCH /reschedule (mantem id e code)
    CONFIRMED --> CANCELLED: DELETE /appointments/{id}
    CONFIRMED --> COMPLETED: (futuramente, job pos-horario)
    CONFIRMED --> NO_SHOW: (futuramente, job pos-horario)
    CANCELLED --> [*]
    COMPLETED --> [*]
    NO_SHOW --> [*]
```

E a do `Hold`:

```mermaid
stateDiagram-v2
    [*] --> ACTIVE: POST /appointments/holds
    ACTIVE --> CONSUMED: POST /appointments (consumido na confirmacao)
    ACTIVE --> CONSUMED: PATCH /reschedule (com holdId)
    ACTIVE --> RELEASED: DELETE /appointments/holds/{id}
    ACTIVE --> EXPIRED: job periodico apos expiresAt
    CONSUMED --> [*]
    RELEASED --> [*]
    EXPIRED --> [*]
```

> Tecnicamente todos os estados terminais sao `consumed=true` no banco; o que muda e
> o motivo (linkado ou nao a um appointment / cancelamento explicito / TTL job).

