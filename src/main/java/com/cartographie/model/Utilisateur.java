package com.cartographie.model;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   private String nom;
   private String prenom;

   @Column(unique = true, nullable = false)
   private String email;

   @Column(unique = true)
   private String googleId; // ID unique venant de Keycloak/Google (sub)

   private String motDePasse; // Mot de passe haché pour connexion locale

   @ManyToOne
   @JoinColumn(name = "id_role")
   private Role role;

   @ManyToMany(mappedBy = "participants")
   private java.util.List<Projet> projetsParticipes = new java.util.ArrayList<>();

   @Column(nullable = false)
   private boolean actif = true;

   private String pays;
   private String ville;
   private String genre;

   public Utilisateur() {
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getNom() {
      return nom;
   }

   public void setNom(String nom) {
      this.nom = nom;
   }

   public String getPrenom() {
      return prenom;
   }

   public void setPrenom(String prenom) {
      this.prenom = prenom;
   }

   public String getEmail() {
      return email;
   }

   public void setEmail(String email) {
      this.email = email;
   }

   public String getGoogleId() {
      return googleId;
   }

   public void setGoogleId(String googleId) {
      this.googleId = googleId;
   }

   public String getMotDePasse() {
      return motDePasse;
   }

   public void setMotDePasse(String motDePasse) {
      this.motDePasse = motDePasse;
   }

   public Role getRole() {
      return role;
   }

   public void setRole(Role role) {
      this.role = role;
   }

   public boolean isActif() {
      return actif;
   }

   public void setActif(boolean actif) {
      this.actif = actif;
   }

   public java.util.List<Projet> getProjetsParticipes() {
      return projetsParticipes;
   }

   public void setProjetsParticipes(java.util.List<Projet> projetsParticipes) {
      this.projetsParticipes = projetsParticipes;
   }

   public String getPays() {
      return pays;
   }

   public void setPays(String pays) {
      this.pays = pays;
   }

   public String getVille() {
      return ville;
   }

   public void setVille(String ville) {
      this.ville = ville;
   }

   public String getGenre() {
      return genre;
   }

   public void setGenre(String genre) {
      this.genre = genre;
   }

}
