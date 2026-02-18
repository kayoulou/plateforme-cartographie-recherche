package com.cartographie.repository;

import com.cartographie.model.Domaine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomaineRepository extends JpaRepository<Domaine, Long> {
    java.util.Optional<Domaine> findByNom(String nom);
}
