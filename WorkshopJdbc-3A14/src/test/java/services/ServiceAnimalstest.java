package services;

import entities.Animals;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour la classe Serviceanimals
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceanimalsTest {

    private static Serviceanimals service;
    private static Animals testAnimal;
    private static int testAnimalId;

    @BeforeAll
    static void setUpBeforeClass() {
        System.out.println("=== Début des tests Serviceanimals ===");
        service = new Serviceanimals();
    }

    @AfterAll
    static void tearDownAfterClass() {
        System.out.println("=== Fin des tests Serviceanimals ===");
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Ajouter un animal")
    void testAjouter() throws SQLException {
        // Arrange
        testAnimal = new Animals(
                "Vache",
                "Holstein",
                LocalDate.of(2023, 1, 15),
                "Bonne santé"
        );

        // Act
        service.ajouter(testAnimal);
        List<Animals> animals = service.recuperer();

        // Assert
        assertFalse(animals.isEmpty(), "La liste ne devrait pas être vide après ajout");

        // Récupérer l'ID du dernier animal ajouté
        Animals lastAnimal = animals.get(animals.size() - 1);
        testAnimalId = lastAnimal.getId();

        System.out.println("✅ Animal ajouté avec ID: " + testAnimalId);
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Récupérer tous les animaux")
    void testRecuperer() throws SQLException {
        // Act
        List<Animals> animals = service.recuperer();

        // Assert
        assertNotNull(animals, "La liste ne devrait pas être null");
        assertTrue(animals.size() > 0, "La liste devrait contenir au moins 1 animal");

        System.out.println("✅ Nombre d'animaux récupérés: " + animals.size());
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Récupérer un animal par ID")
    void testRecupererParId() throws SQLException {
        // Arrange
        Animals animal = new Animals(
                "Chèvre",
                "Alpine",
                LocalDate.of(2022, 6, 10),
                "Excellente"
        );
        service.ajouter(animal);

        List<Animals> animals = service.recuperer();
        int id = animals.get(animals.size() - 1).getId();

        // Act
        Animals result = service.recupererParId(id);

        // Assert
        assertNotNull(result, "L'animal devrait être trouvé");
        assertEquals("Chèvre", result.getType(), "Le type devrait être 'Chèvre'");
        assertEquals("Alpine", result.getBreed(), "La race devrait être 'Alpine'");

        System.out.println("✅ Animal trouvé: " + result.getType() + " - " + result.getBreed());
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Rechercher par type")
    void testRechercherParType() throws SQLException {
        // Arrange
        String typeRecherche = "Vache";

        // Act
        List<Animals> vaches = service.rechercherParType(typeRecherche);

        // Assert
        assertNotNull(vaches, "La liste ne devrait pas être null");

        for (Animals animal : vaches) {
            assertEquals(typeRecherche, animal.getType(),
                    "Tous les animaux devraient être de type: " + typeRecherche);
        }

        System.out.println("✅ Nombre de vaches trouvées: " + vaches.size());
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Rechercher par race")
    void testRechercherParRace() throws SQLException {
        // Arrange
        String raceRecherche = "Holstein";

        // Act
        List<Animals> holstein = service.rechercherParRace(raceRecherche);

        // Assert
        assertNotNull(holstein, "La liste ne devrait pas être null");

        for (Animals animal : holstein) {
            assertEquals(raceRecherche, animal.getBreed(),
                    "Tous les animaux devraient être de race: " + raceRecherche);
        }

        System.out.println("✅ Nombre d'animaux Holstein trouvés: " + holstein.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Modifier un animal")
    void testModifier() throws SQLException {
        // Arrange
        List<Animals> animals = service.recuperer();
        Animals animalAModifier = animals.get(animals.size() - 1);
        int id = animalAModifier.getId();

        animalAModifier.setHealthStatus("Sous traitement");

        // Act
        service.modifier(animalAModifier);
        Animals animalModifie = service.recupererParId(id);

        // Assert
        assertNotNull(animalModifie, "L'animal modifié devrait exister");
        assertEquals("Sous traitement", animalModifie.getHealthStatus(),
                "Le statut de santé devrait être 'Sous traitement'");

        System.out.println("✅ Animal modifié: nouveau statut = " + animalModifie.getHealthStatus());
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Compter les animaux")
    void testCompterAnimaux() throws SQLException {
        // Act
        int count = service.compterAnimaux();
        List<Animals> animals = service.recuperer();

        // Assert
        assertEquals(animals.size(), count,
                "Le comptage devrait correspondre au nombre d'animaux dans la liste");
        assertTrue(count > 0, "Il devrait y avoir au moins 1 animal");

        System.out.println("✅ Nombre total d'animaux: " + count);
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Supprimer un animal")
    void testSupprimer() throws SQLException {
        // Arrange
        List<Animals> animalsAvant = service.recuperer();
        int countAvant = animalsAvant.size();

        Animals animalASupprimer = animalsAvant.get(animalsAvant.size() - 1);

        // Act
        service.supprimer(animalASupprimer);
        List<Animals> animalsApres = service.recuperer();
        int countApres = animalsApres.size();

        // Assert
        assertEquals(countAvant - 1, countApres,
                "Le nombre d'animaux devrait diminuer de 1 après suppression");

        System.out.println("✅ Animal supprimé. Avant: " + countAvant + ", Après: " + countApres);
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Récupérer animal inexistant")
    void testRecupererParIdInexistant() throws SQLException {
        // Arrange
        int idInexistant = 99999;

        // Act
        Animals result = service.recupererParId(idInexistant);

        // Assert
        assertNull(result, "Devrait retourner null pour un ID inexistant");

        System.out.println("✅ Test ID inexistant réussi: null retourné");
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Rechercher type inexistant")
    void testRechercherTypeInexistant() throws SQLException {
        // Arrange
        String typeInexistant = "Dinosaure";

        // Act
        List<Animals> result = service.rechercherParType(typeInexistant);

        // Assert
        assertNotNull(result, "La liste ne devrait pas être null");
        assertTrue(result.isEmpty(), "La liste devrait être vide pour un type inexistant");

        System.out.println("✅ Test type inexistant réussi: liste vide");
    }
}