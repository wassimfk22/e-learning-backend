# 🎓 Plateforme E-Learning - Spring Boot

## Prérequis
- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Configuration
1. Créer la base de données PostgreSQL :
```sql
CREATE DATABASE elearning_db;
```

2. Modifier `src/main/resources/application.properties` si nécessaire :
   - `spring.datasource.username`
   - `spring.datasource.password`

## Lancement
```bash
mvn spring-boot:run
```

Les tables seront créées automatiquement (`ddl-auto=update`).

## Admin par défaut
- **Email** : admin@school.com
- **Mot de passe** : Admin@123

## API Endpoints principaux

### 🔐 Authentification
| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/auth/login` | Connexion (retourne JWT) |
| POST | `/api/auth/register` | Inscription (ETUDIANT, ENSEIGNANT, MODERATEUR) |

### 📚 Niveaux / Modules / Cours
| Méthode | URL | Description |
|---------|-----|-------------|
| GET/POST | `/api/niveaux` | CRUD Niveaux |
| GET/POST | `/api/modules` | CRUD Modules |
| GET/POST | `/api/cours` | CRUD Cours |
| GET | `/api/modules/niveau/{id}` | Modules par niveau |
| GET | `/api/cours/module/{id}` | Cours par module |

### 📝 Évaluations
| Méthode | URL | Description |
|---------|-----|-------------|
| POST | `/api/evaluations/quiz` | Créer un quiz |
| POST | `/api/evaluations/examen` | Créer un examen |
| POST | `/api/evaluations/questions` | Ajouter une question |
| POST | `/api/evaluations/tentative/{id}/submit` | Soumettre une tentative |

### 📊 Résultats & Progression
| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/resultats/etudiant/{id}` | Résultats d'un étudiant |
| GET | `/api/progression/modules/{id}` | Progression modules |

### 📢 Annonces & Notifications
| Méthode | URL | Description |
|---------|-----|-------------|
| GET/POST | `/api/annonces` | CRUD Annonces |
| GET | `/api/notifications/{userId}` | Notifications |
| PUT | `/api/notifications/{id}/read` | Marquer comme lue |

### 🎥 Streams & Planning
| Méthode | URL | Description |
|---------|-----|-------------|
| GET/POST | `/api/streams` | CRUD Streams |
| GET | `/api/communautes` | Communautés |
| GET | `/api/communautes/{id}/messages` | Chat communauté |

### 🔧 Administration (ADMIN only)
| Méthode | URL | Description |
|---------|-----|-------------|
| GET | `/api/admin/utilisateurs` | Tous les utilisateurs |
| POST | `/api/admin/moderateurs` | Créer un modérateur |
| DELETE | `/api/admin/utilisateurs/{id}` | Supprimer un utilisateur |

## Utilisation avec Postman
1. POST `/api/auth/login` avec `{"email":"admin@school.com","motDePasse":"Admin@123"}`
2. Copier le token JWT retourné
3. Ajouter le header `Authorization: Bearer <token>` à toutes les requêtes
