package com.school.elearning.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqQuizService {

    private final GroqService groqService;
    private final ObjectMapper objectMapper;

    private static final String SYSTEM_QUIZ = """
            Tu es un expert pédagogique. À partir d'un texte extrait d'un PDF de cours,
            tu génères un quiz QCM en FRANÇAIS.
            
            Tu réponds UNIQUEMENT en JSON pur, sans markdown, sans texte avant ou après.
            Format attendu :
            [
              {
                "enonce": "Question ici ?",
                "choixPossibles": ["A) ...", "B) ...", "C) ...", "D) ..."],
                "bonneReponse": "A) ...",
                "points": 2.0
              }
            ]
            
            RÈGLES ABSOLUES :
            - Génère exactement le nombre de questions demandé.
            - La bonneReponse DOIT être l'un des choixPossibles, mot pour mot.
            - Réponds UNIQUEMENT en JSON valide, sans aucun texte autour.
            - Uniquement en FRANÇAIS.
            """;

    public List<QuizQuestionIA> genererQuestionsDepuisTexte(String texte, int nombreQuestions) {
        String prompt = String.format(
            "Génère exactement %d questions QCM basées sur ce contenu de cours :\n\n%s",
            nombreQuestions,
            texte.length() > 4000 ? texte.substring(0, 4000) : texte
        );

        log.info("Génération de {} questions quiz via Groq...", nombreQuestions);
        String rawResponse = groqService.ask(SYSTEM_QUIZ, prompt);

        try {
            String clean = rawResponse.trim();
            // Nettoyer les balises markdown si présentes
            if (clean.contains("```json")) {
                clean = clean.substring(clean.indexOf("```json") + 7);
            } else if (clean.contains("```")) {
                clean = clean.substring(clean.indexOf("```") + 3);
            }
            if (clean.contains("```")) {
                clean = clean.substring(0, clean.lastIndexOf("```"));
            }
            // Extraire le tableau JSON
            int debut = clean.indexOf('[');
            int fin = clean.lastIndexOf(']');
            if (debut != -1 && fin != -1) {
                clean = clean.substring(debut, fin + 1);
            }
            return objectMapper.readValue(clean, new TypeReference<List<QuizQuestionIA>>() {});
        } catch (Exception e) {
            log.error("Erreur parsing JSON quiz Groq : {}", e.getMessage());
            throw new RuntimeException("Impossible de parser les questions générées par l'IA : " + e.getMessage());
        }
    }

    public record QuizQuestionIA(
        String enonce,
        List<String> choixPossibles,
        String bonneReponse,
        double points
    ) {}
}