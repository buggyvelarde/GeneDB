package org.genedb.db.loading;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This was useful for debugging a problem with database connection usage.
 * It just adds some simple logging ot the DBCP BasicDataSource. Currently
 * the Test configuration uses this as its dataSource.
 * <p>
 * The utility of this class is fairly limited. When we're sure the connection
 * problems have been cleared up, it can be removed.
 *
 * @author rh11
 *
 */
public class LoggingDataSource extends BasicDataSource {
    private static final Logger logger = Logger.getLogger(LoggingDataSource.class);

    private boolean logStackTrace = false;
    private boolean neverLogStackTrace = false;
    public void setLogStackTrace(boolean logStackTrace) {
        this.logStackTrace = logStackTrace && !neverLogStackTrace;
    }

    public void setNeverLogStackTrace(boolean neverLogStackTrace) {
        this.neverLogStackTrace = neverLogStackTrace;
        if (neverLogStackTrace) {
            logStackTrace = false;
        }
    }

    @Override
    public synchronized void close() throws SQLException {
        logger.trace(String.format("close [active=%d, idle=%d]", getNumActive(), getNumIdle()),
            logStackTrace ? new Throwable("Stack trace") : null);
        super.close();
    }

    @Override
    public Connection getConnection() throws SQLException {
        boolean logStackTrace = !this.neverLogStackTrace
                                && (this.logStackTrace || getNumActive() > 0);

        logger.trace(String.format("getConnection [active=%d, idle=%d]", getNumActive(), getNumIdle()),
            logStackTrace ? new Throwable("Stack trace") : null);
        return super.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        logger.trace(String.format("getConnection(%s, %s)", username, password),
            logStackTrace ? new Throwable("Stack trace") : null);
        return super.getConnection(username, password);
    }

}
