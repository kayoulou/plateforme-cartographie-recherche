package com.cartographie.service;

import com.cartographie.dto.ProjetDto;

import java.util.List;

public interface IProjetService {
    List<ProjetDto> findByUserEmail(String email);

    List<ProjetDto> findAll();

    List<ProjetDto> findAll(String keyword, String statut, String domaine);

    ProjetDto findById(Long id);

    void saveWithUser(ProjetDto dto, String email, boolean isPrivilegedUser);

    void delete(Long id);
}
