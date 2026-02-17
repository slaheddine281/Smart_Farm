package entities;

public class User {

    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String role;
    private String photoProfessionelle;

    public User() {}

    public User(int id, String username, String email, String passwordHash, String role, String photoProfessionelle) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.photoProfessionelle = photoProfessionelle;
    }

    public User(String username, String email, String passwordHash, String role, String photoProfessionelle) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.photoProfessionelle = photoProfessionelle;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getPhotoProfessionelle() { return photoProfessionelle; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setRole(String role) { this.role = role; }
    public void setPhotoProfessionelle(String photoProfessionelle) { this.photoProfessionelle = photoProfessionelle; }

    @Override
    public String toString() {
        return username + " | " + email + " | " + role;
    }
}