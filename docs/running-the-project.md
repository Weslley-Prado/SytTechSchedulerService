# Como Rodar o SytTech Scheduler Service

Guia operacional cobrindo os três ambientes — **Desenvolvimento (dev)**,
**Homologação (hml)** e **Produção (prod)**.

> Requisitos comuns:
> - **JDK 25** (Eclipse Temurin recomendado)
> - **Maven Wrapper** (`./mvnw`) — já incluso no repositório
> - **Docker** + **Docker Compose** (qualquer ambiente containerizado)
> - **PostgreSQL 16+** (provisionado via Compose em dev/hml; gerenciado em prod)
>
> Variáveis e seus significados: ver [`environment-variables.md`](./environment-variables.md).
> Fluxos: [`flows.md`](./flows.md). Banco: [`database.md`](./database.md).

---

## 0. Mapa de profiles do Spring

| Profile  | Arquivo                              | Quando usar                                            | Origem do schema |
|----------|--------------------------------------|--------------------------------------------------------|------------------|
| `dev`    | `application-dev.properties`         | Workstation do dev (com `compose.yaml`).               | Flyway `migration` + `seed`. |
| `local`  | `application-local.properties` (gitignored) | Sobrescreve `dev` com **segredos pessoais** (SMTP).   | Hibernate `update` (sem Flyway). |
| `hml`    | *(não existe — usar `prod` + `.env.hml`)* | Servidor de QA / staging.                            | Flyway `migration` apenas. |
| `prod`   | `application-prod.properties`        | Produção.                                              | Flyway `migration` apenas, `validate-on-migrate=true`, `clean-disabled=true`. |

> **Regra de ouro:** `db/seed` (massa de demo) **só** é carregado em `dev`.
> Nunca em `hml`/`prod`.

---

## 1. Desenvolvimento (dev)

Objetivo: subir tudo localmente em <60 s, com banco populado por seeds e
e-mails apenas no log (sem SMTP).

### 1.1 Subir tudo via Docker Compose (recomendado)

```bash
# 1) clone e entre no diretório
cd SytTechSchedulerService

# 2) copie o .env de exemplo (cria valores default seguros)
cp .env.example .env       # se existir; senão veja a seção 10 de environment-variables.md

# 3) suba a stack (Postgres + App)
docker compose up --build

# 4) valide
curl http://localhost:8082/actuator/health
# → {"status":"UP"}
```

Acessos:
- **API**: `http://localhost:8082`
- **Health**: `http://localhost:8082/actuator/health`
- **Postgres**: `localhost:5432` (user `myuser` / pass `secret`)
- **Logs do app**: `docker compose logs -f app`
- **Logs do banco**: `docker compose logs -f postgres`

Operações úteis:
```bash
docker compose down            # para os containers, mantém volume
docker compose down -v         # zera TUDO (perde dados do banco)
docker compose restart app     # reinicia só a app
docker compose exec postgres psql -U myuser -d syttechscheduler
```

### 1.2 Rodar a app fora do container, banco no Compose

Útil para hot-reload no IDE.

```bash
# 1) sobe só o Postgres
docker compose up -d postgres

# 2) roda a app (profile dev por padrão)
./mvnw spring-boot:run

# OU com o JAR
./mvnw clean package -DskipTests
java -jar target/SytTechSchedulerService-0.0.1-SNAPSHOT.jar
```

### 1.3 Profile `local` (e-mail real via Gmail, opcional)

Para receber e-mails de verdade no seu Gmail durante o dev:

```bash
# 1) gere uma App Password em https://myaccount.google.com/apppasswords
# 2) edite src/main/resources/application-local.properties (gitignored)
#    com seu MAIL_USERNAME e MAIL_PASSWORD
# 3) suba com ambos profiles ativos:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,local
```

> ⚠️ `application-local.properties` **NUNCA** pode ser commitado (já está no
> `.gitignore`). Se uma senha vazar, **revogue imediatamente** em
> https://myaccount.google.com/apppasswords.

### 1.4 Testes

```bash
./mvnw test                                  # unitários
./mvnw verify                                # unit + integração + checkstyle + pmd + spotless
./mvnw checkstyle:check pmd:check spotless:check   # apenas qualidade
```

### 1.5 Seeds de demonstração

Arquivos `R__*.sql` em `src/main/resources/db/seed/` são reaplicados sempre
que o checksum muda. Eles ficam **idempotentes** (`INSERT ... ON CONFLICT DO NOTHING`).
Para zerar e recriar:
```bash
docker compose down -v && docker compose up --build
```

---

## 2. Homologação (hml / staging)

Ambiente equivalente a produção, mas com:
- banco isolado;
- e-mails para um domínio de teste (Mailtrap / Mailpit / inbox específico);
- segredos próprios (`SYTTECH_JWT_SECRET` diferente do prod);
- **sem** seeds, **com** validação de migrations ligada.

> **Não existe `application-hml.properties`.** A recomendação é reutilizar o
> profile `prod` e diferenciar tudo por variáveis de ambiente.

### 2.1 Build do artefato

Pipeline CI (GitHub Actions / GitLab CI) deve executar:

```bash
./mvnw -B clean verify
./mvnw -B package -DskipTests -Dmaven.antrun.skip=true
```

Artefato final: `target/SytTechSchedulerService-0.0.1-SNAPSHOT.jar`.

### 2.2 Build da imagem Docker

```bash
docker build -t syttech/scheduler:hml-$(git rev-parse --short HEAD) .
docker tag syttech/scheduler:hml-$(git rev-parse --short HEAD) syttech/scheduler:hml-latest
docker push syttech/scheduler:hml-latest
```

### 2.3 `.env.hml` (no servidor de HML, **fora do repo**)

```env
SPRING_PROFILES_ACTIVE=prod

# Banco — servidor gerenciado de HML
DB_URL=jdbc:postgresql://hml-db.internal:5432/syttechscheduler_hml
DB_USERNAME=syttech_hml
DB_PASSWORD=<senha-hml-rotacionada>

# App
SERVER_PORT=8082
APP_PORT=8082

# JWT — segredo PRÓPRIO de HML (32+ chars, NUNCA igual ao prod)
SYTTECH_JWT_SECRET=<openssl rand -base64 48>
SYTTECH_JWT_TTL=3600

# E-mail — Mailtrap / Mailpit / inbox de QA
SYTTECH_EMAIL_ENABLED=true
SYTTECH_EMAIL_FROM=SytTech HML <no-reply@hml.syttech.com>
SYTTECH_APP_BASE_URL=https://hml.scheduler.syttech.com
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=587
MAIL_USERNAME=<mailtrap-user>
MAIL_PASSWORD=<mailtrap-pass>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

# Pool — meio termo entre dev e prod
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=10

# Modulith / Flyway — defaults de prod
SPRING_FLYWAY_LOCATIONS=classpath:db/migration
SPRING_FLYWAY_VALIDATE_ON_MIGRATE=true
SPRING_FLYWAY_CLEAN_DISABLED=true
```

### 2.4 Rodar em HML

```bash
docker run -d \
  --name syttech-scheduler-hml \
  --restart=unless-stopped \
  --env-file /etc/syttech/.env.hml \
  -p 8082:8082 \
  syttech/scheduler:hml-latest
```

Ou via `docker-compose.hml.yaml` apontando para o banco externo:
```bash
docker compose -f docker-compose.hml.yaml --env-file .env.hml up -d
```

### 2.5 Smoke tests pós-deploy

```bash
# Health
curl -fsS https://hml.scheduler.syttech.com/actuator/health
# Listar unidades (sanity)
curl -fsS "https://hml.scheduler.syttech.com/api/v1/units?page=0&size=5"
# Login com user de QA
curl -fsS -X POST https://hml.scheduler.syttech.com/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"qa@syttech.com","password":"<senha-qa>"}'
```

Coleção Postman: `postman/SytTech-Scheduler.postman_collection.json` — basta
trocar a variável `baseUrl` para o host de HML.

### 2.6 Rollback em HML

```bash
docker stop syttech-scheduler-hml && docker rm syttech-scheduler-hml
docker run -d --name syttech-scheduler-hml \
  --env-file /etc/syttech/.env.hml -p 8082:8082 \
  syttech/scheduler:hml-<sha-anterior>
```

> Migrations do Flyway **não rebobinam** automaticamente. Sempre faça
> deploy de schema → app, e mantenha migrations **aditivas** (compatíveis
> com a versão anterior por ao menos um release).

---

## 3. Produção (prod)

Ambiente público, com tráfego real. Diferenças em relação a HML:
- segredos rotacionados via cofre (AWS Secrets Manager / Vault / GCP Secret Manager);
- pool de conexões maior;
- métricas/observabilidade obrigatórias;
- backup automático do Postgres;
- *blue/green* ou *rolling* deploy.

### 3.1 Checklist obrigatório antes do primeiro deploy

- [ ] `SYTTECH_JWT_SECRET` **rotacionado** (não usar o default; mínimo 32 chars random).
- [ ] `DB_PASSWORD` e `MAIL_PASSWORD` em **cofre de segredos** (não em `.env` no disco).
- [ ] TLS terminado em ingress/load-balancer (a app fala HTTP interno).
- [ ] Backup do Postgres habilitado (PITR ou snapshots diários).
- [ ] `spring.flyway.clean-disabled=true` (já é o default em prod).
- [ ] `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` (já é o default em prod).
- [ ] Pasta `db/seed` **excluída** das migrations (já é o default em prod).
- [ ] Healthcheck `/actuator/health` exposto ao orquestrador (k8s liveness/readiness, ECS healthcheck, etc.).
- [ ] Logs estruturados sendo coletados (stdout → CloudWatch / Loki / ELK).
- [ ] Alertas configurados para `5xx`, latência p95, profundidade do pool Hikari.

### 3.2 Build do artefato de produção

```bash
# Em pipeline CI, em cima de uma tag git
git tag v0.1.0 && git push origin v0.1.0

./mvnw -B clean verify
docker build -t registry.syttech.com/scheduler:v0.1.0 .
docker push registry.syttech.com/scheduler:v0.1.0
```

### 3.3 `.env.prod` (lido do cofre, **nunca em disco persistente**)

```env
SPRING_PROFILES_ACTIVE=prod

# Banco — instância gerenciada com SSL
DB_URL=jdbc:postgresql://prod-db.internal:5432/syttechscheduler?sslmode=require
DB_USERNAME=syttech_app
DB_PASSWORD=${secrets:syttech/scheduler/db_password}

# App
SERVER_PORT=8082

# JWT
SYTTECH_JWT_SECRET=${secrets:syttech/scheduler/jwt_secret}
SYTTECH_JWT_TTL=3600

# E-mail (SES, SendGrid, Postmark...)
SYTTECH_EMAIL_ENABLED=true
SYTTECH_EMAIL_FROM=SytTech <no-reply@syttech.com>
SYTTECH_APP_BASE_URL=https://scheduler.syttech.com
MAIL_HOST=email-smtp.sa-east-1.amazonaws.com
MAIL_PORT=587
MAIL_USERNAME=${secrets:syttech/scheduler/smtp_user}
MAIL_PASSWORD=${secrets:syttech/scheduler/smtp_pass}
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true

# Pool — produção sob carga
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=5
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=20
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=10000

# Hold TTL — produção típica 10 min
SCHEDULER_HOLD_TTL_MINUTES=10
```

### 3.4 Rodar em produção

#### Opção A — Docker standalone (servidor único)
```bash
docker run -d \
  --name syttech-scheduler \
  --restart=always \
  --env-file /run/secrets/syttech-scheduler.env \
  -p 8082:8082 \
  --memory=1g --cpus=1 \
  registry.syttech.com/scheduler:v0.1.0
```

#### Opção B — Kubernetes (Deployment + Service + HPA)
```yaml
# trecho ilustrativo — versionar em outro repo de infra
apiVersion: apps/v1
kind: Deployment
metadata: { name: scheduler }
spec:
  replicas: 2
  strategy: { type: RollingUpdate, rollingUpdate: { maxUnavailable: 0, maxSurge: 1 } }
  template:
    spec:
      containers:
        - name: app
          image: registry.syttech.com/scheduler:v0.1.0
          ports: [{ containerPort: 8082 }]
          envFrom: [{ secretRef: { name: scheduler-env } }]
          readinessProbe: { httpGet: { path: /actuator/health, port: 8082 }, initialDelaySeconds: 30, periodSeconds: 10 }
          livenessProbe:  { httpGet: { path: /actuator/health, port: 8082 }, initialDelaySeconds: 60, periodSeconds: 30 }
          resources:
            requests: { cpu: 250m, memory: 512Mi }
            limits:   { cpu: "1",  memory: 1Gi }
```

#### Opção C — `java -jar` direto (VM tradicional)
```bash
java -XX:+UseContainerSupport -XX:MaxRAMPercentage=75 \
     -jar app.jar --spring.profiles.active=prod
```

### 3.5 Migrations em produção

- **Flyway roda no startup da app.** O primeiro pod a subir aplica as
  migrations; os demais ficam esperando o `flyway_schema_history` ser liberado.
- Em deploys com múltiplas instâncias, prefira **migrations aditivas**
  (adicionar coluna nullable, criar índice CONCURRENTLY etc.) para evitar
  janelas de incompatibilidade entre versões antigas e novas rodando juntas.
- Migrations destrutivas (drop column, rename) → seguir o padrão
  **expand → migrate → contract** em 3 releases.

### 3.6 Observabilidade mínima

| Métrica                      | Como                                            |
|------------------------------|-------------------------------------------------|
| Health                       | `GET /actuator/health` (já habilitado)         |
| Metrics Prometheus           | adicionar `micrometer-registry-prometheus` + `GET /actuator/prometheus` |
| Logs                         | stdout em JSON (configurar `logback-spring.xml`) |
| Tracing                      | OpenTelemetry agent como `-javaagent`           |
| Pool Hikari                  | métricas `hikaricp.connections.*`              |
| Eventos Modulith pendentes   | query em `event_publication WHERE completion_date IS NULL` |

### 3.7 Backup / restore do banco

```bash
# Backup lógico (cron diário)
pg_dump -h prod-db.internal -U syttech_app -F c -f scheduler-$(date +%F).dump syttechscheduler

# Restore para HML (validação de backup)
pg_restore -h hml-db.internal -U syttech_hml -d syttechscheduler_hml \
           --clean --if-exists scheduler-2026-05-20.dump
```

### 3.8 Rollback em produção

1. **App**: redeploy da imagem anterior (`registry.syttech.com/scheduler:v0.0.9`).
2. **Banco**: NUNCA reverta migrations via `flyway:undo` em prod. Se uma
   migration causou dano, escreva uma **nova migration corretiva**
   (`V99__fix_xxx.sql`) e deixe na frente.
3. Em caso catastrófico: restore do snapshot mais recente (RPO documentado).

---

## 4. Comparação rápida dev × hml × prod

| Aspecto                       | dev                          | hml                          | prod                         |
|-------------------------------|------------------------------|------------------------------|------------------------------|
| Profile Spring                | `dev` (`+local` opcional)    | `prod` + `.env.hml`          | `prod` + cofre               |
| Origem do schema              | Flyway `migration` + `seed`  | Flyway `migration`           | Flyway `migration`           |
| `hibernate.ddl-auto`          | `validate` (`update` em local)| `validate`                  | `validate`                   |
| `flyway.validate-on-migrate`  | default                      | `true`                       | `true`                       |
| `flyway.clean-disabled`       | default                      | `true`                       | `true`                       |
| Pool Hikari (max)             | 5                            | 10                           | 20                           |
| JWT secret                    | default dev (inseguro)       | rotacionado por env          | rotacionado em cofre         |
| E-mail                        | log (`syttech.email.enabled=false`) | Mailtrap/SMTP teste     | SES/SendGrid real            |
| TLS                           | não                          | terminado em proxy           | obrigatório                  |
| Backup do banco               | volume Docker                | snapshot diário              | PITR + snapshot              |
| `show-sql`                    | `true`                       | `false`                      | `false`                      |
| Seed data                     | sim                          | **não**                      | **não**                      |

---

## 5. Troubleshooting comum

| Sintoma                                                | Causa provável                                                                 | Solução |
|--------------------------------------------------------|--------------------------------------------------------------------------------|---------|
| App não sobe — `Schema-validation: missing table`      | Migration nova não foi aplicada / pasta `db/seed` incluída em prod.            | Verifique `SPRING_FLYWAY_LOCATIONS` e os arquivos `V*.sql`. |
| `FlywayValidateException` no startup                   | Checksum de migration já aplicada mudou.                                       | Nunca edite migration já aplicada — escreva nova. Em dev: `docker compose down -v`. |
| `409 Conflict` em `POST /appointments/holds`           | Outro cliente acabou de reservar o mesmo slot (índice UNIQUE parcial).         | Cliente deve buscar `/availability` de novo. |
| `410 Gone` em `POST /appointments`                     | Hold expirou (TTL `scheduler.hold.ttl-minutes`).                               | Recriar o hold. |
| `401 Unauthorized` em endpoints autenticados           | Sem header `Authorization: Bearer ...` ou JWT expirado.                        | Chamar `/auth/refresh` ou `/auth/login`. |
| `[EMAIL] would send ...` no log mas e-mail não chega   | `SYTTECH_EMAIL_ENABLED=false` (default).                                       | Setar `true` + credenciais SMTP. |
| Gmail: `535-5.7.8 Username and Password not accepted`  | Senha normal em vez de **App Password**, ou `FROM` ≠ `MAIL_USERNAME`.          | Gerar App Password; alinhar `from` com o usuário. |
| Build do container falha em `mvnw dependency:go-offline`| Cache Maven sujo / proxy.                                                     | `./mvnw -U clean package` ou liberar saída para `repo.maven.apache.org`. |

---

## 6. Comandos resumidos (cola na parede)

```bash
# DEV
docker compose up --build                                      # tudo
docker compose down -v                                         # reset total
./mvnw spring-boot:run                                         # app local
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,local    # com Gmail real
./mvnw verify                                                  # CI local

# HML / PROD
./mvnw -B clean package -DskipTests
docker build -t syttech/scheduler:<tag> .
docker run -d --env-file .env.<amb> -p 8082:8082 syttech/scheduler:<tag>

# Healthcheck
curl -fsS http://localhost:8082/actuator/health
```

