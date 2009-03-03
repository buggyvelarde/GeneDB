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

import org.genedb.querying.core.Query;
import org.genedb.querying.core.QueryException;
import org.genedb.querying.core.QueryUtils;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class JdbcQuery extends NamedParameterJdbcDaoSupport implements Query {

    private String sql;
    protected String[] paramNames;
    protected String name;

    public String getParseableDescription() {
        return QueryUtils.makeParseableDescription(name, paramNames, this);
    }



    protected List<String> runQuery() {

        @SuppressWarnings("unchecked")
        List<String> results = getNamedParameterJdbcTemplate().query(sql,
                new BeanPropertySqlParameterSource(this),
                new RowMapper() {
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return rs.getString(0);
                    }
        });

        return results;
    }

    public List<String> getResults() throws QueryException {
        return runQuery();
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }



    public String[] prepareModelData(int count) {
        return null;
    }

}
