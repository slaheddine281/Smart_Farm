package services;

import utils.MyDatabase;
import java.sql.*;
import java.util.*;

/**
 * Service de statistiques avancées pour Smart Farm.
 */
public class ServiceStatistiques {

    private final Connection conn;

    public ServiceStatistiques() {
        conn = MyDatabase.getInstance().getConnection();
        if (conn == null) throw new RuntimeException("Connexion MySQL impossible.");
    }

    // ── KPI généraux ────────────────────────────────────────

    public int getTotalProductions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM animal_production";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getTotalAnimaux() throws SQLException {
        String sql = "SELECT COUNT(*) FROM animals";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double getTotalParType(String type) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM animal_production WHERE production_type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    /** Moyenne de production par jour (tous types confondus) */
    public double getMoyenneParJour() throws SQLException {
        String sql = "SELECT COALESCE(AVG(qty_jour), 0) FROM " +
                "(SELECT production_date, SUM(quantity) as qty_jour " +
                " FROM animal_production GROUP BY production_date) AS daily";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    // ── Santé des animaux ────────────────────────────────────

    public Map<String, Integer> getSanteStats() throws SQLException {
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("bon", 0);
        stats.put("surveillance", 0);
        stats.put("malade", 0);

        String sql = "SELECT health_status, COUNT(*) as nb FROM animals GROUP BY health_status";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String s = rs.getString("health_status").toLowerCase();
                int nb   = rs.getInt("nb");
                if (s.contains("bonne") || s.contains("excellente")) {
                    stats.put("bon", stats.get("bon") + nb);
                } else if (s.contains("surveillance")) {
                    stats.put("surveillance", stats.get("surveillance") + nb);
                } else {
                    stats.put("malade", stats.get("malade") + nb);
                }
            }
        }
        return stats;
    }

    // ── Top 5 animaux producteurs ────────────────────────────

    /** Retourne [ [animalInfo, prodType, totalQty], ... ] */
    public List<Object[]> getTop5Animaux() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT a.id, a.type, a.breed, ap.production_type, SUM(ap.quantity) as total " +
                "FROM animal_production ap " +
                "INNER JOIN animals a ON ap.animal_id = a.id " +
                "GROUP BY a.id, a.type, a.breed, ap.production_type " +
                "ORDER BY total DESC LIMIT 5";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int rank = 1;
            while (rs.next()) {
                list.add(new Object[]{
                        rank++,
                        rs.getString("type") + " - " + rs.getString("breed") + " (#" + rs.getInt("id") + ")",
                        rs.getString("production_type"),
                        String.format("%.2f", rs.getDouble("total"))
                });
            }
        }
        return list;
    }

    // ── Production par type d'animal ─────────────────────────

    /** Retourne [ [typeAnimal, typeProd, totalQty, nbEntrees], ... ] */
    public List<Object[]> getProdParTypeAnimal() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT a.type as animal_type, ap.production_type, " +
                "SUM(ap.quantity) as total, COUNT(*) as nb " +
                "FROM animal_production ap " +
                "INNER JOIN animals a ON ap.animal_id = a.id " +
                "GROUP BY a.type, ap.production_type " +
                "ORDER BY a.type, total DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("animal_type"),
                        rs.getString("production_type"),
                        String.format("%.2f", rs.getDouble("total")),
                        rs.getInt("nb")
                });
            }
        }
        return list;
    }

    // ── 5 dernières productions ──────────────────────────────

    public List<Object[]> getDernieresProductions() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT a.type, a.breed, ap.production_type, ap.quantity, ap.production_date " +
                "FROM animal_production ap " +
                "INNER JOIN animals a ON ap.animal_id = a.id " +
                "ORDER BY ap.production_date DESC, ap.id DESC LIMIT 5";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getString("type") + " (" + rs.getString("breed") + ")",
                        rs.getString("production_type"),
                        String.format("%.2f", rs.getDouble("quantity")),
                        rs.getDate("production_date").toLocalDate().toString()
                });
            }
        }
        return list;
    }

    // ── Chiffre d'affaires ───────────────────────────────────

    private static final Map<String, Double> PRIX = Map.of(
            "Lait",   2.50,
            "Œufs",  0.30,
            "Laine",  8.00,
            "Viande", 12.00
    );

    /** Retourne [ [produit, qtyVendue, prixUnit, caTotal], ... ] */
    public List<Object[]> getCAParProduit() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT ci.production_type, SUM(ci.quantite_achat) as qty_vendue " +
                "FROM commande_items ci GROUP BY ci.production_type ORDER BY qty_vendue DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("production_type");
                double qty  = rs.getDouble("qty_vendue");
                double prix = PRIX.getOrDefault(type, 1.0);
                list.add(new Object[]{
                        type,
                        String.format("%.2f", qty),
                        String.format("%.2f €", prix),
                        String.format("%.2f €", qty * prix)
                });
            }
        }
        return list;
    }

    public double getCaTotal() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_prix), 0) FROM commandes";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public int getNbCommandes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM commandes";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double getPanierMoyen() throws SQLException {
        String sql = "SELECT COALESCE(AVG(total_prix), 0) FROM commandes";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }

    public int getTotalCommandes() throws SQLException {
        return getNbCommandes();
    }
}