package entities;

import java.time.LocalDate;

/**
 * Classe Animals correspondant à la table 'animals' dans la base de données smart_farm
 */
public class Animals {

    // Attributs correspondant exactement à la table 'animals'
    private int id;
    private String type;           // Type d'animal (Vache, Chèvre, etc.)
    private String breed;          // Race (Holstein, Alpine, etc.)
    private LocalDate birthDate;   // Date de naissance (IMPORTANT : LocalDate !)
    private String healthStatus;   // Statut de santé

    // ========== CONSTRUCTEURS ==========

    // Constructeur vide
    public Animals() {
    }

    // Constructeur SANS ID (pour les nouveaux animaux)
    public Animals(String type, String breed, LocalDate birthDate, String healthStatus) {
        this.type = type;
        this.breed = breed;
        this.birthDate = birthDate;        // ✅ IMPORTANT : Assigner birthDate
        this.healthStatus = healthStatus;
    }

    // Constructeur AVEC ID (pour les animaux existants)
    public Animals(int id, String type, String breed, LocalDate birthDate, String healthStatus) {
        this.id = id;
        this.type = type;
        this.breed = breed;
        this.birthDate = birthDate;        // ✅ IMPORTANT : Assigner birthDate
        this.healthStatus = healthStatus;
    }

    // ========== GETTERS ET SETTERS ==========

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    // ✅ GETTER BIRTHDATE - Retourne LocalDate
    public LocalDate getBirthDate() {
        return birthDate;
    }

    // ✅ SETTER BIRTHDATE - Accepte LocalDate
    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    // ========== TOSTRING ==========

    @Override
    public String toString() {
        return "Animals{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", breed='" + breed + '\'' +
                ", birthDate=" + birthDate +
                ", healthStatus='" + healthStatus + '\'' +
                '}';
    }
}