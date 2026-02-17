package entities;

import java.time.LocalDate;

/**
 * Classe AnimalProduction correspondant à la table 'animal_production' dans la base de données smart_farm
 * VERSION SIMPLIFIÉE avec double au lieu de BigDecimal
 */
public class AnimalProduction {

    // Attributs correspondant exactement à la table 'animal_production'
    private int id;
    private int animalId;              // Référence vers l'animal (foreign key)
    private String productionType;     // Type de production (Lait, Œufs, Laine, etc.)
    private double quantity;           // Quantité produite (simplifié avec double)
    private LocalDate productionDate;  // Date de production

    // Constructeur vide
    public AnimalProduction() {
    }

    // Constructeur avec tous les paramètres (avec ID)
    public AnimalProduction(int id, int animalId, String productionType, double quantity, LocalDate productionDate) {
        this.id = id;
        this.animalId = animalId;
        this.productionType = productionType;
        this.quantity = quantity;
        this.productionDate = productionDate;
    }

    // Constructeur sans ID (pour les nouvelles productions)
    public AnimalProduction(int animalId, String productionType, double quantity, LocalDate productionDate) {
        this.animalId = animalId;
        this.productionType = productionType;
        this.quantity = quantity;
        this.productionDate = productionDate;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAnimalId() {
        return animalId;
    }

    public void setAnimalId(int animalId) {
        this.animalId = animalId;
    }

    public String getProductionType() {
        return productionType;
    }

    public void setProductionType(String productionType) {
        this.productionType = productionType;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public LocalDate getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(LocalDate productionDate) {
        this.productionDate = productionDate;
    }

    @Override
    public String toString() {
        return "AnimalProduction{" +
                "id=" + id +
                ", animalId=" + animalId +
                ", productionType='" + productionType + '\'' +
                ", quantity=" + quantity +
                ", productionDate=" + productionDate +
                '}';
    }
}