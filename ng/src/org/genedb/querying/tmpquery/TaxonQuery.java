package org.genedb.querying.tmpquery;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;

public interface TaxonQuery {

    public abstract TaxonNodeList getTaxons();

    public abstract void setTaxons(TaxonNodeList taxons);

}