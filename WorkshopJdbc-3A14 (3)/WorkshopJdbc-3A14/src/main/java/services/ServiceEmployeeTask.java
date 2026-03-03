package services;

import entities.EmployeeTask;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceEmployeeTask {

    private Connection connection;
    private GoogleCalendarService calendarService;
    private WhatsAppService whatsAppService; // ✅ NOUVEAU : Service WhatsApp
    private static final Logger LOGGER = Logger.getLogger(ServiceEmployeeTask.class.getName());

    public ServiceEmployeeTask() {
        connection = MyDatabase.getInstance().getConnection();

        // ✅ Initialiser Google Calendar Service
        try {
            this.calendarService = new GoogleCalendarService();
            LOGGER.info("✅ Google Calendar Service initialisé");
        } catch (Exception e) {
            LOGGER.warning("⚠️ Google Calendar non disponible : " + e.getMessage());
            this.calendarService = null;
        }

        // ✅ NOUVEAU : Initialiser WhatsApp Service
        try {
            this.whatsAppService = new WhatsAppService();
            LOGGER.info("✅ WhatsApp Service initialisé");
        } catch (Exception e) {
            LOGGER.warning("⚠️ WhatsApp non disponible : " + e.getMessage());
            this.whatsAppService = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ MÉTHODE 1 : Ajouter avec Calendar uniquement (existant)
    // ─────────────────────────────────────────────────────────────

    public void ajouterAvecCalendar(EmployeeTask task, String eventType) {
        try {
            ajouter(task);
            LOGGER.info("✅ Tâche ajoutée en base (ID: " + task.getId() + ")");

            if (calendarService != null) {
                creerEvenementCalendar(task, eventType);
                LOGGER.info("📅 Événement Calendar créé avec succès !");
            } else {
                LOGGER.warning("⚠️ Service Calendar indisponible");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur SQL : " + e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'ajout de la tâche", e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "⚠️ Erreur Calendar : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ MÉTHODE 2 : NOUVELLE - Ajouter avec Calendar + WhatsApp
    // ─────────────────────────────────────────────────────────────

    /**
     * Ajoute une tâche avec Google Calendar ET notification WhatsApp
     * @param task La tâche à ajouter
     * @param eventType Type d'événement Calendar
     * @param employeePhone Numéro WhatsApp de l'employé (ex: +21650093975)
     * @param employeeName Nom complet de l'employé
     */
    public void ajouterAvecCalendarEtWhatsApp(EmployeeTask task, String eventType,
                                              String employeePhone, String employeeName) {
        try {
            // 1️⃣ Ajouter la tâche en base de données
            ajouter(task);
            LOGGER.info("✅ Tâche ajoutée en base (ID: " + task.getId() + ")");

            // 2️⃣ Créer l'événement Google Calendar
            if (calendarService != null) {
                creerEvenementCalendar(task, eventType);
                LOGGER.info("📅 Événement Calendar créé !");
            } else {
                LOGGER.warning("⚠️ Calendar indisponible");
            }

            // 3️⃣ ✅ Envoyer notification WhatsApp
            if (whatsAppService != null && employeePhone != null && !employeePhone.isEmpty()) {
                whatsAppService.sendTaskNotification(
                        employeePhone,
                        employeeName,
                        task.getTaskDescription(),
                        task.getTaskDate().toString()
                );
                LOGGER.info("📱 Notification WhatsApp envoyée à " + employeePhone);
            } else {
                LOGGER.warning("⚠️ WhatsApp indisponible ou numéro manquant");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "❌ Erreur SQL : " + e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'ajout de la tâche", e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "⚠️ Erreur d'intégration : " + e.getMessage(), e);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ MÉTHODE 3 : NOUVELLE - Envoyer WhatsApp uniquement
    // ─────────────────────────────────────────────────────────────

    /**
     * Envoyer une notification WhatsApp sans créer de tâche
     * Utile pour les rappels ou mises à jour
     */
    public boolean envoyerNotificationWhatsApp(String employeePhone, String employeeName,
                                               String message) {
        if (whatsAppService == null) {
            LOGGER.warning("⚠️ WhatsApp Service non disponible");
            return false;
        }

        try {
            whatsAppService.sendWhatsAppMessage(employeePhone, message);
            LOGGER.info("📱 Message WhatsApp envoyé à " + employeePhone);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "❌ Erreur envoi WhatsApp : " + e.getMessage(), e);
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ MÉTHODE PRIVÉE : Créer événement Calendar (inchangée)
    // ─────────────────────────────────────────────────────────────

    private void creerEvenementCalendar(EmployeeTask task, String eventType) throws Exception {
        String employeeName = task.getEmployeeName() != null ? task.getEmployeeName() : "Employé #" + task.getEmployeeId();
        String position = task.getEmployeePosition() != null ? task.getEmployeePosition() : "";
        LocalDate taskDate = task.getTaskDate();
        String description = task.getTaskDescription();

        switch (eventType.toLowerCase()) {
            case "soin_animal":
                calendarService.createAnimalCareEvent(employeeName, description, taskDate, "Soin programmé - Smart Farm");
                break;
            case "traitement_veterinaire":
                calendarService.createVetTreatmentEvent(employeeName, description, taskDate, "Dr. Vétérinaire");
                break;
            case "tache_agricole":
                calendarService.createAgriculturalTaskEvent(description, taskDate, employeeName);
                break;
            case "shift_employe":
                LocalDateTime shiftStart = taskDate.atTime(8, 0);
                LocalDateTime shiftEnd = taskDate.atTime(17, 0);
                calendarService.createEmployeeShiftEvent(employeeName, position, shiftStart, shiftEnd);
                break;
            case "general":
            default:
                LocalDateTime start = taskDate.atTime(9, 0);
                LocalDateTime end = taskDate.atTime(10, 0);
                calendarService.createEvent(
                        "📋 Tâche : " + description.substring(0, Math.min(30, description.length())),
                        "Assigné à : " + employeeName + "\n" + description,
                        start, end, "Smart Farm");
                break;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ MÉTHODES CRUD EXISTANTES (inchangées)
    // ─────────────────────────────────────────────────────────────

    public void ajouter(EmployeeTask task) throws SQLException {
        String req = "INSERT INTO employee_tasks (employee_id, task_description, task_date, rating) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, task.getEmployeeId());
        ps.setString(2, task.getTaskDescription());
        ps.setDate(3, Date.valueOf(task.getTaskDate()));
        ps.setInt(4, task.getRating());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            task.setId(rs.getInt(1));
        }
    }

    public void modifier(EmployeeTask task) throws SQLException {
        String req = "UPDATE employee_tasks SET employee_id=?, task_description=?, task_date=?, rating=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, task.getEmployeeId());
        ps.setString(2, task.getTaskDescription());
        ps.setDate(3, Date.valueOf(task.getTaskDate()));
        ps.setInt(4, task.getRating());
        ps.setInt(5, task.getId());
        ps.executeUpdate();
    }

    public void updateRating(int taskId, int rating) throws SQLException {
        String req = "UPDATE employee_tasks SET rating=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, rating);
        ps.setInt(2, taskId);
        ps.executeUpdate();
    }

    public void supprimer(EmployeeTask task) throws SQLException {
        String req = "DELETE FROM employee_tasks WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, task.getId());
        ps.executeUpdate();
    }

    public List<EmployeeTask> recuperer() throws SQLException {
        List<EmployeeTask> list = new ArrayList<>();
        String req = "SELECT et.id, et.employee_id, et.task_description, et.task_date, et.rating, " +
                "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, " +
                "e.position AS employee_position " +
                "FROM employee_tasks et " +
                "JOIN employees e ON et.employee_id = e.id " +
                "ORDER BY et.task_date DESC";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            EmployeeTask task = new EmployeeTask(
                    rs.getInt("id"), rs.getInt("employee_id"),
                    rs.getString("task_description"),
                    rs.getDate("task_date").toLocalDate()
            );
            task.setEmployeeName(rs.getString("employee_name"));
            task.setEmployeePosition(rs.getString("employee_position"));
            task.setRating(rs.getInt("rating"));
            list.add(task);
        }
        return list;
    }

    public List<EmployeeTask> recupererParEmploye(int employeeId) throws SQLException {
        List<EmployeeTask> list = new ArrayList<>();
        String req = "SELECT et.id, et.employee_id, et.task_description, et.task_date, et.rating, " +
                "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, " +
                "e.position AS employee_position " +
                "FROM employee_tasks et " +
                "JOIN employees e ON et.employee_id = e.id " +
                "WHERE et.employee_id=? ORDER BY et.task_date DESC";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, employeeId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            EmployeeTask task = new EmployeeTask(
                    rs.getInt("id"), rs.getInt("employee_id"),
                    rs.getString("task_description"),
                    rs.getDate("task_date").toLocalDate()
            );
            task.setEmployeeName(rs.getString("employee_name"));
            task.setEmployeePosition(rs.getString("employee_position"));
            task.setRating(rs.getInt("rating"));
            list.add(task);
        }
        return list;
    }

    public double getAverageRatingForEmployee(int employeeId) throws SQLException {
        String req = "SELECT AVG(rating) as avg_rating FROM employee_tasks WHERE employee_id=? AND rating > 0";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, employeeId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getDouble("avg_rating");
        }
        return 0.0;
    }

    public List<EmployeeTask> recupererParRating(int minRating) throws SQLException {
        List<EmployeeTask> list = new ArrayList<>();
        String req = "SELECT et.id, et.employee_id, et.task_description, et.task_date, et.rating, " +
                "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, " +
                "e.position AS employee_position " +
                "FROM employee_tasks et " +
                "JOIN employees e ON et.employee_id = e.id " +
                "WHERE et.rating >= ? ORDER BY et.rating DESC, et.task_date DESC";

        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, minRating);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            EmployeeTask task = new EmployeeTask(
                    rs.getInt("id"), rs.getInt("employee_id"),
                    rs.getString("task_description"),
                    rs.getDate("task_date").toLocalDate()
            );
            task.setEmployeeName(rs.getString("employee_name"));
            task.setEmployeePosition(rs.getString("employee_position"));
            task.setRating(rs.getInt("rating"));
            list.add(task);
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    // ✅ GETTERS POUR VÉRIFIER LA DISPONIBILITÉ DES SERVICES
    // ─────────────────────────────────────────────────────────────

    public boolean isCalendarAvailable() {
        return calendarService != null;
    }

    public boolean isWhatsAppAvailable() { // ✅ NOUVEAU
        return whatsAppService != null;
    }
}