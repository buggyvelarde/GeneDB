package org.genedb.web.gui;

import java.util.BitSet;

/**
 * A BitSet that cannot be modified.
 *
 * @author rh11
 *
 */
public class UnmodifiableBitSet extends BitSet {

    public UnmodifiableBitSet() {
        // empty
    }

    public UnmodifiableBitSet(BitSet set) {
        super();
        super.or(set);
    }

    @Override
    public void and(BitSet set) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void andNot(BitSet set) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void clear() {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void clear(int bitIndex) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void flip(int bitIndex) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void or(BitSet set) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void set(int bitIndex, boolean value) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void set(int fromIndex, int toIndex) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void set(int bitIndex) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }

    @Override
    public void xor(BitSet set) {
        throw new RuntimeException("Cannot modify an UnmodifiableBitSet");
    }
}
