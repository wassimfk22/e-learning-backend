package com.school.elearning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqCoursService {

    private final GroqService groqService;

    private static final String SYSTEM_COURS = """
            Tu es un expert pédagogique senior. À partir d'un texte extrait d'un PDF de cours,
            tu génères un cours structuré, clair et complet en FRANÇAIS.
            
            RÈGLES :
            - Commence directement par le contenu, sans introduction générique.
            - Utilise des titres avec === TITRE === pour chaque section.
            - Pour chaque concept technique, fournis des exemples concrets.
            - Ajoute des astuces avec → ASTUCE : ...
            - Ajoute des mises en garde avec → ATTENTION : ...
            - Termine par === RÉSUMÉ ===
            - Réponds UNIQUEMENT en FRANÇAIS.
            """;

    public String genererCoursDepuisTexte(String texte) {
        String prompt = "Génère un cours complet et structuré basé sur ce contenu :\n\n" 
                + (texte.length() > 4000 ? texte.substring(0, 4000) : texte);
        log.info("Génération cours via Groq...");
        return groqService.ask(SYSTEM_COURS, prompt);
    }
}