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

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configurable
public abstract class HqlQuery implements Query {

    @Autowired
    private SessionFactory sessionFactory;
    protected String name;
    private int order;

    //private List<CachedParamDetails> cachedParamDetailsList = new ArrayList<CachedParamDetails>();
    //private Map<String, CachedParamDetails> cachedParamDetailsMap = new HashMap<String, CachedParamDetails>();

    public String getParseableDescription() {
        return QueryUtils.makeParseableDescription(name, getParamNames(), this);
    }

    protected List<String> runQuery() {
        Session session = SessionFactoryUtils.doGetSession(sessionFactory, false);

        String hql = restrictQueryByOrganism(getHql(), getOrganismHql());
        org.hibernate.Query query = session.createQuery(hql);
        populateQueryWithParams(query);

        @SuppressWarnings("unchecked") List<String> ret = query.list();
        return ret;
    }

    private String restrictQueryByOrganism(String hql, String organismClause) {
        if (!StringUtils.hasLength(organismClause)) {
            return hql.replace("@ORGANISM@", "");
        }
        return hql.replace("@ORGANISM@", organismClause);
    }

    protected abstract void populateQueryWithParams(org.hibernate.Query query);

    public List<String> getResults() throws QueryException {
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

    public String getQueryDescription() {
        return "";
    }
}
