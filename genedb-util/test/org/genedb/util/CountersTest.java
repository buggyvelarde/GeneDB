package org.genedb.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CountersTest {

    private Counters c = new Counters();

    @Test
    public void firstTime() {
        assertEquals(1, c.nextval("one"));
        assertEquals(1, c.nextval("two"));
    }

    @Test
    public void secondTime() {
        c.nextval("one"); assertEquals(2, c.nextval("one")); assertEquals(3, c.nextval("one"));
        c.nextval("two"); assertEquals(2, c.nextval("two")); assertEquals(3, c.nextval("two"));
        assertEquals(4, c.nextval("one"));
    }

    @Test
    public void reset() {
        assertEquals(1, c.nextval("x"));
        assertEquals(2, c.nextval("x"));
        c.clear();
        assertEquals(1, c.nextval("x"));
        assertEquals(2, c.nextval("x"));
    }
}
