package org.genedb.db.domain.objects;

import org.gmod.schema.mapped.AnalysisFeature;
import org.gmod.schema.mapped.DbXRef;
import org.gmod.schema.mapped.FeatureLoc;
import org.springframework.util.StringUtils;

import org.apache.log4j.Logger;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A region of a polypeptide that has an associated entry in an external database.
 *
 * @author rh11
 */
@XStreamAlias("polypeptide_region")
public class DatabasePolypeptideRegion extends PolypeptideRegion {
    private static final long serialVersionUID = 1934739510074730316L;

    private static final Logger logger = Logger.getLogger(DatabasePolypeptideRegion.class);

    private static final Map<String,Color> colorsByDatabase = new HashMap<String,Color>() {
        private static final long serialVersionUID = -7259392507851534561L;
        {
            put("Pfam",          new Color(248, 57,  217));
            put("PIRSF",         new Color(52,  33,  135));
            put("Prosite",       new Color(130, 68,  225));
            put("SMART",         new Color(247, 65,  66));
            put("PRINTS",        new Color(57,  45,  209));
            put("ProDom",        new Color(0,   160, 9));
            put("Superfamily",   new Color(0,   199, 127));
            put("TIGR_TIGRFAMS", new Color(0,   255, 255));
        }};

    /**
     * Groups DatabasePolyPeptideRegions by database name.
     * Within each database, the superclass comparison is used.
     */
    @Override
    public int compareTo(LocatedFeature other) {
        if (other instanceof DatabasePolypeptideRegion) {
            int databaseComparison = this.database.compareTo(((DatabasePolypeptideRegion)other).database);
            if (databaseComparison != 0) {
                return databaseComparison;
            }
        }
        return super.compareTo(other);
    }

    @Override
    public String getStratumId() {
        return database;
    }


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

        String score = region.getScore(); // FIXME
        if (!StringUtils.hasLength(score)) {
            AnalysisFeature af = region.getAnalysisFeature();
            if (af != null) {
                score = Double.toString(af.getRawScore());
            }
        }

        return new DatabasePolypeptideRegion(dbxref.getDb().getName(), dbxref.getAccession(), dbxref.getDescription(),
            dbxref.getUrl(), score, domainLoc.getFmin(), domainLoc.getFmax());
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
