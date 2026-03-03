package entities;

import java.time.LocalDate;

/**
 * Classe AnimalProduction avec jointure objet vers Animals.
 * ✅ MODIFIÉ : animal (objet) au lieu de animalId (int)
 */
public class AnimalProduction {

    private int         id;
    private Animals     animal;            // ✅ Objet complet au lieu d'un simple ID
    private String      productionType;
    private double      quantity;
    private LocalDate   productionDate;

    // ══════════════════════════════════════════════
    //  CONSTRUCTEURS
    // ══════════════════════════════════════════════

    public AnimalProduction() {
    }

    // Constructeur avec ID (lecture BDD)
    public AnimalProduction(int id, Animals animal, String productionType,
                            double quantity, LocalDate productionDate) {
        this.id             = id;
        this.animal         = animal;
        this.productionType = productionType;
        this.quantity       = quantity;
        this.productionDate = productionDate;
    }

    // Constructeur sans ID (création)
    public AnimalProduction(Animals animal, String productionType,
                            double quantity, LocalDate productionDate) {
        this.animal         = animal;
        this.productionType = productionType;
        this.quantity       = quantity;
        this.productionDate = productionDate;
    }

    public AnimalProduction(int testAnimalId, String lait, double quantity, LocalDate of) {

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

    public Animals getAnimal() {
        return animal;
    }

    public void setAnimal(Animals animal) {
        this.animal = animal;
    }

    /**
     * Helper : retourne l'ID de l'animal (pour compatibilité SQL).
     */
    public int getAnimalId() {
        return animal != null ? animal.getId() : 0;
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

    // ══════════════════════════════════════════════
    //  TOSTRING
    // ══════════════════════════════════════════════

    @Override
    public String toString() {
        return "AnimalProduction{" +
                "id=" + id +
                ", animal=" + (animal != null ? animal.getType() + " #" + animal.getId() : "null") +
                ", productionType='" + productionType + '\'' +
                ", quantity=" + quantity +
                ", productionDate=" + productionDate +
                '}';
    }
}