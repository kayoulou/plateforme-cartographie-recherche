package com.cartographie.repository;

import com.cartographie.model.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProjetRepository extends JpaRepository<Projet, Long> {

    List<Projet> findByCreateurEmail(String email);

    List<Projet> findByParticipants_Email(String email);

    @Query("SELECT SUM(p.budgetEstime) FROM Projet p")
    Double sumAllBudgets();

    @Query("SELECT AVG(p.niveauAvancement) FROM Projet p")
    Double getMoyenneAvancement();
}
