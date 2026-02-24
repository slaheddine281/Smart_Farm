package mains;

import entities.Employee;
import services.ServiceEmployee;   // ← important : le nouveau service

import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        ServiceEmployee service = new ServiceEmployee();

        // Exemples d'employés (conformément à la nouvelle entité Employee)
        Employee emp1 = new Employee("Foulen", "Ben Foulen", "+21655123456", "Technicien agricole");
        Employee emp2 = new Employee("Ahmed", "Ben Ahmed", "+21698765432", "Responsable élevage");

        try {
            // Optionnel : ajouter des employés pour tester (décommente si besoin)
            // service.ajouter(emp1);
            // service.ajouter(emp2);

            // Récupérer et afficher tous les employés de la table
            List<Employee> employees = service.recuperer();

            if (employees.isEmpty()) {
                System.out.println("Aucun employé trouvé dans la table 'employees'.");
            } else {
                System.out.println("Liste des employés :");
                System.out.println("-----------------------------------");
                for (Employee e : employees) {
                    System.out.println(e);
                }
                System.out.println("-----------------------------------");
                System.out.println("Nombre total d'employés : " + employees.size());
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            e.printStackTrace();
        }
    }
}