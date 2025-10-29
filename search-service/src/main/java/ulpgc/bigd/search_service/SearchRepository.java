package ulpgc.bigd.search_service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public class SearchRepository {

    private static final Path DATAMART_DIR = Paths.get("../indexing-service/datamart").toAbsolutePath().normalize();
    private static final Path METADATA_FILE = DATAMART_DIR.resolve("metadata.json");
    private static final Path INDEX_FILE = DATAMART_DIR.resolve("inverted-index.json");

    private static final Gson gson = new Gson();

    private static Map<Integer, Map<String, Object>> metadata = new HashMap<>();
    private static Map<String, Set<Integer>> invertedIndex = new HashMap<>();

    static {
        loadData();
    }

    private static void loadData() {
        try {
            if (Files.exists(METADATA_FILE)) {
                Type metaType = new TypeToken<Map<Integer, Map<String, Object>>>(){}.getType();
                metadata = gson.fromJson(Files.newBufferedReader(METADATA_FILE), metaType);
                if (metadata == null) metadata = new HashMap<>();
            }
            if (Files.exists(INDEX_FILE)) {
                Type idxType = new TypeToken<Map<String, Set<Integer>>>(){}.getType();
                invertedIndex = gson.fromJson(Files.newBufferedReader(INDEX_FILE), idxType);
                if (invertedIndex == null) invertedIndex = new HashMap<>();
            }
            System.out.printf("📚 Loaded %d metadata entries, %d index terms%n", metadata.size(), invertedIndex.size());
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load datamart files: " + e.getMessage());
        }
    }

    public static List<Book> search(String term, String author, String language, Integer year, Integer bookId) {
        String t = term == null ? "" : term.toLowerCase().trim();
        String a = author == null ? "" : author.toLowerCase().trim();
        String l = language == null ? "" : language.toLowerCase().trim();

        Set<Integer> candidateIds = new HashSet<>();

        if (bookId != null) {
            if (metadata.containsKey(bookId)) {
                candidateIds.add(bookId);
            }
        } else if (!t.isEmpty()) {
            // Buscar clave exacta
            candidateIds.addAll(invertedIndex.getOrDefault(t, Collections.emptySet()));

            // Si no hay coincidencias exactas, buscar aproximadas
            if (candidateIds.isEmpty()) {
                for (Map.Entry<String, Set<Integer>> e : invertedIndex.entrySet()) {
                    String key = e.getKey().toLowerCase().trim();
                    if (key.contains(t)) {
                        candidateIds.addAll(e.getValue());
                    }
                }
            }

            // Si sigue sin resultados, buscar en metadata (por título o autor)
            if (candidateIds.isEmpty()) {
                for (Map.Entry<Integer, Map<String, Object>> e : metadata.entrySet()) {
                    Map<String, Object> meta = e.getValue();
                    String title = Optional.ofNullable(meta.get("title")).map(Object::toString).orElse("").toLowerCase();
                    String auth = Optional.ofNullable(meta.get("author")).map(Object::toString).orElse("").toLowerCase();
                    if (title.contains(t) || auth.contains(t)) {
                        candidateIds.add(e.getKey());
                    }
                }
            }
        } else {
            candidateIds.addAll(metadata.keySet());
        }

        List<Book> results = new ArrayList<>();
        for (Integer id : candidateIds) {
            Map<String, Object> meta = metadata.get(id);
            if (meta == null) continue;

            String title = Optional.ofNullable(meta.get("title")).map(Object::toString).orElse("");
            String auth = Optional.ofNullable(meta.get("author")).map(Object::toString).orElse("");
            String lang = Optional.ofNullable(meta.get("language")).map(Object::toString).orElse("");
            int y = parseYear(meta.get("year"));

            // Filtrado final
            if ((!a.isEmpty() && !auth.toLowerCase().contains(a)) ||
                    (!l.isEmpty() && !lang.equalsIgnoreCase(l)) ||
                    (year != null && y != year)) {
                continue;
            }

            results.add(new Book(id, title, auth, lang, y));
        }

        return results;
    }


    private static String safe(Object value) {
        return value == null ? "" : value.toString();
    }

    private static int parseYear(Object yearObj) {
        if (yearObj == null || yearObj.toString().isBlank()) return 0;
        try {
            return Integer.parseInt(yearObj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
