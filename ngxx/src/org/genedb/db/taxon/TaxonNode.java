/*
 * Created on Aug 4, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.genedb.db.taxon;

import org.gmod.schema.mapped.Organism;
import org.gmod.schema.mapped.OrganismProp;
import org.gmod.schema.mapped.Phylonode;
import org.gmod.schema.mapped.PhylonodeOrganism;
import org.gmod.schema.mapped.PhylonodeProp;

import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author art
 *
 */
public class TaxonNode implements Serializable {

	private transient Logger logger = Logger.getLogger(TaxonNode.class);

    private String taxonId;
    transient private TaxonNode parent;
    transient private Phylonode phylonode;
    private List<TaxonNode> children = new ArrayList<TaxonNode>();
    private boolean webLinkable = false;
    private boolean organism = false;
    private boolean populated = false;
    private boolean childrenPopulated = false;
    private Map<String, String> appDetails = Maps.newHashMap();
    private Map<TaxonNameType, String> names = new HashMap<TaxonNameType, String>(7);

    public TaxonNode(Phylonode phylonode) {
        this.phylonode = phylonode;
        names.put(TaxonNameType.LABEL, phylonode.getLabel());
        names.put(TaxonNameType.FULL, phylonode.getLabel()); // will be
                                                                // overriden for
                                                                // orgs later

        Collection<PhylonodeOrganism> pos = phylonode.getPhylonodeOrganisms();
        // System.err.println("Looking at '"+shortName+"'");
        if (pos != null && pos.size() > 0) {
            if (pos.size() > 1) {
                logger.error("We have too many PhylonodeOrganisms");
            } else {
                Organism org = pos.iterator().next().getOrganism();
                organism = true;
                // TODO What organism props do we want?

                this.taxonId = org.getPropertyValue("genedb_misc", "taxonId");
                // String curatorName = getOrganismProperty(org, "curatorName");
                // String curatorEmail = getOrganismProperty(org,
                // "curatorEmail");
                // String nickname = getOrganismProperty(org, "nickname");
                // String curatorName = getOrganismProperty(org, "curatorName");
                names.put(TaxonNameType.HTML_SHORT, org.getPropertyValue("genedb_misc", "htmlShortName"));
                names.put(TaxonNameType.HTML_FULL, org.getPropertyValue("genedb_misc", "htmlFullName"));
                // int translationTable =
                // Integer.parseInt(getOrganismProperty(org,
                // "translationTable"));
                // int mitochondrialTranslationTable =
                // Integer.parseInt(getOrganismProperty(org,
                // "mitochondrialTranslationTable"));
                populated = org.isPopulated();
                String fullName = org.getGenus() + ' ' + org.getSpecies();
                names.put(TaxonNameType.FULL, fullName);

                for (PhylonodeProp phylonodeProp : phylonode.getPhylonodeProps()) {
                	appDetails.put(phylonodeProp.getType().getName(), phylonodeProp.getValue());
                }
            }
        }
    }

    public boolean isRoot() {
        return (parent == null);
    }

    public boolean isLeaf() {
        return children.size() == 0;
    }

    public String getLabel() {
        return getName(TaxonNameType.LABEL);
    }

    public String getName(TaxonNameType tnt) {
        return names.get(tnt);
    }

    public String getTaxonId() {
        return taxonId;
    }

    public TaxonNode getParent() {
        return this.parent;
    }

    private void setParent(TaxonNode parent) {
        this.parent = parent;
    }

    void addChild(TaxonNode child) {
        this.children.add(child);
        child.setParent(this);
    }

    public List<TaxonNode> getChildren() {
        return new ArrayList<TaxonNode>(this.children);
    }

    public Phylonode getPhylonode() {
        return this.phylonode;
    }

    public boolean isWebLinkable() {
        return webLinkable;
    }

    public Map<String, String> getAppDetails() {
    	return Collections.unmodifiableMap(appDetails);
    }

    public List<String> getAllChildrenNames() {
        List<TaxonNode> allChildren = getAllChildren();
        List<String> names = new ArrayList<String>();

        for (TaxonNode child : allChildren) {
            if (child.isOrganism()) {
                names.add(child.getLabel());
            }
        }
        if (isOrganism()) {
            names.add(getLabel());
        }
        return names;
    }

    public boolean isOrganism() {
        return organism;
    }

    public List<TaxonNode> getAllChildren() {
        List<TaxonNode> ret = new ArrayList<TaxonNode>();

        List<TaxonNode> immediateChildren = getChildren();
        for (TaxonNode child : immediateChildren) {
            ret.add(child);
            ret.addAll(child.getAllChildren());
        }
        Collections.sort(ret, new Comparator<TaxonNode>() {
            @Override
            public int compare(TaxonNode tn1, TaxonNode tn2) {
                return tn1.getLabel().compareToIgnoreCase(tn2.getLabel());
            }
        });
        return ret;
    }

    /* Slightly modified method (from above) that returns a list of all children names within single quotes (ready for SQL queries)
     * Didn't want to change above method incase it disrupted other classes */
    public List<String> getAllChildrenNamesInSQLFormat() {
        List<TaxonNode> allChildren = getAllChildren();
        List<String> names = new ArrayList<String>();

        for (TaxonNode child : allChildren) {
            if (child.isOrganism()) {
                names.add("'"+child.getLabel()+"'");
            }
        }
        if (isOrganism()) {
            names.add("'"+getLabel()+"'");
        }
        return names;
    }


    @Override
    public String toString() {
        return String.format("taxon id='%s' fullName='%s' label='%s' organism='%s' ",
            getTaxonId(), getName(TaxonNameType.FULL), getLabel(), organism);
    }

    public boolean isPopulated() {
        return populated;
    }

	public boolean isChildrenPopulated() {
		return childrenPopulated;
	}

	public void setChildrenPopulated(boolean childrenPopulated) {
		if (childrenPopulated && !this.childrenPopulated && getParent() != null) {
			logger.trace("Trying to call on parent from child");
			getParent().setChildrenPopulated(true);
		} else {
			logger.trace("Not calling on parent from child");
		}
		this.childrenPopulated = childrenPopulated;
	}

}
