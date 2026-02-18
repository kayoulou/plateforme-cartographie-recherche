package com.cartographie.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjetDto {
    private Long id;

    private boolean actif;
    private Long responsableId;
    private String responsableNom;
    @Builder.Default
    private java.util.List<Long> participantIds = new java.util.ArrayList<>();
    @Builder.Default
    private java.util.List<String> participantNames = new java.util.ArrayList<>();

    @NotBlank(message = "Le titre du projet est obligatoire")
    private String titre;

    @NotBlank(message = "La description ne peut pas être vide")
    private String description;

    @NotBlank(message = "Le statut est obligatoire (En cours, Terminé, Suspendu)")
    private String statut;

    @NotNull(message = "Le budget est obligatoire")
    @PositiveOrZero(message = "Le budget doit être un nombre positif")
    private Double budgetEstime;

    @Min(value = 0, message = "L'avancement doit être au moins de 0%")
    @Max(value = 100, message = "L'avancement ne peut pas dépasser 100%")
    private Integer niveauAvancement;

    @NotBlank(message = "L'institution est obligatoire")
    private String institution;

    @NotNull(message = "Le domaine de recherche est obligatoire")
    private Long domaineId;

    private String domaineNom; // Pour l'affichage

    @NotNull(message = "La date de début est obligatoire")
    private java.time.LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private java.time.LocalDate dateFin;

    private String createurNom;
    private String createurPrenom;
}
