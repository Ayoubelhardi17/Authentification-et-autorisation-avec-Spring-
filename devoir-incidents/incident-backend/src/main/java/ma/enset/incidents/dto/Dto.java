package ma.enset.incidents.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ma.enset.incidents.model.Category;
import ma.enset.incidents.model.Priority;
import ma.enset.incidents.model.Role;
import ma.enset.incidents.model.Status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Tous les DTO (objets echanges avec le frontend). Les entites JPA ne sont jamais exposees directement. */
public final class Dto {
    private Dto() {}

    // ---------- authentification ----------
    /** Pas de champ "role" : l'utilisateur ne peut pas choisir son role. */
    public record RegisterRequest(
            @NotBlank(message = "Le nom complet est obligatoire") @Size(max = 100) String fullName,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            @NotBlank(message = "Le mot de passe est obligatoire")
            @Size(min = 8, max = 100, message = "Le mot de passe doit contenir au moins 8 caractères") String password) {}

    public record LoginRequest(
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            @NotBlank(message = "Le mot de passe est obligatoire") String password) {}

    public record RefreshRequest(@NotBlank(message = "Refresh token obligatoire") String refreshToken) {}

    public record AuthResponse(String token, String refreshToken, String role, String fullName, String email) {}

    // ---------- utilisateurs ----------
    public record UserResponse(Long id, String fullName, String email, Role role, boolean enabled, LocalDateTime createdAt) {}

    /** Creation d'un compte par un ADMIN (permet de creer TECHNICIAN / ADMIN). */
    public record CreateUserRequest(
            @NotBlank(message = "Le nom complet est obligatoire") String fullName,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email,
            @NotBlank @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères") String password,
            @NotNull(message = "Le rôle est obligatoire") Role role) {}

    public record ProfileUpdateRequest(
            @NotBlank(message = "Le nom complet est obligatoire") String fullName,
            @NotBlank(message = "L'email est obligatoire") @Email(message = "Email invalide") String email) {}

    /** Un nouveau token est renvoye car le sujet du JWT (l'email) peut avoir change. */
    public record ProfileUpdateResponse(UserResponse profile, String token) {}

    public record PasswordChangeRequest(
            @NotBlank(message = "L'ancien mot de passe est obligatoire") String oldPassword,
            @NotBlank @Size(min = 8, message = "Le nouveau mot de passe doit contenir au moins 8 caractères") String newPassword,
            @NotBlank(message = "La confirmation est obligatoire") String confirmPassword) {}

    // ---------- incidents ----------
    public record IncidentRequest(
            @NotBlank(message = "Le titre est obligatoire") @Size(max = 150, message = "Titre trop long (150 max)") String title,
            @NotBlank(message = "La description est obligatoire") @Size(max = 4000) String description,
            @NotNull(message = "La catégorie est obligatoire") Category category,
            @NotNull(message = "La priorité est obligatoire") Priority priority) {}

    public record StatusRequest(@NotNull(message = "Le statut est obligatoire") Status status) {}

    public record CommentRequest(
            @NotBlank(message = "La note ne peut pas être vide") @Size(max = 2000) String content) {}

    public record CommentResponse(Long id, String author, String content, LocalDateTime createdAt) {}

    public record HistoryResponse(Long id, String message, String actor, LocalDateTime createdAt) {}

    public record IncidentResponse(Long id, String title, String description, Category category, Priority priority,
                                   Status status, LocalDateTime createdAt, LocalDateTime updatedAt,
                                   Long createdById, String createdBy, Long assignedToId, String assignedTo) {}

    public record IncidentDetailResponse(IncidentResponse incident, List<CommentResponse> comments,
                                         List<HistoryResponse> history) {}

    // ---------- dashboard ----------
    public record DashboardResponse(String role, Map<String, Long> stats, List<IncidentResponse> latestIncidents) {}
}
