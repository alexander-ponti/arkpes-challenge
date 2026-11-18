# Ark Funds API

REST API for managing **funds**, **investors**, and the **transactions** investors make against funds, with basic reporting.
Java 25 · Spring Boot 4 · Spring Data JPA · H2 (file-backed) · OpenAPI/Swagger UI.

## Run it (one command)

```bash
docker compose up --build
```

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Health: <http://localhost:8080/actuator/health>

No Docker? Requires JDK 25:

Start locally:

- macOS/Linux: `./gradlew bootRun`
- Windows: `.\gradlew.bat bootRun`

Run tests:

- macOS/Linux: `./gradlew test`
- Windows: `.\gradlew.bat test`

## Domain model

- **Fund** and **Investor** are independent entities. The many-to-many relationship is derived from transactions:
  a fund's investors are those who have transacted with it, and an investor's funds are those they have transacted with.
- **Transaction** = investor + fund + type + amount + date. Amount is always positive; the effect on the fund comes from the type:

| Type | Effect |
|---|---|
| CONTRIBUTION | Credit |
| INTEREST_INCOME | Credit |
| DISTRIBUTION | Debit |
| GENERAL_EXPENSE | Debit |
| MANAGEMENT_FEE | Debit |

Net balance = total credits − total debits.

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST / GET | `/api/funds` | Create / list (paged) |
| GET / PUT / DELETE | `/api/funds/{id}` | Read / update / delete (409 if it has transactions) |
| GET | `/api/funds/{id}/investors` | Investors in the fund |
| GET | `/api/funds/{id}/report?from=&to=` | Credits, debits, net, totals by type, per-investor positions |
| POST / GET | `/api/investors` | Create / list (paged) |
| GET / PUT / DELETE | `/api/investors/{id}` | Read / update / delete (409 if it has transactions) |
| GET | `/api/investors/{id}/funds` | Funds the investor participates in |
| GET | `/api/investors/{id}/report?from=&to=` | Net position per fund and overall |
| POST | `/api/transactions` | Create |
| GET | `/api/transactions?fundId=&investorId=&type=&from=&to=&page=&size=` | Search (paged, filterable) |
| GET / PUT / DELETE | `/api/transactions/{id}` | Read / update / delete |

Dates are ISO (`2024-03-31`). Report `from`/`to` are inclusive and optional.

### Quick example

```bash
curl -X POST localhost:8080/api/funds -H 'Content-Type: application/json' -d '{"name":"Growth Fund I"}'
curl -X POST localhost:8080/api/investors -H 'Content-Type: application/json' -d '{"name":"Alice","email":"alice@example.com"}'
curl -X POST localhost:8080/api/transactions -H 'Content-Type: application/json' \
  -d '{"fundId":1,"investorId":1,"type":"CONTRIBUTION","amount":1000.00,"transactionDate":"2024-01-10"}'
curl localhost:8080/api/funds/1/report
```

## Design notes

- Layered: controller → service → repository; DTO records keep the API separate from entities.
- Money uses `BigDecimal` (`NUMERIC(19,4)`), never floating point.
- Bean Validation on input; errors are RFC 7807 problem responses (400 validation, 404 missing, 409 conflicts such as duplicate fund names / investor emails or deleting entities that have transactions).
- Reports aggregate in the database (`GROUP BY`) rather than loading every transaction. Indexes on fund+date and investor.
- Paged list endpoints; transaction search built with JPA Specifications.
- Data persists in `./data` (Docker volume `ark-data`).

## Production hardening (out of scope here)

- Replace H2 with PostgreSQL, etc and manage the schema with Flyway/Liquibase (`ddl-auto: update` is for convenience only).
- Authentication/authorization and per-client tenant isolation (clients sign up, so each client's funds and investors should be scoped by a tenant id).
- Audit trail / soft delete for financial records, idempotency keys on transaction creation, optimistic locking.
- Metrics/tracing, CI pipeline, rate limiting.
