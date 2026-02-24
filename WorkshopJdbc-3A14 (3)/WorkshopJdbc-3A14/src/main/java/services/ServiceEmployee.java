package services;

import entities.Employee;
import utils.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceEmployee implements IService<Employee> {

    private Connection connection;

    public ServiceEmployee() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Employee emp) throws SQLException {
        String req = "INSERT INTO employees (first_name, last_name, phone, position) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, emp.getFirstName());
        ps.setString(2, emp.getLastName());
        ps.setString(3, emp.getPhone());
        ps.setString(4, emp.getPosition());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Employee emp) throws SQLException {
        String req = "UPDATE employees SET first_name=?, last_name=?, phone=?, position=? WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setString(1, emp.getFirstName());
        ps.setString(2, emp.getLastName());
        ps.setString(3, emp.getPhone());
        ps.setString(4, emp.getPosition());
        ps.setInt(5, emp.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(Employee emp) throws SQLException {
        String req = "DELETE FROM employees WHERE id=?";
        PreparedStatement ps = connection.prepareStatement(req);
        ps.setInt(1, emp.getId());
        ps.executeUpdate();
    }

    @Override
    public List<Employee> recuperer() throws SQLException {
        List<Employee> list = new ArrayList<>();
        String req = "SELECT * FROM employees";
        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            list.add(new Employee(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("phone"),
                    rs.getString("position")
            ));
        }
        return list;
    }
}