package ulpgc.bigd.control_module;

import com.google.gson.Gson;

import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

public class Orchestrator {

    private static final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String INGESTION_URL;
    private final String INDEXING_URL;

    public Orchestrator() {
        // URLs parametrizables con variables de entorno
        this.INGESTION_URL = System.getenv().getOrDefault("INGESTION_URL", "http://localhost:7001");
        this.INDEXING_URL = System.getenv().getOrDefault("INDEXING_URL", "http://localhost:7002");
    }

    public Map<String, Object> ingestBook(int bookId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(INGESTION_URL + "/ingest/" + bookId))
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(res.body(), Map.class);
    }

    public Map<String, Object> indexBook(int bookId) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(INDEXING_URL + "/index/update/" + bookId))
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(res.body(), Map.class);
    }
}
