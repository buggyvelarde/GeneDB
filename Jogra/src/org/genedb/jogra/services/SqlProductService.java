package org.genedb.jogra.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import com.google.common.collect.Lists;

import org.genedb.jogra.domain.Product;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.util.StringUtils;

public class SqlProductService implements ProductService {

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

    @SuppressWarnings("unchecked")
    @Override
    public List<Product> getProductList(boolean restrictToGeneLinked) {

        String sql = "select distinct cvt.name, cvt.cvterm_id from CvTerm cvt, Cv cv, feature_cvterm fct"
            + " where cvt.cvterm_id=fct.cvterm_id and cvt.cv_id=cv.cv_id and cv.name='genedb_products' order by cvt.name";

            RowMapper mapper = new RowMapper() {
                public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Product product = new Product(rs.getString("name"), rs.getInt("cvterm_id"));
                    return product;
                }
            };

            return this.jdbcTemplate.query(sql, mapper);
    }

    @Override
    public MethodResult rationaliseProduct(Product newProduct, List<Product> oldProducts) {

        Object[] params = new Object[2];
        params[0] = ""+newProduct.getId();

        List<String> oldProductIds = Lists.newArrayListWithExpectedSize(oldProducts.size());
        for (Product oldProduct : oldProducts) {
            oldProductIds.add(""+ oldProduct.getId());
        }

        params[1] = StringUtils.collectionToCommaDelimitedString(oldProductIds);

        sqlUpdate.update(params);

        String sql = "delete from cvterm where cvterm_id in (" + params[1] + ")";
        jdbcTemplate.execute(sql);

        return MethodResult.SUCCESS;
    }

}
