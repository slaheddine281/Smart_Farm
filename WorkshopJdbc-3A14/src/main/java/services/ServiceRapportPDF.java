package services;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import utils.MyDatabase;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ServiceRapportPDF {

    private static final DeviceRgb VERT       = new DeviceRgb(27,  138, 74);
    private static final DeviceRgb VERT_CLAIR = new DeviceRgb(39,  174, 96);
    private static final DeviceRgb VERT_BG    = new DeviceRgb(232, 248, 240);
    private static final DeviceRgb BLEU       = new DeviceRgb(41,  128, 185);
    private static final DeviceRgb BLEU_BG    = new DeviceRgb(235, 245, 251);
    private static final DeviceRgb ORANGE     = new DeviceRgb(230, 126, 34);
    private static final DeviceRgb ORANGE_BG  = new DeviceRgb(254, 243, 224);
    private static final DeviceRgb ROUGE      = new DeviceRgb(192, 57,  43);
    private static final DeviceRgb ROUGE_BG   = new DeviceRgb(253, 237, 236);
    private static final DeviceRgb GRIS       = new DeviceRgb(44,  62,  80);
    private static final DeviceRgb GRIS_CLAIR = new DeviceRgb(248, 249, 250);
    private static final DeviceRgb GRIS_LINE  = new DeviceRgb(220, 230, 220);
    private static final DeviceRgb BLANC      = new DeviceRgb(255, 255, 255);

    private final Connection conn;

    public ServiceRapportPDF() {
        conn = MyDatabase.getInstance().getConnection();
        if (conn == null) throw new RuntimeException("Connexion MySQL impossible.");
    }

    @FunctionalInterface
    public interface ProgressCallback {
        void update(String message, int pct);
    }

    // ══════════════════════════════════════════════
    //  POINT D'ENTREE
    // ══════════════════════════════════════════════

    public String genererRapport(String dossier, ProgressCallback cb) throws Exception {
        notifier(cb, "Collecte des donnees...", 15);

        String nom  = "SmartFarm_Rapport_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".pdf";
        String path = dossier + File.separator + nom;

        notifier(cb, "Generation du PDF...", 50);
        construirePDF(path);

        notifier(cb, "Rapport pret !", 100);
        return path;
    }

    // ══════════════════════════════════════════════
    //  CONSTRUCTION PDF
    // ══════════════════════════════════════════════

    private void construirePDF(String path) throws Exception {
        PdfWriter   writer = new PdfWriter(path);
        PdfDocument pdf    = new PdfDocument(writer);
        Document    doc    = new Document(pdf, PageSize.A4);
        doc.setMargins(0, 45, 50, 45);

        PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        ajouterHeader(doc, bold, normal);
        ajouterInfoRapport(doc, bold, normal);
        ajouterSectionAnimaux(doc, bold, normal);
        doc.add(new Paragraph("\n"));
        ajouterSectionProductions(doc, bold, normal);
        ajouterFooter(pdf, normal);

        doc.close();
    }

    // ── Header vert ──────────────────────────────
    private void ajouterHeader(Document doc, PdfFont bold, PdfFont normal) throws Exception {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth().setMargin(0);

        Cell cell = new Cell()
                .add(new Paragraph("SMART FARM")
                        .setFont(bold).setFontSize(30)
                        .setFontColor(BLANC).setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(4))
                .add(new Paragraph("Rapport Mensuel — Animaux & Productions")
                        .setFont(normal).setFontSize(13)
                        .setFontColor(new DeviceRgb(200, 240, 215))
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(VERT)
                .setPaddingTop(35).setPaddingBottom(30)
                .setBorder(new SolidBorder(VERT, 0));
        header.addCell(cell);
        doc.add(header);
    }

    // ── Infos rapport ────────────────────────────
    private void ajouterInfoRapport(Document doc, PdfFont bold, PdfFont normal) throws Exception {
        String date = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy",
                        java.util.Locale.FRENCH));

        int nbAnimaux   = compter("animals");
        int nbProds     = compter("animal_production");
        int nbMalades   = compterMalades();

        doc.add(new Paragraph("").setMarginTop(20));

        // Ligne d'infos
        Table info = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                .useAllAvailableWidth().setMarginBottom(20);

        info.addCell(carteKPI("Date", date, VERT, VERT_BG, bold, normal));
        info.addCell(carteKPI("Total animaux", String.valueOf(nbAnimaux), BLEU, BLEU_BG, bold, normal));
        info.addCell(carteKPI("Animaux a surveiller", String.valueOf(nbMalades),
                nbMalades > 0 ? ORANGE : VERT,
                nbMalades > 0 ? ORANGE_BG : VERT_BG, bold, normal));
        doc.add(info);
    }

    // ── Section animaux ──────────────────────────
    private void ajouterSectionAnimaux(Document doc, PdfFont bold, PdfFont normal) throws Exception {
        doc.add(titreSectionElement("LISTE DES ANIMAUX — ETAT DE SANTE", bold));

        Table table = new Table(UnitValue.createPercentArray(new float[]{0.5f, 1.2f, 1.5f, 1.5f, 1.8f}))
                .useAllAvailableWidth();

        // En-têtes
        String[] headers = {"#", "Type", "Race", "Naissance", "Etat de sante"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFont(bold).setFontSize(10)
                            .setFontColor(BLANC).setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(VERT)
                    .setPadding(9)
                    .setBorder(new SolidBorder(VERT, 0)));
        }

        // Données
        String sql = "SELECT id, type, breed, birth_date, health_status " +
                "FROM animals ORDER BY type, id";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int row = 0;
            while (rs.next()) {
                DeviceRgb bg = row % 2 == 0 ? BLANC : GRIS_CLAIR;
                String statut = rs.getString("health_status");
                DeviceRgb couleurStatut = getCouleurStatut(statut);
                DeviceRgb bgStatut     = getBgStatut(statut);

                table.addCell(cellule(String.valueOf(rs.getInt("id")),
                        normal, 9, bg, TextAlignment.CENTER));
                table.addCell(cellule(rs.getString("type"),
                        bold,   10, bg, TextAlignment.LEFT));
                table.addCell(cellule(rs.getString("breed"),
                        normal, 9,  bg, TextAlignment.LEFT));
                table.addCell(cellule(
                        rs.getDate("birth_date").toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        normal, 9, bg, TextAlignment.CENTER));

                // Cellule statut colorée
                table.addCell(new Cell()
                        .add(new Paragraph(statut)
                                .setFont(bold).setFontSize(9)
                                .setFontColor(couleurStatut)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBackgroundColor(bgStatut)
                        .setPadding(8)
                        .setBorder(new SolidBorder(GRIS_LINE, 0.5f)));
                row++;
            }
        }
        doc.add(table);

        // Légende
        doc.add(new Paragraph("  Vert = Bonne sante / Excellente     "
                + "Orange = Sous surveillance     Rouge = Malade / Traitement")
                .setFont(normal).setFontSize(8)
                .setFontColor(new DeviceRgb(127, 140, 141))
                .setItalic().setMarginTop(6));
    }

    // ── Section productions ──────────────────────
    private void ajouterSectionProductions(Document doc, PdfFont bold, PdfFont normal) throws Exception {
        doc.add(titreSectionElement("PRODUCTIONS DU MOIS EN COURS", bold));

        // Stats globales par type
        Table stats = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                .useAllAvailableWidth().setMarginBottom(16);

        String sqlStats = "SELECT production_type, " +
                "SUM(quantity) as total, AVG(quantity) as moy, COUNT(*) as nb " +
                "FROM animal_production " +
                "WHERE production_date >= DATE_FORMAT(CURDATE(),'%Y-%m-01') " +
                "GROUP BY production_type ORDER BY total DESC";

        boolean hasData = false;
        try (PreparedStatement ps = conn.prepareStatement(sqlStats);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasData = true;
                String type  = rs.getString("production_type");
                double total = rs.getDouble("total");
                stats.addCell(carteKPI(
                        type,
                        String.format("%.1f", total),
                        BLEU, BLEU_BG, bold, normal));
            }
        }

        if (!hasData) {
            doc.add(new Paragraph("Aucune production enregistree ce mois-ci.")
                    .setFont(normal).setFontSize(11)
                    .setFontColor(new DeviceRgb(127, 140, 141))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(20));
            return;
        }
        doc.add(stats);

        // Tableau détaillé
        Table table = new Table(UnitValue.createPercentArray(new float[]{0.5f, 1.2f, 1.5f, 1.2f, 1.2f}))
                .useAllAvailableWidth();

        String[] headers = {"#", "Animal", "Type production", "Quantite", "Date"};
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFont(bold).setFontSize(10)
                            .setFontColor(BLANC).setTextAlignment(TextAlignment.CENTER))
                    .setBackgroundColor(BLEU)
                    .setPadding(9)
                    .setBorder(new SolidBorder(BLEU, 0)));
        }

        String sqlDetail = "SELECT ap.id, a.type, a.breed, ap.production_type, " +
                "ap.quantity, ap.production_date " +
                "FROM animal_production ap " +
                "INNER JOIN animals a ON ap.animal_id = a.id " +
                "WHERE ap.production_date >= DATE_FORMAT(CURDATE(),'%Y-%m-01') " +
                "ORDER BY ap.production_date DESC, ap.id DESC";

        try (PreparedStatement ps = conn.prepareStatement(sqlDetail);
             ResultSet rs = ps.executeQuery()) {
            int row = 0;
            while (rs.next()) {
                DeviceRgb bg = row % 2 == 0 ? BLANC : GRIS_CLAIR;
                table.addCell(cellule(String.valueOf(rs.getInt("id")),
                        normal, 9, bg, TextAlignment.CENTER));
                table.addCell(cellule(rs.getString("type") + "\n(" + rs.getString("breed") + ")",
                        normal, 9, bg, TextAlignment.LEFT));
                table.addCell(cellule(rs.getString("production_type"),
                        bold,   10, bg, TextAlignment.LEFT));
                table.addCell(cellule(String.format("%.2f", rs.getDouble("quantity")),
                        normal, 9, bg, TextAlignment.CENTER));
                table.addCell(cellule(
                        rs.getDate("production_date").toLocalDate()
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        normal, 9, bg, TextAlignment.CENTER));
                row++;
            }
        }
        doc.add(table);
    }

    // ── Footer ───────────────────────────────────
    private void ajouterFooter(PdfDocument pdf, PdfFont normal) throws Exception {
        int nb = pdf.getNumberOfPages();
        for (int i = 1; i <= nb; i++) {
            PdfCanvas canvas = new PdfCanvas(pdf.getPage(i));
            canvas.setFillColor(VERT)
                    .rectangle(0, 0, PageSize.A4.getWidth(), 28)
                    .fill();
            canvas.release();

            new com.itextpdf.layout.Canvas(pdf.getPage(i), pdf.getPage(i).getPageSize())
                    .showTextAligned(
                            new Paragraph("Smart Farm  |  Rapport genere le " +
                                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) +
                                    "  |  Page " + i + " / " + nb)
                                    .setFont(normal).setFontSize(8)
                                    .setFontColor(new DeviceRgb(200, 240, 215)),
                            PageSize.A4.getWidth() / 2, 10,
                            i, TextAlignment.CENTER, VerticalAlignment.BOTTOM, 0)
                    .close();
        }
    }

    // ══════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════

    private Paragraph titreSectionElement(String texte, PdfFont bold) throws Exception {
        return new Paragraph(texte)
                .setFont(bold).setFontSize(12)
                .setFontColor(BLANC)
                .setBackgroundColor(VERT)
                .setPadding(11).setPaddingLeft(16)
                .setMarginBottom(0).setMarginTop(10);
    }

    private Cell carteKPI(String label, String valeur, DeviceRgb couleur,
                          DeviceRgb bg, PdfFont bold, PdfFont normal) throws Exception {
        return new Cell()
                .add(new Paragraph(valeur).setFont(bold).setFontSize(20)
                        .setFontColor(couleur).setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(2))
                .add(new Paragraph(label).setFont(normal).setFontSize(9)
                        .setFontColor(new DeviceRgb(100, 110, 120))
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(bg).setPadding(14)
                .setBorder(new SolidBorder(couleur, 1.5f));
    }

    private Cell cellule(String texte, PdfFont font, float size,
                         DeviceRgb bg, TextAlignment align) throws Exception {
        return new Cell()
                .add(new Paragraph(texte).setFont(font).setFontSize(size)
                        .setFontColor(GRIS).setTextAlignment(align))
                .setBackgroundColor(bg).setPadding(8)
                .setBorder(new SolidBorder(GRIS_LINE, 0.5f));
    }

    private DeviceRgb getCouleurStatut(String s) {
        s = s.toLowerCase();
        if (s.contains("bonne") || s.contains("excellen")) return VERT;
        if (s.contains("surveil"))                          return ORANGE;
        return ROUGE;
    }

    private DeviceRgb getBgStatut(String s) {
        s = s.toLowerCase();
        if (s.contains("bonne") || s.contains("excellen")) return VERT_BG;
        if (s.contains("surveil"))                          return ORANGE_BG;
        return ROUGE_BG;
    }

    private int compter(String table) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM " + table);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    private int compterMalades() {
        String sql = "SELECT COUNT(*) FROM animals WHERE " +
                "LOWER(health_status) NOT LIKE '%bonne%' AND " +
                "LOWER(health_status) NOT LIKE '%excellen%'";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { return 0; }
    }

    private void notifier(ProgressCallback cb, String msg, int pct) {
        if (cb != null) cb.update(msg, pct);
    }
}