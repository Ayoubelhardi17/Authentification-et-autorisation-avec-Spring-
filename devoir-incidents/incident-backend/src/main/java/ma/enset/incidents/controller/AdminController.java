package ma.enset.incidents.controller;

import jakarta.validation.Valid;
import ma.enset.incidents.dto.Dto.*;
import ma.enset.incidents.model.User;
import ma.enset.incidents.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** /api/admin/** : ADMIN uniquement (securise deux fois : SecurityConfig + @PreAuthorize). */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<UserResponse> users() {
        return adminService.users();
    }

    @GetMapping("/technicians")
    public List<UserResponse> technicians() {
        return adminService.technicians();
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createUser(req));
    }

    @PutMapping("/users/{id}/enable")
    public UserResponse enable(@PathVariable Long id, @AuthenticationPrincipal User current) {
        return adminService.setEnabled(id, true, current);
    }

    @PutMapping("/users/{id}/disable")
    public UserResponse disable(@PathVariable Long id, @AuthenticationPrincipal User current) {
        return adminService.setEnabled(id, false, current);
    }

    @GetMapping("/statistics")
    public Map<String, Long> statistics() {
        return adminService.statistics();
    }
}
