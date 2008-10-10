package org.genedb.db.loading;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Configurable;

import java.sql.SQLException;

import javax.annotation.PostConstruct;

@Configurable
public class NotifyingDataSource extends BasicDataSource {

    protected static final Log logger = LogFactory.getLog(NotifyingDataSource.class);

    @PostConstruct
    public void report() {
        logger.info(String.format("Connection Details url='%s', username='%s', password='%s', driver='%s'",
                url, username, password, driverClassName));
    }


}
