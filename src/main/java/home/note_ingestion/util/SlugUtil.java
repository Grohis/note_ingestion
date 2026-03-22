package home.note_ingestion.util;

public class SlugUtil {

    public static String toSlug(String input) {
        if (input == null) {
            return "";
        }

        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }
}