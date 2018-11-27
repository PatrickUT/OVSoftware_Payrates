package nl.utwente.di.OVSoftware;

import nl.utwente.di.OVSoftware.models.GoogleAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GoogleAccountTest {

	private GoogleAccount acc = new GoogleAccount("group8@student.utwente.nl");

	@BeforeEach
	public void setUp() {

	}

	@Test
	public void testUsernameAndPassword() {
		assertNotNull(acc.getEmail());
		assertTrue(acc.getEmail().equals("group8@student.utwente.nl"));
	}

}
