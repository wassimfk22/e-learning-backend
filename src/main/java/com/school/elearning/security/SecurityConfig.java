package com.school.elearning.security;

import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Auth publique ──────────────────────────────────────
                .requestMatchers("/api/auth/**").permitAll()

                // ── Fichiers statiques (photos uploadées) ──────────────
                .requestMatchers("/uploads/**").permitAll()

                // ── Admin : CRUD complet tous utilisateurs ─────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── Modération : CRUD étudiants ────────────────────────
                .requestMatchers("/api/moderation/**").hasAnyRole("ADMIN", "MODERATEUR")

                // ══════════════════════════════════════════════════════
                // MODULES
                // ══════════════════════════════════════════════════════

                // Lecture : tous les authentifiés
                .requestMatchers(HttpMethod.GET, "/api/modules").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/modules/{id}").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/modules/niveau/**").authenticated()

                // ⚠️ IMPORTANT : /mes-modules AVANT la règle générale POST/PUT/PATCH/DELETE
                // Sinon Spring Security essaie de matcher comme une route MODERATEUR/ADMIN
                .requestMatchers(HttpMethod.GET, "/api/modules/mes-modules").hasRole("ENSEIGNANT")

                // Écriture : MODERATEUR + ADMIN uniquement
                .requestMatchers(HttpMethod.POST,   "/api/modules/**").hasAnyRole("ADMIN", "MODERATEUR")
                .requestMatchers(HttpMethod.PUT,    "/api/modules/**").hasAnyRole("ADMIN", "MODERATEUR")
                .requestMatchers(HttpMethod.PATCH,  "/api/modules/**").hasAnyRole("ADMIN", "MODERATEUR")
                .requestMatchers(HttpMethod.DELETE, "/api/modules/**").hasAnyRole("ADMIN", "MODERATEUR")

                // ══════════════════════════════════════════════════════
                // NIVEAUX
                // ══════════════════════════════════════════════════════
                .requestMatchers(HttpMethod.GET,    "/api/niveaux/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/niveaux/**").hasAnyRole("ADMIN", "MODERATEUR")
                .requestMatchers(HttpMethod.PUT,    "/api/niveaux/**").hasAnyRole("ADMIN", "MODERATEUR")
                .requestMatchers(HttpMethod.DELETE, "/api/niveaux/**").hasAnyRole("ADMIN", "MODERATEUR")

                // ══════════════════════════════════════════════════════
                // COURS
                // ══════════════════════════════════════════════════════
                .requestMatchers(HttpMethod.GET, "/api/cours/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/cours/**").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.PUT, "/api/cours/**").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.DELETE, "/api/cours/**").hasRole("ENSEIGNANT")
                
                // ══════════════════════════════════════════════════════
                // QUIZS
                // ══════════════════════════════════════════════════════
                
                .requestMatchers(HttpMethod.POST,   "/api/quiz/**").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.GET,    "/api/quiz/**").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.PUT,    "/api/quiz/**").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.PATCH,  "/api/quiz/**").hasRole("ENSEIGNANT")
                .requestMatchers(HttpMethod.DELETE, "/api/quiz/**").hasRole("ENSEIGNANT")
                 
                // Examens enseignant
                .requestMatchers("/api/examens/**").hasRole("ENSEIGNANT")
                 
                // Étudiant : quiz, cours, examens, progression
                .requestMatchers("/api/etudiant/**").hasRole("ETUDIANT")
//                .requestMatchers("/api/progression/**").hasRole("ETUDIANT")
                
                // Pour les routes de la communauté
                .requestMatchers("/api/communaute/**").hasRole("ETUDIANT")
                .requestMatchers("/uploads/communaute/**").permitAll()
                
                // ══════════════════════════════════════════════════════
                // ANNONCES
                // ══════════════════════════════════════════════════════
                // Annonces
	             .requestMatchers(HttpMethod.GET,    "/api/annonces/**").authenticated()
	             .requestMatchers(HttpMethod.POST,   "/api/annonces/**").hasAnyRole("ADMIN", "MODERATEUR")
	             .requestMatchers(HttpMethod.PUT,    "/api/annonces/**").hasAnyRole("ADMIN", "MODERATEUR")
	             .requestMatchers(HttpMethod.DELETE, "/api/annonces/**").hasAnyRole("ADMIN", "MODERATEUR")
	             
	            //══════════════════════════════════════════════════════
	            // PROGRESSION
	            // ══════════════════════════════════════════════════════
	             .requestMatchers(HttpMethod.GET,  "/api/progression/ma-progression").hasRole("ETUDIANT")
	             .requestMatchers(HttpMethod.POST, "/api/progression/inscrire/**").hasRole("ETUDIANT")
	             .requestMatchers(HttpMethod.GET,  "/api/progression/etudiant/**")
	                 .hasAnyRole("ADMIN", "MODERATEUR", "ENSEIGNANT")
	             .requestMatchers(HttpMethod.GET,  "/api/progression/module/**")
	                 .hasAnyRole("ADMIN", "MODERATEUR", "ENSEIGNANT")
	             .requestMatchers(HttpMethod.GET,  "/api/progression/niveau/**")
	                 .hasAnyRole("ADMIN", "MODERATEUR")
	                 
	              // ══════════════════════════════════════════════════════
	              // CALENDRIER
	              // ══════════════════════════════════════════════════════
	              .requestMatchers(HttpMethod.GET,    "/api/calendrier/**").authenticated()
	              .requestMatchers(HttpMethod.POST,   "/api/calendrier/evenements/**").hasAnyRole("ADMIN", "MODERATEUR")
	              .requestMatchers(HttpMethod.PUT,    "/api/calendrier/evenements/**").hasAnyRole("ADMIN", "MODERATEUR")
	              .requestMatchers(HttpMethod.DELETE, "/api/calendrier/evenements/**").hasAnyRole("ADMIN", "MODERATEUR")
	                 
	             // Questions pédagogiques
	              .requestMatchers("/api/questions-pedagogiques/**").authenticated()
	              
	             // Notifications
	              .requestMatchers("/api/notifications/**").authenticated()
	              
	             // Boite de réception
	              .requestMatchers("/api/boite-reception/**").authenticated()

                // ── Tout le reste : authentifié ────────────────────────
                 .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
            "http://localhost:8081",
            "http://localhost:3000"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}