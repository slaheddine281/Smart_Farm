package entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entête d'une commande visiteur.
 */
public class Commande {

    private int               id;
    private String            visiteurNom;
    private LocalDateTime     dateCommande;
    private double            totalPrix;
    private String            statut;
    private List<CommandeItem> items;

    public Commande() {
        this.visiteurNom  = "Visiteur";
        this.dateCommande = LocalDateTime.now();
        this.statut       = "En attente";
        this.items        = new ArrayList<>();
        this.totalPrix    = 0.0;
    }

    // ── Getters / Setters ────────────────────────
    public int               getId()           { return id; }
    public void              setId(int id)     { this.id = id; }

    public String            getVisiteurNom()              { return visiteurNom; }
    public void              setVisiteurNom(String n)      { this.visiteurNom = n; }

    public LocalDateTime     getDateCommande()             { return dateCommande; }
    public void              setDateCommande(LocalDateTime d) { this.dateCommande = d; }

    public double            getTotalPrix()                { return totalPrix; }
    public void              setTotalPrix(double t)        { this.totalPrix = t; }

    public String            getStatut()                   { return statut; }
    public void              setStatut(String s)           { this.statut = s; }

    public List<CommandeItem> getItems()                   { return items; }
    public void              setItems(List<CommandeItem> i){ this.items = i; }

    /** Recalcule le total à partir des items du panier. */
    public void recalculerTotal() {
        this.totalPrix = items.stream()
                .mapToDouble(CommandeItem::getSousTotal)
                .sum();
    }
}