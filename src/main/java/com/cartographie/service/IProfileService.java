package com.cartographie.service;

import com.cartographie.dto.UtilisateurDto;

public interface IProfileService {
    UtilisateurDto getConnectedUser(String email);
    void updateProfile(UtilisateurDto dto, String email);
}
