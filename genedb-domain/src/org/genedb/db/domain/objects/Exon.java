package org.genedb.db.domain.objects;

import java.io.Serializable;

public class Exon implements Serializable {
    private int start, stop;
    
    public Exon() { }
    public Exon(int start, int stop) {
        if (start >= stop)
            throw new IllegalArgumentException(String.format("Exon specified as %d..%d; %1$d >= %2$d", start, stop));
        this.start = start;
        this.stop = stop;
    }

    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }
    public void setStop(int stop) {
        this.stop = stop;
    }
}
