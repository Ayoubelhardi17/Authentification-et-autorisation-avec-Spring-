package ma.enset.incidents.service;

import ma.enset.incidents.dto.Dto.*;
import ma.enset.incidents.exception.Exceptions.*;
import ma.enset.incidents.model.*;
import ma.enset.incidents.repository.IncidentRepository;
import ma.enset.incidents.repository.RefreshTokenRepository;
import ma.enset.incidents.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepo;
    private final IncidentRepository incidentRepo;
    private final RefreshTokenRepository refreshRepo;
    private final IncidentService incidentService;
    private final PasswordEncoder encoder;

    public AdminService(UserRepository userRepo, IncidentRepository incidentRepo, RefreshTokenRepository refreshRepo,
                        IncidentService incidentService, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.incidentRepo = incidentRepo;
        this.refreshRepo = refreshRepo;
        this.incidentService = incidentService;
        this.encoder = encoder;
    }

    // ---------------------------------------------------------------- utilisateurs
    @Transactional(readOnly = true)
    public List<UserResponse> users() {
        return userRepo.findAll().stream().map(UserMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> technicians() {
        return userRepo.findByRoleAndEnabledTrue(Role.TECHNICIAN).stream().map(UserMapper::toResponse).toList();
    }

    public UserResponse createUser(CreateUserRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new ConflictException("Cet email est déjà utilisé");
        }
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password()));
        u.setRole(req.role());
        u.setEnabled(true);
        return UserMapper.toResponse(userRepo.save(u));
    }

    public UserResponse setEnabled(Long id, boolean enabled, User current) {
        User u = userRepo.findById(id).orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
        if (!enabled && u.getId().equals(current.getId())) {
            throw new BadRequestException("Vous ne pouvez pas désactiver votre propre compte");
        }
        u.setEnabled(enabled);
        userRepo.save(u);
        if (!enabled) {
            refreshRepo.deleteByUser(u); // revoque les refresh tokens
        }
        return UserMapper.toResponse(u);
    }

    // ---------------------------------------------------------------- statistiques
    @Transactional(readOnly = true)
    public Map<String, Long> statistics() {
        Map<String, Long> s = new LinkedHashMap<>();
        s.put("totalUsers", userRepo.count());
        s.put("employees", userRepo.countByRole(Role.EMPLOYEE));
        s.put("technicians", userRepo.countByRole(Role.TECHNICIAN));
        s.put("admins", userRepo.countByRole(Role.ADMIN));
        s.put("totalIncidents", incidentRepo.count());
        s.put("openIncidents", incidentRepo.countByStatus(Status.OPEN));
        s.put("assignedIncidents", incidentRepo.countByStatus(Status.ASSIGNED));
        s.put("inProgressIncidents", incidentRepo.countByStatus(Status.IN_PROGRESS));
        s.put("resolvedIncidents", incidentRepo.countByStatus(Status.RESOLVED));
        s.put("closedIncidents", incidentRepo.countByStatus(Status.CLOSED));
        return s;
    }

    // ---------------------------------------------------------------- dashboard selon le role
    @Transactional(readOnly = true)
    public DashboardResponse dashboard(User current) {
        Map<String, Long> s = new LinkedHashMap<>();
        List<IncidentResponse> latest = List.of();
        switch (current.getRole()) {
            case EMPLOYEE -> {
                User u = userRepo.findById(current.getId()).orElseThrow();
                s.put("total", incidentRepo.countByCreatedBy(u));
                s.put("open", incidentRepo.countByCreatedByAndStatus(u, Status.OPEN));
                s.put("inProgress", incidentRepo.countByCreatedByAndStatus(u, Status.IN_PROGRESS));
                s.put("resolvedOrClosed", incidentRepo.countByCreatedByAndStatusIn(u, List.of(Status.RESOLVED, Status.CLOSED)));
                latest = incidentRepo.findTop5ByCreatedByOrderByCreatedAtDesc(u).stream().map(incidentService::toResponse).toList();
            }
            case TECHNICIAN -> {
                User u = userRepo.findById(current.getId()).orElseThrow();
                s.put("assigned", incidentRepo.countByAssignedTo(u));
                s.put("critical", incidentRepo.countByAssignedToAndPriority(u, Priority.CRITICAL));
                s.put("inProgress", incidentRepo.countByAssignedToAndStatus(u, Status.IN_PROGRESS));
                s.put("resolved", incidentRepo.countByAssignedToAndStatusIn(u, List.of(Status.RESOLVED, Status.CLOSED)));
                latest = incidentRepo.findTop5ByAssignedToOrderByCreatedAtDesc(u).stream().map(incidentService::toResponse).toList();
            }
            case ADMIN -> s.putAll(statistics());
        }
        return new DashboardResponse(current.getRole().name(), s, latest);
    }
}
