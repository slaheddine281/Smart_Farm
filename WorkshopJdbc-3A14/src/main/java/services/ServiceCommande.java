package services;

import entities.Commande;
import entities.CommandeItem;
import utils.MyDatabase;

import java.sql.*;

/**
 * Service pour enregistrer une commande en BDD
 * et décrémenter la quantité dans animal_production.
 */
public class ServiceCommande {

    private final Connection conn;

    public ServiceCommande() {
        conn = MyDatabase.getInstance().getConnection();
    }

    // ══════════════════════════════════════════════
    //  ENREGISTRER UNE COMMANDE COMPLÈTE
    // ══════════════════════════════════════════════

    /**
     * 1. Insère l'entête dans `commandes`
     * 2. Insère chaque ligne dans `commande_items`
     * 3. Décrémente la quantité dans `animal_production`
     * Tout est fait dans une transaction — si une étape échoue, tout est annulé.
     */
    public void enregistrerCommande(Commande commande) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // ── Étape 1 : Insérer l'entête commande ──
            int commandeId = insertCommande(commande);

            // ── Étape 2 : Insérer les items + décrémenter stock ──
            for (CommandeItem item : commande.getItems()) {
                insertCommandeItem(commandeId, item);
                decrementerProduction(item.getProductionId(), item.getQuantiteAchat());
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── Insérer entête ───────────────────────────
    private int insertCommande(Commande commande) throws SQLException {
        String sql = "INSERT INTO commandes (visiteur_nom, date_commande, total_prix, statut) " +
                "VALUES (?, NOW(), ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, commande.getVisiteurNom());
            ps.setDouble(2, commande.getTotalPrix());
            ps.setString(3, commande.getStatut());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("Impossible de récupérer l'ID de la commande.");
        }
    }

    // ── Insérer une ligne ────────────────────────
    private void insertCommandeItem(int commandeId, CommandeItem item) throws SQLException {
        String sql = "INSERT INTO commande_items " +
                "(commande_id, production_id, production_type, quantite_achat, prix_unitaire, sous_total) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,    commandeId);
            ps.setInt(2,    item.getProductionId());
            ps.setString(3, item.getProductionType());
            ps.setDouble(4, item.getQuantiteAchat());
            ps.setDouble(5, item.getPrixUnitaire());
            ps.setDouble(6, item.getSousTotal());
            ps.executeUpdate();
        }
    }

    // ── Décrémenter le stock ─────────────────────
    private void decrementerProduction(int productionId, double quantite) throws SQLException {
        String sql = "UPDATE animal_production SET quantity = quantity - ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, quantite);
            ps.setInt(2,    productionId);
            ps.executeUpdate();
        }
    }

    // ══════════════════════════════════════════════
    //  VÉRIFIER STOCK DISPONIBLE
    // ══════════════════════════════════════════════

    /**
     * Retourne la quantité disponible pour une production donnée.
     */
    public double getQuantiteDisponible(int productionId) throws SQLException {
        String sql = "SELECT quantity FROM animal_production WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("quantity");
            return 0.0;
        }
    }
}