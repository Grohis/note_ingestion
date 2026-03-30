package home.note_ingestion.controller;

import home.note_ingestion.service.PhotoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping
    public ResponseEntity<Void> upload(@RequestParam("file") MultipartFile file) throws IOException {
        photoService.upload(file.getOriginalFilename(), file.getBytes());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> get(@PathVariable String fileName) throws IOException {
        byte[] data = photoService.get(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                .body(data);
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity<Void> delete(@PathVariable String fileName) throws IOException {
        photoService.delete(fileName);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<String> list() throws IOException {
        return photoService.list();
    }
}
