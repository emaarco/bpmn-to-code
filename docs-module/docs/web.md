# Web Application

Browser-based code generation. No Gradle or Maven required.

## Why Web?

- **No Installation**: Use directly in your browser
- **Quick Experiments**: Test bpmn-to-code without project setup
- **Cross-Platform**: Works anywhere with a web browser
- **Self-Hostable**: Deploy in your own infrastructure
- **Same Core**: 100% identical output to Gradle and Maven plugins

## Hosted Version

**Live at**: [bpmn-to-code.miragon.io](https://bpmn-to-code.miragon.io/static/index.html)

### How to Use

1. **Upload BPMN files** - Drag & drop or file picker
2. **Configure settings**:
   - Output language (Kotlin or Java)
   - Process engine (Camunda 7, Zeebe, or Operaton)
   - Package path
3. **Generate** - Click generate button
4. **Download** - Individual files or ZIP with all generated code

### Features

- Syntax highlighting for generated code
- Preview before download
- Multiple file upload support
- ZIP download for all files
- Stateless processing (no data persistence)

## Self-Hosting

### Docker (Recommended)

Pull and run the container:

```bash
docker pull emaarco/bpmn-to-code-web:latest
docker run -p 8080:8080 emaarco/bpmn-to-code-web:latest
```

Access at `http://localhost:8080`

**Docker Hub**: [emaarco/bpmn-to-code-web](https://hub.docker.com/r/emaarco/bpmn-to-code-web)

### Docker Compose

```yaml
version: '3.8'

services:
  bpmn-to-code-web:
    image: emaarco/bpmn-to-code-web:latest
    ports:
      - "8080:8080"
    environment:
      - SERVER_PORT=8080
    restart: unless-stopped
```

Run with:

```bash
docker-compose up -d
```

### From Source

**Prerequisites**:

- JDK 21+
- Gradle 8+

**Steps**:

```bash
# Clone repository
git clone https://github.com/emaarco/bpmn-to-code.git
cd bpmn-to-code

# Run web application
./gradlew :bpmn-to-code-web:run
```

Access at `http://localhost:8080`

## API Documentation

The web application also exposes a REST API for programmatic access:

- **Swagger UI**: `http://localhost:8080/swagger`
- **OpenAPI/ReDoc**: `http://localhost:8080/openapi`

### Example API Usage

```bash
# Upload BPMN and generate API
curl -X POST http://localhost:8080/api/generate \
  -F "file=@process.bpmn" \
  -F "outputLanguage=KOTLIN" \
  -F "processEngine=ZEEBE" \
  -F "packagePath=com.example"
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | HTTP port | `8080` |
| `SERVER_HOST` | Bind address | `0.0.0.0` |

### Docker Example

```bash
docker run \
  -p 9000:9000 \
  -e SERVER_PORT=9000 \
  emaarco/bpmn-to-code-web:latest
```

## Security Considerations

The web application is fully stateless:

- **No file persistence**: Uploaded BPMN files are processed in memory and never saved
- **No database**: No data storage whatsoever
- **Session-free**: Each request is independent

For production self-hosting, consider:

- HTTPS/TLS termination (use reverse proxy)
- Rate limiting
- Network isolation
- Authentication (if required)

## Troubleshooting

### Container won't start

Check:

- Port 8080 is available (or use `-p 9000:8080` to map to different port)
- Docker has sufficient resources
- Logs: `docker logs <container-id>`

### Generation fails

Verify:

- BPMN file is valid XML
- Process engine selection matches your BPMN dialect
- Package path is valid Java/Kotlin package name

### Can't access from other machines

Ensure:

- Container is bound to `0.0.0.0` (not `localhost`)
- Firewall allows connections on port 8080
- Use `-e SERVER_HOST=0.0.0.0` if needed

## Related

- [Gradle Plugin](gradle.md) - Build integration
- [Maven Plugin](maven.md) - Build integration
- [Architecture](architecture.md) - How it works
- [Docker Hub](https://hub.docker.com/r/emaarco/bpmn-to-code-web) - Container registry
- [GitHub](https://github.com/emaarco/bpmn-to-code) - Source code
