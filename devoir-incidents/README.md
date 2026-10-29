# Gestion des incidents IT – Spring Security, JWT et React

Application web permettant à des employés de déclarer des incidents informatiques / cybersécurité,
à des techniciens de les traiter et à un administrateur de superviser l'ensemble.
Devoir « Sécurité des systèmes distribués ».

```
devoir-incidents/
├── incident-backend/    Spring Boot 3.3 (Java 17), Spring Security, JWT (jjwt 0.13.0), JPA, H2/MySQL
├── incident-frontend/   React 18 + Vite, React Router, Axios
├── tests/               Collection Postman + script de tests de sécurité automatisés
├── docs/                Diagrammes (entités, architecture)
└── README.md
```

## 1. Prérequis
- JDK 17 ou plus récent, Maven 3.9+
- Node.js 18+ et npm

## 2. Lancer le backend (port 8080)
```bash
cd incident-backend
mvn spring-boot:run
```
- API : http://localhost:8080
- Console H2 : http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:incidentdb`, user `sa`, mot de passe vide)
- La base est **en mémoire** : elle est recréée (avec les comptes de test) à chaque démarrage.
- Pour MySQL : décommenter les lignes MySQL dans `src/main/resources/application.properties`.
- Clé JWT : variable d'environnement `JWT_SECRET` (au moins 32 caractères) ; une valeur de développement existe par défaut.

## 3. Lancer le frontend (port 5173)
```bash
cd incident-frontend
npm install
npm run dev
```
Ouvrir http://localhost:5173. Le backend autorise uniquement cette origine (CORS).

| Service  | Port | Configuration |
|----------|------|---------------|
| Backend  | 8080 | `server.port` |
| Frontend | 5173 | `vite.config.js` |

## 4. Comptes de test (créés au démarrage)
| Rôle | Email | Mot de passe |
|------|-------|--------------|
| ADMIN | admin@demo.ma | Admin123! |
| TECHNICIAN | tech1@demo.ma | Tech1234! |
| TECHNICIAN | tech2@demo.ma | Tech1234! |
| EMPLOYEE | employee1@demo.ma | Employee1! |
| EMPLOYEE | employee2@demo.ma | Employee2! |

L'inscription publique (`/auth/register`) crée **toujours** un EMPLOYEE. Seul un ADMIN peut créer un
TECHNICIAN ou un ADMIN (page *Utilisateurs* ou `POST /api/admin/users`).

## 5. Rôles
- **EMPLOYEE** : profil, déclarer un incident, consulter uniquement ses incidents.
- **TECHNICIAN** : consulter les incidents qui lui sont affectés, changer leur statut, ajouter des notes.
- **ADMIN** : tous les incidents, tous les utilisateurs, activer/désactiver un compte, affecter un incident, statistiques.

## 6. Principaux endpoints
| Méthode | URL | Accès |
|---------|-----|-------|
| POST | `/auth/register` | public |
| POST | `/auth/login` | public |
| POST | `/auth/refresh` | public (refresh token) |
| POST | `/auth/logout` | public (révoque le refresh token) |
| GET / PUT | `/api/profile` | authentifié |
| PUT | `/api/profile/password` | authentifié |
| GET | `/api/dashboard` | authentifié (selon le rôle) |
| POST | `/api/incidents` | EMPLOYEE |
| GET | `/api/incidents/my` | EMPLOYEE |
| GET | `/api/incidents/assigned` | TECHNICIAN |
| GET | `/api/incidents` | ADMIN |
| GET | `/api/incidents/{id}` | propriétaire, technicien affecté ou ADMIN |
| PUT | `/api/incidents/{id}/status` | TECHNICIAN (affecté) ou ADMIN |
| PUT | `/api/incidents/{id}/assign/{technicianId}` | ADMIN |
| POST | `/api/incidents/{id}/comments` | TECHNICIAN (affecté) ou ADMIN |
| GET | `/api/admin/users`, `/api/admin/technicians`, `/api/admin/statistics` | ADMIN |
| POST | `/api/admin/users` | ADMIN |
| PUT | `/api/admin/users/{id}/enable` · `/disable` | ADMIN |

Format d'erreur : `{ "status": 404, "message": "Incident introuvable" }` (+ `errors` par champ pour les erreurs de validation).

## 7. Fonctionnalités réalisées
- Inscription / connexion, mots de passe BCrypt, JWT (email, date de création, expiration, rôle), durée de vie 5 minutes
- Spring Security stateless, filtre JWT, CORS pour `http://localhost:5173`
- Contrôle d'accès par rôle : `requestMatchers` (`/api/admin/**`) + `@EnableMethodSecurity` / `@PreAuthorize`
- Contrôle du propriétaire d'un incident côté backend (utilisateur lu dans le `SecurityContext`)
- Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`, `@NotNull`) et gestion globale des erreurs (`@RestControllerAdvice`)
- Compte désactivable : connexion refusée, JWT existant refusé immédiatement
- Historique des incidents (`IncidentHistory`) et notes de traitement (`IncidentComment`)
- Profil : nom, email, changement de mot de passe (vérification de l'ancien)
- Dashboards EMPLOYEE / TECHNICIAN / ADMIN
- **Bonus** : refresh token (rotation, révocation à la déconnexion)
- React : `ProtectedRoute`, `RoleRoute`, interceptor Axios de requête (Bearer) et de réponse (401)

## 8. Choix à connaître
- Après une affectation, le statut passe de `OPEN` à `ASSIGNED` ; le technicien le fait ensuite passer à `IN_PROGRESS` puis `RESOLVED`
  (le scénario du sujet « OPEN → IN_PROGRESS » devient donc « ASSIGNED → IN_PROGRESS »).
- Un incident `CLOSED` n'est plus modifiable, sauf par un ADMIN (réouverture).
- Un employé qui consulte l'incident d'un autre reçoit **403** ; un id inexistant renvoie **404**.
- Le rôle et l'état du compte sont relus en base à chaque requête (le claim `role` du JWT est présent mais l'autorité vient de la base).
- Pour observer la redirection « session expirée » après 5 minutes, mettre `VITE_ENABLE_REFRESH=false` dans `incident-frontend/.env`
  (sinon le refresh token renouvelle le JWT en silence ; la redirection n'a lieu que si le refresh échoue).
- Le JWT est stocké dans `localStorage` (exigé par le sujet) : simple mais exposé à un vol par XSS ; une alternative plus sûre est un cookie `HttpOnly`.

## 9. Tests de sécurité
- `tests/incident-security.postman_collection.json` : à importer dans Postman (lancer *Collection Runner*, dossier par dossier).
- `python3 tests/security_tests.py` : rejoue le scénario du sujet et les 9 tests de sécurité (backend démarré, sans dépendance externe).
- Le test « JWT expiré » utilise un jeton signé avec une durée négative : voir le script, ou attendre 5 minutes avec un vrai token.
