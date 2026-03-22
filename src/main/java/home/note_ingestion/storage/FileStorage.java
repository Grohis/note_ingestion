package home.note_ingestion.storage;

import home.note_ingestion.model.Note;
import home.note_ingestion.util.SlugUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

public class FileStorage {

    private final Path root = Paths.get("data");

    public void save(Note note) {
        try {
            // 1. путь: data/user/topic
            Path dir = root
                    .resolve(note.getUser())
                    .resolve(note.getTopic());

            Files.createDirectories(dir);

            // 2. slug
            String slug = SlugUtil.toSlug(note.getTitle());
            if (slug.isEmpty()) {
                slug = "note";
            }

            // 3. имя файла
            String filename = slug + ".md";
            Path path = dir.resolve(filename);

            // 4. защита от перезаписи
            int counter = 1;
            while (Files.exists(path)) {
                filename = slug + "-" + counter + ".md";
                path = dir.resolve(filename);
                counter++;
            }

            // 5. metadata + текст
            String content = """
                    ---
                    title: %s
                    created: %s
                    ---
                    
                    %s
                    """.formatted(
                    note.getTitle(),
                    LocalDateTime.now(),
                    note.getText()
            );

            // 6. запись
            Files.writeString(
                    path,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );

            System.out.println("Сохранили файл: " + path.toAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("TITLE = " + note.getTitle());
    }

    public List<String> list(String user, String topic) {
        try {
            Path dir = root
                    .resolve(user)
                    .resolve(topic);

            if (!Files.exists(dir)) {
                return List.of();
            }

            try (Stream<Path> files = Files.list(dir)) {
                return files
                        .map(path -> path.getFileName().toString())
                        .sorted()
                        .toList();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String read(String user, String topic, String fileName) {
        try {
            Path file = root
                    .resolve(user)
                    .resolve(topic)
                    .resolve(fileName);

            if (!Files.exists(file)) {
                throw new RuntimeException("file not found");
            }

            return Files.readString(file);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}