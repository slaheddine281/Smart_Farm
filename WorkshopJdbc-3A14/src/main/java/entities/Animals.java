package entities;

import java.time.LocalDate;

/**
 * Classe Animals - entité Animal de la ferme.
 */
public class Animals {

    private int       id;
    private String    type;
    private String    breed;
    private LocalDate birthDate;
    private String    healthStatus;

    // ══════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ══════════════════════════════════════════════

    public Animals() {
    }

    // Sans ID (création)
    public Animals(String type, String breed, LocalDate birthDate, String healthStatus) {
        this.type         = type;
        this.breed        = breed;
        this.birthDate    = birthDate;
        this.healthStatus = healthStatus;
    }

    // Avec ID (lecture BDD)
    public Animals(int id, String type, String breed, LocalDate birthDate, String healthStatus) {
        this.id           = id;
        this.type         = type;
        this.breed        = breed;
        this.birthDate    = birthDate;
        this.healthStatus = healthStatus;
    }

    // ══════════════════════════════════════════════
    //  GETTERS / SETTERS
    // ══════════════════════════════════════════════

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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    // ══════════════════════════════════════════════
    //  TOSTRING
    // ══════════════════════════════════════════════

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