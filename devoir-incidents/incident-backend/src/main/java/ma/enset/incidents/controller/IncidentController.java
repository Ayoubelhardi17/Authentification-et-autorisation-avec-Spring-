package ma.enset.incidents.controller;

import jakarta.validation.Valid;
import ma.enset.incidents.dto.Dto.*;
import ma.enset.incidents.model.User;
import ma.enset.incidents.service.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService service;

    public IncidentController(IncidentService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<IncidentResponse> create(@Valid @RequestBody IncidentRequest req,
                                                   @AuthenticationPrincipal User current) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req, current));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<IncidentResponse> my(@AuthenticationPrincipal User current) {
        return service.myIncidents(current);
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public List<IncidentResponse> assigned(@AuthenticationPrincipal User current) {
        return service.assignedIncidents(current);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<IncidentResponse> all() {
        return service.allIncidents();
    }

    /** Le controle proprietaire/role est fait dans le service (section 12 du devoir). */
    @GetMapping("/{id}")
    public IncidentDetailResponse detail(@PathVariable Long id, @AuthenticationPrincipal User current) {
        return service.getDetail(id, current);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public IncidentResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest req,
                                   @AuthenticationPrincipal User current) {
        return service.updateStatus(id, req.status(), current);
    }

    @PutMapping("/{id}/assign/{technicianId}")
    @PreAuthorize("hasRole('ADMIN')")
    public IncidentResponse assign(@PathVariable Long id, @PathVariable Long technicianId,
                                   @AuthenticationPrincipal User current) {
        return service.assign(id, technicianId, current);
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('TECHNICIAN','ADMIN')")
    public ResponseEntity<CommentResponse> comment(@PathVariable Long id, @Valid @RequestBody CommentRequest req,
                                                   @AuthenticationPrincipal User current) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addComment(id, req.content(), current));
    }
}
