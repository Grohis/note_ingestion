package home.note_ingestion.controller;

import home.note_ingestion.service.PhotoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    private String detectContentType(Path path) {
        try {
            String type = Files.probeContentType(path);
            return type != null ? type : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        } catch (IOException e) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping
    public ResponseEntity<String> upload(
            @RequestParam String folder,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        photoService.upload(folder, fileName, file.getBytes());

        return ResponseEntity.ok("/photos/" + folder + "/" + fileName);
    }

    @GetMapping("/{folder}/{fileName}")
    public ResponseEntity<byte[]> get(
            @PathVariable String folder,
            @PathVariable String fileName
    ) throws IOException {

        validate(folder, fileName);

        Path path = photoService.getPath(folder, fileName);
        byte[] data = photoService.get(folder, fileName);

        String contentType = detectContentType(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(data);
    }

    @DeleteMapping("/{folder}/{fileName}")
    public ResponseEntity<Void> delete(
            @PathVariable String folder,
            @PathVariable String fileName,
            @RequestParam String confirm
    ) throws IOException {

        if (!"удалить".equalsIgnoreCase(confirm)) {
            return ResponseEntity.badRequest().build();
        }

        validate(folder, fileName);

        photoService.delete(folder, fileName);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{folder}")
    public List<String> list(@PathVariable String folder) throws IOException {
        return photoService.list(folder);
    }

    @PutMapping("/rename")
    public ResponseEntity<Void> rename(
            @RequestParam String folder,
            @RequestParam String oldName,
            @RequestParam String newName
    ) throws IOException {

        validate(folder, oldName);
        validate(folder, newName);

        photoService.rename(folder, oldName, newName);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/folder/rename")
    public ResponseEntity<Void> renameFolder(
            @RequestParam String oldFolder,
            @RequestParam String newFolder
    ) throws IOException {

        photoService.renameFolder(oldFolder, newFolder);

        return ResponseEntity.ok().build();
    }

    private void validate(String folder, String fileName) {
        if (folder.contains("..") || fileName.contains("..")) {
            throw new IllegalArgumentException("Invalid path");
        }
    }
}
