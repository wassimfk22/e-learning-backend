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
    private final ExamenModuleRepository examenModuleRepository;

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
    // ══════════════════════════════════════════════════════════════

    @Transactional
    public TentativeResponse demarrerQuiz(Long quizId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        Quiz quiz = findQuiz(quizId);

        verifierAccesCours(etudiant, quiz.getCours().getId());

        Optional<TentativeQuiz> existante = tentativeRepository.findByEtudiantAndQuiz(etudiant, quiz);
        if (existante.isPresent()) {
            TentativeQuiz t = existante.get();
            if (t.getStatut() == StatutTentative.SOUMISE) {
                throw new RuntimeException("Vous avez déjà passé ce quiz. Le quiz ne peut être passé qu'une seule fois.");
            }
            if (!t.estExpiree()) {
                return toTentativeResponse(t);
            }
            return toTentativeResponse(soumettreAutomatiquement(t));
        }

        List<QuestionQuiz> toutesQuestions = quiz.getQuestions();
        int nbAfficher = Math.min(quiz.getNombreQuestions(), toutesQuestions.size());

        List<QuestionQuiz> questionsSelectionnees = new ArrayList<>(toutesQuestions);
        Collections.shuffle(questionsSelectionnees);
        questionsSelectionnees = questionsSelectionnees.subList(0, nbAfficher);

        double scoreMax = questionsSelectionnees.stream()
                .mapToDouble(QuestionQuiz::getPoints).sum();

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
    // ══════════════════════════════════════════════════════════════

    public List<QuestionEtudiantResponse> getQuestionsDeTentative(Long tentativeId, Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);
        TentativeQuiz tentative = findTentative(tentativeId, etudiant);

        verifierTentativeActive(tentative);

        Quiz quiz = tentative.getQuiz();
        List<QuestionQuiz> toutesQuestions = quiz.getQuestions();
        int nbAfficher = Math.min(quiz.getNombreQuestions(), toutesQuestions.size());

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
    // 4. SOUMETTRE TOUTES LES RÉPONSES EN UNE FOIS
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

        boolean soumisParExpiration = tentative.estExpiree();

        for (ReponseQuizRequest req : reponses) {
            QuestionQuiz question = questionQuizRepository.findById(req.getQuestionId())
                    .orElseThrow(() -> new RuntimeException("Question introuvable : " + req.getQuestionId()));

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

        double scoreObtenu = reponseRepository.sumPointsByTentative(tentative);
        double scoreMax = tentative.getScoreMax();
        double pourcentage = scoreMax > 0
                ? Math.round((scoreObtenu / scoreMax) * 10000.0) / 100.0
                : 0;

        tentative.setScoreObtenu(scoreObtenu);
        tentative.setPourcentage(pourcentage);
        tentative.setStatut(StatutTentative.SOUMISE);
        tentative.setDateSoumission(LocalDateTime.now());
        tentativeRepository.save(tentative);

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
    // 5. SOUMISSION AUTOMATIQUE PAR EXPIRATION
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
    // 6. RÉSULTAT D'UN QUIZ PASSÉ
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
    // DASHBOARD ÉTUDIANT — VERSION CORRIGÉE
    //
    // - modulesInscrits / modulesTermines : basés sur ProgressionModule
    //   (auto-créées à l'accès aux cours + ProgressionService manuel)
    // - progressionGlobale : moyenne pondérée des 3 composantes
    //   (cours terminés, quiz soumis, examens corrigés)
    //   calculée par module puis moyennée — ou globalement si pas de modules
    // ══════════════════════════════════════════════════════════════

    public DashboardEtudiantResponse getDashboard(Authentication auth) {
        Etudiant etudiant = getEtudiantConnecte(auth);

        // ── 1. Retrouver tous les modules touchés par l'étudiant ──────
        // Combine : ProgressionModule existantes + modules déduits des CoursProgression
        Set<Long> moduleIdsConnus = new HashSet<>();

        // Modules via inscription explicite ou auto
        List<ProgressionModule> progressionModules = progressionRepository.findByEtudiant(etudiant);
        progressionModules.forEach(p -> moduleIdsConnus.add(p.getModule().getId()));

        // Modules déduits des cours consultés (pour l'étudiant pas encore inscrit via ProgressionService)
        List<CoursProgression> toutesCoursProgressions = coursProgressionRepository.findByEtudiant(etudiant);
        toutesCoursProgressions.forEach(cp -> moduleIdsConnus.add(cp.getCours().getModule().getId()));

        // ── 2. S'assurer que chaque module touché a une ProgressionModule ──
        // (au cas où des modules ont été accédés avant ce fix)
        for (CoursProgression cp : toutesCoursProgressions) {
            Module module = cp.getCours().getModule();
            if (!progressionRepository.existsByEtudiantAndModule(etudiant, module)) {
                coursEtudiantService.autoInscrireAuModule(etudiant, module);
                // Recalculer pour ce module
                coursEtudiantService.mettreAJourProgressionModule(etudiant, module);
            }
        }

        // Recharger après éventuelles créations
        progressionModules = progressionRepository.findByEtudiant(etudiant);

        // ── 3. Stats modules ──────────────────────────────────────────
        int modulesInscrits = progressionModules.size();
        int modulesTermines = (int) progressionModules.stream()
                .filter(p -> p.getStatut() == StatutProgression.TERMINE).count();

        // ── 4. Progression par module (réponse détaillée) ─────────────
        List<ProgressionModuleResponse> progressionResponses = progressionModules.stream()
                .map(p -> toProgressionResponse(p, etudiant))
                .collect(Collectors.toList());

        // ── 5. Calcul de la progressionGlobale ────────────────────────
        // On calcule par module pour être précis, puis on moyenne
        float progressionGlobale = calculerProgressionGlobale(etudiant, progressionModules);

        // ── 6. Derniers quiz ──────────────────────────────────────────
        List<TentativeResponse> derniersQuizzes = tentativeRepository
                .findTop5ByEtudiantOrderByDateDebutDesc(etudiant)
                .stream().map(this::toTentativeResponse)
                .collect(Collectors.toList());

        // ── 7. Derniers examens ───────────────────────────────────────
        List<PassageExamenResponse> derniersExamens = passageExamenRepository
                .findTop5ByEtudiantOrderByDateDebutDesc(etudiant)
                .stream().map(p -> examenEnseignantService.toPassageResponse(p))
                .collect(Collectors.toList());

        // ── 8. Cours récents ──────────────────────────────────────────
        List<CoursProgressionResponse> coursRecents = toutesCoursProgressions.stream()
                .sorted(Comparator.comparing(
                        cp -> cp.getDateDerniereConsultation() != null
                                ? cp.getDateDerniereConsultation()
                                : cp.getDatePremierAcces(),
                        Comparator.reverseOrder()))
                .limit(5)
                .map(coursEtudiantService::toResponse)
                .collect(Collectors.toList());

        // ── 9. Niveau ─────────────────────────────────────────────────
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
    // CALCUL PROGRESSION GLOBALE
    // Pour chaque module inscrit :
    //   - % cours terminés
    //   - % quiz soumis (du module)
    //   - % examens corrigés (du module)
    // Moyenne de ces 3 composantes par module, puis moyenne des modules
    // ══════════════════════════════════════════════════════════════

    private float calculerProgressionGlobale(Etudiant etudiant, List<ProgressionModule> progressionModules) {
        if (progressionModules.isEmpty()) {
            // Fallback : on regarde juste les cours/quiz globaux
            return calculerProgressionFallback(etudiant);
        }

        float totalProgression = 0f;
        int nbModulesAvecDonnees = 0;

        for (ProgressionModule pm : progressionModules) {
            Module module = pm.getModule();
            float progressionModule = calculerProgressionDuModule(etudiant, module);
            totalProgression += progressionModule;
            nbModulesAvecDonnees++;
        }

        return nbModulesAvecDonnees > 0 ? totalProgression / nbModulesAvecDonnees : 0f;
    }

    private float calculerProgressionDuModule(Etudiant etudiant, Module module) {
        float totalComposantes = 0f;
        int nbComposantes = 0;

        // Composante 1 : Cours terminés
        long totalCours = module.getCours() != null ? module.getCours().size() : 0;
        if (totalCours > 0) {
            long coursTermines = coursProgressionRepository
                    .findTerminesParModuleId(etudiant, module.getId()).size();
            float pctCours = (float) coursTermines / totalCours * 100f;
            totalComposantes += pctCours;
            nbComposantes++;
        }

        // Composante 2 : Quiz soumis du module
        List<TentativeQuiz> tentativesModule = tentativeRepository
                .findByEtudiantAndModuleId(etudiant, module.getId());
        long totalQuizModule = module.getCours() != null
                ? module.getCours().stream()
                        .flatMap(c -> c.getQuizzes() != null ? c.getQuizzes().stream() : java.util.stream.Stream.empty())
                        .count()
                : 0;
        if (totalQuizModule > 0) {
            long quizSoumisModule = tentativesModule.stream()
                    .filter(t -> t.getStatut() == StatutTentative.SOUMISE).count();
            float pctQuiz = (float) quizSoumisModule / totalQuizModule * 100f;
            totalComposantes += pctQuiz;
            nbComposantes++;
        }

        // Composante 3 : Examens corrigés du module
        List<PassageExamen> examensModule = passageExamenRepository
                .findCorigesParModuleId(etudiant, module.getId());
        // Total examens du module — on passe par le repo pour éviter le problème
        // d'instanciation de la classe abstraite Evaluation
        long totalExamensModule = examenModuleRepository.findByModuleId(module.getId()).size();
        if (totalExamensModule > 0) {
            long examensCorrigesModule = examensModule.size();
            float pctExamens = (float) examensCorrigesModule / totalExamensModule * 100f;
            // Bonus : si corrigé, utiliser la note moyenne ramenée sur 100
            if (!examensModule.isEmpty()) {
                float noteMoyenne = (float) examensModule.stream()
                        .mapToDouble(p -> p.getNoteFinale() / 20.0 * 100.0)
                        .average().orElse(0);
                pctExamens = noteMoyenne;
            }
            totalComposantes += pctExamens;
            nbComposantes++;
        }

        return nbComposantes > 0 ? totalComposantes / nbComposantes : 0f;
    }

    private float calculerProgressionFallback(Etudiant etudiant) {
        // Utilisé quand l'étudiant n'a aucune ProgressionModule (ancien comportement)
        List<CoursProgression> toutesCP = coursProgressionRepository.findByEtudiant(etudiant);
        long totalCours = toutesCP.size();
        long coursTermines = toutesCP.stream()
                .filter(cp -> cp.getStatut() == StatutCoursProgression.TERMINE).count();
        float pctCours = totalCours > 0 ? (float) coursTermines / totalCours * 100f : 0f;

        List<TentativeQuiz> toutesT = tentativeRepository.findByEtudiantOrderByDateDebutDesc(etudiant);
        long totalQuiz = toutesT.size();
        long quizSoumis = toutesT.stream()
                .filter(t -> t.getStatut() == StatutTentative.SOUMISE).count();
        float pctQuiz = totalQuiz > 0 ? (float) quizSoumis / totalQuiz * 100f : 0f;

        List<PassageExamen> examensCoriges = passageExamenRepository
                .findTop5ByEtudiantOrderByDateDebutDesc(etudiant).stream()
                .filter(p -> p.getStatut() == StatutPassageExamen.CORRIGE)
                .collect(Collectors.toList());
        float pctExamens = examensCoriges.isEmpty() ? 0f : (float) examensCoriges.stream()
                .mapToDouble(p -> p.getNoteFinale() / 20.0 * 100.0)
                .average().orElse(0);

        int nbC = (totalCours > 0 ? 1 : 0) + (totalQuiz > 0 ? 1 : 0) + (!examensCoriges.isEmpty() ? 1 : 0);
        return nbC > 0 ? (pctCours + pctQuiz + pctExamens) / nbC : 0f;
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
        tentative.setDateSoumission(tentative.getDateExpiration());
        TentativeQuiz sauvee = tentativeRepository.save(tentative);

        mettreAJourResultatEtProgression(sauvee);
        return sauvee;
    }

    // ══════════════════════════════════════════════════════════════
    // MISE À JOUR RÉSULTAT + PROGRESSION après quiz soumis
    // ══════════════════════════════════════════════════════════════

    @Transactional
    private void mettreAJourResultatEtProgression(TentativeQuiz tentative) {
        Etudiant etudiant = tentative.getEtudiant();
        Module module = tentative.getQuiz().getCours().getModule();

        // S'assurer que l'inscription au module existe
        coursEtudiantService.autoInscrireAuModule(etudiant, module);

        double scoreMax = tentative.getScoreMax();
        float noteSur20 = scoreMax > 0
                ? (float) Math.round((tentative.getScoreObtenu() / scoreMax) * 20 * 100) / 100f
                : 0f;

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

        Note note = new Note();
        note.setValeur(noteSur20);
        note.setDateObtention(new java.util.Date());
        note.setType(TypeNote.QUIZ);
        note.setResultat(resultat);
        noteRepository.save(note);

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

        // Mettre à jour la ProgressionModule (composante quiz)
        progressionRepository.findByEtudiant(etudiant).stream()
                .filter(p -> p.getModule().getId().equals(module.getId()))
                .findFirst()
                .ifPresent(progression -> {
                    long totalQuizzes = module.getCours().stream()
                            .flatMap(c -> c.getQuizzes() != null ? c.getQuizzes().stream() : java.util.stream.Stream.empty())
                            .count();
                    long quizzesSoumis = tentativesModule.stream()
                            .filter(t -> t.getStatut() == StatutTentative.SOUMISE).count();

                    if (totalQuizzes > 0) {
                        float completudeQuiz = (float) quizzesSoumis / totalQuizzes * 100f;
                        // Combine avec la progression cours déjà calculée
                        float actuel = progression.getPourcentageCompletude();
                        progression.setPourcentageCompletude(Math.max(actuel, completudeQuiz));
                    }

                    if (progression.getPourcentageCompletude() >= 100f) {
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

        Module module = p.getModule();

        ProgressionModuleResponse r = new ProgressionModuleResponse();
        r.setId(p.getId());
        r.setModuleId(module.getId());
        r.setModuleTitre(module.getTitre());
        r.setNiveauNom(module.getNiveau() != null ? module.getNiveau().getNom() : null);
        r.setStatut(p.getStatut());
        r.setPourcentageCompletude(p.getPourcentageCompletude());
        r.setDateInscription(p.getDateInscription());

        resultat.ifPresent(res -> {
            r.setMoyenneQuizs(res.getMoyenneQuizs());
            r.setMoyenneExamens(res.getMoyenneExamens());
            r.setMoyenneGenerale(res.getMoyenneGenerale());
        });

        // ── Stats cours ───────────────────────────────────────────
        int totalCours = module.getCours() != null ? module.getCours().size() : 0;
        List<CoursProgression> terminesCP = coursProgressionRepository
                .findTerminesParModuleId(etudiant, module.getId());
        long consultesCount = coursProgressionRepository
                .countByEtudiantAndModuleId(etudiant, module.getId());
        int coursTerminesCount = terminesCP.size();
        int coursEnCoursCount = (int) (consultesCount - coursTerminesCount);

        r.setTotalCours(totalCours);
        r.setCoursTermines(coursTerminesCount);
        r.setCoursEnCours(Math.max(0, coursEnCoursCount));
        r.setPourcentageCours(totalCours > 0 ? (float) coursTerminesCount / totalCours * 100f : 0f);

        // ── Stats quiz ────────────────────────────────────────────
        long totalQuizModule = module.getCours() != null
                ? module.getCours().stream()
                        .flatMap(c -> c.getQuizzes() != null ? c.getQuizzes().stream() : java.util.stream.Stream.empty())
                        .count()
                : 0;
        List<TentativeQuiz> tentativesModule = tentativeRepository
                .findByEtudiantAndModuleId(etudiant, module.getId());
        long quizSoumisCount = tentativesModule.stream()
                .filter(t -> t.getStatut() == StatutTentative.SOUMISE).count();

        r.setTotalQuiz((int) totalQuizModule);
        r.setQuizSoumis((int) quizSoumisCount);
        r.setPourcentageQuiz(totalQuizModule > 0 ? (float) quizSoumisCount / totalQuizModule * 100f : 0f);

        // ── Stats examens ─────────────────────────────────────────
        long totalExamensModule = examenModuleRepository.findByModuleId(module.getId()).size();
        List<PassageExamen> examensCorrigesModule = passageExamenRepository
                .findCorigesParModuleId(etudiant, module.getId());
        float pctExamens = 0f;
        if (!examensCorrigesModule.isEmpty()) {
            pctExamens = (float) examensCorrigesModule.stream()
                    .mapToDouble(pe -> pe.getNoteFinale() / 20.0 * 100.0)
                    .average().orElse(0);
        }

        r.setTotalExamens((int) totalExamensModule);
        r.setExamensCorrigesCount(examensCorrigesModule.size());
        r.setPourcentageExamens(pctExamens);

        return r;
    }
}