package org.genedb.query.sql;

import org.apache.log4j.Logger;
import org.genedb.query.AbstractQuery;
import org.genedb.query.Result;
import org.genedb.query.SimpleListResult;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Query that retrieves information from a database
 *
 * @author art
 */
public class SqlQuery extends AbstractQuery implements DataSourceAware {

    protected String sql;
    protected DataSource dataSource;

    /**
     * The args that will be injected into the sql statement placeholders ('?').
     */
    protected Object[] args;

    private static final Logger logger = Logger.getLogger(SqlQuery.class);

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    /**
     * A setter for the args object. It's protected so that the subclasses have to call it.
     *
     * @param args
     */
    protected void setArgs(Object[] args)
    {
    	this.args = args;
    }


    public Object[] getArgs()
    {
    	return args.clone();
    }



    /**
     * @param args
     */
    public static void main(String[] args) throws SQLException {
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
        JdbcTemplate jt = new JdbcTemplate(dataSource);

        jt.query(sql, args,
            new RowCallbackHandler() {
            public void processRow(ResultSet rs) throws SQLException {
            	ResultSetMetaData meta = rs.getMetaData();
            	int colNum = meta.getColumnCount();

            	Object[] result = new Object[colNum];

            	// gv1 notes that this doesn't take into account column names
            	for (int i = 1; i <= colNum;i++)
            	{
            		result[i-1] = rs.getObject(i);
            	}
            	slr.add(result);
            }
        }
        );
        return slr;
    }

    /**
     *
     * Will run the query using the supplied callBack.
     *
     * @param callBack
     * @throws Exception
     */
	public void processCallBack(RowCallbackHandler callBack)
	{
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		logger.debug(args);
		logger.debug(sql);
		jt.query(sql, args, callBack);
	}
	
	public void processCallBack(String _sql, Object[] _args, RowCallbackHandler callBack)
	{
		JdbcTemplate jt = new JdbcTemplate(dataSource);
		logger.debug(_args);
		logger.debug(_sql);
		jt.query(_sql, _args, callBack);
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
