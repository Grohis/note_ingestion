package home.note_ingestion.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import java.nio.file.*;
import java.util.Comparator;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NoteFlowTest {

    @Autowired
    private MockMvc mockMvc;

    private final String API_KEY = "123";

    private final String user = "test_user";
    private final String topic = "test_topic";

    @BeforeEach
    void cleanup() throws Exception {
        Path root = Paths.get("data");
        if (Files.exists(root)) {
            Files.walk(root)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        }
    }

    @Test
    void fullFlowTest() throws Exception {

        StringBuilder report = new StringBuilder();
        report.append("\n=== TEST START: ").append(LocalDateTime.now()).append(" ===\n");

        // STEP 1 — создать заметку
        String createBody = """
                {
                  "user": "test_user",
                  "topic": "test_topic",
                  "title": "note1",
                  "text": "hello"
                }
                """;

        mockMvc.perform(post("/notes")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk());

        String file = "note1.md";

        report.append("STEP 1: создал заметку\n");

        // STEP 2 — изменить
        mockMvc.perform(post("/notes/update")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", topic)
                        .param("file", file)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("updated-1"))
                .andExpect(status().isOk());

        report.append("STEP 2: изменил заметку\n");


        // STEP 3 — rename
        String renamedFile = mockMvc.perform(post("/notes/rename")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", topic)
                        .param("oldName", file)
                        .param("newTitle", "note-renamed"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        report.append("STEP 3: переименовал заметку\n");


        // STEP 4 — update после rename
        mockMvc.perform(post("/notes/update")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", topic)
                        .param("file", renamedFile)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("updated-2"))
                .andExpect(status().isOk());
        report.append("STEP 4: изменил заметку\n");


        // STEP 5 — rename topic
        String newTopic = "topic-renamed";

        mockMvc.perform(post("/notes/rename-topic")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("oldTopic", topic)
                        .param("newTopic", newTopic))
                .andExpect(status().isOk());
        report.append("STEP 5: переименовал папку\n");


        // STEP 6 — update после rename topic
        mockMvc.perform(post("/notes/update")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", newTopic)
                        .param("file", renamedFile)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("updated-3"))
                .andExpect(status().isOk());
        report.append("STEP 6: изменил заметку\n");


        // STEP 7 — rename ещё раз
        String renamedFile2 = mockMvc.perform(post("/notes/rename")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", newTopic)
                        .param("oldName", renamedFile)
                        .param("newTitle", "final-note"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        report.append("STEP 7: переименовал заметку\n");


        // STEP 8 — финальный update
        mockMvc.perform(post("/notes/update")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", newTopic)
                        .param("file", renamedFile2)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("updated-final"))
                .andExpect(status().isOk());
        report.append("STEP 8: изменил заметку\n");


        // STEP 9 — финальная проверка контента
        mockMvc.perform(get("/notes/content")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", newTopic)
                        .param("file", renamedFile2))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("updated-final")));

        report.append("=== TEST SUCCESS: ").append(LocalDateTime.now()).append(" ===\n");
        System.out.println(report);
    }
}