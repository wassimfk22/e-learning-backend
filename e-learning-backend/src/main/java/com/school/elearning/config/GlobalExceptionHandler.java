package com.school.elearning.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Intercepte toutes les RuntimeException et retourne un JSON propre
 * au lieu d'un stacktrace HTML illisible.
 *
 * AVANT (sans ce handler) : 500 + page HTML d'erreur
 * APRÈS (avec ce handler)  : 400/403/404 + JSON { message, timestamp }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "erreur", ex.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                ));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccess(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "erreur", "Accès refusé — rôle insuffisant",
                        "timestamp", LocalDateTime.now().toString()
                ));
    }
}
