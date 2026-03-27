package home.note_ingestion.controller;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NoteMetadataParser {

    private static final Pattern TAGS_PATTERN =
            Pattern.compile("tags:\\s*\\[(.*?)]");

    public static List<String> extractTags(String content) {
        if (!content.startsWith("---")) {
            return Collections.emptyList();
        }

        int end = content.indexOf("---", 3);
        if (end == -1) return Collections.emptyList();

        String header = content.substring(0, end);

        Matcher matcher = TAGS_PATTERN.matcher(header);
        if (matcher.find()) {
            String raw = matcher.group(1);

            String[] parts = raw.split(",");
            List<String> result = new ArrayList<>();

            for (String p : parts) {
                String tag = p.trim().toLowerCase();
                if (!tag.isEmpty()) {
                    result.add(tag);
                }
            }

            return result;
        }

        return Collections.emptyList();
    }

    public static String buildFileContent(String content, List<String> tags) {
        String normalized = tags == null
                ? ""
                : String.join(", ", tags.stream()
                .map(t -> t.toLowerCase().trim())
                .toList());

        return "---\n" +
                "tags: [" + normalized + "]\n" +
                "---\n\n" +
                content;
    }

    public static String stripHeader(String content) {
        if (!content.startsWith("---")) return content;

        int end = content.indexOf("---", 3);
        if (end == -1) return content;

        return content.substring(end + 3).trim();
    }
}