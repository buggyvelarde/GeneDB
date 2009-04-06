package org.genedb.web.mvc.model;

import org.genedb.db.audit.ChangeSet;
import org.genedb.db.audit.ChangeTracker;

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

    private List<IndexUpdater> indexUpdaters;

    private ChangeTracker changeTracker;

    private String clientName;

    private boolean processChangeSet() throws SQLException {

        ChangeSet changeSet = changeTracker.changes(clientName);

        boolean allOK = true;
        for (IndexUpdater indexUpdater : indexUpdaters) {
            boolean result = indexUpdater.updateAllCaches(changeSet);
            if (! result) {
                allOK = false;
                break;
            }
        }
        if (allOK) {
            // changeSet.commit();
        }
        return allOK;
    }

    public static void main(String[] args) throws SQLException {
        ConfigurableApplicationContext ctx = new ClassPathXmlApplicationContext(
            new String[] {"classpath:applicationContext.xml", "classpath:periodicUpdater.xml"});
        ctx.refresh();
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
