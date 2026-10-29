package ma.enset.incidents.repository;

import ma.enset.incidents.model.Role;
import ma.enset.incidents.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    List<User> findByRoleAndEnabledTrue(Role role);
}
