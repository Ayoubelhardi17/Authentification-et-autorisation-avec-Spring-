package ma.enset.incidents.service;

import ma.enset.incidents.dto.Dto.*;
import ma.enset.incidents.exception.Exceptions.*;
import ma.enset.incidents.model.RefreshToken;
import ma.enset.incidents.model.Role;
import ma.enset.incidents.model.User;
import ma.enset.incidents.repository.RefreshTokenRepository;
import ma.enset.incidents.repository.UserRepository;
import ma.enset.incidents.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-expiration-days}")
    private long refreshDays;

    public AuthService(UserRepository userRepo, RefreshTokenRepository refreshRepo,
                       PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.refreshRepo = refreshRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    /** Regle metier 1 et 2 : le role est TOUJOURS EMPLOYEE, il n'existe aucun champ role dans la requete. */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepo.existsByEmail(email)) {
            throw new ConflictException("Cet email est déjà utilisé");
        }
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(req.password())); // BCrypt
        u.setRole(Role.EMPLOYEE);
        u.setEnabled(true);
        userRepo.save(u);
        return buildResponse(u);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User u = userRepo.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));
        // On verifie d'abord le mot de passe pour ne pas reveler l'etat d'un compte a un inconnu
        if (!encoder.matches(req.password(), u.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }
        // Regle metier 7 : compte desactive => connexion refusee
        if (!u.isEnabled()) {
            throw new ForbiddenException("Compte désactivé. Contactez l'administrateur.");
        }
        return buildResponse(u);
    }

    /** Rotation : l'ancien refresh token est supprime, un nouveau est emis. */
    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        RefreshToken rt = refreshRepo.findByToken(req.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalide"));
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            refreshRepo.delete(rt);
            throw new UnauthorizedException("Refresh token expiré, reconnectez-vous");
        }
        User u = rt.getUser();
        if (!u.isEnabled()) {
            refreshRepo.delete(rt);
            throw new ForbiddenException("Compte désactivé");
        }
        refreshRepo.delete(rt);
        return buildResponse(u);
    }

    /** Deconnexion : le refresh token est revoque cote serveur. */
    @Transactional
    public void logout(RefreshRequest req) {
        refreshRepo.deleteByToken(req.refreshToken());
    }

    public AuthResponse buildResponse(User u) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID().toString());
        rt.setUser(u);
        rt.setExpiresAt(Instant.now().plus(refreshDays, ChronoUnit.DAYS));
        refreshRepo.save(rt);
        return new AuthResponse(jwtService.generateToken(u), rt.getToken(), u.getRole().name(), u.getFullName(), u.getEmail());
    }
}
