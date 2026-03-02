

        package entities;

import java.time.LocalDate;

public class EmployeeTask {

    private int id;
    private int employeeId;
    private String employeeName;
    private String employeePosition;
    private String taskDescription;
    private LocalDate taskDate;
    private int rating; // ✅ NOUVEAU - Rating 0-5 étoiles

    // ── Constructeurs ──────────────────────────────
    public EmployeeTask() {}

    public EmployeeTask(int id, int employeeId, String taskDescription, LocalDate taskDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
        this.rating = 0; // Par défaut non noté
    }

    public EmployeeTask(int employeeId, String taskDescription, LocalDate taskDate) {
        this.employeeId = employeeId;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
        this.rating = 0;
    }

    // ── Getters & Setters ──────────────────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeePosition() { return employeePosition; }
    public void setEmployeePosition(String employeePosition) { this.employeePosition = employeePosition; }

    // ✅ NOUVEAU - Rating
    public int getRating() { return rating; }
    public void setRating(int rating) {
        if (rating >= 0 && rating <= 5) {
            this.rating = rating;
        }
    }

    // ✅ Méthode pour afficher les étoiles visuellement
    public String getRatingStars() {
        if (rating == 0) return "☆☆☆☆☆ (Not rated)";

        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                stars.append("⭐");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }

    // ✅ Méthode utile pour l'affichage
    public String getEmployeeDisplay() {
        if (employeeName != null && employeePosition != null) {
            return employeeName + " (" + employeePosition + ")";
        } else if (employeeName != null) {
            return employeeName;
        } else {
            return "Employee #" + employeeId;
        }
    }

    @Override
    public String toString() {
        return "Task{id=" + id + ", employee='" + employeeName +
                "', description='" + taskDescription +
                "', date=" + taskDate +
                ", rating=" + rating + "/5}";
    }
}










