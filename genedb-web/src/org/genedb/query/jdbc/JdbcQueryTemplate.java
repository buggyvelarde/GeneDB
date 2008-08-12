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

package org.genedb.query.jdbc;

import org.genedb.querying.core.QueryTemplate;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;


public class JdbcQueryTemplate extends QueryTemplate {

    private String sql;
    private String dataSource;

    @Override
    public void processNewPrototype(BeanDefinitionBuilder bdb) {
        bdb.addPropertyValue("sql", sql);
        bdb.addPropertyReference("dataSource", dataSource);
    }

    @Required
    public void setSql(String sql) {
        this.sql = sql;
    }

    @Required
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

}
