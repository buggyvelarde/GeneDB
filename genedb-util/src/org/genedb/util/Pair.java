package org.genedb.util;

public class Pair<S, T> {

    private S first;
    private T second;

    Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

}