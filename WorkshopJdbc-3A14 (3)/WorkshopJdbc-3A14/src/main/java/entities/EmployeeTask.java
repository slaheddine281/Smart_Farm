package entities;

import java.time.LocalDate;

public class EmployeeTask {

    private int id;
    private int employeeId;
    private String employeeName;      // ✅ NOUVEAU - Nom complet de l'employé
    private String employeePosition;  // ✅ NOUVEAU - Position de l'employé
    private String taskDescription;
    private LocalDate taskDate;

    // ── Constructeurs ──────────────────────────────
    public EmployeeTask() {}

    public EmployeeTask(int id, int employeeId, String taskDescription, LocalDate taskDate) {
        this.id = id;
        this.employeeId = employeeId;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
    }

    public EmployeeTask(int employeeId, String taskDescription, LocalDate taskDate) {
        this.employeeId = employeeId;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
    }

    // ── Getters & Setters existants ───────────────
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }

    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }

    // ── NOUVEAUX Getters & Setters ────────────────
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEmployeePosition() { return employeePosition; }
    public void setEmployeePosition(String employeePosition) { this.employeePosition = employeePosition; }

    // ✅ Méthode utile pour l'affichage dans la TableView
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
                "', description='" + taskDescription + "', date=" + taskDate + "}";
    }
}