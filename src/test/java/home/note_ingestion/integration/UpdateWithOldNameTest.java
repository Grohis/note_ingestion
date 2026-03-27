package home.note_ingestion.integration;
// не совсем понимаю его как он работает

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UpdateWithOldNameTest {

    @Autowired
    private MockMvc mockMvc;

    private final String API_KEY = "123";
    private final String user = "test_user";
    private final String topic = "test_topic";

    @Test
    void updateWithOldNameAfterRename_shouldReturn404() throws Exception {

        // STEP 1 — create
        mockMvc.perform(post("/notes")
                        .header("X-API-KEY", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                  "user": "test_user",
                  "topic": "test_topic",
                  "title": "note1",
                  "text": "hello"
                }
                """))
                .andExpect(status().isOk());

        String oldFile = "note1.md";

        // STEP 2 — rename
        String newFile = mockMvc.perform(post("/notes/rename")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", topic)
                        .param("oldName", oldFile)
                        .param("newTitle", "note-renamed"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assert !newFile.equals(oldFile);

        // STEP 3 — update старым именем → ожидаем 404
        mockMvc.perform(post("/notes/update")
                        .header("X-API-KEY", API_KEY)
                        .param("user", user)
                        .param("topic", topic)
                        .param("file", oldFile)
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("fail"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("file not found")));
//
    }
}