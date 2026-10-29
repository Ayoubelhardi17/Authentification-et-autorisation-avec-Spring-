package ma.enset.incidents.controller;

import jakarta.validation.Valid;
import ma.enset.incidents.dto.Dto.*;
import ma.enset.incidents.model.User;
import ma.enset.incidents.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserResponse get(@AuthenticationPrincipal User current) {
        return profileService.get(current);
    }

    @PutMapping
    public ProfileUpdateResponse update(@AuthenticationPrincipal User current,
                                        @Valid @RequestBody ProfileUpdateRequest req) {
        return profileService.update(current, req);
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal User current,
                                                              @Valid @RequestBody PasswordChangeRequest req) {
        profileService.changePassword(current, req);
        return ResponseEntity.ok(Map.of("message", "Mot de passe modifié"));
    }
}
