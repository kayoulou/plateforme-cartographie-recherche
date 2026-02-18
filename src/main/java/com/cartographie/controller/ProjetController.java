package com.cartographie.controller;

import com.cartographie.service.IProjetService;
import com.cartographie.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import com.cartographie.dto.ProjetDto;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/projets")
@Tag(name = "Projets", description = "Gestion des projets de recherche")
// @RequiredArgsConstructor
public class ProjetController {
    // On injecte l'interface pour respecter le principe d'abstraction
    private final IProjetService projetService;
    private final com.cartographie.repository.DomaineRepository domaineRepository;
    private final com.cartographie.repository.UtilisateurRepository utilisateurRepository;
    private final AuditService auditService;
    private final com.cartographie.repository.RoleRepository roleRepository;

    // Constructeur manuel pour l'injection de dépendances
    public ProjetController(IProjetService projetService,
            com.cartographie.repository.DomaineRepository domaineRepository,
            com.cartographie.repository.UtilisateurRepository utilisateurRepository,
            AuditService auditService,
            com.cartographie.repository.RoleRepository roleRepository) {
        this.projetService = projetService;
        this.domaineRepository = domaineRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.auditService = auditService;
        this.roleRepository = roleRepository;
    }

    // Route pour le Candidat : Voir uniquement ses projets (Admin/Gestionnaire
    // voient tout)
    @GetMapping("/mes-projets")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister mes projets", description = "Affiche les projets de l'utilisateur connecté ou tous si admin/gestionnaire")
    public String voirMesProjets(Model model, Authentication auth,
            @org.springframework.web.bind.annotation.RequestParam(required = false) String keyword) {
        String email = getEmailFromAuth(auth);
        String nom = getNameFromAuth(auth);

        boolean isPrivileged = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_GESTIONNAIRE"));

        model.addAttribute("userName", nom);
        model.addAttribute("keyword", keyword);

        java.util.List<com.cartographie.dto.ProjetDto> projets;

        if (isPrivileged) {
            projets = projetService.findAll(keyword, null, null);
        } else {
            projets = projetService.findByUserEmail(email);
            if (keyword != null && !keyword.isEmpty()) {
                String k = keyword.toLowerCase();
                projets = projets.stream()
                        .filter(p -> (p.getTitre() != null && p.getTitre().toLowerCase().contains(k)) ||
                                (p.getDescription() != null && p.getDescription().toLowerCase().contains(k)))
                        .collect(java.util.stream.Collectors.toList());
            }
        }
        model.addAttribute("projets", projets);

        return "dashboard-candidat";
    }

    // Route pour Gestionnaire/Admin : Voir tout
    @GetMapping("/tous")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'ADMIN')")
    @Operation(summary = "Lister tous les projets", description = "Vue globale filtrable pour les gestionnaires et administrateurs")
    public String voirTousLesProjets(Model model,
            @org.springframework.web.bind.annotation.RequestParam(name = "keyword", required = false) String keyword,
            @org.springframework.web.bind.annotation.RequestParam(name = "statut", required = false) String statut,
            @org.springframework.web.bind.annotation.RequestParam(name = "domaine", required = false) String domaine) {
        model.addAttribute("projets", projetService.findAll(keyword, statut, domaine));
        model.addAttribute("domaines", domaineRepository.findAll()); // Pour le filtre
        model.addAttribute("keyword", keyword);
        model.addAttribute("statut", statut);
        model.addAttribute("domaine", domaine);
        return "projets/liste_globale";
    }

    @GetMapping("/nouveau")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'GESTIONNAIRE', 'ADMIN')")
    @Hidden
    public String formulaire(Model model) {
        model.addAttribute("projet", new ProjetDto());
        model.addAttribute("domaines", domaineRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "projets/formulaire_declaration";
    }

    private String getEmailFromAuth(Authentication auth) {
        if (auth instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            return ((org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) auth)
                    .getPrincipal().getAttribute("email");
        }
        return auth.getName(); // For form login
    }

    private String getNameFromAuth(Authentication auth) {
        if (auth instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
            org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken token = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) auth;
            String name = token.getPrincipal().getAttribute("given_name");
            return name != null ? name : token.getPrincipal().getAttribute("name");
        }
        return auth.getName();
    }

    @PostMapping("/enregistrer")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'GESTIONNAIRE', 'ADMIN')")
    @Operation(summary = "Enregistrer un projet", description = "Crée ou met à jour un projet avec validation des données")
    public String enregistrer(@Valid @ModelAttribute("projet") ProjetDto dto,
            BindingResult result,
            Model model,
            Authentication auth) {

        System.out.println("DEBUG: Registering project. DTO: " + dto);

        if (result.hasErrors()) {
            model.addAttribute("domaines", domaineRepository.findAll());
            model.addAttribute("utilisateurs", utilisateurRepository.findAll());
            return "projets/formulaire_declaration";
        }

        try {
            // Appel de la méthode de l'interface
            String email = getEmailFromAuth(auth);

            // Ensure user exists locally (Just in case)
            if (utilisateurRepository.findByEmail(email).isEmpty()) {
                if (auth instanceof org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                    System.out.println("USER NOT FOUND IN DB (OAuth2). Creating on the fly...");
                    org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken oauthToken = (org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) auth;

                    com.cartographie.model.Utilisateur newUser = new com.cartographie.model.Utilisateur();
                    newUser.setEmail(email);
                    newUser.setNom(oauthToken.getPrincipal().getAttribute("family_name"));
                    newUser.setPrenom(oauthToken.getPrincipal().getAttribute("given_name"));
                    newUser.setActif(true); // Default active
                    roleRepository.findByLibelle("CANDIDAT").ifPresent(newUser::setRole);
                    utilisateurRepository.save(newUser);
                } else {
                    // Should not happen for Form Login as they need DB user to login
                    throw new RuntimeException("Utilisateur introuvable pour l'email : " + email);
                }
            }

            boolean isPrivileged = auth.getAuthorities().stream()
                    .anyMatch(
                            a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_GESTIONNAIRE"));

            projetService.saveWithUser(dto, email, isPrivileged);

            if (isPrivileged) {
                return "redirect:/projets/tous";
            }
            return "redirect:/projets/mes-projets";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            model.addAttribute("domaines", domaineRepository.findAll());
            model.addAttribute("utilisateurs", utilisateurRepository.findAll());
            return "projets/formulaire_declaration";
        }
    }

    @GetMapping("/modifier/{id}")
    @PreAuthorize("hasAnyRole('CANDIDAT', 'GESTIONNAIRE', 'ADMIN')")
    @Hidden
    public String editer(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model,
            Authentication auth) {
        ProjetDto dto = projetService.findById(id);
        model.addAttribute("projet", dto);
        model.addAttribute("domaines", domaineRepository.findAll());
        model.addAttribute("utilisateurs", utilisateurRepository.findAll());
        return "projets/formulaire_declaration";
    }

    @GetMapping("/details/{id}")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'ADMIN', 'CANDIDAT')")
    @Hidden
    public String voirDetails(@org.springframework.web.bind.annotation.PathVariable("id") Long id, Model model) {
        model.addAttribute("projet", projetService.findById(id));
        return "projets/details";
    }

    @PostMapping("/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    @Hidden
    public String supprimerProjet(@org.springframework.web.bind.annotation.RequestParam("id") Long id,
            Authentication auth) {
        projetService.delete(id);
        auditService.log("PROJECT_DELETE", "Deleted project ID " + id);
        return "redirect:/projets/tous?deleted";
    }
}
