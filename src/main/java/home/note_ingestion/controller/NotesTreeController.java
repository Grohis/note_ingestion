package home.note_ingestion.controller;

import home.note_ingestion.service.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notes")
public class NotesTreeController {

    private final NoteService service;

    public NotesTreeController(NoteService service) {
        this.service = service;
    }

    // Возвращаем дерево: topic -> файлы
    @GetMapping("/tree")
    public Map<String, List<String>> tree(@RequestParam String user) {
        // Берём все топики
        // Для каждого топика возвращаем список файлов
        // В качестве упрощения просто читаем папки в data/user/*
        return service.listTopics(user).stream()
                .collect(Collectors.toMap(
                        topic -> topic,
                        topic -> service.list(user, topic)
                ));
    }
}