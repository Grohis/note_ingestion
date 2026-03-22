package home.note_ingestion.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    // Сохраняем имя пользователя в сессии
    @PostMapping("/set")
    public String setUser(@RequestParam String user, HttpSession session) {
        if (user == null || user.isBlank()) {
            return "user is empty";
        }
        session.setAttribute("currentUser", user);
        return "user saved in session: " + user;
    }

    // Получаем текущего пользователя из сессии
    @GetMapping("/get")
    public String getUser(HttpSession session) {
        Object user = session.getAttribute("currentUser");
        if (user == null) {
            return "no user in session";
        }
        return user.toString();
    }

    // Очистка сессии (например, для выхода)
    @PostMapping("/clear")
    public String clearUser(HttpSession session) {
        session.removeAttribute("currentUser");
        return "user cleared from session";
    }
}