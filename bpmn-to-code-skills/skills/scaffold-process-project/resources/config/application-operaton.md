# Spring Config — OPERATON

Two profiles. Substitute `{{artifactId}}` with the actual artifact ID.

---

## `src/main/resources/application.yml`

Full-stack profile. Requires `docker compose up` in `stack/`.

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/{{artifactId}}
    username: admin
    password: admin
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.PostgreSQLDialect

operaton:
  bpm:
    admin-user:
      id: admin
      password: admin
    database:
      type: postgresql
      schema-update: true
```

---

## `src/main/resources/application-local.yml`

Local dev profile — H2 in-memory, no Docker needed.
Activate with `--spring.profiles.active=local`.

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:{{artifactId}};DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect

operaton:
  bpm:
    database:
      type: h2
```
