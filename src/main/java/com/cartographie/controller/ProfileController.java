package com.cartographie.controller;

import com.cartographie.model.Utilisateur;
import com.cartographie.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.cartographie.service.KeycloakSyncService keycloakSyncService;

    @GetMapping("")
    public String viewProfile(Model model, Authentication auth) {
        String email = getEmailFromAuth(auth);
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        model.addAttribute("user", user);
        return "profile/view";
    }

    @GetMapping("/edit")
    public String editProfile(Model model, Authentication auth) {
        String email = getEmailFromAuth(auth);
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        model.addAttribute("user", user);
        return "profile/edit";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute Utilisateur updatedUser, Authentication auth,
            RedirectAttributes redirectAttributes) {
        String email = getEmailFromAuth(auth);
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Only update allowed fields
        user.setPays(updatedUser.getPays());
        user.setVille(updatedUser.getVille());
        user.setGenre(updatedUser.getGenre());
        user.setNom(updatedUser.getNom());
        user.setPrenom(updatedUser.getPrenom());

        utilisateurRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès.");
        return "redirect:/profile";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Les mots de passe ne correspondent pas.");
            return "redirect:/profile";
        }

        String email = getEmailFromAuth(auth);
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Impossible d'identifier l'utilisateur.");
            return "redirect:/profile";
        }

        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // 1. Update Local Password
        user.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(user);

        // 2. Update Keycloak Password
        boolean kcUpdated = keycloakSyncService.updateUserPassword(email, newPassword);

        if (kcUpdated) {
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié avec succès (Local et Keycloak).");
        } else {
            // If Google Auth, password reset might not be allowed or necessary, so this
            // warning is key.
            redirectAttributes.addFlashAttribute("warning",
                    "Mot de passe modifié localement. Mise à jour Keycloak échouée (ou compte externe).");
        }

        return "redirect:/profile";
    }

    private String getEmailFromAuth(Authentication auth) {
        if (auth == null)
            return null;
        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            return ((org.springframework.security.oauth2.core.user.OAuth2User) auth.getPrincipal())
                    .getAttribute("email");
        }
        return auth.getName();
    }
}
