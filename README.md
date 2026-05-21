# SytTechSchedulerService

Backend do **SytTech Scheduler** — gerencia agendamentos e expõe uma API REST.

-   Spring Boot 4 · Java 25 · Spring Modulith
-   Arquitetura hexagonal (igual ao módulo `crm` do `SytTechPortalService`):
    ```
    scheduler/
    ├── adapter/
    │   ├── input/{web,graphql,soap}
    │   └── output/{persistence,client}
    ├── config/                # beans do módulo
    ├── domain/
    │   ├── event/
    │   ├── model/
    │   └── service/
    ├── ports/
    │   ├── in/                # use case interfaces
    │   └── out/               # ports de saída (persistence/client)
    ├── repository/            # JPA repositories
    └── usecase/               # use case implementations (@Service)
    ```
-   Fronteiras do módulo declaradas em `scheduler/package-info.java` via
    `@ApplicationModule(allowedDependencies = {"shared::kernel"})`.
-   Exceções base no `shared::kernel`
    (`DomainValidationException` → 422, `ResourceNotFoundException` → 404).

## Requisitos

-   Java 24+
-   Docker (para subir o Postgres via `compose.yaml`)
-   Maven Wrapper incluído (`./mvnw`)

## Quickstart

```bash
cd syttechscheduler/SytTechSchedulerService

# 1. Banco local (Postgres na 5432 — ajuste DB_NAME se quiser)
docker compose up -d

# 2. Build + testes
./mvnw clean verify

# 3. Run (porta 8082)
./mvnw spring-boot:run
```

## Ciclo de desenvolvimento

| Comando                     | O que faz                                                    |
| --------------------------- | ------------------------------------------------------------ |
| `./mvnw test`               | Testes unitários                                             |
| `./mvnw spring-boot:run`    | Sobe o serviço em `http://localhost:8082`                    |
| `./mvnw package`            | Gera `target/SytTechSchedulerService-0.0.1-SNAPSHOT.jar`   |
| `./mvnw generate-sources`   | Regenera código a partir dos OpenAPI YAMLs em `contract/input/` (quando houver) |
| `./mvnw spotless:apply`     | Formata tudo (Google Java Format — AOSP)                     |
| `./mvnw checkstyle:check`   | Valida estilo                                                |
| `./mvnw pmd:check`          | Detecta code smells                                          |

Pre-commit hook é instalado automaticamente pelo plugin `maven-antrun-plugin` na fase `initialize`
e executa `spotless:apply`, `checkstyle:check` e `pmd:check` antes de cada commit.

## Contract-first (OpenAPI)

Adicione um YAML em `src/main/resources/contract/input/<feature>-api.yaml` e, no `pom.xml`, declare
uma execução do `openapi-generator-maven-plugin` (o plugin já está presente sem execuções). Depois
disso, o controller implementa a interface gerada — mesmo padrão do `CustomerController` no
`SytTechPortalService`.

## Migrations

Toda mudança de schema é um arquivo novo em `src/main/resources/db/migration/V*__descricao.sql`.
Nunca edite migrations já aplicadas.

## Profiles

-   `dev` (default): aponta para o Postgres local do `compose.yaml`, `ddl-auto=validate`, logs SQL ligados.
-   `prod`: variáveis de ambiente `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT`.

## Portas

-   Dev/Prod: `8082` (o `SytTechPortalService` usa `8081`, então podem rodar lado a lado).

