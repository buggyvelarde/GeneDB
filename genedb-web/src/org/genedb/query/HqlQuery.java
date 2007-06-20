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

package org.genedb.query;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.List;

public abstract class HqlQuery extends HibernateDaoSupport implements Query {
    
    private String hql;
    protected String[] paramNames;
    protected String name;

    public String getParseableDescription() {
        return QueryUtils.makeParseableDescription(name, paramNames, getParamValues());
    }
    
    protected List<String> runQuery() {
        @SuppressWarnings({"unchecked","cast"})
        List<String> ret = (List<String>) getHibernateTemplate().findByNamedParam(
                hql,
                paramNames, getParamValues());
        return ret;
    }
    
    abstract Object[] getParamValues();

    public List<String> getResults() throws QueryException {
        return runQuery();
    }

    public void setHql(String hql) {
        this.hql = hql;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

}
