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

        org.hibernate.Query query = session.createQuery(getHql());
        populateQueryWithParams(query);

        @SuppressWarnings("unchecked") List<String> ret = query.list();
        return ret;
    }


    protected abstract void populateQueryWithParams(org.hibernate.Query query);

//  private void setQueryVarBasedOnType(org.hibernate.Query query, CachedParamDetails cpd) {
//
//      Type type = cpd.getType();
//
//      try {
//
//          if (type.equals(Integer.TYPE)) {
//              query.setInteger(cpd.getName(), cpd.getField().getInt(this));
//          }
//
//      } catch (IllegalArgumentException exp) {
//          throw new RuntimeException("Internal typing/access exception", exp);
//      } catch (IllegalAccessException exp) {
//          throw new RuntimeException("Internal typing/access exception", exp);
//      }
//  }

//  private void prepareCachedParamDetailsList() {
//      for (Field field : this.getClass().getFields()) {
//          Annotation annotation = field.getAnnotation(QueryParam.class);
//          if (annotation != null) {
//              CachedParamDetails cpd = new CachedParamDetails(field, annotation);
//              cachedParamDetailsList.add(cpd);
//              cachedParamDetailsMap.put(cpd.getName(), cpd);
//          }
//      }
//      Collections.sort(cachedParamDetailsList);
//  }

    public List<String> getResults() throws QueryException {
        return runQuery();
    }

    protected abstract String getHql();

    protected abstract String[] getParamNames();

    public List<HtmlFormDetails> getFormDetails() {
        List<HtmlFormDetails> ret = new ArrayList<HtmlFormDetails>();

        /*
        for (String name : getParamNames()) {
            HtmlFormDetails htd = new HtmlFormDetails();
            //htd.setName(name);
            //htd.setDefaultValue
        }
         */


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
