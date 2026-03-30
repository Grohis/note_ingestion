package home.note_ingestion.service;

import home.note_ingestion.storage.PhotoStorage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;


@Service
public class PhotoService {

    private final PhotoStorage storage;

    public PhotoService(PhotoStorage storage) {
        this.storage = storage;
    }

    public void upload(String folder, String fileName, byte[] data) throws IOException {
        storage.save(folder, fileName, data);
    }

    public byte[] get(String folder, String fileName) throws IOException {
        return storage.read(folder, fileName);
    }

    public void delete(String folder, String fileName) throws IOException {
        storage.delete(folder, fileName);
    }

    public void rename(String folder, String oldName, String newName) throws IOException {
        storage.renameFile(folder, oldName, newName);
    }

    public void renameFolder(String oldFolder, String newFolder) throws IOException {
        storage.renameFolder(oldFolder, newFolder);
    }

    public List<String> list(String folder) throws IOException {
        try (Stream<Path> stream = storage.list(folder)) {
            return stream
                    .map(p -> p.getFileName().toString())
                    .toList();
        }
    }

    public Path getPath(String folder, String fileName) {
        return storage.getPath(folder, fileName);
    }
}
