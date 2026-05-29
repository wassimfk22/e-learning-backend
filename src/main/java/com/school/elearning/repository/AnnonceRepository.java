package com.school.elearning.repository;

import com.school.elearning.model.Annonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    // Toutes les annonces triées par date décroissante (pour l'accueil)
    List<Annonce> findAllByOrderByDatePublicationDesc();

    // Annonces d'un auteur spécifique
    List<Annonce> findByAuteurIdOrderByDatePublicationDesc(Long auteurId);
    
}