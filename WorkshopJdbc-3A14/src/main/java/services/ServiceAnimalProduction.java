package services;

import entities.AnimalProduction;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations CRUD sur les productions animales
 * VERSION SIMPLIFIÉE avec double au lieu de BigDecimal
 */
public class ServiceAnimalProduction implements IService<AnimalProduction> {

    private Connection connection;

    public ServiceAnimalProduction() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(AnimalProduction production) throws SQLException {
        String req = "INSERT INTO animal_production (animal_id, production_type, quantity, production_date) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, production.getAnimalId());
        ps.setString(2, production.getProductionType());
        ps.setDouble(3, production.getQuantity());
        ps.setDate(4, Date.valueOf(production.getProductionDate()));

        ps.executeUpdate();
        System.out.println("✅ Production ajoutée avec succès!");
    }

    @Override
    public void modifier(AnimalProduction production) throws SQLException {
        String req = "UPDATE animal_production SET animal_id=?, production_type=?, quantity=?, production_date=? WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, production.getAnimalId());
        ps.setString(2, production.getProductionType());
        ps.setDouble(3, production.getQuantity());
        ps.setDate(4, Date.valueOf(production.getProductionDate()));
        ps.setInt(5, production.getId());

        ps.executeUpdate();
        System.out.println("✅ Production modifiée avec succès!");
    }

    @Override
    public void supprimer(AnimalProduction production) throws SQLException {
        String req = "DELETE FROM animal_production WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, production.getId());

        ps.executeUpdate();
        System.out.println("✅ Production supprimée avec succès!");
    }

    @Override
    public List<AnimalProduction> recuperer() throws SQLException {
        List<AnimalProduction> productionsList = new ArrayList<>();
        String req = "SELECT * FROM animal_production";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            int id = rs.getInt("id");
            int animalId = rs.getInt("animal_id");
            String productionType = rs.getString("production_type");
            double quantity = rs.getDouble("quantity");
            LocalDate productionDate = rs.getDate("production_date").toLocalDate();

            AnimalProduction production = new AnimalProduction(id, animalId, productionType, quantity, productionDate);
            productionsList.add(production);
        }

        return productionsList;
    }

    /**
     * Récupérer une production par son ID
     */
    public AnimalProduction recupererParId(int id) throws SQLException {
        String req = "SELECT * FROM animal_production WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            int animalId = rs.getInt("animal_id");
            String productionType = rs.getString("production_type");
            double quantity = rs.getDouble("quantity");
            LocalDate productionDate = rs.getDate("production_date").toLocalDate();

            return new AnimalProduction(id, animalId, productionType, quantity, productionDate);
        }

        return null;
    }

    /**
     * Récupérer toutes les productions d'un animal spécifique
     */
    public List<AnimalProduction> recupererParAnimal(int animalId) throws SQLException {
        List<AnimalProduction> productionsList = new ArrayList<>();
        String req = "SELECT * FROM animal_production WHERE animal_id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, animalId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            String productionType = rs.getString("production_type");
            double quantity = rs.getDouble("quantity");
            LocalDate productionDate = rs.getDate("production_date").toLocalDate();

            AnimalProduction production = new AnimalProduction(id, animalId, productionType, quantity, productionDate);
            productionsList.add(production);
        }

        return productionsList;
    }

    /**
     * Rechercher des productions par type (ex: "Lait", "Œufs")
     */
    public List<AnimalProduction> rechercherParType(String productionType) throws SQLException {
        List<AnimalProduction> productionsList = new ArrayList<>();
        String req = "SELECT * FROM animal_production WHERE production_type=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, productionType);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            int animalId = rs.getInt("animal_id");
            double quantity = rs.getDouble("quantity");
            LocalDate productionDate = rs.getDate("production_date").toLocalDate();

            AnimalProduction production = new AnimalProduction(id, animalId, productionType, quantity, productionDate);
            productionsList.add(production);
        }

        return productionsList;
    }

    /**
     * Récupérer les productions entre deux dates
     */
    public List<AnimalProduction> recupererParPeriode(LocalDate dateDebut, LocalDate dateFin) throws SQLException {
        List<AnimalProduction> productionsList = new ArrayList<>();
        String req = "SELECT * FROM animal_production WHERE production_date BETWEEN ? AND ?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setDate(1, Date.valueOf(dateDebut));
        ps.setDate(2, Date.valueOf(dateFin));

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            int animalId = rs.getInt("animal_id");
            String productionType = rs.getString("production_type");
            double quantity = rs.getDouble("quantity");
            LocalDate productionDate = rs.getDate("production_date").toLocalDate();

            AnimalProduction production = new AnimalProduction(id, animalId, productionType, quantity, productionDate);
            productionsList.add(production);
        }

        return productionsList;
    }

    /**
     * Calculer la production totale d'un animal
     */
    public double calculerProductionTotale(int animalId) throws SQLException {
        String req = "SELECT SUM(quantity) as total FROM animal_production WHERE animal_id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, animalId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getDouble("total");
        }

        return 0.0;
    }

    /**
     * Calculer la production totale par type
     */
    public double calculerProductionTotaleParType(String productionType) throws SQLException {
        String req = "SELECT SUM(quantity) as total FROM animal_production WHERE production_type=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, productionType);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getDouble("total");
        }

        return 0.0;
    }

    /**
     * Calculer la moyenne de production d'un animal
     */
    public double calculerMoyenneProduction(int animalId) throws SQLException {
        String req = "SELECT AVG(quantity) as moyenne FROM animal_production WHERE animal_id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, animalId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getDouble("moyenne");
        }

        return 0.0;
    }

    /**
     * Récupérer les dernières productions (10 dernières)
     */
    public List<AnimalProduction> recupererDernieresProductions() throws SQLException {
        List<AnimalProduction> productionsList = new ArrayList<>();
        String req = "SELECT * FROM animal_production ORDER BY production_date DESC LIMIT 10";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            int id = rs.getInt("id");
            int animalId = rs.getInt("animal_id");
            String productionType = rs.getString("production_type");
            double quantity = rs.getDouble("quantity");
            LocalDate productionDate = rs.getDate("production_date").toLocalDate();

            AnimalProduction production = new AnimalProduction(id, animalId, productionType, quantity, productionDate);
            productionsList.add(production);
        }

        return productionsList;
    }

    /**
     * Compter le nombre de productions
     */
    public int compterProductions() throws SQLException {
        String req = "SELECT COUNT(*) as total FROM animal_production";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }

    /**
     * Compter le nombre de productions d'un animal
     */
    public int compterProductionsParAnimal(int animalId) throws SQLException {
        String req = "SELECT COUNT(*) as total FROM animal_production WHERE animal_id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, animalId);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }
}