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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author art
 *
 */
public class TaxonNode {

    private String taxonId;
    private TaxonNode parent;
    private Phylonode phylonode;
    private List<TaxonNode> children = new ArrayList<TaxonNode>();
    private boolean webLinkable = false;
    private boolean organism = false;
    private boolean hasOrganismFeature = false;
    private Map<String, Map<String, Object>> appDetails = new HashMap<String, Map<String, Object>>();
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
                System.err.println("We have too many PhylonodeOrganisms");
            } else {
                Organism org = pos.iterator().next().getOrganism();
                organism = true;
                // System.err.println("Found organism for '"+shortName+"'");
                // TODO What organism props do we want?

                this.taxonId = getOrganismProperty(org, "taxonId");
                // String curatorName = getOrganismProperty(org, "curatorName");
                // String curatorEmail = getOrganismProperty(org,
                // "curatorEmail");
                // String nickname = getOrganismProperty(org, "nickname");
                // String curatorName = getOrganismProperty(org, "curatorName");
                names.put(TaxonNameType.HTML_SHORT, getOrganismProperty(org, "htmlShortName"));
                names.put(TaxonNameType.HTML_FULL, getOrganismProperty(org, "htmlFullName"));
                // int translationTable =
                // Integer.parseInt(getOrganismProperty(org,
                // "translationTable"));
                // int mitochondrialTranslationTable =
                // Integer.parseInt(getOrganismProperty(org,
                // "mitochondrialTranslationTable"));
                String fullName = org.getGenus() + ' ' + org.getSpecies();
                names.put(TaxonNameType.FULL, fullName);

            }
        }

    }

    private String getOrganismProperty(Organism org, String key) {
        for (OrganismProp prop : org.getOrganismProps()) {
            if (prop.getType().getName().equals(key)) {
                return prop.getValue();
            }
        }
        return null;
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

    public Map<String, Object> getAppDetails(String key) {
        if (appDetails.containsKey(key)) {
            return Collections.unmodifiableMap(appDetails.get(key));
        }
        return Collections.emptyMap();
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

    private List<TaxonNode> getAllChildren() {
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

    @Override
    public String toString() {
        return String.format("taxon id='%s' fullName='%s' label='%s' organism='%s' ",
            getTaxonId(), getName(TaxonNameType.FULL), getLabel(), organism);
    }

	public boolean hasOrganismFeature() {
		return hasOrganismFeature;
	}

	public void setHasOrganismFeature(boolean hasOrganismFeature) {
		this.hasOrganismFeature = hasOrganismFeature;
	}
}
