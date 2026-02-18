package com.cartographie.service;

import com.cartographie.repository.ProjetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatistiqueServiceImpl implements IStatistiqueService {

    private final ProjetRepository projetRepository;

    @Override
    public long countAllProjets() {
        return projetRepository.count();
    }

    @Override
    public Double getBudgetTotal() {
        return projetRepository.sumAllBudgets();
    }

    @Override
    public Map<String, Double> getBudgetParDomaine() {
        return projetRepository.findAll().stream()
                .filter(p -> p.getDomaine() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getDomaine().getNom(),
                        Collectors.summingDouble(p -> p.getBudgetEstime() != null ? p.getBudgetEstime() : 0.0)));
    }

    @Override
    public Map<String, Long> getRepartitionParStatut() {
        return projetRepository.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getStatut(), Collectors.counting()));
    }

    @Override
    public Double getTauxAvancementMoyen() {
        return projetRepository.getMoyenneAvancement();
    }

    @Override
    public Map<String, Long> getProjetsParDomaine() {
        return projetRepository.findAll().stream()
                .filter(p -> p.getDomaine() != null)
                .collect(Collectors.groupingBy(p -> p.getDomaine().getNom(), Collectors.counting()));
    }

    @Override
    public Map<String, Long> getProjetsParParticipant() {
        return projetRepository.findAll().stream()
                .filter(p -> p.getCreateur() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCreateur().getPrenom() + " " + p.getCreateur().getNom(),
                        Collectors.counting()));
    }

    @Override
    public Map<Integer, Long> getEvolutionProjets() {
        return projetRepository.findAll().stream()
                .filter(p -> p.getDateDebut() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getDateDebut().getYear(),
                        Collectors.counting()));
    }
}
