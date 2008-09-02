package org.genedb.db.loading.alternative;

import static org.junit.Assert.*;
import org.junit.Test;

public class EmblLocationTest {

    private static String[] roundTripLocations = new String[] {
        "1..100",
        "12^13",
        "acc123.1:12..100",
        "Database::acc123.1:12..100",
        "complement(join(1..2,3..4,5^6))",
        "complement(join(1..2,3..4,XYZ1.5:5^6))",
        "order(complement(1..5),complement(100..110))",
    };

    private static String[] nonCanonicalLocations = new String[] {
        "23", "23..23",
        "<1", "1..1",
        "<10..>100", "10..100",
    };

    private static String[] invalidLocations = new String[] {
        "12^14",
        "10..5",
        ">10..100",
        ""
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
            assertTrue(threwException);
        }
    }

    @Test
    public void nonCanonical() throws ParsingException {
        assertEquals(0, nonCanonicalLocations.length % 2);
        for(int i=0; i < nonCanonicalLocations.length; i += 2) {
            assertEquals(nonCanonicalLocations[i+1], EmblLocation.parse(nonCanonicalLocations[i]).toString());
        }
    }
}
