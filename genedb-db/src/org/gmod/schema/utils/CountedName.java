package org.gmod.schema.utils;

/**
 * Class to store terms, eg products, and how many occurences there are
 * 
 * @author cp2
 */
public class CountedName {
    private String name;
    private int count;


    public CountedName(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public CountedName(String name, long count) {
        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(String.format("Count %ld is too large!", count));
        }
        
        this.name = name;
        this.count = (int) count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
