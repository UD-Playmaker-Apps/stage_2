# Stage 2 Microservices Scaffold

Este repositorio esqueleto separa el proyecto monolítico en **4** proyectos Maven independientes:
- `ingestion-service` (puerto 7001)
- `indexing-service`  (puerto 7002)
- `search-service`    (puerto 7003)
- `control-module`    (puerto 7004)

Cada servicio usa **Java 17 + Javalin + Gson** y expone APIs REST en JSON tal como se especifica en el enunciado.

# Stage 2 Microservices Scaffold

This skeleton repository breaks the monolithic project into **4** independent Maven projects:
- `ingestion-service` (port 7001)
- `indexing-service`  (port 7002)
- `search-service`    (port 7003)
- `control-module`    (port 7004)

Each service uses **Java 17 + Javalin + Gson** and exposes REST JSON APIs as specified in the assignment.

## Endpoints (summary)

### Ingestion Service
- `POST /ingest/{book_id}` → downloads book (mock)
- `GET /ingest/status/{book_id}` → status
- `GET /ingest/list` → available books

### Indexing Service
- `POST /index/update/{book_id}` → index/update (mock)
- `POST /index/rebuild` → rebuild entire index (mock)
- `GET /index/status` → metrics

### Search Service
- `GET /search?q=...&author=...&language=...&year=...` → search with filters

### Control Module
- `POST /control/run/{book_id}` → orchestrates: ingestion → indexing

## How to build
```bash
# Inside each service directory
mvn clean package
java -jar target/*.jar
````

> Adjust environment variables `INGESTION_URL` and `INDEXING_URL` in the `control` module if you want to run real pipelines for a specific `book_id`.

## Next steps
- Replace mocks with real logic for:
  - Downloading and splitting header/body into the datalake
  - Building indexes + datamarts
  - Accessing indexes from the search service
- Add JMH micro-benchmarks in each service
- Add persistence (files/db) and logs

