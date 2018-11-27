package nl.utwente.di.OVSoftware;

import nl.utwente.di.OVSoftware.models.OVAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class OVAccountTest {

	private OVAccount acc = new OVAccount("a", "123");

	@BeforeEach
	public void setUp() {

	}

	@Test
	public void testUsernameAndPassword() {
		assertNotNull(acc.getUsername());
		assertNotNull(acc.getPassword());
		assertTrue(acc.getUsername().equals("a"));
		assertTrue(acc.getPassword().equals("123"));
	}

}
