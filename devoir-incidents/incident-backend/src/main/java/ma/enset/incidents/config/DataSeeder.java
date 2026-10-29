package ma.enset.incidents.config;

import ma.enset.incidents.model.Role;
import ma.enset.incidents.model.User;
import ma.enset.incidents.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Comptes de test crees au demarrage (la base H2 est en memoire). */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() > 0) return;
            create(repo, encoder, "Administrateur", "admin@demo.ma", "Admin123!", Role.ADMIN);
            create(repo, encoder, "Technicien A", "tech1@demo.ma", "Tech1234!", Role.TECHNICIAN);
            create(repo, encoder, "Technicien B", "tech2@demo.ma", "Tech1234!", Role.TECHNICIAN);
            create(repo, encoder, "Ahmed Benali", "employee1@demo.ma", "Employee1!", Role.EMPLOYEE);
            create(repo, encoder, "Sara Alaoui", "employee2@demo.ma", "Employee2!", Role.EMPLOYEE);
        };
    }

    private void create(UserRepository repo, PasswordEncoder encoder, String name, String email, String pwd, Role role) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPassword(encoder.encode(pwd));
        u.setRole(role);
        u.setEnabled(true);
        repo.save(u);
    }
}
