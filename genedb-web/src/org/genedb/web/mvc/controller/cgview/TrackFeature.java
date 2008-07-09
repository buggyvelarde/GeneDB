package org.genedb.web.mvc.controller.cgview;

public class TrackFeature {

    int start;
    int end;

    public TrackFeature(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public boolean isOverZero() {
        return (start > end);
    }

    @Override
    public String toString() {
        return (end - start) + " bp (" + start + "-" + end + ")";
    }

}
