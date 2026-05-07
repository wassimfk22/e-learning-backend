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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private final CoursRepository coursRepository;
    private final CoursProgressionRepository coursProgressionRepository;
    private final PassageExamenRepository passageExamenRepository;
    private final ExamenEnseignantService examenEnseignantService;
    private final CoursEtudiantService coursEtudiantService;

    // ══════════════════════════════════════════════════════════════
    // 1. LISTER LES QUIZ D'UN COURS
    // ══════════════════════════════════════════════════════════════

    public List<QuizEtudiantResponse> getQuizzesDuCours(Long coursId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        verifierAccesCours(etudiant, coursId);

        return quizRepository.findByCoursId(coursId).stream()
                .map(quiz -> toQuizEtudiantResponse(quiz, etudiant))
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 2. DÉMARRER LE QUIZ (passage unique)
    //    - Vérifie que l'étudiant n'a pas déjà passé ce quiz
    //    - Crée la TentativeQuiz EN_COURS avec dateExpiration
    //    - Tire aléatoirement le bon nombre de questions
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public TentativeResponse demarrerQuiz(Long quizId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Quiz quiz = findQuiz(quizId);

        verifierAccesCours(etudiant, quiz.getCours().getId());

        // Vérifier passage unique : déjà soumis → refus définitif
        Optional<TentativeQuiz> existante = tentativeRepository.findByEtudiantAndQuiz(etudiant, quiz);
        if (existante.isPresent()) {
            TentativeQuiz t = existante.get();
            if (t.getStatut() == StatutTentative.SOUMISE) {
                throw new RuntimeException("Vous avez déjà passé ce quiz. Le quiz ne peut être passé qu'une seule fois.");
            }
            // Session EN_COURS encore valide → renvoyer la tentative existante
            if (!t.estExpiree()) {
                return toTentativeResponse(t);
            }
            // Session EN_COURS expirée → soumettre automatiquement
            return toTentativeResponse(soumettreAutomatiquement(t));
        }

        // Calcul du score max sur les questions qui seront affichées
        List<QuestionQuiz> toutesQuestions = quiz.getQuestions();
        int nbAfficher = Math.min(quiz.getNombreQuestions(), toutesQuestions.size());

        // Pioche aléatoire
        List<QuestionQuiz> questionsSelectionnees = new ArrayList<>(toutesQuestions);
        Collections.shuffle(questionsSelectionnees);
        questionsSelectionnees = questionsSelectionnees.subList(0, nbAfficher);

        double scoreMax = questionsSelectionnees.stream()
                .mapToDouble(QuestionQuiz::getPoints).sum();

        // Création de la tentative
        LocalDateTime maintenant = LocalDateTime.now();
        TentativeQuiz tentative = new TentativeQuiz();
        tentative.setEtudiant(etudiant);
        tentative.setQuiz(quiz);
        tentative.setScoreMax(scoreMax);
        tentative.setScoreObtenu(0);
        tentative.setPourcentage(0);
        tentative.setStatut(StatutTentative.EN_COURS);
        tentative.setDateDebut(maintenant);
        tentative.setDateExpiration(maintenant.plusMinutes(quiz.getDureeMinutes()));

        return toTentativeResponse(tentativeRepository.save(tentative));
    }

    // ══════════════════════════════════════════════════════════════
    // 3. RÉCUPÉRER LES QUESTIONS DE LA TENTATIVE EN COURS
    //    Appelé après demarrerQuiz pour afficher les questions
    // ══════════════════════════════════════════════════════════════

    public List<QuestionEtudiantResponse> getQuestionsDeTentative(Long tentativeId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        TentativeQuiz tentative = findTentative(tentativeId, etudiant);

        verifierTentativeActive(tentative);

        Quiz quiz = tentative.getQuiz();
        List<QuestionQuiz> toutesQuestions = quiz.getQuestions();
        int nbAfficher = Math.min(quiz.getNombreQuestions(), toutesQuestions.size());

        // On reproduit la même pioche déterministe grâce à la seed = id de la tentative
        // → même ordre garanti entre les appels
        List<QuestionQuiz> questions = new ArrayList<>(toutesQuestions);
        Collections.shuffle(questions, new Random(tentative.getId()));
        questions = questions.subList(0, nbAfficher);

        return questions.stream().map(q -> {
            QuestionEtudiantResponse r = new QuestionEtudiantResponse();
            r.setId(q.getId());
            r.setEnonce(q.getEnonce());
            r.setChoixPossibles(q.getChoixPossibles());
            r.setPoints(q.getPoints());
            return r;
        }).collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════════════
    // 4. SOUMETTRE TOUTES LES RÉPONSES EN UNE FOIS (fin de quiz)
    //    L'étudiant envoie la liste complète de ses réponses + clique "Terminer"
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public ScoreFinaleResponse soumettreQuiz(Long tentativeId,
                                              List<ReponseQuizRequest> reponses,
                                              Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        TentativeQuiz tentative = findTentative(tentativeId, etudiant);

        if (tentative.getStatut() == StatutTentative.SOUMISE) {
            throw new RuntimeException("Ce quiz a déjà été soumis.");
        }

        // Si le temps est expiré → on traite quand même les réponses envoyées
        boolean soumisParExpiration = tentative.estExpiree();

        // Enregistrer les réponses
        for (ReponseQuizRequest req : reponses) {
            QuestionQuiz question = questionQuizRepository.findById(req.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question introuvable : " + req.getQuestionId()));

            // On n'enregistre pas deux fois la même question
            if (reponseRepository.existsByTentativeAndQuestion(tentative, question)) {
                continue;
            }

            boolean estCorrecte = question.getBonneReponse()
                    .equalsIgnoreCase(req.getReponseChoisie());

            ReponseQuiz reponse = new ReponseQuiz();
            reponse.setTentative(tentative);
            reponse.setQuestion(question);
            reponse.setReponseChoisie(req.getReponseChoisie());
            reponse.setEstCorrecte(estCorrecte);
            reponse.setPointsObtenus(estCorrecte ? question.getPoints() : 0);
            reponseRepository.save(reponse);
        }

        // Calcul du score final
        double scoreObtenu = reponseRepository.sumPointsByTentative(tentative);
        double scoreMax = tentative.getScoreMax();
        double pourcentage = scoreMax > 0
                ? Math.round((scoreObtenu / scoreMax) * 10000.0) / 100.0
                : 0;

        // Mise à jour de la tentative
        tentative.setScoreObtenu(scoreObtenu);
        tentative.setPourcentage(pourcentage);
        tentative.setStatut(StatutTentative.SOUMISE);
        tentative.setDateSoumission(LocalDateTime.now());
        tentativeRepository.save(tentative);

        // Mise à jour résultats et progression
        mettreAJourResultatEtProgression(tentative);

        String message = genererMessage(pourcentage, soumisParExpiration);

        ScoreFinaleResponse score = new ScoreFinaleResponse();
        score.setTentativeId(tentative.getId());
        score.setScoreObtenu(scoreObtenu);
        score.setScoreMax(scoreMax);
        score.setPourcentage(pourcentage);
        score.setDateSoumission(tentative.getDateSoumission());
        score.setMessage(message);
        score.setSoumisParExpiration(soumisParExpiration);
        return score;
    }

    // ══════════════════════════════════════════════════════════════
    // 5. SOUMISSION AUTOMATIQUE PAR EXPIRATION DU CHRONO
    //    Appelé quand l'étudiant revient sur une tentative expirée
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public ScoreFinaleResponse soumettreParExpiration(Long tentativeId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        TentativeQuiz tentative = findTentative(tentativeId, etudiant);

        if (tentative.getStatut() == StatutTentative.SOUMISE) {
            throw new RuntimeException("Ce quiz a déjà été soumis.");
        }
        if (!tentative.estExpiree()) {
            throw new RuntimeException("Le temps n'est pas encore écoulé.");
        }

        TentativeQuiz soumise = soumettreAutomatiquement(tentative);

        ScoreFinaleResponse score = new ScoreFinaleResponse();
        score.setTentativeId(soumise.getId());
        score.setScoreObtenu(soumise.getScoreObtenu());
        score.setScoreMax(soumise.getScoreMax());
        score.setPourcentage(soumise.getPourcentage());
        score.setDateSoumission(soumise.getDateSoumission());
        score.setSoumisParExpiration(true);
        score.setMessage("⏰ Temps écoulé ! Le quiz a été soumis automatiquement. Score : "
                + String.format("%.1f", soumise.getPourcentage()) + "%");
        return score;
    }

    // ══════════════════════════════════════════════════════════════
    // 6. RÉSULTAT D'UN QUIZ PASSÉ (lecture seule)
    // ══════════════════════════════════════════════════════════════

    public ScoreFinaleResponse getResultat(Long quizId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Quiz quiz = findQuiz(quizId);

        TentativeQuiz tentative = tentativeRepository.findByEtudiantAndQuiz(etudiant, quiz)
                .orElseThrow(() -> new RuntimeException("Vous n'avez pas encore passé ce quiz."));

        if (tentative.getStatut() != StatutTentative.SOUMISE) {
            throw new RuntimeException("Ce quiz est encore en cours.");
        }

        ScoreFinaleResponse score = new ScoreFinaleResponse();
        score.setTentativeId(tentative.getId());
        score.setScoreObtenu(tentative.getScoreObtenu());
        score.setScoreMax(tentative.getScoreMax());
        score.setPourcentage(tentative.getPourcentage());
        score.setDateSoumission(tentative.getDateSoumission());
        score.setMessage(genererMessage(tentative.getPourcentage(), false));
        return score;
    }

 // ══════════════════════════════════════════════════════════════
    // DASHBOARD ÉTUDIANT — VERSION COMPLÈTE
    // progressionGlobale = moyenne(% cours terminés, % quiz réussis, % examens corrigés)
    // ══════════════════════════════════════════════════════════════
 
    public DashboardEtudiantResponse getDashboard(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
 
        // ── Progression modules ──────────────────────────────────
        List<ProgressionModule> progressions = progressionRepository.findByEtudiant(etudiant);
        int modulesInscrits = progressions.size();
        int modulesTermines = (int) progressions.stream()
                .filter(p -> p.getStatut() == StatutProgression.TERMINE).count();
 
        List<ProgressionModuleResponse> progressionResponses = progressions.stream()
                .map(p -> toProgressionResponse(p, etudiant))
                .collect(Collectors.toList());
 
        // ── Derniers quiz ────────────────────────────────────────
        List<TentativeResponse> derniersQuizzes = tentativeRepository
                .findTop5ByEtudiantOrderByDateDebutDesc(etudiant)
                .stream().map(this::toTentativeResponse)
                .collect(Collectors.toList());
 
        // ── Derniers examens ─────────────────────────────────────
        List<PassageExamenResponse> derniersExamens = passageExamenRepository
                .findTop5ByEtudiantOrderByDateDebutDesc(etudiant)
                .stream().map(p -> examenEnseignantService.toPassageResponse(p))
                .collect(Collectors.toList());
 
        // ── Cours récents ────────────────────────────────────────
        List<CoursProgressionResponse> coursRecents = coursProgressionRepository
                .findByEtudiant(etudiant).stream()
                .sorted(java.util.Comparator.comparing(
                        cp -> cp.getDateDerniereConsultation() != null
                                ? cp.getDateDerniereConsultation()
                                : cp.getDatePremierAcces(),
                        java.util.Comparator.reverseOrder()))
                .limit(5)
                .map(coursEtudiantService::toResponse)
                .collect(Collectors.toList());
 
        // ── Progression globale combinée ─────────────────────────
        // Composante 1 : % cours terminés / total cours accessibles
        long totalCours = coursProgressionRepository.findByEtudiant(etudiant).size();
        long coursTermines = coursProgressionRepository.findByEtudiant(etudiant).stream()
                .filter(cp -> cp.getStatut() == com.school.elearning.model.enums.StatutCoursProgression.TERMINE)
                .count();
        float pctCours = totalCours > 0 ? (float) coursTermines / totalCours * 100f : 0f;
 
        // Composante 2 : % quiz soumis (sur le total disponible dans le niveau)
        long totalQuiz = tentativeRepository.findByEtudiantOrderByDateDebutDesc(etudiant).size();
        long quizSoumis = tentativeRepository.findByEtudiantOrderByDateDebutDesc(etudiant).stream()
                .filter(t -> t.getStatut() == StatutTentative.SOUMISE).count();
        float pctQuiz = totalQuiz > 0 ? (float) quizSoumis / totalQuiz * 100f : 0f;
 
        // Composante 3 : note moyenne des examens corrigés (ramenée sur 100)
        float pctExamens = 0f;
        List<PassageExamen> examensCoriges = passageExamenRepository
                .findTop5ByEtudiantOrderByDateDebutDesc(etudiant).stream()
                .filter(p -> p.getStatut() == com.school.elearning.model.enums.StatutPassageExamen.CORRIGE)
                .collect(Collectors.toList());
        if (!examensCoriges.isEmpty()) {
            pctExamens = (float) examensCoriges.stream()
                    .mapToDouble(p -> p.getNoteFinale() / 20.0 * 100.0)
                    .average().orElse(0);
        }
 
        // Moyenne des 3 composantes (si aucune donnée → 0)
        int nbComposantes = (totalCours > 0 ? 1 : 0) + (totalQuiz > 0 ? 1 : 0) + (!examensCoriges.isEmpty() ? 1 : 0);
        float progressionGlobale = nbComposantes > 0
                ? (pctCours + pctQuiz + pctExamens) / nbComposantes
                : 0f;
 
        String niveauNom = (etudiant.getCommunaute() != null
                && etudiant.getCommunaute().getNiveau() != null)
                ? etudiant.getCommunaute().getNiveau().getNom() : null;
 
        return new DashboardEtudiantResponse(
                etudiant.getNom(), etudiant.getPrenom(), niveauNom,
                modulesInscrits, modulesTermines,
                Math.round(progressionGlobale * 100f) / 100f,
                progressionResponses, derniersQuizzes, derniersExamens, coursRecents
        );
    }

    // ══════════════════════════════════════════════════════════════
    // SOUMISSION AUTOMATIQUE (interne)
    // ══════════════════════════════════════════════════════════════

    @Transactional
    private TentativeQuiz soumettreAutomatiquement(TentativeQuiz tentative) {
        double scoreObtenu = reponseRepository.sumPointsByTentative(tentative);
        double scoreMax = tentative.getScoreMax();
        double pourcentage = scoreMax > 0
                ? Math.round((scoreObtenu / scoreMax) * 10000.0) / 100.0 : 0;

        tentative.setScoreObtenu(scoreObtenu);
        tentative.setPourcentage(pourcentage);
        tentative.setStatut(StatutTentative.SOUMISE);
        tentative.setDateSoumission(tentative.getDateExpiration()); // soumis à l'expiration
        TentativeQuiz sauvee = tentativeRepository.save(tentative);

        mettreAJourResultatEtProgression(sauvee);
        return sauvee;
    }

    // ══════════════════════════════════════════════════════════════
    // MISE À JOUR RÉSULTAT + PROGRESSION
    // ══════════════════════════════════════════════════════════════

    @Transactional
    private void mettreAJourResultatEtProgression(TentativeQuiz tentative) {
        Etudiant etudiant = tentative.getEtudiant();
        Module module = tentative.getQuiz().getCours().getModule();

        double scoreMax = tentative.getScoreMax();
        float noteSur20 = scoreMax > 0
                ? (float) Math.round((tentative.getScoreObtenu() / scoreMax) * 20 * 100) / 100f
                : 0f;

        // Récupérer ou créer le Resultat
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

        // Créer la Note
        Note note = new Note();
        note.setValeur(noteSur20);
        note.setDateObtention(new java.util.Date());
        note.setType(TypeNote.QUIZ);
        note.setResultat(resultat);
        noteRepository.save(note);

        // Recalculer la moyenne des quiz du module
        // Chaque quiz est passé une seule fois → moyenne directe
        List<TentativeQuiz> tentativesModule = tentativeRepository
                .findByEtudiantAndModuleId(etudiant, module.getId());

        float nouvelleMoyenne = tentativesModule.isEmpty() ? 0f :
                (float) tentativesModule.stream()
                        .mapToDouble(t -> t.getScoreMax() > 0
                                ? (t.getScoreObtenu() / t.getScoreMax()) * 20 : 0)
                        .average().orElse(0);

        resultat.setMoyenneQuizs(nouvelleMoyenne);
        resultat.setMoyenneGenerale((nouvelleMoyenne + resultat.getMoyenneExamens()) / 2);
        resultatRepository.save(resultat);

        // Mettre à jour la ProgressionModule
        progressionRepository.findByEtudiant(etudiant).stream()
                .filter(p -> p.getModule().getId().equals(module.getId()))
                .findFirst()
                .ifPresent(progression -> {
                    // Calcul : combien de quiz du module ont été soumis ?
                    long totalQuizzes = module.getCours().stream()
                            .flatMap(c -> c.getQuizzes().stream()).count();
                    long quizzesSoumis = tentativesModule.size();

                    float completude = totalQuizzes > 0
                            ? (float) quizzesSoumis / totalQuizzes * 100f : 0f;
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
    // VÉRIFICATION ACCÈS
    // ══════════════════════════════════════════════════════════════

    private void verifierAccesCours(Etudiant etudiant, Long coursId) {
        if (etudiant.getCommunaute() == null || etudiant.getCommunaute().getNiveau() == null) {
            throw new RuntimeException("Vous n'êtes affecté à aucun niveau. Contactez votre modérateur.");
        }
        Long niveauId = etudiant.getCommunaute().getNiveau().getId();
        boolean accesAutorise = coursRepository.existsByIdAndModule_Niveau_Id(coursId, niveauId);
        if (!accesAutorise) {
            throw new RuntimeException("Accès refusé : ce cours n'appartient pas à votre niveau.");
        }
    }

    private void verifierTentativeActive(TentativeQuiz tentative) {
        if (tentative.getStatut() == StatutTentative.SOUMISE) {
            throw new RuntimeException("Ce quiz a déjà été soumis.");
        }
        if (tentative.estExpiree()) {
            // On soumet automatiquement et on informe
            soumettreAutomatiquement(tentative);
            throw new RuntimeException("Le temps est écoulé. Le quiz a été soumis automatiquement.");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // HELPERS
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

    private TentativeQuiz findTentative(Long tentativeId, Etudiant etudiant) {
        TentativeQuiz tentative = tentativeRepository.findById(tentativeId)
                .orElseThrow(() -> new RuntimeException("Tentative introuvable"));
        if (!tentative.getEtudiant().getId().equals(etudiant.getId())) {
            throw new RuntimeException("Accès refusé : ce n'est pas votre tentative");
        }
        return tentative;
    }

    private String genererMessage(double pourcentage, boolean parExpiration) {
        String appreciation;
        if (pourcentage >= 90)      appreciation = "🏆 Excellent !";
        else if (pourcentage >= 70) appreciation = "👍 Bien !";
        else if (pourcentage >= 50) appreciation = "📚 Passable, continuez vos efforts.";
        else                        appreciation = "❌ Insuffisant, révisez ce cours.";

        String base = appreciation + " Score : " + String.format("%.1f", pourcentage) + "%";
        return parExpiration ? "⏰ Temps écoulé. " + base : base;
    }

    // ══════════════════════════════════════════════════════════════
    // MAPPERS
    // ══════════════════════════════════════════════════════════════

    private QuizEtudiantResponse toQuizEtudiantResponse(Quiz quiz, Etudiant etudiant) {
        Optional<TentativeQuiz> tentativeOpt = tentativeRepository
                .findByEtudiantAndQuiz(etudiant, quiz);

        boolean dejaPasse = tentativeOpt
                .map(t -> t.getStatut() == StatutTentative.SOUMISE)
                .orElse(false);

        boolean enCours = tentativeOpt
                .map(t -> t.getStatut() == StatutTentative.EN_COURS && !t.estExpiree())
                .orElse(false);

        int nbAfficher = Math.min(quiz.getNombreQuestions(), quiz.getQuestions().size());
        double pointsTotal = quiz.getQuestions().stream()
                .mapToDouble(QuestionQuiz::getPoints).sum();

        // Questions (sans bonneReponse) — tirage déterministe si tentative en cours
        List<QuestionEtudiantResponse> questions = new ArrayList<>();
        if (!dejaPasse) {
            List<QuestionQuiz> pool = new ArrayList<>(quiz.getQuestions());
            if (enCours && tentativeOpt.isPresent()) {
                Collections.shuffle(pool, new Random(tentativeOpt.get().getId()));
            } else {
                Collections.shuffle(pool);
            }
            pool.subList(0, Math.min(nbAfficher, pool.size())).forEach(q -> {
                QuestionEtudiantResponse qr = new QuestionEtudiantResponse();
                qr.setId(q.getId());
                qr.setEnonce(q.getEnonce());
                qr.setChoixPossibles(q.getChoixPossibles());
                qr.setPoints(q.getPoints());
                questions.add(qr);
            });
        }

        QuizEtudiantResponse r = new QuizEtudiantResponse();
        r.setId(quiz.getId());
        r.setTitre(quiz.getTitre());
        r.setDureeMinutes(quiz.getDureeMinutes());
        r.setNombreQuestions(nbAfficher);
        r.setPointsTotal(pointsTotal);
        r.setCoursTitre(quiz.getCours() != null ? quiz.getCours().getTitre() : null);
        r.setDejaPasse(dejaPasse);
        r.setEnCours(enCours);
        r.setQuestions(questions);

        tentativeOpt.ifPresent(t -> {
            r.setTentativeId(t.getId());
            r.setDateExpiration(t.getDateExpiration());
        });

        return r;
    }

    private TentativeResponse toTentativeResponse(TentativeQuiz t) {
        long secondesRestantes = -1;
        if (t.getStatut() == StatutTentative.EN_COURS && t.getDateExpiration() != null) {
            secondesRestantes = Math.max(0,
                    ChronoUnit.SECONDS.between(LocalDateTime.now(), t.getDateExpiration()));
        }

        TentativeResponse r = new TentativeResponse();
        r.setId(t.getId());
        r.setScoreObtenu(t.getScoreObtenu());
        r.setScoreMax(t.getScoreMax());
        r.setPourcentage(t.getPourcentage());
        r.setStatut(t.getStatut());
        r.setDateDebut(t.getDateDebut());
        r.setDateExpiration(t.getDateExpiration());
        r.setDateSoumission(t.getDateSoumission());
        r.setQuizTitre(t.getQuiz() != null ? t.getQuiz().getTitre() : null);
        r.setDureeMinutes(t.getQuiz() != null ? t.getQuiz().getDureeMinutes() : 0);
        r.setSecondesRestantes(secondesRestantes);
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