package nl.utwente.di.OVSoftware;

import nl.utwente.di.OVSoftware.models.Employee;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeTest {
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee(1337, "Hans", "A");
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getId() {
        assertEquals(1337, employee.getId());
    }

    @Test
    void getName() {
        assertEquals("Hans", employee.getName());
    }

    @Test
    void getStatus() {
        assertEquals("A", employee.getStatus());
    }

    @Test
    void addPayrates() {
        //TODO
    }

    @Test
    void getPayrates() {
        //TODO
    }

    @Test
    void toStringTest() {
        assertEquals("1337 Hans A", employee.toString());
    }
}