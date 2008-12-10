package org.genedb.util;

public class Triplet<S, T, V> {

    private S first;
    private T second;
    private V third;

    public Triplet(S first, T second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    public V getThird() {
        return third;
    }

}
