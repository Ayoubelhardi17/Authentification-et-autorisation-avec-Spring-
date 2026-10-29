package ma.enset.incidents.service;

import ma.enset.incidents.dto.Dto.*;
import ma.enset.incidents.exception.Exceptions.*;
import ma.enset.incidents.model.*;
import ma.enset.incidents.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class IncidentService {

    private final IncidentRepository incidentRepo;
    private final IncidentHistoryRepository historyRepo;
    private final IncidentCommentRepository commentRepo;
    private final UserRepository userRepo;

    public IncidentService(IncidentRepository incidentRepo, IncidentHistoryRepository historyRepo,
                           IncidentCommentRepository commentRepo, UserRepository userRepo) {
        this.incidentRepo = incidentRepo;
        this.historyRepo = historyRepo;
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
    }

    // ------------------------------------------------------------------ creation
    public IncidentResponse create(IncidentRequest req, User current) {
        User owner = loadUser(current.getId());
        Incident i = new Incident();
        i.setTitle(req.title().trim());
        i.setDescription(req.description().trim());
        i.setCategory(req.category());
        i.setPriority(req.priority());
        i.setStatus(Status.OPEN);
        i.setCreatedBy(owner);
        incidentRepo.save(i);
        addHistory(i, "Incident créé", owner.getFullName());
        return toResponse(i);
    }

    // ------------------------------------------------------------------ lectures
    @Transactional(readOnly = true)
    public List<IncidentResponse> myIncidents(User current) {
        return incidentRepo.findByCreatedByOrderByCreatedAtDesc(loadUser(current.getId()))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> assignedIncidents(User current) {
        return incidentRepo.findByAssignedToOrderByCreatedAtDesc(loadUser(current.getId()))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> allIncidents() {
        return incidentRepo.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    /**
     * CONTROLE DU PROPRIETAIRE (section 12) : l'utilisateur vient du SecurityContext (JWT),
     * jamais d'un parametre de la requete. On le compare au proprietaire de l'incident.
     */
    @Transactional(readOnly = true)
    public IncidentDetailResponse getDetail(Long id, User current) {
        Incident i = findIncident(id);
        checkCanRead(i, current);
        List<CommentResponse> comments = commentRepo.findByIncidentOrderByCreatedAtAscIdAsc(i).stream()
                .map(c -> new CommentResponse(c.getId(), c.getAuthor().getFullName(), c.getContent(), c.getCreatedAt()))
                .toList();
        List<HistoryResponse> history = historyRepo.findByIncidentOrderByCreatedAtAscIdAsc(i).stream()
                .map(h -> new HistoryResponse(h.getId(), h.getMessage(), h.getActorName(), h.getCreatedAt()))
                .toList();
        return new IncidentDetailResponse(toResponse(i), comments, history);
    }

    // ------------------------------------------------------------------ statut
    public IncidentResponse updateStatus(Long id, Status newStatus, User current) {
        Incident i = findIncident(id);
        // Regle 5 : un technicien ne modifie que les incidents qui lui sont affectes
        if (current.getRole() == Role.TECHNICIAN && !isAssignedTo(i, current)) {
            throw new ForbiddenException("Cet incident ne vous est pas affecté");
        }
        // Regle 8 : un incident CLOSED n'est plus modifiable normalement (seul un ADMIN peut le rouvrir)
        if (i.getStatus() == Status.CLOSED && current.getRole() != Role.ADMIN) {
            throw new BadRequestException("Incident clôturé : son statut ne peut plus être modifié");
        }
        if (current.getRole() == Role.TECHNICIAN
                && (newStatus == Status.OPEN || newStatus == Status.ASSIGNED)) {
            throw new BadRequestException("Un technicien ne peut pas repasser un incident à " + newStatus);
        }
        if (i.getStatus() == newStatus) {
            throw new BadRequestException("L'incident est déjà au statut " + newStatus);
        }
        Status old = i.getStatus();
        i.setStatus(newStatus);
        incidentRepo.save(i);
        addHistory(i, old + " -> " + newStatus, current.getFullName());
        return toResponse(i);
    }

    // ------------------------------------------------------------------ affectation (ADMIN)
    public IncidentResponse assign(Long incidentId, Long technicianId, User current) {
        Incident i = findIncident(incidentId);
        User tech = userRepo.findById(technicianId)
                .orElseThrow(() -> new NotFoundException("Technicien introuvable"));
        if (tech.getRole() != Role.TECHNICIAN) {
            throw new BadRequestException("L'utilisateur choisi n'est pas un technicien");
        }
        if (!tech.isEnabled()) {
            throw new BadRequestException("Le technicien choisi est désactivé");
        }
        if (i.getStatus() == Status.CLOSED) {
            throw new BadRequestException("Incident clôturé : affectation impossible");
        }
        i.setAssignedTo(tech);
        String msg = "Affecté à " + tech.getFullName();
        if (i.getStatus() == Status.OPEN) {
            i.setStatus(Status.ASSIGNED);
            msg += " (OPEN -> ASSIGNED)";
        }
        incidentRepo.save(i);
        addHistory(i, msg, current.getFullName());
        return toResponse(i);
    }

    // ------------------------------------------------------------------ notes de traitement
    public CommentResponse addComment(Long id, String content, User current) {
        Incident i = findIncident(id);
        if (current.getRole() == Role.TECHNICIAN && !isAssignedTo(i, current)) {
            throw new ForbiddenException("Cet incident ne vous est pas affecté");
        }
        User author = loadUser(current.getId());
        IncidentComment c = new IncidentComment();
        c.setIncident(i);
        c.setAuthor(author);
        c.setContent(content.trim());
        commentRepo.save(c);
        addHistory(i, "Note de traitement ajoutée", author.getFullName());
        return new CommentResponse(c.getId(), author.getFullName(), c.getContent(), c.getCreatedAt());
    }

    // ------------------------------------------------------------------ helpers
    private void checkCanRead(Incident i, User current) {
        switch (current.getRole()) {
            case ADMIN -> { /* acces total */ }
            case EMPLOYEE -> {
                if (!i.getCreatedBy().getId().equals(current.getId())) {
                    throw new ForbiddenException("Accès interdit : cet incident ne vous appartient pas");
                }
            }
            case TECHNICIAN -> {
                if (!isAssignedTo(i, current)) {
                    throw new ForbiddenException("Accès interdit : cet incident ne vous est pas affecté");
                }
            }
        }
    }

    private boolean isAssignedTo(Incident i, User user) {
        return i.getAssignedTo() != null && i.getAssignedTo().getId().equals(user.getId());
    }

    private Incident findIncident(Long id) {
        return incidentRepo.findById(id).orElseThrow(() -> new NotFoundException("Incident introuvable"));
    }

    private User loadUser(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    }

    private void addHistory(Incident i, String message, String actor) {
        IncidentHistory h = new IncidentHistory();
        h.setIncident(i);
        h.setMessage(message);
        h.setActorName(actor);
        historyRepo.save(h);
    }

    public IncidentResponse toResponse(Incident i) {
        User c = i.getCreatedBy();
        User a = i.getAssignedTo();
        return new IncidentResponse(i.getId(), i.getTitle(), i.getDescription(), i.getCategory(), i.getPriority(),
                i.getStatus(), i.getCreatedAt(), i.getUpdatedAt(),
                c.getId(), c.getFullName(),
                a == null ? null : a.getId(), a == null ? null : a.getFullName());
    }
}
