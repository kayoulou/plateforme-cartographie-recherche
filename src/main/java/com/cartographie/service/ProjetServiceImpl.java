package com.cartographie.service;

import com.cartographie.dto.ProjetDto;
import com.cartographie.model.Projet;
import com.cartographie.model.Utilisateur;
import com.cartographie.repository.ProjetRepository;
import com.cartographie.repository.UtilisateurRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjetServiceImpl implements IProjetService {
    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final com.cartographie.repository.DomaineRepository domaineRepository;

    // Constructeur manuel pour l'injection (Solution à l'erreur d'initialisation)
    public ProjetServiceImpl(ProjetRepository projetRepository, UtilisateurRepository utilisateurRepository,
            com.cartographie.repository.DomaineRepository domaineRepository) {
        this.projetRepository = projetRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.domaineRepository = domaineRepository;
    }

    @Override
    public List<ProjetDto> findByUserEmail(String email) {
        List<Projet> created = projetRepository.findByCreateurEmail(email);
        List<Projet> participated = projetRepository.findByParticipants_Email(email);

        java.util.Set<Projet> uniqueProjets = new java.util.HashSet<>(created);
        uniqueProjets.addAll(participated);

        return uniqueProjets.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjetDto> findAll() {
        return projetRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjetDto> findAll(String keyword, String statut, String domaine) {
        return projetRepository.findAll().stream()
                .filter(p -> {
                    // Filter by keyword (Titre or Description or Createur)
                    if (keyword != null && !keyword.isEmpty()) {
                        String k = keyword.toLowerCase();
                        boolean matchTitre = p.getTitre() != null && p.getTitre().toLowerCase().contains(k);
                        boolean matchDesc = p.getDescription() != null && p.getDescription().toLowerCase().contains(k);
                        boolean matchCreateur = p.getCreateur() != null &&
                                (p.getCreateur().getNom().toLowerCase().contains(k)
                                        || p.getCreateur().getPrenom().toLowerCase().contains(k));
                        if (!matchTitre && !matchDesc && !matchCreateur)
                            return false;
                    }

                    // Filter by statut
                    if (statut != null && !statut.isEmpty() && !statut.equals("Tous les Statuts")) {
                        if (p.getStatut() == null || !p.getStatut().equalsIgnoreCase(statut))
                            return false;
                    }

                    // Filter by domaine
                    if (domaine != null && !domaine.isEmpty() && !domaine.equals("Tous les Domaines")) {
                        if (p.getDomaine() == null || !p.getDomaine().getNom().equalsIgnoreCase(domaine))
                            return false;
                    }

                    return true;
                })
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjetDto findById(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
        return mapToDto(projet);
    }

    @Override
    @Transactional
    public void saveWithUser(ProjetDto dto, String email, boolean isPrivilegedUser) {
        Utilisateur createur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        com.cartographie.model.Domaine domaine = null;
        if (dto.getDomaineId() != null) {
            domaine = domaineRepository.findById(dto.getDomaineId())
                    .orElseThrow(() -> new RuntimeException("Domaine non trouvé"));
        }

        Projet projet;
        if (dto.getId() != null) {
            projet = projetRepository.findById(dto.getId())
                    .orElseThrow(() -> new RuntimeException("Projet introuvable pour mise à jour"));

            // Verify ownership: Only enforce if NOT privileged
            if (!isPrivilegedUser && !projet.getCreateur().getEmail().equals(email)) {
                throw new RuntimeException("Accès refusé : Vous n'êtes pas le créateur de ce projet");
            }
        } else {
            projet = new Projet();
            projet.setCreateur(createur); // Set creator only on creation
        }

        if (dto.getResponsableId() != null) {
            Utilisateur responsable = utilisateurRepository.findById(dto.getResponsableId())
                    .orElseThrow(() -> new RuntimeException("Responsable non trouvé"));
            projet.setResponsable(responsable);
        }

        if (isPrivilegedUser && dto.getParticipantIds() != null) {
            List<Utilisateur> participants = utilisateurRepository.findAllById(dto.getParticipantIds());
            projet.setParticipants(participants);
        }

        projet.setTitre(dto.getTitre());
        projet.setDescription(dto.getDescription());
        projet.setStatut(dto.getStatut());
        projet.setBudgetEstime(dto.getBudgetEstime());
        projet.setNiveauAvancement(dto.getNiveauAvancement());
        projet.setInstitution(dto.getInstitution());
        projet.setDateDebut(dto.getDateDebut());
        projet.setDateFin(dto.getDateFin());
        projet.setDomaine(domaine);

        projetRepository.save(projet);
    }

    private ProjetDto mapToDto(Projet projet) {
        ProjetDto dto = new ProjetDto();
        dto.setId(projet.getId());
        dto.setTitre(projet.getTitre());
        dto.setDescription(projet.getDescription());
        dto.setStatut(projet.getStatut());
        dto.setBudgetEstime(projet.getBudgetEstime());
        dto.setNiveauAvancement(projet.getNiveauAvancement());
        dto.setInstitution(projet.getInstitution());
        dto.setDateDebut(projet.getDateDebut());
        dto.setDateFin(projet.getDateFin());
        if (projet.getDomaine() != null) {
            dto.setDomaineId(projet.getDomaine().getId());
            dto.setDomaineNom(projet.getDomaine().getNom());
        }
        if (projet.getCreateur() != null) {
            dto.setCreateurNom(projet.getCreateur().getNom());
            dto.setCreateurPrenom(projet.getCreateur().getPrenom());
        }
        if (projet.getResponsable() != null) {
            dto.setResponsableId(projet.getResponsable().getId());
            dto.setResponsableNom(projet.getResponsable().getNom() + " " + projet.getResponsable().getPrenom());
        }

        if (projet.getParticipants() != null) {
            dto.setParticipantIds(
                    projet.getParticipants().stream().map(Utilisateur::getId).collect(Collectors.toList()));
            dto.setParticipantNames(
                    projet.getParticipants().stream()
                            .map(u -> u.getPrenom() + " " + u.getNom() + " (" + u.getEmail() + ")")
                            .collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        projetRepository.deleteById(id);
    }
}
