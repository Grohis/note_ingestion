package home.note_ingestion.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ApiKeyFilter implements Filter {

    @Value("${app.api.key}")
    private String apiKey;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // health без авторизации
        if (path.equals("/health")) {
            chain.doFilter(request, response);
            return;
        }

        // photos пока без авторизации
        if (path.startsWith("/photos")) {
            chain.doFilter(request, response);
            return;
        }

        // защита только notes
        if (path.startsWith("/notes")) {

            String header = req.getHeader("X-API-KEY");

            if (header == null || !header.equals(apiKey)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("Unauthorized");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}