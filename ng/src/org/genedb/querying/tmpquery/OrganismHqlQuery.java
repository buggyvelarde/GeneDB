package org.genedb.querying.tmpquery;

import org.genedb.db.taxon.TaxonNode;
import org.genedb.db.taxon.TaxonNodeList;
import org.genedb.querying.core.HqlQuery;
import org.genedb.querying.core.QueryClass;
import org.genedb.querying.core.QueryParam;

import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

import java.util.HashSet;
import java.util.Set;

@QueryClass(
        title="Coding and pseudogenes by protein length",
        shortDesc="Get a list of transcripts ",
        longDesc=""
    )
public abstract class OrganismHqlQuery extends HqlQuery implements TaxonQuery {

    @QueryParam(
            order=1,
            title="Organism(s) to search"
    )
    protected TaxonNodeList taxons;


    @Override
    protected String getOrganismHql() {
        if (taxons==null || taxons.getNodeCount()==0) {
            return " " ;
        }
        return "and f.organism.abbreviation in (:organismList)";
    }


    public void setTaxons(TaxonNodeList taxons) {
        this.taxons = taxons;
    }

    public TaxonNodeList getTaxons() {
        return taxons;
    }

    @Override
    protected String[] getParamNames() {
        return new String[] {"taxons"};
    }

    @Override
    protected void populateQueryWithParams(org.hibernate.Query query) {
        if (taxons != null && taxons.getNodeCount() > 0) {
            Set<String> names = new HashSet<String>();
            for (TaxonNode node : taxons.getNodes()) {
                names.addAll(node.getAllChildrenNames());
            }
            query.setParameterList("organismList", names);
            System.err.println(StringUtils.collectionToDelimitedString(names, " "));
        }
    }

    protected String[] arrayAppend(String[] superParamNames, String[] thisQuery) {
        String[] ret = new String[superParamNames.length+thisQuery.length];
        System.arraycopy(superParamNames, 0, ret, 0, superParamNames.length);
        System.arraycopy(thisQuery, 0, ret, superParamNames.length, thisQuery.length);
        return ret;
    }

    @Override
    protected void extraValidation(Errors errors) {
        // Deliberately empty
    }

}
