package com.cartographie.model;

import jakarta.persistence.*;

import java.sql.Date;
import java.time.LocalDate;

@Entity
@Table(name = "participations")
public class Participation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "id_projet")
    private Projet projet;

    //@Temporal(TemporalType.TIMESTAMP)
    private LocalDate dateAssignation;

    public Participation() {}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
    public Projet getProjet() {
        return projet;
    }
    public void setProjet(Projet projet) {
        this.projet = projet;
    }
    public LocalDate getDateAssignation() {
        return dateAssignation;
    }
    public void setDateAssignation(LocalDate dateAssignation) {
        this.dateAssignation = dateAssignation;
    }

}

