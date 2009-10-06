package org.genedb.web.mvc.controller.download;

import java.util.Iterator;
import java.util.List;

public interface DataFetcher {

    Iterator<DataRow> iterator(List<String> ids, String fieldDelim);

}
