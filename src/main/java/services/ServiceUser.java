package services;

import entities.User;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceUser {

    private  static Connection connection;

    public ServiceUser() {
        connection = MyDatabase.getInstance().getConnection();
    }


    public void ajouter(User u) throws SQLException {
        System.out.println("🔍 Inserting user: " + u.getUsername() + " | " + u.getEmail() + " | " + u.getRole());

        String sql = "INSERT INTO users(username, email, password_hash, role , photo_Professionelle) VALUES(?, ?, ?, ? , ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getPhotoProfessionelle());
            int rows = ps.executeUpdate();
            System.out.println("✅ Rows inserted: " + rows);
        }
    }

    // tasti l mail mawjoud wela la
    private boolean emailExiste(String email) throws SQLException {
        String req = "SELECT COUNT(*) FROM users WHERE email = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    // traja3 l urers lkol
    public List<User> recuperer() throws SQLException {
        List<User> users = new ArrayList<>();
        String req = "SELECT * FROM users";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("role"),
                    rs.getString("photo_professionelle")
            ));
        }
        return users;
    }

    // traja3 l user bl  email w mot de passe
    public User getUserByEmailAndPassword(String email, String password) throws SQLException {
        String req = "SELECT * FROM users WHERE email = ? AND password_hash = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, email);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("role"),
                    rs.getString("photo_professionelle")
            );
        }
        return null;
    }

    // tfasa5 user bel ID
    public void supprimer(int userId) throws SQLException {
        String req = "DELETE FROM users WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, userId);
        ps.executeUpdate();
    }

    // tmodifi l user
    public void modifier(User user) throws SQLException {
        String req = "UPDATE users SET username = ?, email = ?, password_hash = ?, role = ?, photo_professionelle = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, user.getUsername());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getPasswordHash());
        ps.setString(4, user.getRole());
        ps.setString(5, user.getPhotoProfessionelle());
        ps.setInt(6, user.getId());
        ps.executeUpdate();
    }
}