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

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
//import org.hibernate.validator.ClassValidator;
//import org.hibernate.validator.InvalidValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configurable
public abstract class HqlQuery implements Query {
	
	private static final Logger logger = Logger.getLogger(HqlQuery.class);
	
    @Autowired
    protected SessionFactory sessionFactory;
    protected String name;
    private int order;

    private static final int MAX_RESULTS = 100000;
    private int maxResults = MAX_RESULTS;

    /**
     * Size of result retrieved
     */
    protected boolean isActualResultSizeSameAsMax;

    protected static final String RESTRICT_TO_TRANSCRIPTS_ONLY = " and f.type.name in ('mRNA', 'rRNA', 'scRNA', 'snoRNA', 'snRNA', 'snRNA', 'transcript', 'tRNA')";

    protected static final String RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES = " and f.type.name in ('mRNA', 'rRNA', 'scRNA', 'snoRNA', 'snRNA', 'snRNA', 'transcript', 'tRNA', 'pseudogenic_transcript')";
    
    protected static final String RESTRICT_TO_TRANSCRIPTS_AND_PSEUDOGENES_AND_POLYPEPTIDES = " and f.type.name in ('mRNA', 'rRNA', 'scRNA', 'snoRNA', 'snRNA', 'snRNA', 'transcript', 'tRNA', 'pseudogenic_transcript', 'polypeptide')";

    //private List<CachedParamDetails> cachedParamDetailsList = new ArrayList<CachedParamDetails>();
    //private Map<String, CachedParamDetails> cachedParamDetailsMap = new HashMap<String, CachedParamDetails>();

    public String getParseableDescription() {
        return QueryUtils.makeParseableDescription(name, getParamNames(), this);
    }

    protected List runQuery() {
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);

        String hql = restrictQueryByOrganism(getHql(), getOrganismHql());
        
        org.hibernate.Query query = session.createQuery(hql);
        populateQueryWithParams(query);
        
        logger.debug(query.getQueryString());

        //Set max result to prevent max memory error
        query.setMaxResults(maxResults);

        //Run query
        List ret = query.list();

        //Get the result size
        if (ret!= null && maxResults==ret.size()){
            isActualResultSizeSameAsMax = true;
        }

        return ret;
    }

    @Override
    public boolean isMaxResultsReached() {
        return isActualResultSizeSameAsMax;
    }

    protected String restrictQueryByOrganism(String hql, String organismClause) {
        if (!StringUtils.hasLength(organismClause)) {
            return hql.replace("@ORGANISM@", "");
        }
        return hql.replace("@ORGANISM@", organismClause);
    }

    protected abstract void populateQueryWithParams(org.hibernate.Query query);

    public List getResults() throws QueryException {
        return runQuery();
    }

    protected abstract String getHql();

    protected abstract String getOrganismHql();

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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    


    //@Override
    public void validate(Object target, Errors errors) {
        //@SuppressWarnings("unchecked") T query = (T) target;
        //ClassValidator queryValidator = new ClassValidator(this.getClass());
        //InvalidValue[] invalids = queryValidator.getInvalidValues(target);
        //for (InvalidValue invalidValue: invalids){
        //    errors.rejectValue(invalidValue.getPropertyPath(), null, invalidValue.getMessage());
        //}

        //extraValidation(errors);
    }

    protected abstract void extraValidation(Errors errors);

    //@Override
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return this.getClass().isAssignableFrom(clazz);
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    
    

}
