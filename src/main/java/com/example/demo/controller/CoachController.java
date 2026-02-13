package com.example.demo.controller;

import com.example.demo.model.Coach;
import com.example.demo.service.CoachService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    /**
     * CREATE - Créer un nouveau coach
     * POST /api/coaches
     */
    @PostMapping
    public ResponseEntity<Coach> createCoach(@RequestBody Coach coach) {
        Coach createdCoach = coachService.createCoach(coach);
        return new ResponseEntity<>(createdCoach, HttpStatus.CREATED);
    }

    /**
     * READ - Récupérer tous les coaches avec pagination
     * GET /api/coaches?page=0&size=10
     */
    @GetMapping
    public ResponseEntity<Page<Coach>> getAllCoaches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Coach> coaches = coachService.getAllCoaches(pageable);
        return ResponseEntity.ok(coaches);
    }

    /**
     * READ - Récupérer un coach par ID
     * GET /api/coaches/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Coach> getCoachById(@PathVariable String id) {
        return coachService.getCoachById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * READ - Récupérer les coaches par domaine (sans pagination)
     * GET /api/coaches/domain/{domain}
     */
    @GetMapping("/domain/{domain}")
    public ResponseEntity<List<Coach>> getCoachesByDomain(@PathVariable String domain) {
        List<Coach> coaches = coachService.getCoachesByDomain(domain);
        return ResponseEntity.ok(coaches);
    }

    /**
     * READ - Récupérer les coaches par domaine avec pagination
     * GET /api/coaches/domain-paginated/{domain}?page=0&size=10
     */
    @GetMapping("/domain-paginated/{domain}")
    public ResponseEntity<Page<Coach>> getCoachesByDomainPaginated(
            @PathVariable String domain,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Coach> coaches = coachService.getCoachesByDomain(domain, pageable);
        return ResponseEntity.ok(coaches);
    }

    /**
     * UPDATE - Mettre à jour un coach
     * PUT /api/coaches/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Coach> updateCoach(@PathVariable String id, @RequestBody Coach coach) {
        try {
            Coach updatedCoach = coachService.updateCoach(id, coach);
            return ResponseEntity.ok(updatedCoach);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE - Supprimer un coach
     * DELETE /api/coaches/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoach(@PathVariable String id) {
        coachService.deleteCoach(id);
        return ResponseEntity.noContent().build();
    }
}

