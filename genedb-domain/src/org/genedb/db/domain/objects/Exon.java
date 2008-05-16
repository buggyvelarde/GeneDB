package org.genedb.db.domain.objects;

import java.io.Serializable;

public class Exon implements Serializable, Comparable<Exon> {
    private int start, end;
    
    /**
     * Implements the "leftmost-first then longest-first" rule.
     * 
     * An Exon should only be compared to others on the same strand of the
     * same chromosome, otherwise the results are undefined.
     */
    public int compareTo(Exon other) {
        int ret = this.start - other.start;
        if (ret != 0)
            return ret;
        return other.end - this.end;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + start;
        result = prime * result + end;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        final Exon other = (Exon) obj;
        return (this.start == other.start && this.end == other.end);
    }

    public Exon() { }
    public Exon(int start, int stop) {
        if (start >= stop)
            throw new IllegalArgumentException(String.format("Exon specified as %d..%d; %1$d >= %2$d", start, stop));
        this.start = start;
        this.end = stop;
    }

    public int getStart() {
        return start;
    }
    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }
    public void setEnd(int stop) {
        this.end = stop;
    }
}
