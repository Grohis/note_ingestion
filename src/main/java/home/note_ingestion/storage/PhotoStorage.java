package home.note_ingestion.storage;

import home.note_ingestion.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Component
public class PhotoStorage {

    private final Path root;
    private static final Logger log = LoggerFactory.getLogger(PhotoStorage.class);

    public PhotoStorage(@Value("${app.storage.root:data}") String storageRoot) {
        this.root = Paths.get(storageRoot);
    }

    public Path save(String folder, String fileName, byte[] content) throws IOException {
        Path dir = root.resolve(folder);
        Files.createDirectories(dir);

        Path filePath = dir.resolve(fileName);
        Files.write(filePath, content);

        log.info("Saved file: folder={}, file={}, size={} bytes, path={}",
                folder, fileName, content.length, filePath);

        return filePath;
    }

    public byte[] read(String folder, String fileName) throws IOException {
        Path filePath = root.resolve(folder).resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new NotFoundException("file not found: " + fileName);
        }

        byte[] content = Files.readAllBytes(filePath);

        log.info("Read file: folder={}, file={}, size={} bytes, path={}",
                folder, fileName, content.length, filePath);

        return content;
    }

    public void delete(String folder, String fileName) throws IOException {
        Path filePath = root.resolve(folder).resolve(fileName);

        boolean deleted = Files.deleteIfExists(filePath);

        log.info("Delete file: folder={}, file={}, path={}, deleted={}",
                folder, fileName, filePath, deleted);
    }

    public Stream<Path> list(String folder) throws IOException {
        Path dir = root.resolve(folder);

        if (!Files.exists(dir)) {
            return Stream.empty();
        }

        return Files.list(dir);
    }

    public void renameFile(String folder, String oldName, String newName) throws IOException {
        Path source = root.resolve(folder).resolve(oldName);
        Path target = root.resolve(folder).resolve(newName);

        Files.move(source, target);
    }

    public void renameFolder(String oldFolder, String newFolder) throws IOException {
        Path source = root.resolve(oldFolder);
        Path target = root.resolve(newFolder);

        Files.move(source, target);
    }

    public Path getPath(String folder, String fileName) {
        return root.resolve(folder).resolve(fileName);
    }
}