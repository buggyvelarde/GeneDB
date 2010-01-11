package org.genedb.db.domain.objects;

import org.gmod.schema.mapped.DbXRef;

import com.thoughtworks.xstream.annotations.XStreamAlias;


/**
 * A group of polypeptide domains.
 * Its subfeatures are individual domain predictions.
 *
 * @author rh11
 */
@XStreamAlias("Interpro")
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
        return String.format("%s:%s", database, accession);
    }

    @Override
    public String getName() {
        return accession;
    }

    @Override
    public String getUrl() {
        return urlPrefix + accession;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
