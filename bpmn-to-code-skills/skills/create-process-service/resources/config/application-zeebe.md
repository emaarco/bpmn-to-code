# Spring Config — ZEEBE

Two profiles. Substitute `{{artifactId}}` with the actual artifact ID.

Note: Zeebe is always external — there is no H2 shortcut. Both profiles require the
Docker stack to be running (`cd stack && docker compose up -d`).

---

## `src/main/resources/application.yml`

```yaml
server:
  port: 8090  # Avoids clash with Zeebe's own port 8080

zeebe:
  client:
    broker:
      gateway-address: localhost:26500
    security:
      plaintext: true
```

---

## `src/main/resources/application-local.yml`

```yaml
# Zeebe must be running: cd stack && docker compose up -d
zeebe:
  client:
    broker:
      gateway-address: localhost:26500
    security:
      plaintext: true
```
