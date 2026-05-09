package com.school.elearning.service;

import com.school.elearning.dto.MessageRequest;
import com.school.elearning.dto.MessageResponse;
import com.school.elearning.model.*;
import com.school.elearning.model.enums.TypeMessageCommunaute;
import com.school.elearning.repository.CommunauteRepository;
import com.school.elearning.repository.EtudiantRepository;
import com.school.elearning.repository.MessageRepository;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunauteMessageService {

    private final MessageRepository messageRepository;
    private final CommunauteRepository communauteRepository;
    private final EtudiantRepository etudiantRepository;
    private final FileStorageService fileStorageService;

    // ══════════════════════════════════════════════════════════════
    // 1. LISTER TOUS LES MESSAGES DE MA COMMUNAUTÉ
    //    Retourne uniquement les messages racines avec leurs réponses
    // ══════════════════════════════════════════════════════════════

    public List<MessageResponse> getMessagesDeMaCommunaute(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Communaute communaute = verifierAppartenance(etudiant);

        return messageRepository
                .findByCommunauteIdAndParentIsNullOrderByDateEnvoiAsc(communaute.getId())
                .stream()
                .map(m -> toResponse(m, true)) // true = inclure les réponses
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 2. ENVOYER UN MESSAGE TEXTE
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public MessageResponse envoyerMessage(MessageRequest request, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Communaute communaute = verifierAppartenance(etudiant);

        if (request.getContenu() == null || request.getContenu().isBlank()) {
            throw new RuntimeException("Le contenu du message ne peut pas être vide.");
        }

        Message message = new Message();
        message.setContenu(request.getContenu().trim());
        message.setType(TypeMessageCommunaute.TEXT);
        message.setDateEnvoi(new Date());
        message.setExpediteur(etudiant);
        message.setCommunaute(communaute);
        message.setSupprime(false);

        // Réponse à un message précis ?
        if (request.getParentId() != null) {
            Message parent = findMessageDeLaCommunaute(request.getParentId(), communaute.getId());
            if (parent.isSupprime()) {
                throw new RuntimeException("Impossible de répondre à un message supprimé.");
            }
            message.setParent(parent);
        }

        return toResponse(messageRepository.save(message), false);
    }

    // ══════════════════════════════════════════════════════════════
    // 3. ENVOYER UN FICHIER (IMAGE ou PDF)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public MessageResponse envoyerFichier(MultipartFile fichier, String contenu,
                                           Long parentId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Communaute communaute = verifierAppartenance(etudiant);

        // Déterminer le type selon l'extension
        String filename = fichier.getOriginalFilename();
        if (filename == null) throw new RuntimeException("Fichier invalide");

        TypeMessageCommunaute type;
        String ext = filename.toLowerCase();
        if (ext.endsWith(".jpg") || ext.endsWith(".jpeg")
                || ext.endsWith(".png") || ext.endsWith(".webp")) {
            type = TypeMessageCommunaute.IMAGE;
        } else if (ext.endsWith(".pdf")) {
            type = TypeMessageCommunaute.PDF;
        } else {
            throw new RuntimeException("Format non supporté. Utilisez JPG, PNG, WEBP ou PDF.");
        }

        // Sauvegarder le fichier sur disque
        String chemin = fileStorageService.sauvegarderFichierCommunaute(fichier, etudiant.getId());

        Message message = new Message();
        message.setContenu(contenu); // peut être null
        message.setType(type);
        message.setFichierUrl(chemin);
        message.setDateEnvoi(new Date());
        message.setExpediteur(etudiant);
        message.setCommunaute(communaute);
        message.setSupprime(false);

        if (parentId != null) {
            Message parent = findMessageDeLaCommunaute(parentId, communaute.getId());
            if (parent.isSupprime()) {
                throw new RuntimeException("Impossible de répondre à un message supprimé.");
            }
            message.setParent(parent);
        }

        return toResponse(messageRepository.save(message), false);
    }

    // ══════════════════════════════════════════════════════════════
    // 4. MODIFIER UN MESSAGE (texte uniquement, par l'auteur)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public MessageResponse modifierMessage(Long messageId, String nouveauContenu,
                                            Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Message message = findMessageParId(messageId);

        verifierAuteur(message, etudiant);

        if (message.isSupprime()) {
            throw new RuntimeException("Impossible de modifier un message supprimé.");
        }
        if (message.getType() != TypeMessageCommunaute.TEXT) {
            throw new RuntimeException("Seuls les messages texte peuvent être modifiés.");
        }
        if (nouveauContenu == null || nouveauContenu.isBlank()) {
            throw new RuntimeException("Le nouveau contenu ne peut pas être vide.");
        }

        message.setContenu(nouveauContenu.trim());
        message.setDateModification(new Date());

        return toResponse(messageRepository.save(message), false);
    }

    // ══════════════════════════════════════════════════════════════
    // 5. SUPPRIMER UN MESSAGE (par l'auteur)
    //    Soft delete : le message reste en base pour ne pas casser le fil
    //    Les réponses sont conservées mais le contenu est masqué
    //    Si le message n'a pas de réponses → suppression physique
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public void supprimerMessage(Long messageId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Message message = findMessageParId(messageId);

        verifierAuteur(message, etudiant);

        // Si le message a des réponses → soft delete (masquer le contenu)
        if (message.getReponses() != null && !message.getReponses().isEmpty()) {
            message.setSupprime(true);
            message.setContenu(null);
            message.setFichierUrl(null);
            messageRepository.save(message);
        } else {
            // Pas de réponses → suppression physique
            // Si c'est une image/pdf, supprimer aussi le fichier du disque
            if (message.getFichierUrl() != null) {
                fileStorageService.supprimerPhoto(message.getFichierUrl());
            }
            messageRepository.delete(message);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
    // ══════════════════════════════════════════════════════════════

    private Etudiant getEtudiantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Pas un étudiant"));
    }

    private Communaute verifierAppartenance(Etudiant etudiant) {
        if (etudiant.getCommunaute() == null) {
            throw new RuntimeException("Vous n'êtes affecté à aucune communauté. Contactez votre modérateur.");
        }
        return etudiant.getCommunaute();
    }

    private Message findMessageParId(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message introuvable : " + messageId));
    }

    private Message findMessageDeLaCommunaute(Long messageId, Long communauteId) {
        Message message = findMessageParId(messageId);
        if (!message.getCommunaute().getId().equals(communauteId)) {
            throw new RuntimeException("Ce message n'appartient pas à votre communauté.");
        }
        return message;
    }

    private void verifierAuteur(Message message, Etudiant etudiant) {
        if (!message.getExpediteur().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Vous ne pouvez modifier/supprimer que vos propres messages.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // MAPPER
    // ══════════════════════════════════════════════════════════════

    public MessageResponse toResponse(Message m, boolean inclureReponses) {
        MessageResponse r = new MessageResponse();
        r.setId(m.getId());

        // Masquer le contenu si supprimé
        if (m.isSupprime()) {
            r.setContenu("[Message supprimé]");
            r.setSupprime(true);
            r.setFichierUrl(null);
        } else {
            r.setContenu(m.getContenu());
            r.setSupprime(false);
            r.setFichierUrl(m.getFichierUrl());
        }

        r.setType(m.getType());
        r.setDateEnvoi(m.getDateEnvoi());
        r.setDateModification(m.getDateModification());
        r.setModifie(m.getDateModification() != null);

        // Auteur
        Utilisateur expediteur = m.getExpediteur();
        r.setExpediteurId(expediteur.getId());
        r.setExpediteurNom(expediteur.getNom());
        r.setExpediteurPrenom(expediteur.getPrenom());
        r.setExpediteurPhoto(expediteur.getPhoto());

        // Message parent (aperçu)
        if (m.getParent() != null) {
            Message parent = m.getParent();
            r.setParentId(parent.getId());
            if (!parent.isSupprime() && parent.getContenu() != null) {
                String resume = parent.getContenu();
                r.setParentContenuResume(resume.length() > 50
                        ? resume.substring(0, 50) + "..." : resume);
            } else if (parent.isSupprime()) {
                r.setParentContenuResume("[Message supprimé]");
            } else if (parent.getFichierUrl() != null) {
                r.setParentContenuResume("[" + parent.getType().name() + "]");
            }
        }

        // Réponses (un seul niveau de profondeur pour éviter la récursion)
        if (inclureReponses && m.getReponses() != null) {
            r.setReponses(m.getReponses().stream()
                    .map(rep -> toResponse(rep, false)) // false = pas de sous-réponses
                    .collect(Collectors.toList()));
        } else {
            r.setReponses(new ArrayList<>());
        }

        return r;
    }
    
    
    
}