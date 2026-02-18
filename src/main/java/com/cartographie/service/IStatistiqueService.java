package com.cartographie.service;

import java.util.Map;

public interface IStatistiqueService {
    long countAllProjets();

    Double getBudgetTotal();

    Map<String, Double> getBudgetParDomaine();

    Map<String, Long> getRepartitionParStatut();

    Double getTauxAvancementMoyen();

    Map<String, Long> getProjetsParDomaine();

    // Nouvelles méthodes pour le dashboard complet
    Map<String, Long> getProjetsParParticipant();

    Map<Integer, Long> getEvolutionProjets(); // Année -> Nombre de projets
}
