package org.genedb.web.mvc.controller.download;

import java.util.List;

public interface DataFetcher <T> {

    TroubleTrackingIterator<DataRow> iterator(List<T> ids, String fieldDelim);

}
