package ulpgc.bigd.search_service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class SearchRepository {

    private static final Path DATAMART_DIR = Paths.get("./indexing-service/datamart").toAbsolutePath().normalize();
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
                Type metaType = new TypeToken<Map<Integer, Map<String, Object>>>() {}.getType();
                metadata = gson.fromJson(Files.newBufferedReader(METADATA_FILE), metaType);
                if (metadata == null) metadata = new HashMap<>();
            }
            if (Files.exists(INDEX_FILE)) {
                Type idxType = new TypeToken<Map<String, Set<Integer>>>() {}.getType();
                invertedIndex = gson.fromJson(Files.newBufferedReader(INDEX_FILE), idxType);
                if (invertedIndex == null) invertedIndex = new HashMap<>();
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load datamart files: " + e.getMessage());
            metadata = new HashMap<>();
            invertedIndex = new HashMap<>();
        }
    }

    public static List<Book> search(String term, String author, String language, Integer year, Integer bookId) {
        String t = term == null ? "" : term.toLowerCase();
        String a = author == null ? "" : author.toLowerCase();
        String l = language == null ? "" : language.toLowerCase();

        // Filtramos por inverted index si hay término, o por todos los libros
        Set<Integer> candidateIds;

        if (bookId != null) {
            // Si se especifica bookId, solo se considera ese
            candidateIds = metadata.containsKey(bookId) ? Set.of(bookId) : Collections.emptySet();
        } else if (!t.isEmpty()) {
            candidateIds = invertedIndex.getOrDefault(t, Collections.emptySet());
        } else {
            candidateIds = metadata.keySet();
        }

        List<Book> results = new ArrayList<>();

        for (Integer id : candidateIds) {
            Map<String, Object> meta = metadata.get(id);
            if (meta == null) continue;

            String title = Optional.ofNullable(meta.get("title")).map(Object::toString).orElse("").toLowerCase();
            String auth = Optional.ofNullable(meta.get("author")).map(Object::toString).orElse("").toLowerCase();
            String lang = Optional.ofNullable(meta.get("language")).map(Object::toString).orElse("").toLowerCase();
            int y = parseYear(meta.get("year"));

            // Filtrado por todos los criterios
            if ((!t.isEmpty() && !title.contains(t)) ||
                    (!a.isEmpty() && !auth.contains(a)) ||
                    (!l.isEmpty() && !lang.equals(l)) ||
                    (year != null && y != year)) {
                continue;
            }

            results.add(new Book(id,
                    meta.getOrDefault("title", "").toString(),
                    meta.getOrDefault("author", "").toString(),
                    meta.getOrDefault("language", "").toString(),
                    y));
        }

        return results;
    }

    private static int parseYear(Object yearObj) {
        if (yearObj == null) return 0;
        if (yearObj instanceof Number) return ((Number) yearObj).intValue();
        try {
            return Integer.parseInt(yearObj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
