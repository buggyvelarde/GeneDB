package org.genedb.web.mvc.controller.download;

import java.util.Iterator;
import java.util.List;

public interface TroubleTrackingIterator<E> extends Iterator<E> {
    List<Integer> getProblems();

}
