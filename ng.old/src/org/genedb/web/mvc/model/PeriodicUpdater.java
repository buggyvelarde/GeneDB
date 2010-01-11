package org.genedb.web.mvc.model;

import org.genedb.db.audit.ChangeSet;
import org.genedb.db.audit.ChangeTracker;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.SQLException;
import java.util.List;

/**
 * Program designed to be called from eg Hudson or cron
 *
 * It receives a ChangeSet, runs through an ordered list of updating routines,
 * and if they all succeed, tell them all to commit
 *
 */
public class PeriodicUpdater {

    private Logger logger = Logger.getLogger(PeriodicUpdater.class);

    private List<IndexUpdater> indexUpdaters;

    private ChangeTracker changeTracker;

    private String clientName = PeriodicUpdater.class.getCanonicalName();

    boolean processChangeSet() throws SQLException {

        logger.warn("About to fetch changeset");

        ChangeSet changeSet = changeTracker.changes(clientName);

        logger.warn("Changeset has been fetched");

        boolean allOK = true;
        for (IndexUpdater indexUpdater : indexUpdaters) {
            logger.warn(String.format("About to run indexer '%s'", indexUpdater));
            boolean result = indexUpdater.updateAllCaches(changeSet);
            if (! result) {
                allOK = false;
                break;
            }
        }
        if (allOK) {
            logger.debug("Would normally call commit at this stage");
            // changeSet.commit();
        }
        return allOK;
    }

    public static void main(String[] args) throws SQLException {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"classpath:applicationContext.xml", "classpath:periodicUpdater.xml"});
        PeriodicUpdater pu = ctx.getBean("periodicUpdater", PeriodicUpdater.class);
        boolean success = pu.processChangeSet();
        System.exit( success ? 0 : 1 );
    }

    public ChangeTracker getChangeTracker() {
        return changeTracker;
    }

    public void setChangeTracker(ChangeTracker changeTracker) {
        this.changeTracker = changeTracker;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<IndexUpdater> getIndexUpdaters() {
        return indexUpdaters;
    }

    public void setIndexUpdaters(List<IndexUpdater> indexUpdaters) {
        this.indexUpdaters = indexUpdaters;
    }

}
