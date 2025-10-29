package ulpgc.bigd.control_module;

import io.javalin.Javalin;
import com.google.gson.Gson;
import java.util.Map;

public class App {

    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        int port = 7004; // puerto del control-module
        ControlController controller = new ControlController();

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        }).start(port);

        System.out.println("✅ Control Module running on port " + port);

        // Endpoints
        app.get("/status", ctx -> {
            Map<String, Object> status = controller.getSystemStatus();
            ctx.result(gson.toJson(status));
        });

        app.post("/process/{bookId}", ctx -> {
            int bookId;
            try {
                bookId = Integer.parseInt(ctx.pathParam("bookId"));
            } catch (NumberFormatException e) {
                ctx.status(400).result(gson.toJson(Map.of("error", "Invalid bookId")));
                return;
            }

            Map<String, Object> result = controller.processBook(bookId);
            ctx.result(gson.toJson(result));
        });
    }
}
