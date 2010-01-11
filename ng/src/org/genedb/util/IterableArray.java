package org.genedb.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple wrapper that implements the Iterable interface for arrays.
 *
 * @author rh11
 *
 * @param <T>
 */
public class IterableArray<T> implements Iterable<T> {
    private T[] array;
    /**
     * Create an iterator for the specified array.
     * @param <S> the type of the array elements
     * @param array
     */
    public <S extends T> IterableArray(S[] array) {
        this.array = array;
    }
    /**
     * Create an iterator for the specified array.
     * The effect is the same as calling the constructor, except
     * that the compiler will infer the generic type from the type
     * of the array.
     * @param <T>
     * @param array the array
     * @return the IterableArray
     */
    public static <T> IterableArray<T> fromArray(T[] array) {
        return new IterableArray<T>(array);
    }
    /**
     * {@inheritDoc}
     * <p>
     * This iterator does not support the <code>remove()</code> operation.
     */
    public Iterator<T> iterator() {
        return new Iterator<T> () {
            int i = 0;

            public boolean hasNext() {
                return i < array.length;
            }

            public T next() {
                if (i >= array.length) {
                    throw new NoSuchElementException();
                }
                return array[i++];
            }

            public void remove() {
                throw new UnsupportedOperationException("IterableArray does not support removal");
            }
        };
    }

}
