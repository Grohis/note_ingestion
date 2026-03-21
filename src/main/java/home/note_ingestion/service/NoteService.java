package home.note_ingestion.service;

import home.note_ingestion.model.Note;
import home.note_ingestion.storage.FileStorage;
import org.springframework.stereotype.Service;

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
}