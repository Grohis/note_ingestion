package home.note_ingestion.storage;

import home.note_ingestion.model.Note;

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
            // 1. формируем путь: data/user/topic
            Path dir = root
                    .resolve(note.getUser())
                    .resolve(note.getTopic());

            // 2. создаём папки если нет
            Files.createDirectories(dir);

            // 3. имя файла
            String fileName = LocalDateTime.now()
                    .withNano(0)
                    .toString()
                    .replace(":", "-") + ".md";

            Path file = dir.resolve(fileName);

            // 4. содержимое
            String content = note.getText();

            // 5. запись
            Files.writeString(
                    file,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );

            System.out.println("Сохранили файл: " + file.toAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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