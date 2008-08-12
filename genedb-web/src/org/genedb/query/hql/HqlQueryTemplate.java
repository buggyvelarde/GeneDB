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

package org.genedb.query.hql;

import org.genedb.querying.core.QueryTemplate;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;


public class HqlQueryTemplate extends QueryTemplate {

    private String hql;
    private String sessionFactory;

    @Override
    public void processNewPrototype(BeanDefinitionBuilder bdb) {
        bdb.addPropertyValue("hql", hql);
        bdb.addPropertyReference("sessionFactory", sessionFactory);
    }

    @Required
    public void setHql(String hql) {
        this.hql = hql;
    }

    @Required
    public void setSessionFactory(String sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
