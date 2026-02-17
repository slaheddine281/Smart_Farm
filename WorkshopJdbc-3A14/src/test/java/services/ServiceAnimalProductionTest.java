package services;

import entities.Animals;
import entities.AnimalProduction;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe ServiceAnimalProduction
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceAnimalProductionTest {

    private static ServiceAnimalProduction serviceProduction;
    private static Serviceanimals serviceAnimals;
    private static int testAnimalId;
    private static int testProductionId;

    @BeforeAll
    static void setUpBeforeClass() throws SQLException {
        System.out.println("=== Début des tests ServiceAnimalProduction ===");
        serviceProduction = new ServiceAnimalProduction();
        serviceAnimals = new Serviceanimals();

        // Créer un animal de test pour les productions
        Animals testAnimal = new Animals(
                "Vache",
                "Holstein",
                LocalDate.of(2023, 1, 15),
                "Bonne santé"
        );
        serviceAnimals.ajouter(testAnimal);

        // Récupérer l'ID de l'animal créé
        List<Animals> animals = serviceAnimals.recuperer();
        testAnimalId = animals.get(animals.size() - 1).getId();

        System.out.println("✅ Animal de test créé avec ID: " + testAnimalId);
    }

    @AfterAll
    static void tearDownAfterClass() {
        System.out.println("=== Fin des tests ServiceAnimalProduction ===");
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Ajouter une production")
    void testAjouter() throws SQLException {
        // Arrange
        AnimalProduction production = new AnimalProduction(
                testAnimalId,
                "Lait",
                25.5,
                LocalDate.of(2024, 2, 10)
        );

        // Act
        serviceProduction.ajouter(production);
        List<AnimalProduction> productions = serviceProduction.recuperer();

        // Assert
        assertFalse(productions.isEmpty(), "La liste ne devrait pas être vide après ajout");

        AnimalProduction lastProduction = productions.get(productions.size() - 1);
        testProductionId = lastProduction.getId();

        System.out.println("✅ Production ajoutée avec ID: " + testProductionId);
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Récupérer toutes les productions")
    void testRecuperer() throws SQLException {
        // Act
        List<AnimalProduction> productions = serviceProduction.recuperer();

        // Assert
        assertNotNull(productions, "La liste ne devrait pas être null");
        assertTrue(productions.size() > 0, "La liste devrait contenir au moins 1 production");

        System.out.println("✅ Nombre de productions récupérées: " + productions.size());
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Récupérer une production par ID")
    void testRecupererParId() throws SQLException {
        // Arrange
        AnimalProduction production = new AnimalProduction(
                testAnimalId,
                "Lait",
                27.0,
                LocalDate.of(2024, 2, 11)
        );
        serviceProduction.ajouter(production);

        List<AnimalProduction> productions = serviceProduction.recuperer();
        int id = productions.get(productions.size() - 1).getId();

        // Act
        AnimalProduction result = serviceProduction.recupererParId(id);

        // Assert
        assertNotNull(result, "La production devrait être trouvée");
        assertEquals("Lait", result.getProductionType(), "Le type devrait être 'Lait'");
        assertEquals(27.0, result.getQuantity(), 0.01, "La quantité devrait être 27.0");

        System.out.println("✅ Production trouvée: " + result.getProductionType() + " - " + result.getQuantity() + "L");
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Récupérer productions par animal")
    void testRecupererParAnimal() throws SQLException {
        // Act
        List<AnimalProduction> productions = serviceProduction.recupererParAnimal(testAnimalId);

        // Assert
        assertNotNull(productions, "La liste ne devrait pas être null");
        assertTrue(productions.size() > 0, "L'animal devrait avoir au moins 1 production");

        for (AnimalProduction prod : productions) {
            assertEquals(testAnimalId, prod.getAnimalId(),
                    "Toutes les productions devraient appartenir à l'animal " + testAnimalId);
        }

        System.out.println("✅ Nombre de productions de l'animal " + testAnimalId + ": " + productions.size());
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Rechercher par type de production")
    void testRechercherParType() throws SQLException {
        // Arrange
        String typeRecherche = "Lait";

        // Act
        List<AnimalProduction> laitProductions = serviceProduction.rechercherParType(typeRecherche);

        // Assert
        assertNotNull(laitProductions, "La liste ne devrait pas être null");

        for (AnimalProduction prod : laitProductions) {
            assertEquals(typeRecherche, prod.getProductionType(),
                    "Toutes les productions devraient être de type: " + typeRecherche);
        }

        System.out.println("✅ Nombre de productions de lait trouvées: " + laitProductions.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Récupérer productions par période")
    void testRecupererParPeriode() throws SQLException {
        // Arrange
        LocalDate debut = LocalDate.of(2024, 2, 1);
        LocalDate fin = LocalDate.of(2024, 2, 28);

        // Act
        List<AnimalProduction> productions = serviceProduction.recupererParPeriode(debut, fin);

        // Assert
        assertNotNull(productions, "La liste ne devrait pas être null");

        for (AnimalProduction prod : productions) {
            assertTrue(
                    !prod.getProductionDate().isBefore(debut) &&
                            !prod.getProductionDate().isAfter(fin),
                    "La production devrait être dans la période"
            );
        }

        System.out.println("✅ Productions en février 2024: " + productions.size());
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Calculer production totale par animal")
    void testCalculerProductionTotale() throws SQLException {
        // Act
        double total = serviceProduction.calculerProductionTotale(testAnimalId);

        // Assert
        assertTrue(total > 0, "La production totale devrait être supérieure à 0");

        System.out.println("✅ Production totale de l'animal " + testAnimalId + ": " + total + "L");
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Calculer production totale par type")
    void testCalculerProductionTotaleParType() throws SQLException {
        // Act
        double totalLait = serviceProduction.calculerProductionTotaleParType("Lait");

        // Assert
        assertTrue(totalLait > 0, "La production totale de lait devrait être supérieure à 0");

        System.out.println("✅ Production totale de lait: " + totalLait + "L");
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Calculer moyenne de production")
    void testCalculerMoyenneProduction() throws SQLException {
        // Act
        double moyenne = serviceProduction.calculerMoyenneProduction(testAnimalId);

        // Assert
        assertTrue(moyenne > 0, "La moyenne devrait être supérieure à 0");

        System.out.println("✅ Moyenne de production de l'animal " + testAnimalId + ": " + moyenne + "L");
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Modifier une production")
    void testModifier() throws SQLException {
        // Arrange
        List<AnimalProduction> productions = serviceProduction.recuperer();
        AnimalProduction productionAModifier = productions.get(productions.size() - 1);
        int id = productionAModifier.getId();

        double nouvelleQuantite = 30.5;
        productionAModifier.setQuantity(nouvelleQuantite);

        // Act
        serviceProduction.modifier(productionAModifier);
        AnimalProduction productionModifiee = serviceProduction.recupererParId(id);

        // Assert
        assertNotNull(productionModifiee, "La production modifiée devrait exister");
        assertEquals(nouvelleQuantite, productionModifiee.getQuantity(), 0.01,
                "La quantité devrait être " + nouvelleQuantite);

        System.out.println("✅ Production modifiée: nouvelle quantité = " + productionModifiee.getQuantity() + "L");
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Compter les productions")
    void testCompterProductions() throws SQLException {
        // Act
        int count = serviceProduction.compterProductions();
        List<AnimalProduction> productions = serviceProduction.recuperer();

        // Assert
        assertEquals(productions.size(), count,
                "Le comptage devrait correspondre au nombre de productions dans la liste");
        assertTrue(count > 0, "Il devrait y avoir au moins 1 production");

        System.out.println("✅ Nombre total de productions: " + count);
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Compter productions par animal")
    void testCompterProductionsParAnimal() throws SQLException {
        // Act
        int count = serviceProduction.compterProductionsParAnimal(testAnimalId);

        // Assert
        assertTrue(count > 0, "L'animal devrait avoir au moins 1 production");

        System.out.println("✅ Nombre de productions de l'animal " + testAnimalId + ": " + count);
    }

    @Test
    @Order(13)
    @DisplayName("Test 13: Récupérer dernières productions")
    void testRecupererDernieresProductions() throws SQLException {
        // Act
        List<AnimalProduction> dernieres = serviceProduction.recupererDernieresProductions();

        // Assert
        assertNotNull(dernieres, "La liste ne devrait pas être null");
        assertTrue(dernieres.size() <= 10, "La liste devrait contenir au maximum 10 productions");

        // Vérifier que les productions sont triées par date DESC
        if (dernieres.size() > 1) {
            LocalDate datePrecedente = dernieres.get(0).getProductionDate();
            for (int i = 1; i < dernieres.size(); i++) {
                LocalDate dateActuelle = dernieres.get(i).getProductionDate();
                assertTrue(
                        !dateActuelle.isAfter(datePrecedente),
                        "Les productions devraient être triées par date décroissante"
                );
                datePrecedente = dateActuelle;
            }
        }

        System.out.println("✅ Nombre de dernières productions: " + dernieres.size());
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: Supprimer une production")
    void testSupprimer() throws SQLException {
        // Arrange
        List<AnimalProduction> productionsAvant = serviceProduction.recuperer();
        int countAvant = productionsAvant.size();

        AnimalProduction productionASupprimer = productionsAvant.get(productionsAvant.size() - 1);

        // Act
        serviceProduction.supprimer(productionASupprimer);
        List<AnimalProduction> productionsApres = serviceProduction.recuperer();
        int countApres = productionsApres.size();

        // Assert
        assertEquals(countAvant - 1, countApres,
                "Le nombre de productions devrait diminuer de 1 après suppression");

        System.out.println("✅ Production supprimée. Avant: " + countAvant + ", Après: " + countApres);
    }

    @Test
    @Order(15)
    @DisplayName("Test 15: Récupérer production inexistante")
    void testRecupererParIdInexistant() throws SQLException {
        // Arrange
        int idInexistant = 99999;

        // Act
        AnimalProduction result = serviceProduction.recupererParId(idInexistant);

        // Assert
        assertNull(result, "Devrait retourner null pour un ID inexistant");

        System.out.println("✅ Test ID inexistant réussi: null retourné");
    }

    @Test
    @Order(16)
    @DisplayName("Test 16: Rechercher type inexistant")
    void testRechercherTypeInexistant() throws SQLException {
        // Arrange
        String typeInexistant = "Or";

        // Act
        List<AnimalProduction> result = serviceProduction.rechercherParType(typeInexistant);

        // Assert
        assertNotNull(result, "La liste ne devrait pas être null");
        assertTrue(result.isEmpty(), "La liste devrait être vide pour un type inexistant");

        System.out.println("✅ Test type inexistant réussi: liste vide");
    }
}