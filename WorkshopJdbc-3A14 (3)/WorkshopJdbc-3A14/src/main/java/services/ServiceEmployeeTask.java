package services;

import entities.EmployeeTask;
import utils.MyDatabase;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ServiceEmployeeTask {

    private Connection connection;

    public ServiceEmployeeTask() {
        connection = MyDatabase.getInstance().getConnection();
    }

    // ── CREATE ─────────────────────────────────────
    public void ajouter(EmployeeTask task) throws SQLException {
        String req = "INSERT INTO employee_tasks (employee_id, task_description, task_date) VALUES (?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, task.getEmployeeId());
        ps.setString(2, task.getTaskDescription());
        ps.setDate(3, Date.valueOf(task.getTaskDate()));
        ps.executeUpdate();
    }

    // ── UPDATE ─────────────────────────────────────
    public void modifier(EmployeeTask task) throws SQLException {
        String req = "UPDATE employee_tasks SET employee_id=?, task_description=?, task_date=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, task.getEmployeeId());
        ps.setString(2, task.getTaskDescription());
        ps.setDate(3, Date.valueOf(task.getTaskDate()));
        ps.setInt(4, task.getId());
        ps.executeUpdate();
    }

    // ── DELETE ─────────────────────────────────────
    public void supprimer(EmployeeTask task) throws SQLException {
        String req = "DELETE FROM employee_tasks WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, task.getId());
        ps.executeUpdate();
    }

    // ── READ ALL avec JOINTURE ────────────────────
    public List<EmployeeTask> recuperer() throws SQLException {
        List<EmployeeTask> list = new ArrayList<>();
        

        String req = "SELECT et.id, et.employee_id, et.task_description, et.task_date, " +
                     "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, " +
                     "e.position AS employee_position " +
                     "FROM employee_tasks et " +
                     "JOIN employees e ON et.employee_id = e.id " +
                     "ORDER BY et.task_date DESC";
        
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            EmployeeTask task = new EmployeeTask(
                rs.getInt("id"),
                rs.getInt("employee_id"),
                rs.getString("task_description"),
                rs.getDate("task_date").toLocalDate()
            );
            // ✅ AJOUTÉ - Remplir les nouveaux champs
            task.setEmployeeName(rs.getString("employee_name"));
            task.setEmployeePosition(rs.getString("employee_position"));
            list.add(task);
        }
        return list;
    }

    // ── READ by Employee ID  ──────────
    public List<EmployeeTask> recupererParEmploye(int employeeId) throws SQLException {
        List<EmployeeTask> list = new ArrayList<>();
        

        String req = "SELECT et.id, et.employee_id, et.task_description, et.task_date, " +
                     "CONCAT(e.first_name, ' ', e.last_name) AS employee_name, " +
                     "e.position AS employee_position " +
                     "FROM employee_tasks et " +
                     "JOIN employees e ON et.employee_id = e.id " +
                     "WHERE et.employee_id=? " +
                     "ORDER BY et.task_date DESC";
        
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, employeeId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            EmployeeTask task = new EmployeeTask(
                rs.getInt("id"),
                rs.getInt("employee_id"),
                rs.getString("task_description"),
                rs.getDate("task_date").toLocalDate()
            );
            //
            task.setEmployeeName(rs.getString("employee_name"));
            task.setEmployeePosition(rs.getString("employee_position"));
            list.add(task);
        }
        return list;
    }
}
