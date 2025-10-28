package ulpgc.bigd.control_module;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {
        OkHttpClient client = new OkHttpClient();

        // Llamada a ingestion-service
        Request request = new Request.Builder()
                .url("http://localhost:7001/ingest/1342")
                .post(okhttp3.RequestBody.create(new byte[0]))
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(response.body().string());
        }

        // Aquí seguirías con indexing-service y search-service
    }
}
