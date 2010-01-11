package org.genedb.db.loading;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.genedb.db.loading.EmblLocation;
import org.genedb.db.loading.ParsingException;

import org.junit.Test;

import java.util.List;

public class EmblLocationTest {

    private static String[] roundTripLocations = new String[] {
        "1..100",
        "12^13",
        "complement(12^13)",
        "acc123.1:12..100",
        "Database::acc123.1:12..100",
        "complement(join(1..2,3..4,5^6))",
        "complement(join(1..2,3..4,XYZ1.5:5^6))",
        "order(complement(100..110),complement(1..5))",
        "join(complement(100..110),complement(1..5))",
        "<1..100", "1..>100", "<1..>100"
    };

    private static String[] nonCanonicalLocations = new String[] {
        "23", "23..23",
        "<1", "1..1",
    };

    private static String[] invalidLocations = new String[] {
        "12^14",
        "10..5",
        ">10..100",
        "join(10..100",
        "join(10 .. 100)",
        "join(complement(1..10),complement(20..30))",
        ""
    };

    private static String[][] parts = new String[][] {
        new String[] {"join(1..2,3..4)", "1..2", "3..4"},
        new String[] {"complement(complement(1..2))", "1..2"},
        new String[] {"complement(join(1..2,3..4))", "complement(3..4)", "complement(1..2)"},
        new String[] {"join(complement(3..4),complement(1..2))", "complement(3..4)", "complement(1..2)"},
    };

    @Test
    public void roundTrip() throws ParsingException {
        for (String roundTripLocation: roundTripLocations) {
            assertEquals(roundTripLocation, EmblLocation.parse(roundTripLocation).toString());
        }
    }

    @Test
    public void invalid() {
        for (String invalidLocation: invalidLocations) {
            boolean threwException = false;
            try {
                EmblLocation.parse(invalidLocation);
            } catch (ParsingException e) {
                threwException = true;
            }
            assertTrue(String.format("The location '%s' should have failed to parse", invalidLocation), threwException);
        }
    }

    @Test
    public void nonCanonical() throws ParsingException {
        assertEquals(0, nonCanonicalLocations.length % 2);
        for(int i=0; i < nonCanonicalLocations.length; i += 2) {
            assertEquals(nonCanonicalLocations[i+1], EmblLocation.parse(nonCanonicalLocations[i]).toString());
        }
    }

    @Test
    public void parts() throws ParsingException {
        for (String[] partSpec: parts) {
            EmblLocation location = EmblLocation.parse(partSpec[0]);
            List<EmblLocation> parts = location.getParts();
            assertEquals(partSpec.length - 1, parts.size());
            for (int i=0; i < parts.size(); i++) {
                assertEquals(partSpec[i+1], parts.get(i).toString());
            }
        }
    }

    private void testMMS(String locationString, int fmin, int fmax, int strand) throws ParsingException {
        EmblLocation location = EmblLocation.parse(locationString);
        assertEquals(fmin, location.getFmin());
        assertEquals(fmax, location.getFmax());
        assertEquals(strand, location.getStrand());
    }

    @Test
    public void minMaxStrand() throws ParsingException {
        testMMS("1..2", 0, 2, 1);
        testMMS("1^2",  1, 1, 1);
        testMMS("complement(1..10)", 0, 10, -1);
        testMMS("join(10..20,100..110)", 9, 110, 1);
        testMMS("join(complement(100..110),complement(10..20))", 9, 110, -1);
        testMMS("complement(join(10..20,100..110))", 9, 110, -1);
    }
}
