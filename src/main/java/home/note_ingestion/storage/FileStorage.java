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
                //slug = "note";
                slug = note.getTitle();
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

    public void update(String user, String topic, String fileName, String newText) {
        try {
            Path file = root.resolve(user).resolve(topic).resolve(fileName);
            if (!Files.exists(file)) {
                throw new RuntimeException("file not found");
            }

            // Читаем старый файл
            String content = Files.readString(file, StandardCharsets.UTF_8);

            // Извлекаем существующий заголовок (title и topic)
            String[] parts = content.split("---", 3);
            String header = parts.length > 2 ? parts[1] : "";

            String titleLine = header.lines()
                    .filter(l -> l.startsWith("title:"))
                    .findFirst()
                    .orElse("title: " + fileName.replace(".md",""));

            String topicLine = header.lines()
                    .filter(l -> l.startsWith("topic:"))
                    .findFirst()
                    .orElse("topic: " + topic);

            // Формируем новый контент с новым текстом и новым created
            String newContent = """
                    ---
                    %s
                    %s
                    created: %s
                    ---
                    
                    %s
                    """.formatted(titleLine, topicLine, LocalDateTime.now(), newText);

            Files.writeString(file, newContent, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String user, String topic, String fileName) {
        Path topicDir = root.resolve(user).resolve(topic);
        Path file = topicDir.resolve(fileName);

        try {
            if (!Files.exists(file)) {
                throw new RuntimeException("file not found");
            }

            // 1. удаляем файл
            Files.delete(file);
            System.out.println("Файл удалён: " + file.toAbsolutePath());

            // 2. если папка topic пустая → удалить
            if (Files.exists(topicDir) && isDirectoryEmpty(topicDir)) {
                Files.delete(topicDir);
                System.out.println("Папка topic удалена: " + topicDir);
            }

            // 3. если папка user пустая → удалить (опционально, но правильно)
            Path userDir = root.resolve(user);
            if (Files.exists(userDir) && isDirectoryEmpty(userDir)) {
                Files.delete(userDir);
                System.out.println("Папка user удалена: " + userDir);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDirectoryEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }
}