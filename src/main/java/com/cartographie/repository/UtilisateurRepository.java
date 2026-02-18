package com.cartographie.repository;

import com.cartographie.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM Utilisateur u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    java.util.List<Utilisateur> searchUsers(@org.springframework.data.repository.query.Param("keyword") String keyword);
}
