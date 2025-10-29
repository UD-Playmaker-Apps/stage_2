package ulpgc.bigd.search_service;

import io.javalin.http.Context;
import com.google.gson.Gson;
import java.util.*;

public class SearchController {

    private static final Gson gson = new Gson();

    public static void search(Context ctx) {
        String t = ctx.queryParam("t");
        String author = ctx.queryParam("author");
        String language = ctx.queryParam("language");

        // Parse year
        Integer year = null;
        String y = ctx.queryParam("year");
        if (y != null && !y.isEmpty()) {
            try {
                year = Integer.parseInt(y);
            } catch (NumberFormatException ignored) {}
        }

        // Parse bookId
        Integer bookId = null;
        String bId = ctx.queryParam("bookId");
        if (bId != null && !bId.isEmpty()) {
            try {
                bookId = Integer.parseInt(bId);
            } catch (NumberFormatException ignored) {}
        }

        List<Book> results = SearchRepository.search(t, author, language, year, bookId);

        Map<String, Object> filters = new LinkedHashMap<>();
        if (author != null) filters.put("author", author);
        if (language != null) filters.put("language", language);
        if (year != null) filters.put("year", year);
        if (bookId != null) filters.put("bookId", bookId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", t == null ? "" : t);
        response.put("filters", filters);
        response.put("count", results.size());
        response.put("results", results);

        ctx.result(gson.toJson(response));
    }
}
