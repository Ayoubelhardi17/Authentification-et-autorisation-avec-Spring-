package ma.enset.incidents.repository;

import ma.enset.incidents.model.Incident;
import ma.enset.incidents.model.Priority;
import ma.enset.incidents.model.Status;
import ma.enset.incidents.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findAllByOrderByCreatedAtDesc();
    List<Incident> findByCreatedByOrderByCreatedAtDesc(User user);
    List<Incident> findByAssignedToOrderByCreatedAtDesc(User user);
    List<Incident> findTop5ByCreatedByOrderByCreatedAtDesc(User user);
    List<Incident> findTop5ByAssignedToOrderByCreatedAtDesc(User user);

    long countByStatus(Status status);
    long countByCreatedBy(User user);
    long countByCreatedByAndStatus(User user, Status status);
    long countByCreatedByAndStatusIn(User user, Collection<Status> statuses);
    long countByAssignedTo(User user);
    long countByAssignedToAndStatus(User user, Status status);
    long countByAssignedToAndStatusIn(User user, Collection<Status> statuses);
    long countByAssignedToAndPriority(User user, Priority priority);
}
