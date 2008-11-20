package org.genedb.jogra.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.google.common.collect.Lists;

import org.genedb.jogra.domain.BasicGene;
import org.genedb.jogra.domain.Gene;
import org.genedb.jogra.domain.Product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.util.StringUtils;

public class SqlGeneService implements GeneService {

    private JdbcTemplate jdbcTemplate;
    private SqlUpdate sqlUpdate;


    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "update feature_cvterm set cvterm_id=? where feature_cvterm.cvterm_id in (?)";
        sqlUpdate = new SqlUpdate();
        sqlUpdate.setDataSource(dataSource);
        sqlUpdate.setSql(sql);
        sqlUpdate.declareParameter(new SqlParameter(Types.INTEGER));
        sqlUpdate.declareParameter(new SqlParameter(Types.CHAR));
        sqlUpdate.compile();


    }

    @Override
    public Gene findGeneByUniqueName(String name) {
        Gene ret = new Gene();

      String sql = "select distinct cvt.name, cvt.cvterm_id from CvTerm cvt, Cv cv, feature_cvterm fct"
      + " where cvt.cvterm_id=fct.cvterm_id and cvt.cv_id=cv.cv_id and cv.name='genedb_products' order by cvt.name";

//      RowMapper mapper = new RowMapper() {
//          public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
//              Product product = new Product(rs.getString("name"), rs.getInt("cvterm_id"));
//              return product;
//          }
//      };
//
      Object[] args = { name };
      int[] argTypes = { Types.CHAR };
      Map map =  this.jdbcTemplate.queryForMap(sql, args, argTypes);

        //ret.setFeatureId(map.get("feature_id"));

        return ret;
    }

    //@Override
    public List<String> findGeneNamesByPartialName(String search) {

        String sql = "select uniquename from Feature where uniquename like '%?%' order by uniquename";

        return this.jdbcTemplate.queryForList(sql, args);
    }

}
