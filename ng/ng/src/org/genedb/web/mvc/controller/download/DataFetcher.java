package org.genedb.web.mvc.controller.download;

import java.util.List;

public interface DataFetcher <T> {

    TroubleTrackingIterator<String> iterator(List<T> ids, String expression, String fieldDelim);

}
