import entities.Employee;
import org.junit.jupiter.api.*;
import services.ServiceEmployee;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmployeeServiceTest {

 private static ServiceEmployee service;
 private static int createdEmployeeId;

 @BeforeAll
 static void setUp() {
  service = new ServiceEmployee();
 }

 @AfterAll
 static void tearDown() {
  // Optionnel : nettoyer les données de test créées
  // (à activer si vous voulez que les tests soient idempotents)
  // try {
  //     if (createdEmployeeId > 0) {
  //         Employee toDelete = new Employee();
  //         toDelete.setId(createdEmployeeId);
  //         service.supprimer(toDelete);
  //     }
  // } catch (SQLException e) {
  //     System.err.println("Nettoyage après tests échoué : " + e.getMessage());
  // }
 }

 @Test
 @Order(1)
 @DisplayName("Ajout d'un employé → doit exister dans la base")
 void shouldAddEmployee() throws SQLException {
  // Given
  Employee employee = new Employee(
          "TestPrénom",
          "TestNom",
          "+216 99 123 456",
          "Développeur test"
  );

  // When
  service.ajouter(employee);

  // Then
  List<Employee> allEmployees = service.recuperer();

  boolean exists = allEmployees.stream()
          .anyMatch(e ->
                  e.getFirstName().equals("TestPrénom") &&
                          e.getLastName().equals("TestNom") &&
                          e.getPosition().equals("Développeur test")
          );

  assertTrue(exists, "L'employé ajouté doit être retrouvé dans la liste");

  // On garde l'ID pour le test suivant (modification)
  // On prend le dernier ajouté (approximation raisonnable pour un test simple)
  createdEmployeeId = allEmployees.stream()
          .filter(e -> e.getFirstName().equals("TestPrénom") && e.getLastName().equals("TestNom"))
          .mapToInt(Employee::getId)
          .max()
          .orElse(-1);

  assertTrue(createdEmployeeId > 0, "L'ID de l'employé créé doit être valide");
 }

 @Test
 @Order(2)
 @DisplayName("Modification d'un employé → les nouvelles valeurs doivent apparaître")
 void shouldModifyEmployee() throws SQLException {
  // Précondition : on a besoin d'un ID valide
  Assumptions.assumeTrue(createdEmployeeId > 0, "L'ajout précédent a échoué → impossible de tester la modification");

  // Given
  Employee toUpdate = new Employee();
  toUpdate.setId(createdEmployeeId);
  toUpdate.setFirstName("NouveauPrénom");
  toUpdate.setLastName("NouveauNom");
  toUpdate.setPhone("+216 55 987 654");
  toUpdate.setPosition("Testeur senior");

  // When
  service.modifier(toUpdate);

  // Then
  List<Employee> allEmployees = service.recuperer();

  boolean updatedExists = allEmployees.stream()
          .anyMatch(e ->
                  e.getId() == createdEmployeeId &&
                          e.getFirstName().equals("NouveauPrénom") &&
                          e.getLastName().equals("NouveauNom") &&
                          e.getPhone().equals("+216 55 987 654") &&
                          e.getPosition().equals("Testeur senior")
          );

  assertTrue(updatedExists, "L'employé doit avoir été modifié avec les nouvelles valeurs");
 }

 // Bonus : un test de suppression (optionnel mais recommandé)
 @Test
 @Order(3)
 @DisplayName("Suppression d'un employé → il ne doit plus exister")
 void shouldDeleteEmployee() throws SQLException {
  Assumptions.assumeTrue(createdEmployeeId > 0, "Pas d'employé à supprimer");

  // Given
  Employee toDelete = new Employee();
  toDelete.setId(createdEmployeeId);

  // When
  service.supprimer(toDelete);

  // Then
  List<Employee> allEmployees = service.recuperer();

  boolean stillExists = allEmployees.stream()
          .anyMatch(e -> e.getId() == createdEmployeeId);

  assertFalse(stillExists, "L'employé doit avoir été supprimé");
 }
}