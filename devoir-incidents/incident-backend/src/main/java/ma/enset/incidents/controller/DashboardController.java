package ma.enset.incidents.controller;

import ma.enset.incidents.dto.Dto.DashboardResponse;
import ma.enset.incidents.model.User;
import ma.enset.incidents.service.AdminService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dashboard adapte au role de l'utilisateur connecte. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AdminService adminService;

    public DashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public DashboardResponse dashboard(@AuthenticationPrincipal User current) {
        return adminService.dashboard(current);
    }
}
