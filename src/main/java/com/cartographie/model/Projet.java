package com.cartographie.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalDate;

//@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "projets")
public class Projet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    @Column(columnDefinition = "TEXT")
    private String description;

    // @Temporal(TemporalType.DATE)
    private LocalDate dateDebut;

    // @Temporal(TemporalType.DATE)
    private LocalDate dateFin;

    private String statut; // En cours, Terminé, Suspendu
    private Double budgetEstime;
    private String institution;
    private Integer niveauAvancement;

    @ManyToOne
    @JoinColumn(name = "id_domaine")
    private Domaine domaine;

    @ManyToOne
    @JoinColumn(name = "id_createur")
    private Utilisateur createur;

    @ManyToOne
    @JoinColumn(name = "id_responsable")
    private Utilisateur responsable;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "projet_participants", joinColumns = @JoinColumn(name = "projet_id"), inverseJoinColumns = @JoinColumn(name = "utilisateur_id"))
    private java.util.List<Utilisateur> participants = new java.util.ArrayList<>();

    public Projet() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Double getBudgetEstime() {
        return budgetEstime;
    }

    public void setBudgetEstime(Double budgetEstime) {
        this.budgetEstime = budgetEstime;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public Integer getNiveauAvancement() {
        return niveauAvancement;
    }

    public void setNiveauAvancement(Integer niveauAvancement) {
        this.niveauAvancement = niveauAvancement;
    }

    public Domaine getDomaine() {
        return domaine;
    }

    public void setDomaine(Domaine domaine) {
        this.domaine = domaine;
    }

    public Utilisateur getCreateur() {
        return createur;
    }

    public void setCreateur(Utilisateur createur) {
        this.createur = createur;
    }

    public Utilisateur getResponsable() {
        return responsable;
    }

    public void setResponsable(Utilisateur responsable) {
        this.responsable = responsable;
    }

    public java.util.List<Utilisateur> getParticipants() {
        return participants;
    }

    public void setParticipants(java.util.List<Utilisateur> participants) {
        this.participants = participants;
    }

}
