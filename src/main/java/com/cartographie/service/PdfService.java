package com.cartographie.service;

import com.cartographie.dto.ProjetDto;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final IProjetService projetService;

    public void generateProjectReport(HttpServletResponse response) throws IOException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // Titre
        Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitle.setSize(18);
        fontTitle.setColor(Color.BLUE);

        Paragraph paragraph = new Paragraph("Rapport Global des Projets de Recherche", fontTitle);
        paragraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(paragraph);
        document.add(new Paragraph(" ")); // Espace

        // Table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100f);
        table.setWidths(new float[] { 3.5f, 2.0f, 2.0f, 1.5f, 1.5f });
        table.setSpacingBefore(10);

        // Header
        writeTableHeader(table);

        // Data
        List<ProjetDto> projets = projetService.findAll();
        writeTableData(table, projets);

        document.add(table);
        document.close();
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);

        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        cell.setPhrase(new Phrase("Titre du Projet", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Domaine", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Responsable", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Budget", font));
        table.addCell(cell);

        cell.setPhrase(new Phrase("Statut", font));
        table.addCell(cell);
    }

    private void writeTableData(PdfPTable table, List<ProjetDto> projets) {
        for (ProjetDto projet : projets) {
            table.addCell(projet.getTitre());
            table.addCell(projet.getDomaineNom() != null ? projet.getDomaineNom() : "N/A");

            String responsable = "Inconnu";
            if (projet.getCreateurNom() != null) {
                responsable = projet.getCreateurPrenom() + " " + projet.getCreateurNom();
            }
            table.addCell(responsable);

            String budget = projet.getBudgetEstime() != null ? String.format("%,.0f FCFA", projet.getBudgetEstime())
                    : "0 FCFA";
            table.addCell(budget);

            table.addCell(projet.getStatut());
        }
    }
}
