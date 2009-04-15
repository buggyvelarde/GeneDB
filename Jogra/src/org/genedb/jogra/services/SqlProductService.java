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

    @Override
    public List<Product> getProductList(boolean restrictToGeneLinked) {

        String sql =
            "select distinct" +
            "    lower(cvterm.name) as lc_name" +
            "  , cvterm.name" +
            "  , cvterm.cvterm_id" +
            " from feature_cvterm" +
            " join cvterm using (cvterm_id)" +
            " join cv using (cv_id)" +
            " where cv.name='genedb_products'" +
            " order by lower(cvterm.name), cvterm.name";

        RowMapper mapper = new RowMapper() {
            public Product mapRow(ResultSet rs, int rowNum) throws SQLException {
                Product product = new Product(rs.getString("name"), rs.getInt("cvterm_id"));
                return product;
            }
        };

        @SuppressWarnings("unchecked")
        List<Product> products = jdbcTemplate.query(sql, mapper);
        return products;
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
