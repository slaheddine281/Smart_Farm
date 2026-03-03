package services;

import entities.AnimalProduction;
import entities.Animals;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour AnimalProduction avec jointure objet sur Animals.
 * ✅ MODIFIÉ : Charge l'objet Animals complet via JOIN SQL
 */
public class ServiceAnimalProduction {

    private final Connection conn;

    public ServiceAnimalProduction() {
        conn = MyDatabase.getInstance().getConnection();
    }

    // ══════════════════════════════════════════════
    //  RÉCUPÉRER TOUTES LES PRODUCTIONS (avec JOIN)
    // ══════════════════════════════════════════════

    /**
     * Récupère toutes les productions avec leurs animaux.
     * Utilise un JOIN pour charger l'objet Animals en une seule requête.
     */
    public List<AnimalProduction> recuperer() throws SQLException {
        List<AnimalProduction> list = new ArrayList<>();

        String sql = "SELECT " +
                "  ap.id, ap.animal_id, ap.production_type, ap.quantity, ap.production_date, " +
                "  a.type, a.breed, a.birth_date, a.health_status " +
                "FROM animal_production ap " +
                "INNER JOIN animals a ON ap.animal_id = a.id " +
                "ORDER BY ap.production_date DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // ── Créer l'objet Animals ────────────────
                Animals animal = new Animals(
                        rs.getInt("animal_id"),
                        rs.getString("type"),
                        rs.getString("breed"),
                        rs.getDate("birth_date").toLocalDate(),
                        rs.getString("health_status")
                );

                // ── Créer l'objet AnimalProduction ────────
                AnimalProduction prod = new AnimalProduction(
                        rs.getInt("id"),
                        animal,  // ✅ Objet complet, pas juste l'ID
                        rs.getString("production_type"),
                        rs.getDouble("quantity"),
                        rs.getDate("production_date").toLocalDate()
                );

                list.add(prod);
            }
        }

        return list;
    }

    // ══════════════════════════════════════════════
    //  AJOUTER UNE PRODUCTION
    // ══════════════════════════════════════════════

    public void ajouter(AnimalProduction prod) throws SQLException {
        String sql = "INSERT INTO animal_production (animal_id, production_type, quantity, production_date) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,    prod.getAnimalId());  // Helper qui retourne animal.getId()
            ps.setString(2, prod.getProductionType());
            ps.setDouble(3, prod.getQuantity());
            ps.setDate(4,   Date.valueOf(prod.getProductionDate()));
            ps.executeUpdate();
        }
    }

    // ══════════════════════════════════════════════
    //  MODIFIER UNE PRODUCTION
    // ══════════════════════════════════════════════

    public void modifier(AnimalProduction prod) throws SQLException {
        String sql = "UPDATE animal_production SET " +
                "animal_id = ?, production_type = ?, quantity = ?, production_date = ? " +
                "WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,    prod.getAnimalId());
            ps.setString(2, prod.getProductionType());
            ps.setDouble(3, prod.getQuantity());
            ps.setDate(4,   Date.valueOf(prod.getProductionDate()));
            ps.setInt(5,    prod.getId());
            ps.executeUpdate();
        }
    }

    // ══════════════════════════════════════════════
    //  SUPPRIMER UNE PRODUCTION
    // ══════════════════════════════════════════════

    public void supprimer(AnimalProduction prod) throws SQLException {
        String sql = "DELETE FROM animal_production WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, prod.getId());
            ps.executeUpdate();
        }
    }

    // ══════════════════════════════════════════════
    //  STATISTIQUES
    // ══════════════════════════════════════════════

    public int compterProductions() throws SQLException {
        String sql = "SELECT COUNT(*) FROM animal_production";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double calculerProductionTotaleParType(String type) throws SQLException {
        String sql = "SELECT SUM(quantity) FROM animal_production WHERE production_type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    public double calculerMoyenneProduction(int animalId) throws SQLException {
        String sql = "SELECT AVG(quantity) FROM animal_production WHERE animal_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, animalId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    // ══════════════════════════════════════════════
    //  RÉCUPÉRER UNE PRODUCTION PAR ID (avec JOIN)
    // ══════════════════════════════════════════════

    /**
     * Récupère une production spécifique avec son animal.
     */
    public AnimalProduction recupererParId(int id) throws SQLException {
        String sql = "SELECT " +
                "  ap.id, ap.animal_id, ap.production_type, ap.quantity, ap.production_date, " +
                "  a.type, a.breed, a.birth_date, a.health_status " +
                "FROM animal_production ap " +
                "INNER JOIN animals a ON ap.animal_id = a.id " +
                "WHERE ap.id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Animals animal = new Animals(
                            rs.getInt("animal_id"),
                            rs.getString("type"),
                            rs.getString("breed"),
                            rs.getDate("birth_date").toLocalDate(),
                            rs.getString("health_status")
                    );

                    return new AnimalProduction(
                            rs.getInt("id"),
                            animal,
                            rs.getString("production_type"),
                            rs.getDouble("quantity"),
                            rs.getDate("production_date").toLocalDate()
                    );
                }
            }
        }
        return null;
    }
}