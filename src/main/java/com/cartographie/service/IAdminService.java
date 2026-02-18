package com.cartographie.service;

import com.cartographie.dto.UtilisateurDto;

import java.util.List;

public interface IAdminService {
    List<UtilisateurDto> getAllUsers(String keyword);

    void updateRole(Long userId, String newRole);

    void toggleUserStatus(Long userId);

    void deleteUser(Long userId);

    void updateUser(Long userId, UtilisateurDto userDto);

    void createUser(UtilisateurDto dto);

    void createDomaine(String nom, String description);

    void updateDomaine(Long id, String nom, String description);

    void deleteDomaine(Long id);
}
