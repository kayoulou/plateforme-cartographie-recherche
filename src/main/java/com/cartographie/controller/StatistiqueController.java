package com.cartographie.controller;

import com.cartographie.service.IStatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stats")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GESTIONNAIRE', 'ADMIN')")
@Tag(name = "Statistiques", description = "Tableaux de bord et rapports")
public class StatistiqueController {
    private final IStatistiqueService statService;
    private final com.cartographie.service.PdfService pdfService;

    @GetMapping("/dashboard")
    @Operation(summary = "Afficher le tableau de bord", description = "Retourne la vue avec les statistiques détaillées")
    public String afficherDashboard(Model model) {
        model.addAttribute("totalProjets", statService.countAllProjets());
        model.addAttribute("budgetTotal", statService.getBudgetTotal());
        model.addAttribute("avancementMoyen", statService.getTauxAvancementMoyen());
        model.addAttribute("statsDomaine", statService.getProjetsParDomaine());
        model.addAttribute("statsStatut", statService.getRepartitionParStatut());
        model.addAttribute("statsBudgetDomaine", statService.getBudgetParDomaine());
        model.addAttribute("statsParticipant", statService.getProjetsParParticipant());
        model.addAttribute("statsEvolution", statService.getEvolutionProjets());

        return "stats/dashboard"; // Vers ton fichier HTML
    }

    @GetMapping("/rapport")
    @Operation(summary = "Générer un rapport PDF", description = "Télécharge un rapport complet des projets en PDF")
    public void genererRapport(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("application/pdf");
        java.text.DateFormat dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new java.util.Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=rapport_projets_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);

        pdfService.generateProjectReport(response);
    }
}
