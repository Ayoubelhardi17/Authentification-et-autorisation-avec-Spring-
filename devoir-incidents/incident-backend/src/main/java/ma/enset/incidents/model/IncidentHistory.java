package ma.enset.incidents.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Une ligne d'historique : creation, affectation, changement de statut, note ajoutee. */
@Entity
public class IncidentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    @Column(nullable = false, length = 1000)
    private String message;

    /** Nom de l'auteur de l'action (copie : l'historique reste lisible meme si l'utilisateur change de nom). */
    @Column(nullable = false)
    private String actorName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ---- getters / setters (ecrits a la main, sans Lombok) ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Incident getIncident() { return incident; }
    public void setIncident(Incident incident) { this.incident = incident; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
