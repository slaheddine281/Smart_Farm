package mains;

import entities.User;
import services.ServiceUser;

import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        ServiceUser service = new ServiceUser();

        try {
            // 1. Afficher tous les utilisateurs présents dans la base
            System.out.println("=== Liste des utilisateurs dans la base ===");
            List<User> utilisateurs = service.recuperer();

            if (utilisateurs.isEmpty()) {
                System.out.println("Aucun utilisateur trouvé dans la table 'users'.");
            } else {
                System.out.println("Nombre d'utilisateurs : " + utilisateurs.size());
                for (User user : utilisateurs) {
                    System.out.println(user);
                }
            }

            System.out.println("\n----------------------------------------\n");

            // 2. Exemple d'ajout d'un nouvel utilisateur (décommentez si vous voulez tester)
            /*
            User nouveau = new User();
            nouveau.setUsername("test_" + System.currentTimeMillis());
            nouveau.setEmail("test" + System.currentTimeMillis() + "@exemple.com");
            nouveau.setPhotoProfil("photos/avatar-default.jpg");
            nouveau.setMotDePasse("123456");

            System.out.println("Ajout d'un utilisateur de test...");
            service.ajouter(nouveau);

            System.out.println("Utilisateur ajouté → ID = " + nouveau.getUserID());

            // Ré-afficher la liste après ajout
            System.out.println("\nListe après ajout :");
            service.recuperer().forEach(System.out::println);
            */

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
        }
    }
}