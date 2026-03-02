package services;

import entities.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
//import org.mindrot.jbcrypt.BCrypt;

public class ServiceUser implements IService<User> {

    private final Connection connection;

    public ServiceUser(Connection connection) {
        this.connection = connection;
    }

    private String generateCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    private boolean emailExiste(String email) throws SQLException {
        String req = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void ajouter(User u) throws SQLException {
        String sql = "INSERT INTO users(username, email, password_hash, role, photo_professionelle) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getPhotoProfessionelle());
            ps.executeUpdate();
        }
    }

    public List<User> recuperer() throws SQLException {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM users";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"),
                        rs.getString("photo_professionelle")));
            }
        }
        return users;
    }

    public User getUserByEmailAndPassword(String email, String password) throws SQLException {
        String req = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            rs.getString("password_hash"),
                            rs.getString("role"),
                            rs.getString("photo_professionelle"));
                }
            }
        }
        return null;
    }

    // Implements IService<User>.supprimer(User)
    @Override
    public void supprimer(User user) throws SQLException {
        supprimer(user.getId());
    }

    public void supprimer(int userId) throws SQLException {
        String req = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void modifier(User user) throws SQLException {
        String req = "UPDATE users SET username = ?, email = ?, password_hash = ?, role = ?, photo_professionelle = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getPhotoProfessionelle());
            ps.setInt(6, user.getId());
            ps.executeUpdate();
        }
    }

    // ✅ CORRIGÉ : méthode NON statique
    public void sendVerificationCode(String email) throws SQLException {
        if (!emailExiste(email)) {
            throw new SQLException("Aucun utilisateur trouvé avec cet email : " + email);
        }

        String code = generateCode();
        String sql = "UPDATE users SET verification_code = ? WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, email);
            ps.executeUpdate();
        }

        // Send the code by email
        try {
            utils.EmailUtil.sendVerificationCode(email, code);
        } catch (Exception e) {
            throw new SQLException("Code sauvegardé mais envoi email échoué : " + e.getMessage());
        }
    }

    // ✅ Méthode non statique — correcte
    public boolean verifyEmailCode(String email, String code) throws SQLException {
        String sql = "SELECT verification_code FROM users WHERE email = ? AND verification_code = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Supprimer le code après utilisation
                    String clearSql = "UPDATE users SET verification_code = NULL WHERE email = ?";
                    try (PreparedStatement ps2 = connection.prepareStatement(clearSql)) {
                        ps2.setString(1, email);
                        ps2.executeUpdate();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // ✅ Méthode non statique — correcte
    public void resetPassword(String email, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public int countByRole(String role) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, role);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}