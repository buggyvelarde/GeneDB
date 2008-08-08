/*
 * Copyright (c) 2007 Genome Research Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Library General Public License as published
 * by  the Free Software Foundation; either version 2 of the License or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public License
 * along with this program; see the file COPYING.LIB.  If not, write to
 * the Free Software Foundation Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307 USA
 */

package org.genedb.querying.core;


import org.genedb.querying.history.HistoryItem;
import org.genedb.querying.history.HistoryManager;
import org.genedb.querying.history.HistoryType;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

@Configurable
public abstract class LuceneQuery implements Query {

    private static final Logger logger = Logger.getLogger(LuceneQuery.class);

    @Autowired
    private LuceneIndexFactory luceneIndexFactory;

    private LuceneIndex luceneIndex;

    protected String name;

    @PostConstruct
    protected void afterPropertiesSet() {
    	luceneIndex = luceneIndexFactory.getIndex(getluceneIndexName());
    }


    protected abstract String getluceneIndexName();


	//private List<CachedParamDetails> cachedParamDetailsList = new ArrayList<CachedParamDetails>();
	//private Map<String, CachedParamDetails> cachedParamDetailsMap = new HashMap<String, CachedParamDetails>();

    public String getParseableDescription() {
        return QueryUtils.makeParseableDescription(name, getParamNames(), this);
    }

//    protected List<String> runQuery() {
//    	Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);
//
//    	org.hibernate.Query query = session.createQuery(getHql());
//    	populateQueryWithParams(query);
//
//    	@SuppressWarnings("unchecked") List<String> ret = query.list();
//    	return ret;
//    }


//	private void setQueryVarBasedOnType(org.hibernate.Query query, CachedParamDetails cpd) {
//
//		Type type = cpd.getType();
//
//		try {
//
//			if (type.equals(Integer.TYPE)) {
//				query.setInteger(cpd.getName(), cpd.getField().getInt(this));
//			}
//
//		} catch (IllegalArgumentException exp) {
//			throw new RuntimeException("Internal typing/access exception", exp);
//		} catch (IllegalAccessException exp) {
//			throw new RuntimeException("Internal typing/access exception", exp);
//		}
//	}

//	private void prepareCachedParamDetailsList() {
//		for (Field field : this.getClass().getFields()) {
//    		Annotation annotation = field.getAnnotation(QueryParam.class);
//			if (annotation != null) {
//				CachedParamDetails cpd = new CachedParamDetails(field, annotation);
//				cachedParamDetailsList.add(cpd);
//				cachedParamDetailsMap.put(cpd.getName(), cpd);
//			}
//		}
//    	Collections.sort(cachedParamDetailsList);
//	}

    public List<String> getResults() throws QueryException {
    	List<String> names;
    	try {
    		Hits hits = lookupInLucene();
    		names = new ArrayList<String>();
    		Iterator it = hits.iterator();
    		while (it.hasNext()) {
    			Hit hit = (Hit) it.next();
    			Document document = hit.getDocument();
    			logger.debug(StringUtils.collectionToCommaDelimitedString(document.getFields()));
    			names.add(document.get("uniqueName"));
    		}
    		return names;
    	} catch (CorruptIndexException exp) {
    		throw new QueryException(exp);
    	} catch (IOException exp) {
    		throw new QueryException(exp);
    	}
    }

    protected abstract String[] getParamNames();

    public List<HtmlFormDetails> getFormDetails() {
    	List<HtmlFormDetails> ret = new ArrayList<HtmlFormDetails>();

    	for (String name : getParamNames()) {
			HtmlFormDetails htd = new HtmlFormDetails();
			//htd.setName(name);
			//htd.setDefaultValue
		}



    	return ret;
    }

	public Map<String, Object> prepareModelData() {
		return Collections.emptyMap();
	}

    protected static final BooleanQuery geneOrPseudogeneQuery = new BooleanQuery();
    static {
        geneOrPseudogeneQuery.add(new TermQuery(new Term("cvTerm.name","gene")), Occur.SHOULD);
        geneOrPseudogeneQuery.add(new TermQuery(new Term("cvTerm.name","pseudogene")), Occur.SHOULD);
    }

    private Hits lookupInLucene(List<org.apache.lucene.search.Query> queries) throws IOException {

        BooleanQuery booleanQuery = new BooleanQuery();
        for (org.apache.lucene.search.Query query : queries) {
			booleanQuery.add(new BooleanClause(query, Occur.MUST));
		}

        logger.error(String.format("Lucene query is '%s'", booleanQuery.toString()));
        Hits hits = luceneIndex.search(booleanQuery);
        return hits;
    }


    private Hits lookupInLucene() throws IOException {

    	List<org.apache.lucene.search.Query> queries = new ArrayList<org.apache.lucene.search.Query>();
    	getQueryTerms(queries);

        return lookupInLucene(queries);
    }

	protected abstract void getQueryTerms(List<org.apache.lucene.search.Query> queries);


	protected BooleanQuery makeQueryForOrganisms(Collection<String> orgNames) {
        BooleanQuery organismQuery = new BooleanQuery();

        for (String organism : orgNames) {
            organismQuery.add(new TermQuery(new Term("organism.commonName",organism)), Occur.SHOULD);
		}
		return organismQuery;
	}

}
