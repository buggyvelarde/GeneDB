package org.genedb.web.mvc.controller.download;

import java.util.Iterator;
import java.util.List;

public interface DataFetcher <T> {

    Iterator<DataRow> iterator(List<T> ids, String fieldDelim);

}
