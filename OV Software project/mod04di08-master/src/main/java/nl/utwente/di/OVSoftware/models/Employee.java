package nl.utwente.di.OVSoftware.models;

public class Employee {
    private final int id;
    private final String name;
    private final String status;

    //Employees are used to store employee data from the database.
    public Employee(int i, String name, String status) {
        id = i;
        this.name = name;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String toString() {
        return getId() + " " + getName() + " " + getStatus();
    }

}
