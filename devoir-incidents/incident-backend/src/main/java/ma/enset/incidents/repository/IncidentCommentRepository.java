package ma.enset.incidents.repository;

import ma.enset.incidents.model.Incident;
import ma.enset.incidents.model.IncidentComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentCommentRepository extends JpaRepository<IncidentComment, Long> {
    List<IncidentComment> findByIncidentOrderByCreatedAtAscIdAsc(Incident incident);
}
