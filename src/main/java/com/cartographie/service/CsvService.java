package com.cartographie.service;

import com.cartographie.model.Domaine;
import com.cartographie.model.Projet;
import com.cartographie.repository.DomaineRepository;
import com.cartographie.repository.ProjetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvService {

    private final ProjetRepository projetRepository;
    private final DomaineRepository domaineRepository;

    public void importProjets(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            List<Projet> projets = new ArrayList<>();
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                // Skip header
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] data = line.split(";");
                if (data.length >= 2) { // Allow minimal data
                    Projet projet = new Projet();
                    projet.setTitre(data[0]);
                    projet.setDescription(data.length > 1 ? data[1] : "");

                    // Gestion Domaine (création si inexistant pour simplifier)
                    if (data.length > 2 && !data[2].isEmpty()) {
                        String nomDomaine = data[2].trim();
                        Domaine domaine = domaineRepository.findByNom(nomDomaine)
                                .orElseGet(() -> domaineRepository.save(new Domaine(nomDomaine, "")));
                        projet.setDomaine(domaine);
                    }

                    // Dates (Format simple YYYY-MM-DD attendu, sinon null/today)
                    try {
                        projet.setDateDebut(
                                data.length > 3 && !data[3].isEmpty() ? LocalDate.parse(data[3]) : LocalDate.now());
                        projet.setDateFin(data.length > 4 && !data[4].isEmpty() ? LocalDate.parse(data[4]) : null);
                    } catch (Exception e) {
                        projet.setDateDebut(LocalDate.now()); // Fallback
                    }

                    projet.setStatut(data.length > 5 ? data[5] : "En cours");

                    try {
                        projet.setBudgetEstime(
                                data.length > 6 && !data[6].isEmpty() ? Double.parseDouble(data[6]) : 0.0);
                        projet.setNiveauAvancement(
                                data.length > 7 && !data[7].isEmpty() ? Integer.parseInt(data[7]) : 0);
                    } catch (NumberFormatException e) {
                        projet.setBudgetEstime(0.0);
                        projet.setNiveauAvancement(0);
                    }

                    projet.setInstitution(data.length > 8 ? data[8] : "Non spécifié");

                    projets.add(projet);
                }
            }

            projetRepository.saveAll(projets);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'importation du fichier CSV: " + e.getMessage());
        }
    }
}
