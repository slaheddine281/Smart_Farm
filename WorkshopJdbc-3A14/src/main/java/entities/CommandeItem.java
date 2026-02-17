package entities;

/**
 * Représente une ligne du panier (un produit à acheter).
 */
public class CommandeItem {

    private int    productionId;
    private String productionType;
    private double quantiteAchat;
    private double prixUnitaire;
    private double sousTotal;

    public CommandeItem(int productionId, String productionType,
                        double quantiteAchat, double prixUnitaire) {
        this.productionId   = productionId;
        this.productionType = productionType;
        this.quantiteAchat  = quantiteAchat;
        this.prixUnitaire   = prixUnitaire;
        this.sousTotal      = quantiteAchat * prixUnitaire;
    }

    public int    getProductionId()   { return productionId; }
    public String getProductionType() { return productionType; }
    public double getQuantiteAchat()  { return quantiteAchat; }
    public double getPrixUnitaire()   { return prixUnitaire; }
    public double getSousTotal()      { return sousTotal; }

    public void setQuantiteAchat(double q) {
        this.quantiteAchat = q;
        this.sousTotal     = q * prixUnitaire;
    }

    public String getLibelle() {
        return String.format("%s  ×  %.2f  =  %.2f €",
                productionType, quantiteAchat, sousTotal);
    }

    @Override
    public String toString() { return getLibelle(); }
}