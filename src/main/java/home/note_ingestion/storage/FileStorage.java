package home.note_ingestion.storage;

import home.note_ingestion.exception.NotFoundException;
import home.note_ingestion.model.Note;
import home.note_ingestion.util.SlugUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

@Component
public class FileStorage {

    private final Path root;
    private static final Logger log = LoggerFactory.getLogger(FileStorage.class);
    public FileStorage(@Value("${app.storage.root:data}") String storageRoot) {
        this.root = Paths.get(storageRoot);
    }

    public Path getRoot() {
        return root;
    }

    public void save(Note note) {
        try {
            Path dir = root
                    .resolve(note.getUser())
                    .resolve(note.getTopic());

            Files.createDirectories(dir);

            String slug = SlugUtil.toSlug(note.getTitle());
            if (slug.isEmpty()) {
                slug = note.getTitle();
            }

            String filename = ensureMd(slug);
            Path path = dir.resolve(filename);

            int counter = 1;
            while (Files.exists(path)) {
                filename = ensureMd(slug + "-" + counter);
                path = dir.resolve(filename);
                counter++;
            }

            String tagsLine = buildTagsLine(note.getTags());

            String content = """
                    ---
                    title: %s
                    created: %s
                    %s
                    ---
                    
                    %s
                    """.formatted(
                    note.getTitle(),
                    LocalDateTime.now(),
                    tagsLine,
                    note.getText()
            );

            Files.writeString(
                    path,
                    content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW
            );

            // System.out.println("Сохранили файл: " + path.toAbsolutePath());
            log.info("Saved note: user={}, topic={}, file={}",
                    note.getUser(), note.getTopic(), filename);

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
        fileName = ensureMd(fileName);
        try {
            Path file = root
                    .resolve(user)
                    .resolve(topic)
                    .resolve(fileName);


            // System.out.println("READ PATH: " + file.toAbsolutePath());
            log.info("Reading file: user={}, topic={}, file={}", user, topic, fileName);

            if (!Files.exists(file)) {
                throw new NotFoundException("file not found: " + fileName);
            }


            String content = Files.readString(file, StandardCharsets.UTF_8);


            if (!content.startsWith("---")) {
                return content;
            }

            return content;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path buildPath(String user, String topic, String fileName) {
        return root
                .resolve(user)
                .resolve(topic)
                .resolve(fileName);
    }

    public void update(String user, String topic, String fileName, String newContent) {
        try {
            fileName = ensureMd(fileName);

            Path file = buildPath(user, topic, fileName);

            if (!Files.exists(file)) {
                throw new NotFoundException("file not found: " + fileName);
            }

            Files.writeString(
                    file,
                    newContent,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            log.info("Updating file: user={}, topic={}, file={}", user, topic, fileName);
//            log.info("CONTENT:\n{}", newContent);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String user, String topic, String fileName) {
        Path topicDir = root.resolve(user).resolve(topic);
        fileName = ensureMd(fileName);
        Path file = topicDir.resolve(fileName);

        try {
            if (!Files.exists(file)) {
                throw new NotFoundException("file not found: " + fileName);
            }

            Files.delete(file);

            if (Files.exists(topicDir) && isDirectoryEmpty(topicDir)) {
                Files.delete(topicDir);
            }

            Path userDir = root.resolve(user);
            if (Files.exists(userDir) && isDirectoryEmpty(userDir)) {
                Files.delete(userDir);
            }
            log.info("Deleting file: user={}, topic={}, file={}", user, topic, fileName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isDirectoryEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return !stream.iterator().hasNext();
        }
    }

    public String renameFile(String user, String topic, String oldName, String newTitle) {
        try {
            Path dir = root.resolve(user).resolve(topic);

            oldName = ensureMd(oldName);
            Path oldPath = dir.resolve(oldName);

            if (!Files.exists(oldPath)) {
                throw new NotFoundException("file not found: " + oldName);
            }

            String slug = SlugUtil.toSlug(newTitle);
            if (slug.isEmpty()) {
                slug = newTitle;
            }

            String newFileName = slug + ".md";
            Path newPath = dir.resolve(newFileName);

            int counter = 1;
            while (Files.exists(newPath)) {
                newFileName = slug + "-" + counter + ".md";
                newPath = dir.resolve(newFileName);
                counter++;
            }

            Files.move(oldPath, newPath);
            updateTitleInsideFile(newPath, newTitle);
            log.info("Renaming file: user={}, topic={}, oldName={}, newName={}",
                    user, topic, oldName, newFileName);

            return newFileName;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateTitleInsideFile(Path file, String newTitle) throws IOException {
        String content = Files.readString(file, StandardCharsets.UTF_8);

        String updated = content.replaceFirst(
                "title:.*",
                "title: " + newTitle
        );

        Files.writeString(file, updated, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void renameDirectory(String user, String oldTopic, String newTopic) {
        try {
            Path userDir = root.resolve(user);

            Path oldPath = userDir.resolve(oldTopic);
            Path newPath = userDir.resolve(newTopic);

            if (!Files.exists(oldPath)) {
                throw new NotFoundException("topic not found: " + oldTopic);
            }

            if (Files.exists(newPath)) {
                throw new RuntimeException("target topic already exists");
            }

            Files.move(oldPath, newPath);
            log.info("Renaming topic: user={}, oldTopic={}, newTopic={}",
                    user, oldTopic, newTopic);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildTagsLine(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return "tags: []";
        }

        String normalized = tags.stream()
                .map(t -> t.toLowerCase().trim())
                .filter(t -> !t.isEmpty())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return "tags: [" + normalized + "]";
    }

    private String ensureMd(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("File name is empty");
        }

        return name.endsWith(".md") ? name : name + ".md";
    }
}