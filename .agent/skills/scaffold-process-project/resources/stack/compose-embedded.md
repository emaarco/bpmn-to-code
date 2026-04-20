# Docker Compose — Embedded Engines (CAMUNDA_7 / OPERATON)

Use this file for `CAMUNDA_7` and `OPERATON`. Only PostgreSQL is needed;
the process engine runs embedded inside the Spring Boot application.

Write verbatim to `stack/compose.yaml`. Substitute `{{artifactId}}` with the actual artifact ID.

---

```yaml
# Use this only in dev environments. It's not intended for production usage.
services:

  postgres:
    image: postgres:${POSTGRES_VERSION}
    container_name: postgres
    ports:
      - "5432:5432"
    restart: always
    environment:
      POSTGRES_DB: {{artifactId}}
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin
```

---

Write verbatim to `stack/.env`:

```env
POSTGRES_VERSION=16
```

---

**Start command:** `cd stack && docker compose up -d`
