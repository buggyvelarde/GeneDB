package org.genedb.db.domain.objects;

import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureLoc;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * A region of a polypeptide that has an associated entry in an external database.
 *
 * @author rh11
 */
public class DatabasePolypeptideRegion extends PolypeptideRegion {
    private static final Logger logger = Logger.getLogger(DatabasePolypeptideRegion.class);

    private static final Map<String,Color> colorsByDatabase = new HashMap<String,Color>() {{
       put("Pfam",        new Color(255, 0,   255));
       put("Prosite",     new Color(255, 0,   0));
       put("SMART",       new Color(150, 170, 100));
       put("PRINTS",      new Color(160, 140, 180));
       put("ProDom",      new Color(100, 150, 180));
       put("Superfamily", new Color(150, 120, 110));
    }};

    private String url;
    String accession;
    String database;

    public DatabasePolypeptideRegion(String database, String accession, String description, String url, String score, int fmin, int fmax) {
        super(fmin, fmax, description, score);
        this.database    = database;
        this.accession   = accession;
        this.url         = url;
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
            dbxref.getUrl(), region.getScore(), domainLoc.getFmin(), domainLoc.getFmax());
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

    @Override
    public Color getColor() {
        if (colorsByDatabase.containsKey(database)) {
            return colorsByDatabase.get(database);
        }
        logger.info(String.format("The database '%s' has no default color; using red", database));
        return Color.RED;
    }
}
