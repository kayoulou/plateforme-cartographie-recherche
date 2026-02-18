package com.cartographie.service;

import com.cartographie.dto.UtilisateurDto;
import com.cartographie.model.Utilisateur;
import com.cartographie.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements IProfileService {
    private final UtilisateurRepository userRepo;

    @Override
    public UtilisateurDto getConnectedUser(String email) {
        Utilisateur user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return mapToDto(user);
    }

    @Override
    @Transactional
    public void updateProfile(UtilisateurDto dto, String email) {
        Utilisateur user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        userRepo.save(user);
    }

    private UtilisateurDto mapToDto(Utilisateur user) {
        UtilisateurDto dto = new UtilisateurDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        if(user.getRole() != null) dto.setLibelleRole(user.getRole().getLibelle());
        return dto;
    }
}
