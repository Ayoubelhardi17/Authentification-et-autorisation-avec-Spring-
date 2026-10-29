package ma.enset.incidents.repository;

import ma.enset.incidents.model.Incident;
import ma.enset.incidents.model.IncidentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IncidentHistoryRepository extends JpaRepository<IncidentHistory, Long> {
    List<IncidentHistory> findByIncidentOrderByCreatedAtAscIdAsc(Incident incident);
}
