package home.note_ingestion.service;

import home.note_ingestion.model.Note;
import home.note_ingestion.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class NoteService {

    private final FileStorage storage = new FileStorage();

    public void save(Note note) {
        validate(note);
        storage.save(note);
    }

    private void validate(Note note) {
        if (note.getUser() == null || note.getUser().isEmpty()) {
            throw new RuntimeException("user is empty");
        }
        if (note.getTopic() == null || note.getTopic().isEmpty()) {
            throw new RuntimeException("topic is empty");
        }
        if (note.getText() == null || note.getText().isEmpty()) {
            throw new RuntimeException("text is empty");
        }

        if (note.getUser().contains("..") || note.getTopic().contains("..")) {
            throw new RuntimeException("invalid path");
        }
    }

    public List<String> list(String user, String topic) {
        if (user == null || topic == null) {
            throw new RuntimeException("user/topic required");
        }

        return storage.list(user, topic);
    }

    public String read(String user, String topic, String fileName) {
        if (user == null || topic == null || fileName == null) {
            throw new RuntimeException("params required");
        }

        return storage.read(user, topic, fileName);
    }

    public List<String> listTopics(String user) {
        try {
            Path userDir = Paths.get("data").resolve(user);
            if (!Files.exists(userDir)) return List.of();

            try (var stream = Files.list(userDir)) {
                return stream
                        .filter(Files::isDirectory)
                        .map(path -> path.getFileName().toString())
                        .sorted()
                        .toList();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void update(String user, String topic, String fileName, String newText) {
        if (user == null || topic == null || fileName == null || newText == null) {
            throw new RuntimeException("params required");
        }
        storage.update(user, topic, fileName, newText);
    }

    public void delete(String user, String topic, String fileName) {
        if (user == null || topic == null || fileName == null) {
            throw new RuntimeException("params required");
        }
        storage.delete(user, topic, fileName);
    }
}