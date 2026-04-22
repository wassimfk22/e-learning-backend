package com.school.elearning.service;

import com.school.elearning.model.*;
import com.school.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final BoiteReceptionRepository boiteReceptionRepository;
    private final CommunauteRepository communauteRepository;

    public Message envoyerMessagePrive(Utilisateur expediteur, List<Utilisateur> destinataires, String contenu) {
        Message m = new Message();
        m.setContenu(contenu);
        m.setDateEnvoi(new Date());
        m.setEstChat(false);
        m.setEstMultiDestinataires(destinataires.size() > 1);
        m.setExpediteur(expediteur);
        m.setDestinataires(destinataires);
        // Add to sender's boite
        BoiteReception boite = boiteReceptionRepository.findByUtilisateurId(expediteur.getId())
                .orElseThrow(() -> new RuntimeException("Boite de réception non trouvée"));
        m.setBoiteReception(boite);
        return messageRepository.save(m);
    }

    public Message envoyerMessageChat(Utilisateur expediteur, Long communauteId, String contenu) {
        Communaute c = communauteRepository.findById(communauteId)
                .orElseThrow(() -> new RuntimeException("Communauté non trouvée"));
        Message m = new Message();
        m.setContenu(contenu);
        m.setDateEnvoi(new Date());
        m.setEstChat(true);
        m.setEstMultiDestinataires(false);
        m.setExpediteur(expediteur);
        m.setCommunaute(c);
        return messageRepository.save(m);
    }

    public List<Message> getHistoriqueChat(Long communauteId) {
        return messageRepository.findByCommunauteIdAndEstChatTrue(communauteId);
    }
}
