package com.cartographie.controller;

import com.cartographie.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/csv")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GESTIONNAIRE', 'ADMIN')") // Restreint aux gestionnaires/admins
public class CsvController {

    private final CsvService csvService;

    @GetMapping("/upload")
    public String showUploadForm() {
        return "csv/upload";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier à importer.");
            return "redirect:/csv/upload";
        }

        try {
            csvService.importProjets(file);
            redirectAttributes.addFlashAttribute("message", "Importation réussie avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'importation : " + e.getMessage());
        }

        return "redirect:/csv/upload";
    }
}
