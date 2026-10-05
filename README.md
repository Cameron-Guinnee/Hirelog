# Hirelog

A single-user REST API for tracking job applications: companies, applications, contacts and follow-ups, with a full status history, weekly stats and a daily job that flags follow-ups as they come due.

Spring Boot 3.3 · Java 21 · PostgreSQL 16 · Flyway · JWT

## Run it

```bash
cp .env.example .env   # then set HIRELOG_PASSWORD and HIRELOG_JWT_SECRET
docker compose up --build
```

The API listens on `http://localhost:8080`. To run against the Compose database from your IDE instead, start only `db` (`docker compose up db`) and set `HIRELOG_USERNAME`, `HIRELOG_PASSWORD` and `HIRELOG_JWT_SECRET` in the run configuration.

## Authenticate

```bash
curl -s -X POST localhost:8080/api/auth/token \
  -H 'Content-Type: application/json' \
  -d '{"username":"me","password":"change-me"}'
```

Send the returned `accessToken` as `Authorization: Bearer <token>` on every other call. Tokens last 12 hours (`hirelog.security.token-ttl`).

## Endpoints

| Method | Path | Notes |
| --- | --- | --- |
| `GET/POST` | `/api/companies` | |
| `GET/PUT/DELETE` | `/api/companies/{id}` | Deleting a company deletes its applications and contacts |
| `GET/POST` | `/api/applications` | `?status=INTERVIEWING` filters; status defaults to `APPLIED` |
| `GET/PUT/DELETE` | `/api/applications/{id}` | `PUT` edits details; status changes go through `PATCH .../status` |
| `PATCH` | `/api/applications/{id}/status` | `{"status":"SCREENING","note":"..."}`, appended to history |
| `GET` | `/api/applications/{id}/history` | Every status transition, oldest first |
| `GET/POST` | `/api/contacts` | `?companyId=` filters |
| `GET/PUT/DELETE` | `/api/contacts/{id}` | |
| `GET/POST` | `/api/follow-ups` | `?applicationId=` and `?pending=true` filter |
| `GET/PUT/DELETE` | `/api/follow-ups/{id}` | |
| `POST` | `/api/follow-ups/{id}/complete` | |
| `GET` | `/api/follow-ups/due` | Pending follow-ups the scheduler has flagged |
| `GET` | `/api/stats/weekly` | `?weekOf=2026-10-05`, defaults to this week |
| `GET` | `/actuator/health` | Public |

Statuses: `SAVED`, `APPLIED`, `SCREENING`, `INTERVIEWING`, `OFFER`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`, `GHOSTED`.

### Weekly stats

Stats cover applications whose `appliedOn` date falls in the ISO week (Monday to Sunday) containing `weekOf`.

- `applicationsSent`: how many were sent that week.
- `responseRate`: the share that moved past `APPLIED` at any point, rejections included.
- `stages`: a funnel `APPLIED → SCREENING → INTERVIEWING → OFFER`. Each stage counts applications that reached it or went further (taken from status history), and `conversionRate` is that count divided by the previous stage's.

### Follow-up flagging

`FollowUpScheduler` runs on `hirelog.follow-ups.flag-cron` (default `0 0 8 * * *`, 08:00 server time) and stamps `flaggedAt` on every pending follow-up due today or earlier. Moving a flagged follow-up's due date into the future clears the flag. Set `hirelog.scheduling.enabled=false` to turn the job off.

## Develop

```bash
mvn test     # unit tests (JUnit 5 + Mockito)
mvn verify   # plus Testcontainers integration tests; needs Docker
```

Schema changes go in a new `src/main/resources/db/migration/V<n>__*.sql`; Hibernate runs with `ddl-auto: validate`, so the entities must match. CI (`.github/workflows/ci.yml`) runs `mvn verify` on every push and pull request.
