# Variaveis de Ambiente — SytTech Scheduler Service

Documento que descreve **todas** as variaveis usadas pela aplicacao, com seu
significado, valor padrao, escopo (dev / prod) e onde sao consumidas.

> Origem dos defaults: `application.properties`, `application-dev.properties`,
> `application-prod.properties`, `application-local.properties` e `compose.yaml`.
> `.env` na raiz do projeto e lido **automaticamente** pelo Docker Compose.

---

## 1. Banco de dados (PostgreSQL)

| Variavel              | Default                | Origem            | Descricao                                                                 |
| --------------------- | ---------------------- | ----------------- | ------------------------------------------------------------------------- |
| `POSTGRES_DB`         | `syttechscheduler`     | container Postgres| Nome do schema/database criado no `postgres:16-alpine`.                   |
| `POSTGRES_USER`       | `myuser`               | container Postgres| Usuario master do banco.                                                  |
| `POSTGRES_PASSWORD`   | `secret`               | container Postgres| Senha do usuario acima. **Trocar em prod**.                               |
| `POSTGRES_PORT`       | `5432`                 | compose           | Porta exposta no host (mapeada para 5432 do container).                   |
| `DB_URL`              | `jdbc:postgresql://localhost:5432/syttechscheduler` (dev) <br> `jdbc:postgresql://postgres:5432/${POSTGRES_DB}` (compose) | `spring.datasource.url` | URL JDBC. Dentro do compose o host e o nome do servico `postgres`. |
| `DB_USERNAME`         | `myuser`               | `spring.datasource.username` | Usuario JDBC. Casa com `POSTGRES_USER`.                                  |
| `DB_PASSWORD`         | `secret`               | `spring.datasource.password` | Senha JDBC. Casa com `POSTGRES_PASSWORD`.                                |

---

## 2. JPA / Hibernate / Flyway

| Variavel                                | Default (dev)              | Descricao                                                                                                                |
| --------------------------------------- | -------------------------- | ------------------------------------------------------------------------------------------------------------------------ |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`         | `validate`                 | Modo do Hibernate: `validate` (so confere se o schema bate com as entidades), `update`, `create`, `none`. Em prod = `validate`. |
| `SPRING_JPA_SHOW_SQL`                   | `true` (dev) / `false` (prod) | Loga todas as queries no console. Desligado em prod.                                                                  |
| `SPRING_FLYWAY_ENABLED`                 | `true`                     | Liga/desliga o Flyway. Em prod sempre `true`; em dev sempre `true` (a partir do ajuste recente).                         |
| `SPRING_FLYWAY_LOCATIONS`               | `classpath:db/migration,classpath:db/seed` (dev) <br> `classpath:db/migration` (prod) | Pastas onde o Flyway procura `V*__*.sql` (schema) e `R__*.sql` (seed). **Prod NUNCA inclui `db/seed`**. |
| `SPRING_FLYWAY_BASELINE_ON_MIGRATE`     | `true`                     | Quando o banco ja tem tabelas mas nao tem `flyway_schema_history`, cria a baseline na primeira execucao.                 |
| `SPRING_FLYWAY_BASELINE_VERSION`        | `0`                        | Versao inicial usada na baseline.                                                                                        |
| `SPRING_FLYWAY_CLEAN_DISABLED`          | `true` (prod)              | Bloqueia `flyway:clean` (que dropa o schema) — ativo em prod por seguranca.                                              |
| `SPRING_FLYWAY_VALIDATE_ON_MIGRATE`     | `true` (prod)              | Em prod, valida checksums das migrations ja aplicadas a cada subida.                                                     |
| `SPRING_FLYWAY_TABLE`                   | `flyway_schema_history`    | Tabela onde o Flyway grava o historico de migrations.                                                                    |

---

## 3. Conexao / pool (HikariCP)

| Variavel                                      | Default (dev) | Descricao                                                            |
| --------------------------------------------- | ------------- | -------------------------------------------------------------------- |
| `SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE`       | `2`           | Conexoes minimas ociosas no pool.                                    |
| `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE`  | `5`           | Tamanho maximo do pool (subir para >= 10 em prod com carga).         |
| `SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT` | `10000`       | Timeout (ms) para obter conexao do pool.                             |

---

## 4. Servidor HTTP

| Variavel       | Default | Descricao                                              |
| -------------- | ------- | ------------------------------------------------------ |
| `APP_PORT`     | `8082`  | Porta exposta no host (compose).                       |
| `SERVER_PORT`  | `8082`  | Porta interna do Tomcat (`server.port`).               |

---

## 5. Seguranca / JWT (HS256)

| Variavel                          | Default                                                          | Descricao                                                                                            |
| --------------------------------- | ---------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| `SYTTECH_JWT_SECRET`              | `change-me-change-me-change-me-change-me` (app.props) <br> `dev-secret-change-me-change-me-change-me-change-me` (.env) | **Chave HMAC** usada para assinar e verificar JWTs (access + refresh). Minimo 32 chars. Trocar em prod via env. |
| `SYTTECH_JWT_TTL`                 | `3600`                                                           | TTL do **access token** em segundos (default 1h).                                                    |
| `syttech.security.jwt.issuer`     | `syttech.scheduler`                                              | Claim `iss` dos JWTs gerados (`HmacJwtTokenIssuerAdapter`).                                          |

> O **refresh token** tem TTL fixo de 30 dias (hardcoded no `HmacJwtTokenIssuerAdapter`).
> O filtro `JwtAuthenticationFilter` apenas LEEM o token quando presente; nao
> ha protecao global (nao usa `spring-boot-starter-security`). Apenas endpoints
> que chamam `CurrentCustomer.requireId(...)` exigem autenticacao.

---

## 6. E-mail (Transactional)

A aplicacao tem **dois adapters** mutuamente exclusivos (selecionados via `@ConditionalOnProperty`):

| Adapter                              | Quando ativa                  | O que faz                                              |
| ------------------------------------ | ----------------------------- | ------------------------------------------------------ |
| `LoggingEmailNotificationAdapter`    | `syttech.email.enabled=false` (default) | Apenas escreve `[EMAIL] would send ...` no log.        |
| `SmtpEmailNotificationAdapter`       | `syttech.email.enabled=true`  | Envia de verdade via `JavaMailSender`/SMTP.            |

| Variavel                | Default                                                 | Descricao                                                                                                |
| ----------------------- | ------------------------------------------------------- | -------------------------------------------------------------------------------------------------------- |
| `SYTTECH_EMAIL_ENABLED` | `false`                                                 | Liga o `SmtpEmailNotificationAdapter`. **Sem isso, NAO ha envio real**.                                  |
| `SYTTECH_EMAIL_FROM`    | `no-reply@syttech.local` <br> ou `SytTech Scheduler <user@gmail.com>` | Endereco no campo `From:`. **No Gmail TEM que ser igual ao `MAIL_USERNAME`** ou o envio e rejeitado.    |
| `SYTTECH_APP_BASE_URL`  | `http://localhost:8082`                                 | URL base usada para montar links absolutos (verificacao de e-mail, link publico do agendamento).         |
| `MAIL_HOST`             | `localhost`                                             | Host SMTP. Gmail: `smtp.gmail.com`. Mailhog (dev): `localhost`.                                          |
| `MAIL_PORT`             | `1025` (default) / `587` (Gmail)                        | Porta SMTP.                                                                                              |
| `MAIL_USERNAME`         | vazio                                                   | Usuario SMTP. No Gmail = o e-mail completo da conta.                                                     |
| `MAIL_PASSWORD`         | vazio                                                   | Senha SMTP. **Gmail exige App Password** (https://myaccount.google.com/apppasswords).                    |
| `MAIL_SMTP_AUTH`        | `false`                                                 | Habilita autenticacao SMTP. Gmail = `true`.                                                              |
| `MAIL_SMTP_STARTTLS`    | `false`                                                 | Habilita STARTTLS. Gmail (porta 587) = `true`. Para SMTPS direto (porta 465) use `mail.smtp.ssl.enable`. |

### Eventos que disparam e-mail

| Evento                            | Disparado por                          | Listener                  | Template                              |
| --------------------------------- | -------------------------------------- | ------------------------- | ------------------------------------- |
| `CustomerRegisteredEvent`         | `RegisterCustomerUseCase`              | `CustomerEmailListener`   | Link `/auth/verify?token=...`         |
| `AppointmentConfirmedEvent`       | `ConfirmAppointmentUseCase`            | `AppointmentEmailListener`| Codigo + link `/appointments/{code}`  |
| `AppointmentRescheduledEvent`     | `RescheduleAppointmentUseCase`         | `AppointmentEmailListener`| Mesmo template (codigo + novo horario)|
| `AppointmentCancelledEvent`       | `CancelAppointmentUseCase`             | `AppointmentEmailListener`| Mesmo template                        |

> Todos os listeners sao `@TransactionalEventListener` (default `AFTER_COMMIT`),
> ou seja, o e-mail so e tentado depois do commit bem-sucedido no banco.

---

## 7. Negocio (Holds)

| Variavel                          | Default | Descricao                                                                                                       |
| --------------------------------- | ------- | --------------------------------------------------------------------------------------------------------------- |
| `scheduler.hold.ttl-minutes`      | `10`    | TTL (em minutos) de um Hold. Apos esse tempo, um job marca `consumed=true` (efeito "expirado"). Configuravel via env `SCHEDULER_HOLD_TTL_MINUTES`. |

---

## 8. Spring Modulith

A aplicacao usa **Spring Modulith** com persistencia JPA de eventos
(`spring-modulith-starter-jpa`). A tabela `event_publication` e criada
automaticamente pelo Modulith — nao e gerenciada pelo Flyway.

| Variavel                            | Default | Descricao                                                            |
| ----------------------------------- | ------- | -------------------------------------------------------------------- |
| `spring.modulith.events.jdbc-schema`| (none)  | Schema onde o Modulith persiste `event_publication`. Default = padrao. |

---

## 9. Docker Compose

| Variavel                       | Default | Descricao                                                                                       |
| ------------------------------ | ------- | ----------------------------------------------------------------------------------------------- |
| `SPRING_DOCKER_COMPOSE_ENABLED`| `false` | Desabilita o suporte do Spring Boot ao Compose quando rodando dentro do container (ja existe um Postgres dedicado). |

---

## 10. Resumo do `.env` minimo para SUBIR via Docker

```env
# Banco
POSTGRES_DB=syttechscheduler
POSTGRES_USER=myuser
POSTGRES_PASSWORD=secret
POSTGRES_PORT=5432

# App
APP_PORT=8082
SYTTECH_JWT_SECRET=troque-isto-por-uma-string-aleatoria-de-32+chars
SYTTECH_JWT_TTL=3600

# E-mail (deixe enabled=false se nao quiser enviar nada)
SYTTECH_EMAIL_ENABLED=false
SYTTECH_EMAIL_FROM=SytTech Scheduler <no-reply@syttech.local>
SYTTECH_APP_BASE_URL=http://localhost:8082

# Gmail (apenas se SYTTECH_EMAIL_ENABLED=true)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=seu-email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx   # App password — NUNCA commitar
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
```

> O arquivo `.env` esta no `.gitignore`. **Nunca** commitar valores reais.

