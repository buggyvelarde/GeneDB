package org.genedb.db.domain.objects;

import org.gmod.schema.mapped.DbXRef;

import java.util.List;


/**
 * A group of polypeptide domains.
 * Its subfeatures are individual domain predictions.
 *
 * @author rh11
 */
public class InterProHit extends PolypeptideRegionGroup {
    private String urlPrefix, description;
    String accession;
    String database;
    public InterProHit(String database, String urlPrefix, String accession, String description) {
        this.database    = database;
        this.urlPrefix   = urlPrefix;
        this.accession   = accession;
        this.description = description;
    }

    public InterProHit(DbXRef interproDbXRef) {
        this.database    = interproDbXRef.getDb().getName();
        this.urlPrefix   = interproDbXRef.getDb().getUrlPrefix();
        this.accession   = interproDbXRef.getAccession();
        this.description = interproDbXRef.getDescription();
    }

    @Override
    public String getUniqueName() {
        List<PolypeptideRegion> subfeatures = getSubfeatures();
        if (subfeatures.size() == 1) {
            return subfeatures.get(0).getUniqueName();
        }

        return String.format("%s:%s", database, accession);
    }

    @Override
    public String getName() {
        List<PolypeptideRegion> subfeatures = getSubfeatures();
        if (subfeatures.size() == 1) {
            return subfeatures.get(0).getUniqueName();
        }

        return accession;
    }

    @Override
    public String getUrl() {
        return urlPrefix + accession;
    }

    @Override
    public String getDescription() {
        List<PolypeptideRegion> subfeatures = getSubfeatures();
        if (subfeatures.size() == 1) {
            return subfeatures.get(0).getDescription();
        }

        return description;
    }
}
