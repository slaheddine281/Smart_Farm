package services;

import entities.Animals;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service pour gérer les opérations CRUD sur les animaux.
 * ✅ CORRIGÉ : Connexion non-static pour éviter les NullPointerException
 */
public class Serviceanimals implements IService<Animals> {

    // ✅ CORRECTION : Enlever "static" pour éviter les problèmes d'initialisation
    private final Connection connection;

    public Serviceanimals() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Animals animal) throws SQLException {
        String req = "INSERT INTO animals (type, breed, birth_date, health_status) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, animal.getType());
        ps.setString(2, animal.getBreed());
        ps.setDate(3, Date.valueOf(animal.getBirthDate()));
        ps.setString(4, animal.getHealthStatus());

        ps.executeUpdate();
        System.out.println("✅ Animal ajouté avec succès!");
    }

    @Override
    public void modifier(Animals animal) throws SQLException {
        String req = "UPDATE animals SET type=?, breed=?, birth_date=?, health_status=? WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, animal.getType());
        ps.setString(2, animal.getBreed());
        ps.setDate(3, Date.valueOf(animal.getBirthDate()));
        ps.setString(4, animal.getHealthStatus());
        ps.setInt(5, animal.getId());

        ps.executeUpdate();
        System.out.println("✅ Animal modifié avec succès!");
    }

    @Override
    public void supprimer(Animals animal) throws SQLException {
        String req = "DELETE FROM animals WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, animal.getId());

        ps.executeUpdate();
        System.out.println("✅ Animal supprimé avec succès!");
    }

    public List<Animals> recuperer() throws SQLException {
        List<Animals> animalsList = new ArrayList<>();
        String req = "SELECT * FROM animals";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            int id = rs.getInt("id");
            String type = rs.getString("type");
            String breed = rs.getString("breed");
            LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
            String healthStatus = rs.getString("health_status");

            Animals animal = new Animals(id, type, breed, birthDate, healthStatus);
            animalsList.add(animal);
        }

        return animalsList;
    }

    /**
     * Récupérer un animal par son ID
     */
    public Animals recupererParId(int id) throws SQLException {
        String req = "SELECT * FROM animals WHERE id=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            String type = rs.getString("type");
            String breed = rs.getString("breed");
            LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
            String healthStatus = rs.getString("health_status");

            return new Animals(id, type, breed, birthDate, healthStatus);
        }

        return null;
    }

    /**
     * Rechercher des animaux par type
     */
    public List<Animals> rechercherParType(String type) throws SQLException {
        List<Animals> animalsList = new ArrayList<>();
        String req = "SELECT * FROM animals WHERE type=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, type);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            String breed = rs.getString("breed");
            LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
            String healthStatus = rs.getString("health_status");

            Animals animal = new Animals(id, type, breed, birthDate, healthStatus);
            animalsList.add(animal);
        }

        return animalsList;
    }

    /**
     * Rechercher des animaux par race
     */
    public List<Animals> rechercherParRace(String breed) throws SQLException {
        List<Animals> animalsList = new ArrayList<>();
        String req = "SELECT * FROM animals WHERE breed=?";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, breed);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            int id = rs.getInt("id");
            String type = rs.getString("type");
            LocalDate birthDate = rs.getDate("birth_date").toLocalDate();
            String healthStatus = rs.getString("health_status");

            Animals animal = new Animals(id, type, breed, birthDate, healthStatus);
            animalsList.add(animal);
        }

        return animalsList;
    }

    /**
     * Compter le nombre total d'animaux
     */
    public int compterAnimaux() throws SQLException {
        String req = "SELECT COUNT(*) as total FROM animals";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        if (rs.next()) {
            return rs.getInt("total");
        }

        return 0;
    }
}