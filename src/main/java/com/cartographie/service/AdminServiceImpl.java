package com.cartographie.service;

import com.cartographie.dto.UtilisateurDto;
import com.cartographie.model.Domaine;
import com.cartographie.model.Role;
import com.cartographie.model.Utilisateur;
import com.cartographie.repository.DomaineRepository;
import com.cartographie.repository.RoleRepository;
import com.cartographie.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements IAdminService {
    private final UtilisateurRepository userRepo;
    private final DomaineRepository domaineRepo;
    private final RoleRepository roleRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public List<UtilisateurDto> getAllUsers(String keyword) {
        List<Utilisateur> users;
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepo.searchUsers(keyword.trim());
        } else {
            users = userRepo.findAll();
        }
        return users.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateRole(Long userId, String newRoleLibelle) {
        // 1. Trouver l'utilisateur
        Utilisateur user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // 2. Trouver l'objet Role correspondant à la String
        Role role = roleRepo.findByLibelle(newRoleLibelle)
                .orElseThrow(() -> new RuntimeException("Rôle " + newRoleLibelle + " inexistant en base"));

        // 3. Assigner l'objet Role (et non la String)
        user.setRole(role);
        userRepo.save(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long userId) {
        Utilisateur user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setActif(!user.isActif());
        userRepo.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }

    @Override
    public void updateUser(Long userId, UtilisateurDto dto) {
        Utilisateur user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        userRepo.save(user);
    }

    @Override
    @Transactional
    public void createUser(UtilisateurDto dto) {
        Utilisateur user = new Utilisateur();
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setEmail(dto.getEmail());
        user.setActif(true);
        user.setMotDePasse(passwordEncoder.encode("12345678"));

        if (dto.getLibelleRole() != null) {
            roleRepo.findByLibelle(dto.getLibelleRole())
                    .ifPresent(user::setRole);
        } else {
            roleRepo.findByLibelle("CANDIDAT").ifPresent(user::setRole);
        }

        userRepo.save(user);
    }

    @Override
    public void createDomaine(String nom, String description) {
        Domaine domaine = new Domaine();
        domaine.setNom(nom);
        domaine.setDescription(description);
        domaineRepo.save(domaine);
    }

    @Override
    public void updateDomaine(Long id, String nom, String description) {
        Domaine domaine = domaineRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Domaine non trouvé"));
        domaine.setNom(nom);
        domaine.setDescription(description);
        domaineRepo.save(domaine);
    }

    @Override
    public void deleteDomaine(Long id) {
        domaineRepo.deleteById(id);
    }

    private UtilisateurDto mapToDto(Utilisateur user) {
        UtilisateurDto dto = new UtilisateurDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setGoogleId(user.getGoogleId());

        // SOLUTION : On récupère le libellé (String) au lieu de l'objet Role complet
        if (user.getRole() != null) {
            dto.setLibelleRole(user.getRole().getLibelle());
        }
        dto.setActif(user.isActif());

        return dto;
    }
}
