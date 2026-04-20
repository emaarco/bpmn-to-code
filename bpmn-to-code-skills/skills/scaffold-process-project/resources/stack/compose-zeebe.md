# Docker Compose — Zeebe / Camunda 8

Use this file for `ZEEBE`. Includes PostgreSQL, Camunda Orchestration (Zeebe + Operate +
Tasklist), and Elasticsearch. Sourced from emaarco/easy-zeebe `stack/docker-compose.yml`.

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

  orchestration:
    image: camunda/camunda:${CAMUNDA_VERSION}
    container_name: {{artifactId}}-zeebe
    ports:
      - "26500:26500"
      - "9600:9600"
      - "8080:8080"
    restart: always
    healthcheck:
      test: ["CMD-SHELL", "bash -c 'exec 3<>/dev/tcp/127.0.0.1/9600 && echo -e \"GET /actuator/health/status HTTP/1.1\\r\\nHost: localhost\\r\\n\\r\\n\" >&3 && head -n 1 <&3'"]
      interval: 1s
      retries: 30
      start_period: 30s
    configs:
      - source: orchestration-config
        target: /usr/local/camunda/config/application.yaml
    depends_on:
      elasticsearch:
        condition: service_healthy
    networks: [camunda]

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:${ELASTIC_VERSION}
    container_name: elasticsearch
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - cluster.routing.allocation.disk.threshold_enabled=false
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cat/health | grep -q green"]
      interval: 1s
      retries: 30
      start_period: 30s
      timeout: 1s
    networks: [camunda]

volumes:
  elastic:
  zeebe:

networks:
  camunda:

configs:
  orchestration-config:
    content: |
      camunda:
        security:
          authentication:
            method: "basic"
            unprotectedApi: true
          authorizations:
            enabled: false
          initialization:
            users:
              - username: "demo"
                password: "demo"
                name: "Demo User"
                email: "demo@demo.com"
        data:
          secondary-storage:
            type: elasticsearch
            elasticsearch:
              url: "http://elasticsearch:9200"
```

---

Write verbatim to `stack/.env`:

```env
POSTGRES_VERSION=16
CAMUNDA_VERSION=8.7.0
ELASTIC_VERSION=8.13.0
```

---

**Start command:** `cd stack && docker compose up -d`  
**Zeebe UI:** http://localhost:8080 (demo / demo)
