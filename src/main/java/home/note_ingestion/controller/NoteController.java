package home.note_ingestion.controller;

import home.note_ingestion.model.Note;
import home.note_ingestion.service.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService service;

    public NoteController(NoteService service) {
        this.service = service;
    }

    @PostMapping
    public String createNote(@RequestBody Note note) {
        service.save(note);
        return "saved";
    }

    @GetMapping
    public List<String> getNotes(
            @RequestParam String user,
            @RequestParam String topic
    ) {
        return service.list(user, topic);
    }

    @GetMapping("/content")
    public String getContent(
            @RequestParam String user,
            @RequestParam String topic,
            @RequestParam String file
    ) {
        return service.read(user, topic, file);
    }

    @PostMapping("/update")
    public String updateNote(
            @RequestParam String user,
            @RequestParam String topic,
            @RequestParam String file,
            @RequestBody String newText
    ) {
        service.update(user, topic, file, newText);
        return "updated";
    }
}