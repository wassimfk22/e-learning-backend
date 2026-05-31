package com.school.elearning.repository;

import com.school.elearning.model.Evenement;
import com.school.elearning.model.enums.TypeEvenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {

    // Tous les événements d'un calendrier
    @Query("SELECT e FROM Evenement e JOIN e.calendriers c WHERE c.id = :calendrierId ORDER BY e.dateDebut ASC")
    List<Evenement> findByCalendrierIdOrderByDateDebut(@Param("calendrierId") Long calendrierId);

    // Événements d'un calendrier filtrés par type
    @Query("SELECT e FROM Evenement e JOIN e.calendriers c WHERE c.id = :calendrierId AND e.type = :type ORDER BY e.dateDebut ASC")
    List<Evenement> findByCalendrierIdAndType(@Param("calendrierId") Long calendrierId,
                                               @Param("type") TypeEvenement type);

    // Événements à venir d'un calendrier
    @Query("SELECT e FROM Evenement e JOIN e.calendriers c WHERE c.id = :calendrierId AND e.dateDebut >= :now ORDER BY e.dateDebut ASC")
    List<Evenement> findAVenirByCalendrierId(@Param("calendrierId") Long calendrierId,
                                              @Param("now") LocalDateTime now);

    // Événements créés par un modérateur
    List<Evenement> findByModerateurIdOrderByDateDebutAsc(Long moderateurId);
}