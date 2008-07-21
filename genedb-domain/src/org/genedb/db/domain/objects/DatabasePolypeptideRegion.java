package org.genedb.db.domain.objects;

import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureLoc;

import org.apache.log4j.Logger;

/**
 * A region of a polypeptide that has an associated entry in an external database.
 *
 * @author rh11
 */
public class DatabasePolypeptideRegion extends PolypeptideRegion {
    private static final Logger logger = Logger.getLogger(DatabasePolypeptideRegion.class);

    private String url;
    String accession;
    String database;

    public DatabasePolypeptideRegion(String database, String accession, String description, String url, String score, int fmin, int fmax) {
        super(fmin, fmax, description, score);
        this.database    = database;
        this.accession   = accession;
        this.url         = url;
    }

    public static DatabasePolypeptideRegion build(org.gmod.schema.feature.PolypeptideDomain domain) {
        DatabasePolypeptideRegion result = build((org.gmod.schema.feature.PolypeptideRegion) domain);
        if (result == null) {
            return null;
        }
        return result;
    }

    public static DatabasePolypeptideRegion build(org.gmod.schema.feature.PolypeptideRegion region) {
        FeatureLoc domainLoc = region.getRankZeroFeatureLoc();
        DbXRef dbxref = region.getDbXRef();
        if (dbxref == null) {
            logger.error(String.format("The polypeptide region '%s' has no DbXRef",
                region.getUniqueName()));
            return null;
        }

        return new DatabasePolypeptideRegion(dbxref.getDb().getName(), dbxref.getAccession(), dbxref.getDescription(),
            dbxref.getUrl(), null, domainLoc.getFmin(), domainLoc.getFmax());
    }

    String getDatabase() {
        return database;
    }

    String getAccession() {
        return accession;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getUniqueName() {
        return String.format("%s:%s", database, accession);
    }
}
