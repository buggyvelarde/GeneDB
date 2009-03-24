package org.genedb.web.mvc.controller.download;

import org.genedb.querying.core.Query;
import org.genedb.querying.tmpquery.GeneSummary;

import java.io.Serializable;
import java.util.List;

public class ResultEntry implements Serializable {

    List<GeneSummary> results;

    Query query;

    int numOfResults;

    String queryName;

}
