package com.school.elearning.service;

import com.school.elearning.dto.*;
import com.school.elearning.model.*;
import com.school.elearning.model.Module;
import com.school.elearning.model.enums.*;
import com.school.elearning.repository.*;
import com.school.elearning.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EtudiantQuizService {

    private final QuizRepository quizRepository;
    private final QuestionQuizRepository questionQuizRepository;
    private final TentativeQuizRepository tentativeRepository;
    private final ReponseQuizRepository reponseRepository;
    private final EtudiantRepository etudiantRepository;
    private final ProgressionModuleRepository progressionRepository;
    private final ResultatRepository resultatRepository;
    private final NoteRepository noteRepository;
    private final ModuleRepository moduleRepository;
    private final CoursRepository coursRepository;

    // ══════════════════════════════════════════════════════════════
    // 1. LISTER LES QUIZ D'UN COURS
    //    Vérifie que l'étudiant appartient au niveau → module → cours
    // ══════════════════════════════════════════════════════════════

    public List<QuizEtudiantResponse> getQuizzesDuCours(Long coursId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        verifierAccesCours(etudiant, coursId);

        return quizRepository.findByCoursId(coursId).stream()
                .map(quiz -> toQuizEtudiantResponse(quiz, etudiant))
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 2. DÉMARRER UNE TENTATIVE
    //    Crée une TentativeQuiz EN_COURS pour l'étudiant
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public TentativeResponse demarrerTentative(Long quizId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Quiz quiz = findQuiz(quizId);

        // Vérifier accès via niveau
        verifierAccesCours(etudiant, quiz.getCours().getId());

        // Vérifier dates du quiz
        LocalDate aujourd_hui = LocalDate.now();
        if (aujourd_hui.isBefore(quiz.getDateDebut())) {
            throw new RuntimeException("Ce quiz n'est pas encore ouvert. Ouverture le " + quiz.getDateDebut());
        }
        if (aujourd_hui.isAfter(quiz.getDateFin())) {
            throw new RuntimeException("Ce quiz est fermé depuis le " + quiz.getDateFin());
        }

        // Vérifier qu'il n'y a pas déjà une tentative EN_COURS
        Optional<TentativeQuiz> enCours = tentativeRepository
                .findByEtudiantAndQuizAndStatut(etudiant, quiz, StatutTentative.EN_COURS);
        if (enCours.isPresent()) {
            return toTentativeResponse(enCours.get());
        }

        // Compter tentatives soumises
        int tentativesEffectuees = tentativeRepository
                .countByEtudiantAndQuizAndStatut(etudiant, quiz, StatutTentative.SOUMISE);

        if (tentativesEffectuees >= quiz.getNombreTentativesMax()) {
            throw new RuntimeException(
                "Nombre maximum de tentatives atteint (" + quiz.getNombreTentativesMax() + "/" + quiz.getNombreTentativesMax() + ")"
            );
        }

        // Calculer score max
        double scoreMax = quiz.getQuestions().stream()
                .mapToDouble(QuestionQuiz::getPoints).sum();

        // Créer la tentative
        TentativeQuiz tentative = new TentativeQuiz();
        tentative.setEtudiant(etudiant);
        tentative.setQuiz(quiz);
        tentative.setNumeroTentative(tentativesEffectuees + 1);
        tentative.setScoreMax(scoreMax);
        tentative.setScoreObtenu(0);
        tentative.setTentativesRestantes(quiz.getNombreTentativesMax() - tentativesEffectuees - 1);
        tentative.setStatut(StatutTentative.EN_COURS);
        tentative.setDateDebut(LocalDateTime.now());

        return toTentativeResponse(tentativeRepository.save(tentative));
    }

    // ══════════════════════════════════════════════════════════════
    // 3. RÉPONDRE À UNE QUESTION
    
    @Transactional
    public List<ReponseQuizResultat> repondreQuestions(List<ReponseQuizRequest> requests, Authentication auth) {
        // A. Initialisation et vérifications de sécurité
        Etudiant etudiant = getEtudiantConnecte(auth);
        TentativeQuiz tentative = validerEtRecupererTentative(requests.get(0).getTentativeId(), etudiant);

        // B. Enregistrement des réponses une par une
        List<ReponseQuizResultat> resultats = new ArrayList<>();
        for (ReponseQuizRequest req : requests) {
            resultats.add(traiterEnregistrementReponse(req, tentative));
        }

        // C. Finalisation (Calcul du score si fini)
        verifierEtFinaliserTentative(tentative, resultats);

        return resultats;
    }
    
    private TentativeQuiz validerEtRecupererTentative(Long tentativeId, Etudiant etudiant) {
        TentativeQuiz tentative = tentativeRepository.findById(tentativeId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable"));

        if (!tentative.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Accès refusé : ce n'est pas votre tentative");
        }
        if (tentative.getStatut() != StatutTentative.EN_COURS) {
            throw new RuntimeException("Cette tentative est déjà clôturée");
        }
        return tentative;
    }
    
    private ReponseQuizResultat traiterEnregistrementReponse(ReponseQuizRequest req, TentativeQuiz tentative) {
        QuestionQuiz question = questionQuizRepository.findById(req.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question introuvable"));

        // Empêcher de répondre deux fois à la même question
        if (reponseRepository.existsByTentativeAndQuestion(tentative, question)) {
            return creerResultatDejaRepondu(question, req);
        }

        boolean estCorrecte = question.getBonneReponse().equalsIgnoreCase(req.getReponseChoisie());
        
        ReponseQuiz reponse = new ReponseQuiz();
        reponse.setTentative(tentative);
        reponse.setQuestion(question);
        reponse.setReponseChoisie(req.getReponseChoisie());
        reponse.setEstCorrecte(estCorrecte);
        reponse.setPointsObtenus(estCorrecte ? question.getPoints() : 0);
        
        reponseRepository.save(reponse);
        return transformerEnResultat(reponse);
    }
    
    private void verifierEtFinaliserTentative(TentativeQuiz tentative, List<ReponseQuizResultat> resultats) {
        int totalQuestions = tentative.getQuiz().getQuestions().size();
        int questionsRepondues = reponseRepository.countByTentative(tentative);

        if (questionsRepondues >= totalQuestions) {
            // Calcul du score total
            double pointsTotauxObtenus = reponseRepository.sumPointsByTentative(tentative);
            double baremeMaximum = tentative.getQuiz().getQuestions().stream()
                                            .mapToDouble(QuestionQuiz::getPoints).sum();

            // Calcul du pourcentage : (Obtenu / Total) * 100
            double pourcentage = (pointsTotauxObtenus / baremeMaximum) * 100;

            // Mise à jour de la tentative
            tentative.setScoreFinal(pointsTotauxObtenus);
            tentative.setPourcentage(pourcentage);
            tentative.setStatut(StatutTentative.TERMINEE);
            tentative.setDateFin(new Date());
            
            tentativeRepository.save(tentative);

            // On informe le dernier résultat de la liste
            ReponseQuizResultat dernierRes = resultats.get(resultats.size() - 1);
            dernierRes.setToutesReponsesEnvoyees(true);
            dernierRes.setScoreValeur(pourcentage); // On renvoie le % au front
        }
    }

    // ══════════════════════════════════════════════════════════════
    // 4. HISTORIQUE DES TENTATIVES D'UN ÉTUDIANT POUR UN QUIZ
    // ══════════════════════════════════════════════════════════════

    public List<TentativeResponse> getHistoriqueTentatives(Long quizId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Quiz quiz = findQuiz(quizId);
        verifierAccesCours(etudiant, quiz.getCours().getId());

        return tentativeRepository
                .findByEtudiantAndQuizOrderByNumeroTentativeAsc(etudiant, quiz)
                .stream().map(this::toTentativeResponse)
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 5. DASHBOARD ÉTUDIANT
    // ══════════════════════════════════════════════════════════════

    public DashboardEtudiantResponse getDashboard(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        List<ProgressionModule> progressions = progressionRepository.findByEtudiant(etudiant);

        int termines = (int) progressions.stream()
                .filter(p -> p.getStatut() == StatutProgression.TERMINE).count();

        float progressionGlobale = progressions.isEmpty() ? 0 :
                (float) progressions.stream()
                        .mapToDouble(ProgressionModule::getPourcentageCompletude).average().orElse(0);

        String niveauNom = (etudiant.getCommunaute() != null
                && etudiant.getCommunaute().getNiveau() != null)
                ? etudiant.getCommunaute().getNiveau().getNom() : null;

        List<ProgressionModuleResponse> progressionResponses = progressions.stream()
                .map(p -> toProgressionResponse(p, etudiant))
                .collect(Collectors.toList());

        List<TentativeResponse> dernieres = tentativeRepository
                .findTop5ByEtudiantOrderByDateDebutDesc(etudiant)
                .stream().map(this::toTentativeResponse)
                .collect(Collectors.toList());

        return new DashboardEtudiantResponse(
                etudiant.getNom(), etudiant.getPrenom(), niveauNom,
                progressions.size(), termines, progressionGlobale,
                progressionResponses, dernieres
        );
    }

    // ══════════════════════════════════════════════════════════════
    // SOUMISSION AUTOMATIQUE (interne)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    private ScoreFinaleResponse soumettreTentative(TentativeQuiz tentative) {
        // Calculer le score total
        List<ReponseQuiz> reponses = reponseRepository.findByTentative(tentative);
        double scoreObtenu = reponses.stream().mapToDouble(ReponseQuiz::getPointsObtenus).sum();

        tentative.setScoreObtenu(scoreObtenu);
        tentative.setStatut(StatutTentative.SOUMISE);
        tentative.setDateSoumission(LocalDateTime.now());

        int tentativesRestantes = tentative.getTentativesRestantes();
        tentativeRepository.save(tentative);

        // Mettre à jour les résultats et la progression si tentatives épuisées
        boolean tentativesEpuisees = (tentativesRestantes == 0);
        if (tentativesEpuisees) {
            mettreAJourResultatEtProgression(tentative);
        }

        // Message dynamique
        double pourcentage = tentative.getScoreMax() > 0
                ? (scoreObtenu / tentative.getScoreMax()) * 100 : 0;
        String message = genererMessage(pourcentage, tentativesRestantes);

        ScoreFinaleResponse score = new ScoreFinaleResponse();
        score.setTentativeId(tentative.getId());
        score.setNumeroTentative(tentative.getNumeroTentative());
        score.setScoreObtenu(scoreObtenu);
        score.setScoreMax(tentative.getScoreMax());
        score.setPourcentage(Math.round(pourcentage * 100.0) / 100.0);
        score.setTentativesRestantes(tentativesRestantes);
        score.setTentativesEpuisees(tentativesEpuisees);
        score.setDateSoumission(tentative.getDateSoumission());
        score.setMessage(message);
        return score;
    }

    // ══════════════════════════════════════════════════════════════
    // MISE À JOUR RÉSULTAT + PROGRESSION (interne)
    // Appelé quand tentatives épuisées → enregistre le meilleur score
    // ══════════════════════════════════════════════════════════════

    @Transactional
    private void mettreAJourResultatEtProgression(TentativeQuiz tentative) {
        Etudiant etudiant = tentative.getEtudiant();
        Module module = tentative.getQuiz().getCours().getModule();

        // Meilleur score de l'étudiant pour ce quiz
        Double meilleurScore = tentativeRepository
                .findMeilleurScoreByEtudiantAndQuiz(etudiant, tentative.getQuiz());
        double scoreMax = tentative.getScoreMax();
        float noteSur20 = scoreMax > 0
                ? (float) Math.round((meilleurScore / scoreMax) * 20 * 100) / 100f
                : 0f;

        // Récupérer ou créer le Resultat pour cet étudiant/module
        Resultat resultat = resultatRepository
                .findByEtudiantIdAndModuleId(etudiant.getId(), module.getId())
                .orElseGet(() -> {
                    Resultat r = new Resultat();
                    r.setEtudiant(etudiant);
                    r.setModule(module);
                    r.setMoyenneQuizs(0f);
                    r.setMoyenneExamens(0f);
                    r.setMoyenneGenerale(0f);
                    return resultatRepository.save(r);
                });

        // Créer la Note liée à ce quiz
        Note note = new Note();
        note.setValeur(noteSur20);
        note.setDateObtention(new Date());
        note.setType(TypeNote.QUIZ);
        note.setResultat(resultat);
        noteRepository.save(note);

        // Recalculer la moyenne des quiz pour ce module
        List<TentativeQuiz> toutesLesTentativesModule = tentativeRepository
                .findByEtudiantAndModuleId(etudiant, module.getId());

        // Grouper par quiz → garder le meilleur score de chaque quiz
        Map<Long, Double> meilleurScoreParQuiz = new HashMap<>();
        for (TentativeQuiz t : toutesLesTentativesModule) {
            Long quizId = t.getQuiz().getId();
            double scoreNorm = t.getScoreMax() > 0
                    ? (t.getScoreObtenu() / t.getScoreMax()) * 20 : 0;
            meilleurScoreParQuiz.merge(quizId, scoreNorm, Math::max);
        }

        float nouvelleMoyenne = meilleurScoreParQuiz.isEmpty() ? 0f :
                (float) meilleurScoreParQuiz.values().stream()
                        .mapToDouble(Double::doubleValue).average().orElse(0);

        resultat.setMoyenneQuizs(nouvelleMoyenne);
        resultat.setMoyenneGenerale((nouvelleMoyenne + resultat.getMoyenneExamens()) / 2);
        resultatRepository.save(resultat);

        // Mettre à jour la ProgressionModule
        progressionRepository.findByEtudiant(etudiant).stream()
                .filter(p -> p.getModule().getId().equals(module.getId()))
                .findFirst()
                .ifPresent(progression -> {
                    // Calcul du pourcentage de complétion
                    long quizzesTotal = module.getCours().stream()
                            .flatMap(c -> c.getQuizzes().stream()).count();
                    long quizzesTermines = meilleurScoreParQuiz.size();
                    float completude = quizzesTotal > 0
                            ? (float) quizzesTermines / quizzesTotal * 100f : 0f;
                    progression.setPourcentageCompletude(completude);
                    if (completude >= 100f) {
                        progression.setStatut(StatutProgression.TERMINE);
                    } else if (progression.getStatut() == StatutProgression.NON_COMMENCE) {
                        progression.setStatut(StatutProgression.EN_COURS);
                    }
                    progressionRepository.save(progression);
                });
    }

    // ══════════════════════════════════════════════════════════════
    // VÉRIFICATION ACCÈS : Etudiant → Communaute → Niveau → Module → Cours
    // ══════════════════════════════════════════════════════════════

    private void verifierAccesCours(Etudiant etudiant, Long coursId) {
        // L'étudiant doit appartenir à une communauté ayant un niveau
        if (etudiant.getCommunaute() == null || etudiant.getCommunaute().getNiveau() == null) {
            throw new RuntimeException("Vous n'êtes affecté à aucun niveau. Contactez votre modérateur.");
        }

        Long niveauEtudiantId = etudiant.getCommunaute().getNiveau().getId();
        System.out.println("DEBUG -> ID Niveau Etudiant: " + niveauEtudiantId);
        System.out.println("DEBUG -> ID Cours recherché: " + coursId);

        // Vérifier que le cours → module → niveau correspond au niveau de l'étudiant
     // On vérifie directement si le cours appartient au niveau de l'étudiant
        // en passant par le module
        boolean accesAutorise = coursRepository.existsByIdAndModule_Niveau_Id(coursId, niveauEtudiantId);

        if (!accesAutorise) {
            throw new RuntimeException("Accès refusé : ce cours n'appartient pas à votre niveau");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS & MAPPERS
    // ══════════════════════════════════════════════════════════════

    private Etudiant getEtudiantConnecte(Authentication auth) {
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return etudiantRepository.findById(details.getUtilisateur().getId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable ou pas un étudiant"));
    }

    private Quiz findQuiz(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz introuvable : " + id));
    }

    private String genererMessage(double pourcentage, int tentativesRestantes) {
        String appreciation;
        if (pourcentage >= 90) appreciation = "🏆 Excellent !";
        else if (pourcentage >= 70) appreciation = "👍 Bien !";
        else if (pourcentage >= 50) appreciation = "📚 Passable, continuez vos efforts.";
        else appreciation = "❌ Insuffisant, révisez et réessayez.";

        if (tentativesRestantes > 0) {
            return appreciation + " Score : " + String.format("%.1f", pourcentage) + "%. "
                    + "Il vous reste " + tentativesRestantes + " tentative(s).";
        } else {
            return appreciation + " Score final : " + String.format("%.1f", pourcentage) + "%. "
                    + "Toutes vos tentatives sont épuisées.";
        }
    }

    private QuizEtudiantResponse toQuizEtudiantResponse(Quiz quiz, Etudiant etudiant) {
        int effectuees = tentativeRepository.countByEtudiantAndQuizAndStatut(
                etudiant, quiz, StatutTentative.SOUMISE);
        int restantes = Math.max(0, quiz.getNombreTentativesMax() - effectuees);
        LocalDate today = LocalDate.now();
        boolean peutPasser = restantes > 0
                && !today.isBefore(quiz.getDateDebut())
                && !today.isAfter(quiz.getDateFin());

        double pointsTotal = quiz.getQuestions().stream()
                .mapToDouble(QuestionQuiz::getPoints).sum();

        List<QuestionEtudiantResponse> questions = quiz.getQuestions().stream().map(q -> {
            QuestionEtudiantResponse qr = new QuestionEtudiantResponse();
            qr.setId(q.getId());
            qr.setEnonce(q.getEnonce());
            qr.setChoixPossibles(q.getChoixPossibles());
            qr.setPoints(q.getPoints());
            return qr;
        }).collect(Collectors.toList());

        QuizEtudiantResponse r = new QuizEtudiantResponse();
        r.setId(quiz.getId());
        r.setTitre(quiz.getTitre());
        r.setDateDebut(quiz.getDateDebut());
        r.setDateFin(quiz.getDateFin());
        r.setNombreTentativesMax(quiz.getNombreTentativesMax());
        r.setTentativesDejaEffectuees(effectuees);
        r.setTentativesRestantes(restantes);
        r.setPeutPasser(peutPasser);
        r.setNombreQuestions(quiz.getQuestions().size());
        r.setPointsTotal(pointsTotal);
        r.setCoursTitre(quiz.getCours() != null ? quiz.getCours().getTitre() : null);
        r.setQuestions(questions);
        return r;
    }

    private TentativeResponse toTentativeResponse(TentativeQuiz t) {
        double pourcentage = t.getScoreMax() > 0
                ? Math.round((t.getScoreObtenu() / t.getScoreMax()) * 10000.0) / 100.0 : 0;
        TentativeResponse r = new TentativeResponse();
        r.setId(t.getId());
        r.setNumeroTentative(t.getNumeroTentative());
        r.setScoreObtenu(t.getScoreObtenu());
        r.setScoreMax(t.getScoreMax());
        r.setPourcentage(pourcentage);
        r.setTentativesRestantes(t.getTentativesRestantes());
        r.setStatut(t.getStatut());
        r.setDateDebut(t.getDateDebut());
        r.setDateSoumission(t.getDateSoumission());
        r.setQuizTitre(t.getQuiz() != null ? t.getQuiz().getTitre() : null);
        return r;
    }

    private ProgressionModuleResponse toProgressionResponse(ProgressionModule p, Etudiant etudiant) {
        Optional<Resultat> resultat = resultatRepository
                .findByEtudiantIdAndModuleId(etudiant.getId(), p.getModule().getId());
        ProgressionModuleResponse r = new ProgressionModuleResponse();
        r.setId(p.getId());
        r.setModuleId(p.getModule().getId());
        r.setModuleTitre(p.getModule().getTitre());
        r.setNiveauNom(p.getModule().getNiveau() != null ? p.getModule().getNiveau().getNom() : null);
        r.setStatut(p.getStatut());
        r.setPourcentageCompletude(p.getPourcentageCompletude());
        r.setDateInscription(p.getDateInscription());
        resultat.ifPresent(res -> {
            r.setMoyenneQuizs(res.getMoyenneQuizs());
            r.setMoyenneExamens(res.getMoyenneExamens());
            r.setMoyenneGenerale(res.getMoyenneGenerale());
        });
        return r;
    }
    
    
    
}