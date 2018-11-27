package nl.utwente.di.OVSoftware;

import nl.utwente.di.OVSoftware.models.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TableTest {
	private Table table;

	@BeforeEach
	void setup() {
		table = new Table("name", "login", "user", "pass");
	}
	
	@AfterEach
    void tearDown() {
    }

    @Test
    void getName() {
        assertEquals("name", table.getName());
    }

    @Test
    void getLogin() {
        assertEquals("login", table.getLogin());
    }

    @Test
    void getUser() {
        assertEquals("user", table.getUser());
    }
    
    @Test
    void getPass() {
        assertEquals("pass", table.getPass());
    }
}

