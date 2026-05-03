package com.school.elearning.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAlias;
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
            SOURCE : %s
            ---
            MISSION : Résume le texte ci-dessus en 5 points clés.
            
            RÉPONDS UNIQUEMENT SOUS CE FORMAT JSON :
            [
              {"type": "TEXT", "content": "Met ici l'introduction"},
              {"type": "TEXT", "content": "Met ici le concept 1"},
              {"type": "TEXT", "content": "Met ici le concept 2"},
              {"type": "TEXT", "content": "Met ici le concept 3"},
              {"type": "TEXT", "content": "Met ici la conclusion"}
            ]
            
            STRICTEMENT INTERDIT : Ne recopie pas les chiffres scientifiques comme -1.797E308. 
            Ne mets aucun texte avant ou après le crochet [.
            """.formatted(texte.length() > 3000 ? texte.substring(0, 3000) : texte);
    }

    private List<AiContentItem> parseReponse(String json) {
        try {
            // 1. On configure l'objectMapper pour être indulgent avec les erreurs de l'IA
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            // 2. On affiche la réponse pour debug (Regarde ta console d'IDE !)
            System.out.println("--- RÉPONSE BRUTE OLLAMA ---");
            System.out.println(json);
            System.out.println("----------------------------");

            // 3. On extrait uniquement la partie entre [ et ]
            int debutTableau = json.indexOf("[");
            int finTableau = json.lastIndexOf("]");

            if (debutTableau == -1 || finTableau == -1 || finTableau < debutTableau) {
                throw new RuntimeException("L'IA n'a pas inclus de tableau JSON valide dans sa réponse.");
            }

            String jsonPur = json.substring(debutTableau, finTableau + 1).trim();

            // 4. On transforme le JSON en liste d'objets
            return objectMapper.readValue(
                    jsonPur,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, AiContentItem.class)
            );
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'extraction des données : " + e.getMessage());
        }
    }

    // DTO interne ultra-flexible pour encaisser les erreurs de TinyLlama
    public record AiContentItem(
        @JsonAlias({"type", "TYPE", "Type"}) 
        @JsonProperty("type")
        TypeContent type, 

        @JsonAlias({"content", "CONTENT", "Content", "texte", "text"}) 
        @JsonProperty("content")
        String content
    ) {}
    
    
    
}