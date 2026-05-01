package com.school.elearning.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.elearning.dto.CoursResponse;
import com.school.elearning.model.enums.TypeContent;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiCoursGeneratorService {

    private final OpenAiService openAiService;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    @Value("${openai.max-tokens}")
    private int maxTokens;

    public List<AiContentItem> genererContenuDepuisTexte(String texteExtrait) {
        String prompt = construirePrompt(texteExtrait);

        ChatMessage systemMessage = new ChatMessage(
            ChatMessageRole.SYSTEM.value(),
            """
            Tu es un expert pédagogique. Tu génères des résumés de cours clairs et structurés.
            Tu réponds UNIQUEMENT en JSON valide, sans markdown, sans explication.
            """
        );

        ChatMessage userMessage = new ChatMessage(
            ChatMessageRole.USER.value(),
            prompt
        );

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(systemMessage, userMessage))
                .maxTokens(maxTokens)
                .temperature(0.3)
                .build();

        ChatCompletionResult result = openAiService.createChatCompletion(request);
        String jsonReponse = result.getChoices().get(0).getMessage().getContent();

        return parseReponse(jsonReponse);
    }

    private String construirePrompt(String texte) {
        return """
            Voici le contenu d'un document pédagogique :
            
            %s
            
            Génère un cours résumé structuré à partir de ce contenu.
            Retourne UNIQUEMENT un tableau JSON avec cette structure exacte :
            [
              { "type": "TEXT", "content": "Introduction ou titre de section..." },
              { "type": "TEXT", "content": "Explication du concept..." }
            ]
            
            Règles :
            - Utilise uniquement le type "TEXT"
            - Découpe le cours en sections logiques (introduction, concepts clés, résumé)
            - Chaque section doit être claire, concise et pédagogique
            - Minimum 5 sections, maximum 15
            - Réponds UNIQUEMENT avec le tableau JSON, rien d'autre
            """.formatted(texte.length() > 12000 ? texte.substring(0, 12000) : texte);
    }

    private List<AiContentItem> parseReponse(String json) {
        try {
            String nettoye = json.strip()
                    .replaceAll("^```json", "")
                    .replaceAll("^```", "")
                    .replaceAll("```$", "")
                    .strip();
            return objectMapper.readValue(
                    nettoye,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, AiContentItem.class)
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur parsing réponse GPT : " + e.getMessage());
        }
    }

    // DTO interne pour parser la réponse GPT
    public record AiContentItem(TypeContent type, String content) {}
    
    
    
}