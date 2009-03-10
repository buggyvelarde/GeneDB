package org.genedb.querying.tmpquery;

import org.genedb.db.taxon.TaxonNode;

public interface TaxonQuery {

    public abstract TaxonNode[] getTaxons();

    public abstract void setTaxons(TaxonNode[] taxons);

}