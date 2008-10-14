package org.genedb.query.sql;

import org.genedb.query.AbstractQuery;
import org.genedb.query.Result;
import org.genedb.query.SimpleListResult;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * Query that retrieves information from a database
 *
 * @author art
 */
public class SqlQuery extends AbstractQuery implements DataSourceAware {

    private String sql;
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }



    /**
     * @param args
     */
    public static void main(String[] args) {
        SqlQuery sq= new SqlQuery();
        sq.setSql("select gp.geneId from GenePeri gp where gp.nickname like ? and gp.genename like ?");
        //sq.setSql("select * from GenePeri");
        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        ds.setUrl("jdbc:oracle:thin:@ocs3:1534:pat");
        ds.setUsername("gus2");
        ds.setPassword("genedb2");
        sq.setDataSource(ds);
        Result res = sq.process();
        System.out.println(res);
        ds.destroy();
    }

    /**
     * @see org.genedb.zoe.query.Query#process()
     */
    public Result process() {
        final SimpleListResult slr = new SimpleListResult();
        //slr.setType();
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        Object[] args = {"pombe", "cdc%"};
        //Object[] args = new Object[0];
        jt.query(sql, args,
            new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
            slr.add(rs.getString("geneId"));
            }
        }
        );
        return slr;
    }


//    @Override
//    public void writeSpringBean(PrintWriter pw) {
//        pw.println("\tGot a sql query");
//        addProperty(pw, "description", getDescription());
//        addProperty(pw, "help", getHelp());
//        addProperty(pw, "sql", getSql());
//    }

//    private void addProperty(PrintWriter pw, String key, String value) {
//        pw.println("\t\t<property name=\""+key+"\" value=\""+value+"\" />");
//    }

    protected String getSql() {
        return this.sql;
    }

    public int getIndex() {
    // TODO Auto-generated method stub
    return 0;
    }



    public void setIndex(int index) {
    // TODO Auto-generated method stub

    }
}
