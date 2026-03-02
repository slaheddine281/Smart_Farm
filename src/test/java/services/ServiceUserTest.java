package services;

import entities.User;
import org.junit.jupiter.api.*;
import utils.MyDatabase;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceUserTest {

    private static ServiceUser serviceUser;
    private static User testUser;

    @BeforeAll
    static void setUp() {
        serviceUser = new ServiceUser(MyDatabase.getInstance().getConnection());
        testUser = new User(
                "testUser",
                "test@email.com",
                "123456",
                "CLIENT",
                "photo.jpg"
        );
    }

    @Test
    @Order(1)
    void testAjouter() throws SQLException {
        serviceUser.ajouter(testUser);

        List<User> users = serviceUser.recuperer();

        boolean found = users.stream()
                .anyMatch(u -> u.getEmail().equals("test@email.com"));

        assertTrue(found, "L'utilisateur doit être ajouté à la base");
    }

    @Test
    @Order(2)
    void testGetUserByEmailAndPassword() throws SQLException {
        User user = serviceUser.getUserByEmailAndPassword(
                "test@email.com",
                "123456"
        );

        assertNotNull(user);
        assertEquals("testUser", user.getUsername());
        assertEquals("CLIENT", user.getRole());
    }

    @Test
    @Order(3)
    void testModifier() throws SQLException {
        List<User> users = serviceUser.recuperer();
        User user = users.stream()
                .filter(u -> u.getEmail().equals("test@email.com"))
                .findFirst()
                .orElse(null);

        assertNotNull(user);

        user.setUsername("updatedUser");
        serviceUser.modifier(user);

        User updated = serviceUser.getUserByEmailAndPassword(
                "test@email.com",
                "123456"
        );

        assertEquals("updatedUser", updated.getUsername());
    }

    @Test
    @Order(4)
    void testSupprimer() throws SQLException {
        List<User> users = serviceUser.recuperer();
        User user = users.stream()
                .filter(u -> u.getEmail().equals("test@email.com"))
                .findFirst()
                .orElse(null);

        assertNotNull(user);

        serviceUser.supprimer(user.getId());

        User deleted = serviceUser.getUserByEmailAndPassword(
                "test@email.com",
                "123456"
        );

        assertNull(deleted);
    }
}
