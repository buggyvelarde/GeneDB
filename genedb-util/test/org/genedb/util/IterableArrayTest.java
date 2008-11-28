package org.genedb.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Test the IterableArray class.
 *
 * @author rh11
 */
public class IterableArrayTest {
    @Test
    public void testEmpty() {
        IterableArray<?> ia = new IterableArray<Object>(new Object[0]);
        for (Object o: ia) {
            fail("Iterating over an empty array: " + o);
        }
    }

    @Test
    public void testNonEmpty() {
        Integer[] originalArray = new Integer[] {1,2,3,23,12,-9};
        List<Integer> original = new ArrayList<Integer>();
        Collections.addAll(original, originalArray);

        List<Integer> fromIterator = new ArrayList<Integer>();
        for (Integer i: IterableArray.fromArray(originalArray)) {
            fromIterator.add(i);
        }
        assertEquals(original, fromIterator);
    }

    @Test
    public void testOverrun() {
        Iterator<String> it = IterableArray.fromArray(new String[] {"foo", "bar"}).iterator();
        assertTrue(it.hasNext());
        assertEquals("foo", it.next());
        assertTrue(it.hasNext());
        assertEquals("bar", it.next());
        assertFalse(it.hasNext());
        try {
            it.next();
            fail("Expected NoSuchElementException not thrown");
        } catch (Exception e) {
            assertTrue("An exception was thrown, but not a NoSuchElementException: "+e,
                e instanceof NoSuchElementException);
        }
    }
}
