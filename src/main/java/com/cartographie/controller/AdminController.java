package com.cartographie.controller;

import com.cartographie.service.IAdminService;
import com.cartographie.repository.UtilisateurRepository;

import com.cartographie.repository.ProjetRepository;
import com.cartographie.repository.DomaineRepository;
import com.cartographie.service.IStatistiqueService;
import com.cartographie.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final IAdminService adminService;
    private final UtilisateurRepository utilisateurRepository;
    private final ProjetRepository projetRepository;
    private final DomaineRepository domaineRepository;
    private final IStatistiqueService statistiqueService;
    private final AuditService auditService;
    private final com.cartographie.service.KeycloakSyncService keycloakSyncService;

    @GetMapping("/users")
    public String listeUtilisateurs(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("utilisateurs", adminService.getAllUsers(keyword));
        model.addAttribute("keyword", keyword);
        return "admin/utilisateurs";
    }

    @PostMapping("/users/sync")
    public String syncKeycloakUsers(org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        String result = keycloakSyncService.syncUsersToKeycloak();
        redirectAttributes.addFlashAttribute("syncMessage", result);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/role")
    public String changerRole(@RequestParam Long id, @RequestParam String role) {
        adminService.updateRole(id, role);
        auditService.log("ROLE_CHANGE", "User ID " + id + " changed to " + role);
        return "redirect:/admin/users?success";
    }

    @PostMapping("/users/toggle-status")
    public String toggleStatus(@RequestParam Long id) {
        adminService.toggleUserStatus(id);
        auditService.log("USER_STATUS_TOGGLE", "Toggled status for User ID " + id);
        return "redirect:/admin/users?statusChanged";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        com.cartographie.dto.UtilisateurDto dto = new com.cartographie.dto.UtilisateurDto();
        dto.setLibelleRole("CANDIDAT"); // Default
        model.addAttribute("utilisateurDto", dto);
        return "admin/utilisateur_create";
    }

    @PostMapping("/users/create")
    public String createUser(
            @org.springframework.web.bind.annotation.ModelAttribute com.cartographie.dto.UtilisateurDto dto,
            Model model) {
        try {
            adminService.createUser(dto);
            auditService.log("USER_CREATE", "Created user: " + dto.getEmail());
            return "redirect:/admin/users?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/utilisateur_create";
        }
    }

    public String editUser(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        com.cartographie.model.Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("utilisateur", user);
        return "admin/utilisateur_edit";
    }

    @PostMapping("/users/update")
    public String updateUser(
            @org.springframework.web.bind.annotation.ModelAttribute com.cartographie.dto.UtilisateurDto dto) {
        adminService.updateUser(dto.getId(), dto);
        auditService.log("USER_UPDATE", "Updated user ID " + dto.getId());
        return "redirect:/admin/users?updated";
    }

    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Long id) {
        adminService.deleteUser(id);
        auditService.log("USER_DELETE", "Deleted user ID " + id);
        return "redirect:/admin/users?deleted";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // 1. Chiffres clés (KPIs)
        long totalUtilisateurs = utilisateurRepository.count();
        long totalProjets = projetRepository.count();
        long totalDomaines = domaineRepository.count();

        model.addAttribute("totalUtilisateurs", totalUtilisateurs);
        model.addAttribute("totalProjets", totalProjets);
        model.addAttribute("totalDomaines", totalDomaines);

        Double budgetTotal = projetRepository.sumAllBudgets();
        if (budgetTotal == null)
            budgetTotal = 0.0;
        model.addAttribute("budgetTotal", budgetTotal);

        // Budget Prévisionnel (Business Intelligence)
        String budgetGlobalStr = configRepo.findById("BUDGET_GLOBAL")
                .map(com.cartographie.model.Configuration::getValeur)
                .orElse("500000000");
        double budgetGlobal = 500000000.0;
        try {
            budgetGlobal = Double.parseDouble(budgetGlobalStr.replaceAll("\\s+", ""));
        } catch (Exception e) {
            // ignore
        }
        model.addAttribute("budgetRestant", budgetGlobal - budgetTotal);

        // Alertes : Projets en retard
        long projetsEnRetard = projetRepository.findAll().stream()
                .filter(p -> p.getDateFin() != null && p.getDateFin().isBefore(java.time.LocalDate.now())
                        && (p.getNiveauAvancement() == null || p.getNiveauAvancement() < 100))
                .count();
        model.addAttribute("projetsEnRetard", projetsEnRetard);

        // 2. Statistiques Détaillées (Identiques au Gestionnaire)
        model.addAttribute("avancementMoyen", statistiqueService.getTauxAvancementMoyen());
        model.addAttribute("statsDomaine", statistiqueService.getProjetsParDomaine());
        model.addAttribute("statsStatut", statistiqueService.getRepartitionParStatut());
        model.addAttribute("statsBudgetDomaine", statistiqueService.getBudgetParDomaine());
        model.addAttribute("statsEvolution", statistiqueService.getEvolutionProjets());
        model.addAttribute("statsParticipant", statistiqueService.getProjetsParParticipant());

        return "admin/dashboard";
    }

    @GetMapping("/stats")
    public String statistiques(Model model) {
        // Populate model with dashboard data (KPIs, Stats)
        dashboard(model);
        // Return the dedicated statistics view instead of the minimal dashboard
        return "admin/statistics";
    }

    private final com.cartographie.repository.ConfigurationRepository configRepo;

    // ... existing code ...

    @GetMapping("/config")
    public String configuration(Model model) {
        model.addAttribute("domaines", domaineRepository.findAll());

        String anneeScolaire = configRepo.findById("ANNEE_SCOLAIRE")
                .map(com.cartographie.model.Configuration::getValeur)
                .orElse("");
        model.addAttribute("anneeScolaire", anneeScolaire);

        String budgetGlobal = configRepo.findById("BUDGET_GLOBAL")
                .map(com.cartographie.model.Configuration::getValeur)
                .orElse("500000000");
        model.addAttribute("budgetGlobal", budgetGlobal);

        return "admin/config";
    }

    @PostMapping("/config/save")
    public String saveConfig(@RequestParam String anneeScolaire, @RequestParam(required = false) String budgetGlobal) {
        configRepo.save(new com.cartographie.model.Configuration("ANNEE_SCOLAIRE", anneeScolaire));

        if (budgetGlobal != null && !budgetGlobal.isEmpty()) {
            configRepo.save(new com.cartographie.model.Configuration("BUDGET_GLOBAL", budgetGlobal));
        }

        return "redirect:/admin/config?saved";
    }

    private final com.cartographie.service.CsvService csvService;

    @PostMapping("/projets/import")
    public String importProjets(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (file.isEmpty()) {
            return "redirect:/admin/config?error=empty";
        }
        try {
            csvService.importProjets(file);
            auditService.log("CSV_IMPORT", "Imported projects from CSV: " + file.getOriginalFilename());
            return "redirect:/admin/config?imported";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admin/config?error=import";
        }
    }

    // ... existing code ...

    @PostMapping("/domaines/add")
    public String ajouterDomaine(@RequestParam String nom, @RequestParam(required = false) String description) {
        adminService.createDomaine(nom, description);
        auditService.log("DOMAIN_CREATE", "Created domain: " + nom);
        return "redirect:/admin/config?added";
    }

    @PostMapping("/domaines/edit")
    public String modifierDomaine(@RequestParam Long id, @RequestParam String nom,
            @RequestParam(required = false) String description) {
        adminService.updateDomaine(id, nom, description);
        auditService.log("DOMAIN_EDIT", "Updated domain ID " + id + " to " + nom);
        return "redirect:/admin/config?updated";
    }

    @PostMapping("/domaines/delete")
    public String supprimerDomaine(@RequestParam Long id) {
        adminService.deleteDomaine(id);
        auditService.log("DOMAIN_DELETE", "Deleted domain ID " + id);
        return "redirect:/admin/config?deleted";
    }

    @GetMapping("/audit")
    public String viewAuditLogs(Model model) {
        model.addAttribute("logs", auditService.getAllLogs());
        return "admin/audit";
    }
}
