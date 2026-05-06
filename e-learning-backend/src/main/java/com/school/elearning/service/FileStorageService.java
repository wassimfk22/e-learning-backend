package com.school.elearning.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    // Configuré dans application.properties :
    // file.upload-dir=uploads/photos
    @Value("${file.upload-dir:uploads/photos}")
    private String uploadDir;

    // Extensions autorisées pour les photos
    private static final List<String> EXTENSIONS_AUTORISEES = List.of(
        "jpg", "jpeg", "png", "webp"
    );

    // Taille max : 5 Mo
    private static final long TAILLE_MAX_OCTETS = 5 * 1024 * 1024;

    /**
     * Sauvegarde le fichier et retourne le chemin relatif à stocker en base.
     * Exemple retourné : "/uploads/photos/utilisateur_12_a3f2b1c4.jpg"
     */
    public String sauvegarderPhoto(MultipartFile fichier, Long utilisateurId) {
        validerFichier(fichier);

        try {
            // Créer le dossier si inexistant
            Path dossier = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dossier);

            // Nom unique : utilisateur_<id>_<uuid>.<ext>
            String extension = getExtension(fichier.getOriginalFilename());
            String nomFichier = "utilisateur_" + utilisateurId + "_"
                                + UUID.randomUUID().toString().substring(0, 8)
                                + "." + extension;

            Path destination = dossier.resolve(nomFichier);
            Files.copy(fichier.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            // Retourne le chemin relatif exposé via le serveur
            return "/" + uploadDir + "/" + nomFichier;

        } catch (IOException e) {
            throw new RuntimeException("Échec de l'enregistrement du fichier : " + e.getMessage());
        }
    }

    /**
     * Supprime l'ancienne photo du serveur si elle existe.
     * Appeler avant de sauvegarder la nouvelle.
     */
    public void supprimerPhoto(String cheminRelatif) {
        if (cheminRelatif == null || cheminRelatif.isBlank()) return;
        try {
            // Enlever le "/" initial pour obtenir le chemin relatif système
            String chemin = cheminRelatif.startsWith("/")
                ? cheminRelatif.substring(1)
                : cheminRelatif;
            Path fichier = Paths.get(chemin).toAbsolutePath().normalize();
            Files.deleteIfExists(fichier);
        } catch (IOException e) {
            // Non bloquant : on logue mais on ne plante pas
            System.err.println("Impossible de supprimer l'ancienne photo : " + e.getMessage());
        }
    }

    // ── Validation ─────────────────────────────────────────────────

    private void validerFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }
        if (fichier.getSize() > TAILLE_MAX_OCTETS) {
            throw new RuntimeException("Fichier trop lourd (max 5 Mo)");
        }
        String ext = getExtension(fichier.getOriginalFilename());
        if (!EXTENSIONS_AUTORISEES.contains(ext.toLowerCase())) {
            throw new RuntimeException(
                "Format non autorisé : " + ext + ". Acceptés : jpg, jpeg, png, webp"
            );
        }
    }

    private String getExtension(String nomFichier) {
        if (nomFichier == null || !nomFichier.contains(".")) {
            throw new RuntimeException("Nom de fichier invalide");
        }
        return nomFichier.substring(nomFichier.lastIndexOf('.') + 1);
    }
    
    
    
}