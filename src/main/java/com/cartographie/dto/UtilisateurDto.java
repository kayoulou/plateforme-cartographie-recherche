package com.cartographie.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UtilisateurDto {
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Email(message = "Format de l'email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    private String googleId; // Optionnel pour OAuth 2.0

    @NotBlank(message = "Le rôle est obligatoire")
    private String libelleRole;

    private boolean actif;

    private String ancienMotDePasse; // Pour vérification lors du changement
    private String nouveauMotDePasse; // Pour le changement de mot de passe
}
