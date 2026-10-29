package ma.enset.incidents.service;

import ma.enset.incidents.dto.Dto.*;
import ma.enset.incidents.exception.Exceptions.*;
import ma.enset.incidents.model.User;
import ma.enset.incidents.repository.UserRepository;
import ma.enset.incidents.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public ProfileService(UserRepository userRepo, PasswordEncoder encoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public UserResponse get(User current) {
        return UserMapper.toResponse(load(current));
    }

    @Transactional
    public ProfileUpdateResponse update(User current, ProfileUpdateRequest req) {
        User u = load(current);
        String email = req.email().trim().toLowerCase();
        if (!email.equals(u.getEmail()) && userRepo.existsByEmail(email)) {
            throw new ConflictException("Cet email est déjà utilisé");
        }
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        userRepo.save(u);
        return new ProfileUpdateResponse(UserMapper.toResponse(u), jwtService.generateToken(u));
    }

    /** Le backend verifie l'ancien mot de passe avant d'enregistrer le nouveau. */
    @Transactional
    public void changePassword(User current, PasswordChangeRequest req) {
        User u = load(current);
        if (!encoder.matches(req.oldPassword(), u.getPassword())) {
            // 400 (et non 401) pour ne pas declencher la deconnexion automatique du frontend
            throw new BadRequestException("Ancien mot de passe incorrect");
        }
        if (!req.newPassword().equals(req.confirmPassword())) {
            throw new BadRequestException("La confirmation ne correspond pas au nouveau mot de passe");
        }
        if (encoder.matches(req.newPassword(), u.getPassword())) {
            throw new BadRequestException("Le nouveau mot de passe doit être différent de l'ancien");
        }
        u.setPassword(encoder.encode(req.newPassword()));
        userRepo.save(u);
    }

    private User load(User current) {
        return userRepo.findById(current.getId())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));
    }
}
