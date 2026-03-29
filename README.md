# ms-account-service

Microserviço de **contas** (Account) em **Spring Boot**.

## Stack

- Java 17+
- Spring Boot 3.x
- Maven
- PostgreSQL (runtime)
- H2 (testes)

## Requisitos

- Java 17+
- Maven 3.9+
- PostgreSQL (para rodar local com persistência)

## Configuração (variáveis de ambiente)

O serviço foi preparado para sobrescrever configurações via variáveis de ambiente:

- `MS_ACCOUNT_DATASOURCE_URL` (default local: `jdbc:postgresql://localhost:5433/annotation_app`, alinhado ao `docker-compose.yml`)
- `MS_ACCOUNT_DATASOURCE_USERNAME` (default: `annotation_app`)
- `MS_ACCOUNT_DATASOURCE_PASSWORD` (default: `1234` — apenas desenvolvimento local)
- `MS_ACCOUNT_DDL_AUTO` (default: `update`)

## Como rodar localmente

1) Suba um PostgreSQL local (ou use o seu existente).
2) (Opcional) Exportar variáveis de ambiente.
3) Rodar a aplicação:

```bash
mvn spring-boot:run
```

## Como rodar os testes

Os testes usam **H2** com compatibilidade PostgreSQL.

```bash
mvn test
```

## Postman

Há uma collection em `ms-account-service.postman_collection.json`.

## Observabilidade

- Actuator expõe `health` (ver `management.endpoints.web.exposure.include`).

