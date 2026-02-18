package com.cartographie.config;

import com.cartographie.model.Role;
import com.cartographie.model.Domaine;
import com.cartographie.model.Utilisateur;
import com.cartographie.repository.RoleRepository;
import com.cartographie.repository.DomaineRepository;
import com.cartographie.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final DomaineRepository domaineRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize Roles
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(Arrays.asList(
                    new Role("ADMIN"),
                    new Role("GESTIONNAIRE"),
                    new Role("CANDIDAT")));
            System.out.println("DEBUG: Roles initialized.");
        }

        // Initialize Domains if empty
        if (domaineRepository.count() == 0) {
            domaineRepository.saveAll(Arrays.asList(
                    new Domaine("Informatique", "Développement, IA, Réseaux"),
                    new Domaine("Santé", "Médecine, Biologie, Pharmacie"),
                    new Domaine("Agriculture", "Agronomie, Elevage, Pêche")));
            System.out.println("DEBUG: Default domains initialized.");
        }

        // Initialize Default Users
        // Initialize Default Users
        createDefaultUserIfMissing("ADMIN", "admin@carto.com", "admin");
        createDefaultUserIfMissing("GESTIONNAIRE", "gestionnaire@carto.com", "gestionnaire");
        createDefaultUserIfMissing("CANDIDAT", "candidat@carto.com", "candidat");

        // Initialize Specific Requested Users
        createSpecificUserIfMissing("ADMIN", "admin@esmt.sn", "admin", "COMPAORE", "Loîc");
        createSpecificUserIfMissing("GESTIONNAIRE", "gestionnaire@esmt.sn", "gestionnaire", "SAWADOGO", "Jordan");
        createSpecificUserIfMissing("CANDIDAT", "candidat@esmt.sn", "candidat", "OUEDRAOGO", "Josée");
    }

    private void createDefaultUserIfMissing(String roleName, String email, String password) {
        if (utilisateurRepository.findByEmail(email).isEmpty()) {
            Utilisateur user = new Utilisateur();
            user.setNom(roleName);
            user.setPrenom("Test");
            user.setEmail(email);
            user.setMotDePasse(passwordEncoder.encode(password));
            user.setActif(true);

            roleRepository.findByLibelle(roleName).ifPresent(user::setRole);

            utilisateurRepository.save(user);
            System.out.println("DEBUG: Created default user: " + email);
        }
    }

    private void createSpecificUserIfMissing(String roleName, String email, String password, String nom,
            String prenom) {
        if (utilisateurRepository.findByEmail(email).isEmpty()) {
            Utilisateur user = new Utilisateur();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMotDePasse(passwordEncoder.encode(password));
            user.setActif(true);

            roleRepository.findByLibelle(roleName).ifPresent(user::setRole);

            utilisateurRepository.save(user);
            System.out.println("DEBUG: Created specific user: " + email);
        }
    }
}
