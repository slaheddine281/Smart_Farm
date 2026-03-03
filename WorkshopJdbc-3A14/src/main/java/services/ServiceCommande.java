package services;

import entities.Commande;
import entities.CommandeItem;
import utils.MyDatabase;

import java.sql.*;

/**
 * Service commande — enregistre en BDD, décrémente le stock,
 * et enregistre l'ID Stripe après paiement confirmé.
 */
public class ServiceCommande {

    private final Connection conn;

    public ServiceCommande() {
        conn = MyDatabase.getInstance().getConnection();
        if (conn == null) throw new RuntimeException("Connexion MySQL impossible.");
    }

    // ══════════════════════════════════════════════
    //  ENREGISTRER UNE COMMANDE (avec stripe_id)
    // ══════════════════════════════════════════════

    /**
     * Enregistre la commande complète dans une transaction :
     * 1. Insère l'entête dans `commandes` avec le stripeId
     * 2. Insère chaque ligne dans `commande_items`
     * 3. Décrémente le stock dans `animal_production`
     */
    public void enregistrerCommande(Commande commande, String stripeId) throws SQLException {
        conn.setAutoCommit(false);
        try {
            int commandeId = insertCommande(commande, stripeId);
            for (CommandeItem item : commande.getItems()) {
                insertCommandeItem(commandeId, item);
                decrementerProduction(item.getProductionId(), item.getQuantiteAchat());
            }
            conn.commit();
            System.out.println("✅ Commande #" + commandeId + " enregistrée. Stripe ID: " + stripeId);
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Surcharge sans stripeId pour compatibilité */
    public void enregistrerCommande(Commande commande) throws SQLException {
        enregistrerCommande(commande, null);
    }

    // ── Insérer entête ───────────────────────────
    private int insertCommande(Commande commande, String stripeId) throws SQLException {
        String sql = "INSERT INTO commandes (visiteur_nom, date_commande, total_prix, statut, stripe_id) " +
                "VALUES (?, NOW(), ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, commande.getVisiteurNom() != null ? commande.getVisiteurNom() : "Visiteur");
            ps.setDouble(2, commande.getTotalPrix());
            ps.setString(3, "Payée");
            ps.setString(4, stripeId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            throw new SQLException("Impossible de récupérer l'ID commande.");
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

    // ── Vérifier stock ──────────────────────────
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