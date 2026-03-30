package home.note_ingestion.service;

import home.note_ingestion.storage.FileStorage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;


@Service
public class PhotoService {

    private final FileStorage fileStorage;

    public PhotoService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    private static final String FOLDER = "photos";

    public void upload(String fileName, byte[] content) throws IOException {
        fileStorage.saveFile(FOLDER, fileName, content);
    }

    public byte[] get(String fileName) throws IOException {
        return fileStorage.readFile(FOLDER, fileName);
    }

    public void delete(String fileName) throws IOException {
        fileStorage.deleteFile(FOLDER, fileName);
    }

    public List<String> list() throws IOException {
        try (Stream<Path> stream = fileStorage.listFiles(FOLDER)) {
            return stream
                    .map(path -> path.getFileName().toString())
                    .toList();
        }
    }
}
