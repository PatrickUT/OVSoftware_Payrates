package nl.utwente.di.OVSoftware;

import nl.utwente.di.OVSoftware.exceptions.DateException;
import nl.utwente.di.OVSoftware.models.Payrates;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PayratesTest {
    private Payrates payrates;

    @BeforeEach
    void setUp() {
        try {
            payrates = new Payrates(1337, 523.014, "2018-01-01", "2019-01-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getId() {
        assertEquals(1337, payrates.getId());
    }

    @Test
    void getCost() {
        assertEquals(523.014, payrates.getCost());
    }

    @Test
    void getStartDate() {
        assertEquals("2018-01-01", payrates.getStartDate());
    }

    @Test
    void getEndDate() {
        assertEquals("2019-01-01", payrates.getEndDate());
    }

    @Test
    void ValidDateTest() {
        List<Payrates> temp = new ArrayList<Payrates>();
        try {
            temp.add(new Payrates(1, 60, "2016-02-01", "2017-02-05"));
            temp.add(new Payrates(1, 60, "2019-02-06", "2019-02-07"));
            temp.add(new Payrates(1, 60, "2017-02-06", "2018-02-05"));
            temp.add(new Payrates(2, 60, "2016-02-03", "2017-02-05"));
            temp.add(new Payrates(1, 60, "2018-02-06", "2019-02-05"));
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        try {
            Payrates.checkIntegrity(temp);
        } catch (DateException e) {
            fail("These dates are valid");
        }
    }

    @Test
    void InalidDateTest() {
        List<Payrates> temp = new ArrayList<Payrates>();
        try {
            temp.add(new Payrates(1, 60, "2016-02-01", "2017-02-04"));
            temp.add(new Payrates(1, 60, "2019-02-06", "2019-02-07"));
            temp.add(new Payrates(1, 60, "2017-02-06", "2018-02-05"));
            temp.add(new Payrates(2, 60, "2016-02-03", "2017-02-05"));
            temp.add(new Payrates(1, 60, "2018-02-06", "2019-02-05"));
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        try {
            Payrates.checkIntegrity(temp);
            fail("These dates are invalid");
        } catch (DateException e) {
            assertTrue(e.getMessage().equals("3: 2017-02-04 2017-02-06 Start date is not next day from previous end date"));
        }
    }

    @Test
    void WrongStartEndTest() {
        List<Payrates> temp = new ArrayList<Payrates>();
        try {
            temp.add(new Payrates(1, 60, "2018-02-01", "2017-02-05"));
            temp.add(new Payrates(1, 60, "2019-02-06", "2019-02-07"));
            temp.add(new Payrates(1, 60, "2017-02-06", "2018-02-05"));
            temp.add(new Payrates(2, 60, "2016-02-03", "2017-02-05"));
            temp.add(new Payrates(1, 60, "2018-02-06", "2019-02-05"));
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        try {
            Payrates.checkIntegrity(temp);
            fail("These start and end dates are valid");
        } catch (DateException e) {
            assertTrue(e.getMessage().equals("1: 2018-02-01 2017-02-05 Start date after end date"));
        }
    }
}