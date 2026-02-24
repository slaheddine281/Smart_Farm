package entities;

public class Employee {

    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String position;

    public Employee() {}

    public Employee(String firstName, String lastName, String phone, String position) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.position = position;
    }

    public Employee(int id, String firstName, String lastName, String phone, String position) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.position = position;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
}